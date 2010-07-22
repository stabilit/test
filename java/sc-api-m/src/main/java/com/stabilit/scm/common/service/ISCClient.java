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
package com.stabilit.scm.common.service;

import com.stabilit.scm.cln.service.ISessionService;

/**
 * The Interface IServiceConnector.
 * 
 * @author JTraber
 */
public interface ISCClient extends ISC {

	public ISCContext getContext();

	/**
	 * Connects to SC.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void attach() throws Exception;

	/**
	 * Disconnects from SC.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void detach() throws Exception;

	/**
	 * Gets the number of threads.
	 * 
	 * @return the number of threads
	 */
	public int getNumberOfThreads();

	/**
	 * Sets the number of threads.
	 * 
	 * @param numberOfThreads
	 *            the new number of threads
	 */
	public void setNumberOfThreads(int numberOfThreads);

	/**
	 * Gets the connection key.
	 * 
	 * @return the connection key
	 */
	public String getConnectionKey();

	/**
	 * Sets the connection key.
	 * 
	 * @param connectionKey
	 *            the new connection key
	 */
	public void setConnectionType(String connectionKey);

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost();

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort();

	public IFileService newFileService(String serviceName);

	public IPublishService newPublishService(String serviceName);

	public ISessionService newSessionService(String serviceName);

	public void setMaxConnections(int maxConnections);
}
