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
package org.serviceconnector.test.register;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPDeRegisterServiceCall;
import org.serviceconnector.call.SCMPInspectCall;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.unit.SCTest;



public class DeRegisterServiceTestCase extends SuperRegisterTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public DeRegisterServiceTestCase(String fileName) {
		super(fileName);
	}

	@Test
	public void deRegisterServiceCall() throws Exception {
		SCMPDeRegisterServiceCall deRegisterServiceCall = (SCMPDeRegisterServiceCall) SCMPCallFactory.DEREGISTER_SERVICE_CALL
				.newInstance(this.registerRequester, "publish-simulation");

		deRegisterServiceCall.invoke(this.attachCallback, 1000);
		SCTest.checkReply(this.attachCallback.getMessageSync());
		/*************** scmp inspect ********/
		SCMPInspectCall inspectCall = (SCMPInspectCall) SCMPCallFactory.INSPECT_CALL.newInstance(req);
		inspectCall.invoke(this.attachCallback, 1000);
		SCMPMessage inspect = this.attachCallback.getMessageSync();

		/*********************************** Verify registry entries in SC ********************************/
		String inspectMsg = (String) inspect.getBody();
		Map<String, String> inspectMap = SCTest.convertInspectStringToMap(inspectMsg);

		String scEntry = (String) inspectMap.get("serviceRegistry");
		String expectedEntry = "P01_logging:0|publish-simulation:0|P01_RTXS_sc1:0|simulation:0 - simulation_localhost/:7000 : 10|P01_BCST_CH_sc1:0|";
		SCTest.assertEqualsUnorderedStringIgnorePorts(expectedEntry, scEntry);
		super.registerServiceBefore();
	}

	@Test
	public void secondDeRegisterServiceCall() throws Exception {
		super.deRegisterServiceAfter();
		SCMPDeRegisterServiceCall deRegisterServiceCall = (SCMPDeRegisterServiceCall) SCMPCallFactory.DEREGISTER_SERVICE_CALL
				.newInstance(this.registerRequester, "publish-simulation");

		deRegisterServiceCall.invoke(this.attachCallback, 1000);
		SCMPMessage fault = this.attachCallback.getMessageSync();
		Assert.assertTrue(fault.isFault());
		Assert
				.assertEquals(SCMPMsgType.DEREGISTER_SERVICE.getValue(), fault
						.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertEquals(SCMPError.NOT_FOUND.getErrorCode(), fault.getHeader(SCMPHeaderAttributeKey.SC_ERROR_CODE));
		super.registerServiceBefore();
	}
}