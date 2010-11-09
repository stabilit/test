/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *-----------------------------------------------------------------------------*/
package org.serviceconnector.registry;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.serviceconnector.scmp.IRequest;
import org.serviceconnector.scmp.IResponse;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.IPublishTimerRun;
import org.serviceconnector.service.SubscriptionMask;
import org.serviceconnector.util.ITimerRun;
import org.serviceconnector.util.LinkedNode;
import org.serviceconnector.util.LinkedQueue;
import org.serviceconnector.util.TimerTaskWrapper;

/**
 * The Class SubscriptionQueue. The SubscriptionQueue is responsible for queuing incoming data from server, to inform
 * subscriptions about new arrived messages, to observe there timeouts and to know there current position in queue
 * (TimeAwareDataPointer). The queue needs also to handle the deleting of consumed messages and to assure queue does not
 * overflow.
 * 
 * @param <E>
 *            the element type to handle in the queue
 * @author JTraber
 */
public class SubscriptionQueue<E> {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SubscriptionQueue.class);

	/** The timer instance to observe all timeouts in relation to this queue. */
	private Timer timer;
	/** The data queue. */
	private LinkedQueue<E> dataQueue;
	/** The pointer map - maps session id to data pointer and its node in queue. */
	private Map<String, TimeAwareDataPointer> pointerMap;

	/**
	 * Instantiates a new SubscriptionQueue.
	 */
	public SubscriptionQueue() {
		this.dataQueue = new LinkedQueue<E>();
		this.pointerMap = new ConcurrentHashMap<String, TimeAwareDataPointer>();
		this.timer = new Timer("SubscriptionQueueTimer");
	}

	/**
	 * Iterator.
	 * 
	 * @return the iterator
	 */
	public Iterator<E> iterator() {
		return this.dataQueue.iterator();
	}

	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return this.dataQueue.getSize();
	}

	/**
	 * Inserts a new message into the queue.
	 * 
	 * @param message
	 *            the message
	 */
	public void insert(E message) {
		if (message == null) {
			// inserting null value not allowed
			return;
		}
		this.dataQueue.insert(message);
		logger.trace("insert - queue size:" + this.dataQueue.getSize());
		// inform new message arrived
		this.fireNewDataArrived();
		// delete unreferenced nodes in queue
		this.removeNonreferencedNodes();
	}

	/**
	 * Checks for next.
	 * 
	 * @param sessionId
	 *            the session id
	 * @return true, if successful
	 */
	public boolean hasNext(String sessionId) {
		TimeAwareDataPointer ptr = this.pointerMap.get(sessionId);
		return ptr.node != null;
	}

	/**
	 * Return message if any. If no message is available null will be returned.
	 * 
	 * @param sessionId
	 *            the session id
	 * @return the e
	 */
	public E getMessage(String sessionId) {
		TimeAwareDataPointer ptr = this.pointerMap.get(sessionId);
		if (ptr == null) {
			return null;
		}
		LinkedNode<E> node = ptr.getNode();
		if (node == null) {
			// nothing to poll data pointer points to null - return null
			return null;
		}
		E message = node.getValue();
		if (message == null) {
			return null;
		}
		// dereference node, pointer moves to next node
		node.dereference();
		ptr.moveNext();
		logger.trace("getMessage - queue size:" + this.dataQueue.getSize());
		return message;
	}

	/**
	 * Fire new data arrived. Indicates that a new message has been added. Sets data pointer pointing on null elements
	 * to new element if necessary (mask matches & listening mode).
	 */
	private void fireNewDataArrived() {
		Object[] nodeArray = null;
		// TODO, can be improved, separate set of null pointer nodes
		synchronized (this.pointerMap) {
			nodeArray = this.pointerMap.values().toArray();
		}
		// looping through every data pointer - looking for null pointing elements
		LinkedNode<E> newNode = dataQueue.getLast();
		for (int i = 0; i < nodeArray.length; i++) {
			TimeAwareDataPointer ptr = (TimeAwareDataPointer) nodeArray[i];
			if (ptr.getNode() == null) {
				// data pointer points to null - try pointing to new element
				if (ptr.setNode(newNode) == true) {
					// setNode successful - data pointer interested in new node
					if (ptr.listening()) {
						// data pointer in listen mode needs to be informed about new data
						ptr.schedule(0);
					}
				}
			}
		}
	}

	/**
	 * Removes the non referenced nodes. Starts removing nodes in first position of queue - stops at the position a node
	 * is referenced.
	 */
	private void removeNonreferencedNodes() {
		LinkedNode<E> node = this.dataQueue.getFirst();
		while (node != null) {
			if (node.isReferenced()) {
				// stop removing nodes at the position you get a referenced node
				break;
			}
			// remove node
			this.dataQueue.extract();
			logger.trace("remove - queue size:" + this.dataQueue.getSize());
			// reads next node
			node = this.dataQueue.getFirst();
		}
	}

	/**
	 * Listen. Indicates that client is ready for messages. Data pointer changes to listen mode and schedules timeout.
	 * 
	 * @param sessionId
	 *            the session id
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 */
	public void listen(String sessionId, IRequest request, IResponse response) {
		TimeAwareDataPointer dataPointer = pointerMap.get(sessionId);
		// stores request/response in timer run - to answer client correctly at timeout
		dataPointer.timerRun.setRequest(request);
		dataPointer.timerRun.setResponse(response);
		// starts listening and schedules subscription timeout
		dataPointer.startListen();
		dataPointer.schedule();
	}

	/**
	 * Subscribe. Sets up subscription, create data pointer.
	 * 
	 * @param sessionId
	 *            the session id
	 * @param mask
	 *            the filter mask
	 * @param timerRun
	 *            the timer run
	 */
	public void subscribe(String sessionId, SubscriptionMask mask, IPublishTimerRun timerRun) {
		TimeAwareDataPointer dataPointer = new TimeAwareDataPointer(mask, timerRun);
		// Stores sessionId and dataPointer in map
		this.pointerMap.put(sessionId, dataPointer);
	}

	/**
	 * Change subscription.
	 * 
	 * @param sessionId
	 *            the session id
	 * @param filterMask
	 *            the filter mask
	 */
	public void changeSubscription(String sessionId, SubscriptionMask mask) {
		TimeAwareDataPointer dataPointer = this.pointerMap.get(sessionId);
		if (dataPointer != null) {
			dataPointer.changeFilterMask(mask);
		}
	}

	/**
	 * Unsubscribe. Deletes subscription, remove data pointer.
	 * 
	 * @param sessionId
	 *            the session id
	 */
	public void unsubscribe(String sessionId) {
		TimeAwareDataPointer dataPointer = this.pointerMap.get(sessionId);
		this.pointerMap.remove(sessionId);
		dataPointer.destroy();
	}

	/**
	 * The Class TimeAwareDataPointer. Points to a queue node. Knows mask for matching messages and state if
	 * subscription is listening or not. Each subscription has his data pointer - its created when client subscribes.
	 */
	private class TimeAwareDataPointer {
		/** The current node in queue. */
		private LinkedNode<E> node;
		/** The timer run. */
		private IPublishTimerRun timerRun;
		/** The subscription mask. */
		private SubscriptionMask mask;
		/** The listen state. */
		private boolean listening;
		/** The subscription timeouter. */
		private TimerTask subscriptionTimeouter;

		/**
		 * Instantiates a new TimeAwareDataPointer.
		 * 
		 * @param mask
		 *            the filter mask
		 * @param timerRun
		 *            the timer run
		 */
		public TimeAwareDataPointer(SubscriptionMask mask, IPublishTimerRun timerRun) {
			this.timerRun = timerRun;
			this.listening = false;
			this.mask = mask;
			this.subscriptionTimeouter = null;
		}

		/**
		 * Move next. Moves data pointer to the next node in queue.
		 */
		public void moveNext() {
			if (this.node == null) {
				// current node is already null - no move possible
				return;
			}
			while (true) {
				this.node = this.node.getNext();
				if (this.node == null) {
					// last possible node reached - no next move possible
					return;
				}
				if (this.mask.matches((SCMPMessage) this.node.getValue())) {
					this.node.reference();
					// reached node matches mask keep current position
					return;
				}
			}
		}

		/**
		 * Change filter mask.
		 * 
		 * @param filterMask
		 *            the filter mask
		 */
		public void changeFilterMask(SubscriptionMask mask) {
			this.mask = mask;
			if (this.node == null) {
				return;
			}
			if (this.mask.matches((SCMPMessage) this.node.getValue())) {
				// current node matches new mask keep current position
				return;
			} else {
				// move to next matching node
				this.moveNext();
			}
		}

		/**
		 * Checks for next.
		 * 
		 * @return true, if successful
		 */
		public boolean hasNext() {
			return node.getNext() != null;
		}

		/**
		 * Gets the current node.
		 * 
		 * @return the node
		 */
		public LinkedNode<E> getNode() {
			return node;
		}

		/**
		 * Start listen. If subscription is ready to receive messages listen is true.
		 */
		public void startListen() {
			this.listening = true;
		}

		/**
		 * Stop listen. If subscription is not ready to receive messages listen is false.
		 */
		public void stopListen() {
			this.listening = false;
		}

		/**
		 * Checks if is listen.
		 * 
		 * @return true, if is listen
		 */
		public boolean listening() {
			return listening;
		}

		/**
		 * Sets the node.
		 * 
		 * @param node
		 *            the new node
		 * @return true, if successful
		 */
		public boolean setNode(LinkedNode<E> node) {
			if (this.mask.matches((SCMPMessage) node.getValue()) == false) {
				// mask doesn't match - don't set the node
				return false;
			}
			// set the node
			this.node = node;
			// node needs to be referenced by this data pointer
			this.node.reference();
			return true;
		}

		/**
		 * Schedule. Activate timeout for no data message.
		 */
		public void schedule() {
			this.schedule(this.timerRun.getTimeoutMillis());
		}

		/**
		 * Schedule. Activate subscription timeout with a given time.
		 * 
		 * @param timeoutMillis
		 *            the timeout
		 */
		public void schedule(double timeoutMillis) {
			// always cancel old timeouter when schedule of an new timeout is necessary
			this.cancel();
			this.subscriptionTimeouter = new SubscriptionTaskWrapper(this, this.timerRun);
			// schedules subscriptionTimeouter on subscription queue timer
			SubscriptionQueue.this.timer.schedule(this.subscriptionTimeouter, (long) timeoutMillis);
		}

		/**
		 * Destroys data pointer and dereferences node in queue.
		 */
		public void destroy() {
			this.cancel();
			if (node != null) {
				this.node.dereference();
			}
			if (this.listening) {
				// necessary for responding CRP to client! Very important!
				this.timerRun.timeout();
			}
			this.node = null;
			this.timerRun = null;
			this.listening = false;
			this.mask = null;
			this.subscriptionTimeouter = null;
		}

		/**
		 * Cancel. Deactivate subscription timeout.
		 */
		public void cancel() {
			if (this.subscriptionTimeouter != null) {
				this.subscriptionTimeouter.cancel();
				// important to set timeouter null - rescheduling of same instance not possible
				this.subscriptionTimeouter = null;
			}
		}
	}

	/**
	 * The Class SubscriptionTaskWrapper. SubscriptionTaskWrapper times out and calls the target ITimerRun which happens
	 * in super class TimerTaskWrapper. Important to store subscription state in data pointer when time runs out
	 * listening becomes false.
	 */
	private class SubscriptionTaskWrapper extends TimerTaskWrapper {

		/** The data pointer. */
		private TimeAwareDataPointer dataPointer;

		/**
		 * Instantiates a SubscriptionTaskWrapper.
		 * 
		 * @param dataPointer
		 *            the data pointer
		 * @param target
		 *            the target
		 */
		public SubscriptionTaskWrapper(TimeAwareDataPointer dataPointer, ITimerRun target) {
			super(target);
			this.dataPointer = dataPointer;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			// stops listening - ITimerRun gets executed
			this.dataPointer.stopListen();
			super.run();
		}
	}
}