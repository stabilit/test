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
package com.stabilit.scm.cln.call;

import com.stabilit.scm.cln.service.ISCSession;
import com.stabilit.scm.common.net.req.IRequester;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMsgType;

/**
 * The Class SCMPClnDeleteSessionCall. Call deletes a session.
 * 
 * @author JTraber
 */
public class SCMPClnDeleteSessionCall extends SCMPSessionCallAdapter {

	/**
	 * Instantiates a new SCMPClnDeleteSessionCall.
	 */
	public SCMPClnDeleteSessionCall() {
		this(null, null);
	}

	/**
	 * Instantiates a new SCMPClnDeleteSessionCall.
	 * 
	 * @param req
	 *            the requester to use when invoking call
	 * @param scSession
	 *            the sc session
	 */
	public SCMPClnDeleteSessionCall(IRequester req, ISCSession scSession) {
		super(req, scSession);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester req, ISCSession scmpSession) {
		return new SCMPClnDeleteSessionCall(req, scmpSession);
	}

	/**
	 * Sets the service name.
	 * 
	 * @param serviceName
	 *            the new service name
	 */
	public void setServiceName(String serviceName) {
		requestMessage.setHeader(SCMPHeaderAttributeKey.SERVICE_NAME, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.CLN_DELETE_SESSION;
	}
}
