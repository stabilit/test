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

import org.apache.log4j.Logger;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.IRequest;
import org.serviceconnector.scmp.IResponse;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.server.FileServer;
import org.serviceconnector.service.FileSession;
import org.serviceconnector.util.ValidatorUtility;

public class FileUploadCommand extends CommandAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(FileUploadCommand.class);

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.FILE_UPLOAD;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response) throws Exception {
		SCMPMessage message = request.getMessage();
		FileSession session = (FileSession) this.getSessionById(message.getSessionId());
		// cancel session timeout
		this.sessionRegistry.cancelSessionTimeout(session);
		try {
			int oti = message.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
			String remoteFileName = (String) message.getHeader(SCMPHeaderAttributeKey.REMOTE_FILE_NAME);

			FileServer fileServer = session.getFileServer();

			SCMPMessage reply = fileServer.serverUploadFile(session, message, remoteFileName, oti);

			// forward server reply to client
			reply.setIsReply(true);
			reply.setMessageType(getKey());
			response.setSCMP(reply);
		} finally {
			// schedule session timeout
			this.sessionRegistry.scheduleSessionTimeout(session);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		try {
			SCMPMessage message = request.getMessage();
			// remoteFileName
			String remoteFileName = (String) message.getHeader(SCMPHeaderAttributeKey.REMOTE_FILE_NAME);
			if (remoteFileName == null || remoteFileName.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_REMOTE_FILE_NAME, "remoteFileName must be set");
			}
			// operation timeout
			String otiValue = message.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT.getValue());
			ValidatorUtility.validateInt(10, otiValue, 3600000, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
			// serviceName
			String serviceName = message.getServiceName();
			if (serviceName == null || serviceName.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_SERVICE_NAME, "serviceName must be set");
			}
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			logger.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}
}