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
package org.serviceconnector.cmd.sc;

import java.io.IOException;

import org.serviceconnector.Constants;
import org.serviceconnector.casc.ISubscriptionCallback;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.SubscriptionLogger;
import org.serviceconnector.net.req.netty.IdleTimeoutException;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.registry.SubscriptionQueue;
import org.serviceconnector.registry.SubscriptionRegistry;
import org.serviceconnector.scmp.IRequest;
import org.serviceconnector.scmp.IResponse;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.service.IPublishService;
import org.serviceconnector.service.PublishTimeout;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.service.SubscriptionMask;

/**
 * The Class ClnSubscribeCommandCallback.
 */
public class ClnSubscribeCommandCallback implements ISCMPMessageCallback, ISubscriptionCallback {

	/** The callback. */
	private IResponderCallback responderCallback;
	/** The request. */
	private IRequest request;
	/** The response. */
	private IResponse response;
	/** The subscription. */
	private Subscription tempSubscription;
	/** The subscription registry. */
	private SubscriptionRegistry subscriptionRegistry = AppContext.getSubscriptionRegistry();

	/**
	 * Instantiates a new ClnExecuteCommandCallback.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param callback
	 *            the callback
	 * @param tempSubscription
	 *            the subscription
	 */
	public ClnSubscribeCommandCallback(IRequest request, IResponse response, IResponderCallback callback,
			Subscription tempSubscription) {
		this.responderCallback = callback;
		this.request = request;
		this.response = response;
		this.tempSubscription = tempSubscription;
	}

	/** {@inheritDoc} */
	@Override
	public void receive(SCMPMessage reply) {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		int noDataIntervalSeconds = this.tempSubscription.getNoDataInterval();

		if (reply.isFault() == false) {
			boolean rejectSubscriptionFlag = reply.getHeaderFlag(SCMPHeaderAttributeKey.REJECT_SESSION);
			if (rejectSubscriptionFlag == false) {
				// subscription has not been rejected, add server to subscription
				SubscriptionQueue<SCMPMessage> subscriptionQueue = ((IPublishService) this.tempSubscription.getService())
						.getSubscriptionQueue();
				PublishTimeout publishTimeout = new PublishTimeout(subscriptionQueue, noDataIntervalSeconds
						* Constants.SEC_TO_MILLISEC_FACTOR);
				SubscriptionMask subscriptionMask = tempSubscription.getMask();
				subscriptionQueue.subscribe(tempSubscription.getId(), subscriptionMask, publishTimeout);
				// finally add subscription to the registry & schedule subscription timeout internal
				this.subscriptionRegistry.addSubscription(tempSubscription.getId(), tempSubscription);
				SubscriptionLogger.logSubscribe(serviceName, tempSubscription.getId(), subscriptionMask.getValue());
				// forward local subscription no matter what server sends
				reply.setSessionId(tempSubscription.getId());
			} else {
				// subscription has been rejected - remove subscription id from header
				reply.removeHeader(SCMPHeaderAttributeKey.SESSION_ID);
				// creation failed remove from server
				this.tempSubscription.getServer().removeSession(tempSubscription);
			}
		} else {
			reply.removeHeader(SCMPHeaderAttributeKey.SESSION_ID);
			// creation failed remove from server
			this.tempSubscription.getServer().removeSession(tempSubscription);
		}
		// forward reply to client
		reply.setIsReply(true);
		reply.setServiceName(serviceName);
		reply.setMessageType(SCMPMsgType.CLN_SUBSCRIBE);
		response.setSCMP(reply);
		this.responderCallback.responseCallback(request, response);
	}

	/** {@inheritDoc} */
	@Override
	public void receive(Exception ex) {
		// creation failed remove from server
		this.tempSubscription.getServer().removeSession(tempSubscription);
		SCMPMessage fault = null;
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		if (ex instanceof IdleTimeoutException) {
			// operation timeout handling
			fault = new SCMPMessageFault(SCMPError.OPERATION_TIMEOUT, "Operation timeout expired on SC cln subscribe");
		} else if (ex instanceof IOException) {
			fault = new SCMPMessageFault(SCMPError.CONNECTION_EXCEPTION, "broken connection on SC cln subscribe");
		} else if (ex instanceof InterruptedException) {
			fault = new SCMPMessageFault(SCMPError.SC_ERROR, "executing cln subscribe failed, thread interrupted");
		} else {
			fault = new SCMPMessageFault(SCMPError.SC_ERROR, "executing cln subscribe failed");
		}
		// forward reply to client
		fault.setIsReply(true);
		fault.setServiceName(serviceName);
		fault.setMessageType(SCMPMsgType.CLN_SUBSCRIBE);
		response.setSCMP(fault);
		this.responderCallback.responseCallback(request, response);
	}

	/** {@inheritDoc} */
	@Override
	public Subscription getSubscription() {
		return this.tempSubscription;
	}

	public IRequest getRequest() {
		return request;
	}
}
