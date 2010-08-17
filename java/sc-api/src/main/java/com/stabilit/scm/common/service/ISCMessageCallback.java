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

import com.stabilit.scm.cln.service.IService;

/**
 * The Interface ISCMessageCallback. Represents basic functionality for callbacks to be used in asynchronous
 * communication with an SC.
 * 
 * @author JTraber
 */
public interface ISCMessageCallback {

	/**
	 * Callback. Method gets called when reply arrives.
	 * 
	 * @param reply
	 *            the reply
	 * @throws Exception
	 *             the exception
	 */
	public abstract void callback(ISCMessage reply);

	/**
	 * Callback. Method gets called when an error shows up in communication process.
	 * 
	 * @param th
	 *            the throwable
	 */
	public abstract void callback(Throwable th);

	/**
	 * Gets the service which is using the message callback.
	 * 
	 * @return the service
	 */
	public abstract IService getService();
}
