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
package org.serviceconnector.test.sc.srvExecute.synch;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnExecuteCall;
import org.serviceconnector.scmp.SCMPBodyType;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.sc.session.SuperSessionTestCase;

/**
 * @author JTraber
 */
public class SrvExecuteSyncTestCase extends SuperSessionTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public SrvExecuteSyncTestCase(String fileName) {
		super(fileName);
	}

	@Test
	public void multipleSrvExecuteTest() throws Exception {

		for (int i = 0; i < 100; i++) {
			SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(req,
					"simulation", this.sessionId);
			clnExecuteCall.setMessagInfo("message info");
			clnExecuteCall.setRequestBody("get Data (query)");
			clnExecuteCall.invoke(this.sessionCallback, 1000);
			SCMPMessage scmpReply = this.sessionCallback.getMessageSync();

			Assert.assertEquals("message data test case", scmpReply.getBody());
			Assert.assertEquals(SCMPBodyType.TEXT.getValue(), scmpReply.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
			int bodyLength = "message data test case".length();
			Assert.assertEquals(bodyLength + "", scmpReply.getBodyLength() + "");
			Assert.assertEquals(SCMPMsgType.CLN_EXECUTE.getValue(), scmpReply.getMessageType());
			String serviceName = clnExecuteCall.getRequest().getServiceName();
			String sessionId = clnExecuteCall.getRequest().getSessionId();
			Assert.assertEquals(serviceName, scmpReply.getServiceName());
			Assert.assertEquals(sessionId, scmpReply.getSessionId());
		}
	}

	@Test
	public void ExcOnServerExecuteTest() throws Exception {
		SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(req,
				"simulation", this.sessionId);
		clnExecuteCall.setMessagInfo("message info");
		clnExecuteCall.setRequestBody("excOnServer");
		clnExecuteCall.invoke(this.sessionCallback, 1000);
		SCMPMessage scmpReply = this.sessionCallback.getMessageSync();
		System.out.println(scmpReply.getBody());
		Assert.assertTrue(scmpReply.isFault());
		Assert.assertEquals(SCMPError.SERVER_ERROR.getErrorCode(), scmpReply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_CODE));
		
	}
}