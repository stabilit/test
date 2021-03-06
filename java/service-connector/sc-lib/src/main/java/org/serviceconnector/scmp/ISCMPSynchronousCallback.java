/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.scmp;

/**
 * The Interface ISCMPSynchronousCallback.
 */
public interface ISCMPSynchronousCallback extends ISCMPMessageCallback {

	/** {@inheritDoc} */
	@Override
	public abstract void receive(SCMPMessage scmpReply) throws Exception;

	/** {@inheritDoc} */
	@Override
	public abstract void receive(Exception ex);

	/**
	 * Careful, be aware of timeout concept if you use this method. Gets the message synchronous. Waits until message/fault received or time you hand over runs out.<br />
	 * <br />
	 *
	 * @param timeoutMillis the timeout in milliseconds
	 * @return the message sync
	 * @throws Exception the exception
	 */
	public abstract SCMPMessage getMessageSync(int timeoutMillis) throws Exception;

}
