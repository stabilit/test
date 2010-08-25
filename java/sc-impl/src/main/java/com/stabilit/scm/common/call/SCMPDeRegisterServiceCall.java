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
package com.stabilit.scm.common.call;

import org.apache.log4j.Logger;

import com.stabilit.scm.cln.call.ISCMPCall;
import com.stabilit.scm.cln.call.SCMPCallAdapter;
import com.stabilit.scm.common.net.req.IRequester;
import com.stabilit.scm.common.scmp.SCMPMsgType;

/**
 * The Class SCMPDeRegisterServiceCall. Call deregisters service.
 * 
 * @author JTraber
 */
public class SCMPDeRegisterServiceCall extends SCMPCallAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMPDeRegisterServiceCall.class);
	
	/**
	 * Instantiates a new SCMPDeRegisterServiceCall.
	 */
	public SCMPDeRegisterServiceCall() {
		this(null, null);
	}

	/**
	 * Instantiates a new SCMPDeRegisterServiceCall.
	 * 
	 * @param requester
	 *            the requester to use when invoking call
	 * @param serviceName
	 *            the service name
	 */
	public SCMPDeRegisterServiceCall(IRequester requester, String serviceName) {
		super(requester, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public ISCMPCall newInstance(IRequester requester, String serviceName) {
		return new SCMPDeRegisterServiceCall(requester, serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.DEREGISTER_SERVICE;
	}
}
