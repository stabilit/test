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
package org.serviceconnector.web.cmd;

import org.serviceconnector.web.IWebRequest;
import org.serviceconnector.web.IWebResponse;
import org.serviceconnector.web.IWebSession;


/**
 * The Interface IAuthorized.
 * 
 * @author JTraber
 */
public interface IWebCommandAccessible {

	/**
	 * Login.
	 * 
	 * @param request
	 *            the request
	 * @return 
	 * @throws Exception
	 *             the exception
	 */
	public abstract IWebSession login(IWebRequest request, IWebResponse response) throws Exception;

	/**
	 * Checks if is accessible.
	 * 
	 * @param request
	 *            the request
	 * @return true, if is accessible
	 * @throws Exception
	 *             the exception
	 */
	public abstract boolean isAccessible(IWebRequest request) throws Exception;

	/**
	 * Logout.
	 * 
	 * @param request
	 *            the request
	 * @throws Exception
	 *             the exception
	 */
	public abstract void logout(IWebRequest request) throws Exception;

	/**
	 * Gets the accessible context.
	 * 
	 * @return the accessible context
	 */
	public abstract IWebCommandAccessibleContext getAccessibleContext();
}
