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
package com.stabilit.scm.common.scmp;

import java.net.InetSocketAddress;

/**
 * The Interface IRequest abstracts a request.
 */
public interface IRequest {

	/**
	 * Gets the message type.
	 * 
	 * @return the key message type in request.
	 * @throws Exception
	 *             the exception
	 */
	public SCMPMsgType getKey() throws Exception;

	/**
	 * Gets the message.
	 * 
	 * @return the message
	 */
	public SCMPMessage getMessage();

	/**
	 * Sets the scmp message in the request.
	 * 
	 * @param message
	 *            the new scmp message
	 */
	public void setMessage(SCMPMessage message);

	/**
	 * Sets an attribute in attribute map.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setAttribute(String key, Object value);

	/**
	 * Sets the attribute.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setAttribute(SCMPHeaderAttributeKey key, Object value);

	/**
	 * Gets an attribute of the attribute map.
	 * 
	 * @param key
	 *            the key
	 * @return the attribute
	 */
	public Object getAttribute(String key);

	/**
	 * Gets the attribute.
	 * 
	 * @param key
	 *            the key
	 * @return the attribute
	 */
	public Object getAttribute(SCMPHeaderAttributeKey key);

	/**
	 * Gets the socket address.
	 * 
	 * @return the socket address
	 */
	public InetSocketAddress getLocalSocketAddress();

	/**
	 * Gets the remote socket address.
	 * 
	 * @return the remote socket address
	 */
	public InetSocketAddress getRemoteSocketAddress();

	/**
	 * Reads the content of the request.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void read() throws Exception;

	/**
	 * Reads next part.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void readNext() throws Exception;

	/**
	 * Load content on socket to the request. Decodes network frame into an scmp.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void load() throws Exception;
}
