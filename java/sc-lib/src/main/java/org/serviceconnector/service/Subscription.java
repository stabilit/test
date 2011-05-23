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
package org.serviceconnector.service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.serviceconnector.server.IStatefulServer;
import org.serviceconnector.util.TimeoutWrapper;
import org.serviceconnector.util.XMLDumpWriter;

/**
 * The Class Subscription.
 */
public class Subscription extends AbstractSession {

	/** The mask in bytes. */
	private SubscriptionMask mask;
	/** The no data interval. */
	private int noDataIntervalMillis = 0;
	/** The subscription timeout seconds. */
	private double subscriptionTimeoutMillis;

	/**
	 * Instantiates a new subscription.
	 * 
	 * @param mask
	 *            the mask
	 * @param sessionInfo
	 *            the session info
	 * @param ipAddressList
	 *            the ip address list
	 * @param noDataInterval
	 *            the no data interval
	 * @param subscriptionTimeoutMillis
	 *            the subscription timeout millis
	 * @param cascaded
	 *            the cascaded
	 */
	public Subscription(SubscriptionMask mask, String sessionInfo, String ipAddressList, int noDataInterval,
			double subscriptionTimeoutMillis, boolean cascaded) {
		super(sessionInfo, ipAddressList, cascaded);
		this.mask = mask;
		this.noDataIntervalMillis = noDataInterval;
		this.subscriptionTimeoutMillis = subscriptionTimeoutMillis;
	}

	/**
	 * Gets the mask.
	 * 
	 * @return the mask
	 */
	public SubscriptionMask getMask() {
		return mask;
	}

	/**
	 * Sets the mask.
	 * 
	 * @param mask
	 *            the new mask
	 */
	public void setMask(SubscriptionMask mask) {
		this.mask = mask;
	}

	/**
	 * Gets the subscription timeout millis.
	 * 
	 * @return the subscription timeout millis
	 */
	public double getSubscriptionTimeoutMillis() {
		return this.subscriptionTimeoutMillis;
	}

	/**
	 * Gets the server.
	 * 
	 * @return the server {@inheritDoc}
	 */
	@Override
	public IStatefulServer getServer() {
		return (IStatefulServer) this.server;
	}

	/**
	 * Gets the no data interval.
	 * 
	 * @return the no data interval
	 */
	public int getNoDataIntervalMillis() {
		return noDataIntervalMillis;
	}
	
	/**
	 * Dump the subscription into the xml writer.
	 * 
	 * @param writer
	 *            the writer
	 * @throws Exception
	 *             the exception
	 */
	public void dump(XMLDumpWriter writer) throws Exception {
		writer.writeStartElement("subscription");
		writer.writeAttribute("id", this.getId());
		writer.writeAttribute("sessionInfo", this.getSessionInfo());
		writer.writeAttribute("isCascaded", this.isCascaded());
		writer.writeAttribute("mask", this.getMask().getValue());
		writer.writeAttribute("noDataIntervalMillis", this.noDataIntervalMillis);
		writer.writeAttribute("subscriptionTimeoutMillis", this.subscriptionTimeoutMillis);
		ScheduledFuture<TimeoutWrapper> timeouter = this.getTimeout();
		if (timeouter != null) {
			writer.writeAttribute("timeout", timeouter.getDelay(TimeUnit.SECONDS));
		}
		writer.writeElement("ipAddressList", this.getIpAddressList());
		writer.writeElement("creationTime", this.getCreationTime().toString());
		this.getService().dump(writer);
		writer.writeEndElement(); // subscription
	}
}
