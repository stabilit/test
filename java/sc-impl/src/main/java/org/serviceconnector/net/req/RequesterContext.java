/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package org.serviceconnector.net.req;

import org.apache.log4j.Logger;
import org.serviceconnector.net.connection.ConnectionPool;
import org.serviceconnector.scmp.SCMPMessageId;

/**
 * The Class RequesterContext.
 * 
 * @author JTraber
 */
public class RequesterContext {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(RequesterContext.class);

	protected ConnectionPool connectionPool;
	protected SCMPMessageId msgId;

	public RequesterContext(ConnectionPool connectionPool, SCMPMessageId msgId) {
		this.connectionPool = connectionPool;
		this.msgId = msgId;
	}

	/**
	 * Gets the connection pool.
	 * 
	 * @return the connection pool
	 */
	public ConnectionPool getConnectionPool() {
		return this.connectionPool;
	}

	/**
	 * Gets the sCMP message id.
	 * 
	 * @return the sCMP message id
	 */
	public SCMPMessageId getSCMPMessageId() {
		return this.msgId;
	}
}
