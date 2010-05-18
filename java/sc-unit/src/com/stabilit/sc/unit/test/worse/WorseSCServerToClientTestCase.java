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
package com.stabilit.sc.unit.test.worse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.stabilit.sc.ServiceConnector;
import com.stabilit.sc.cln.call.SCMPCallFactory;
import com.stabilit.sc.cln.call.SCMPClnDataCall;
import com.stabilit.sc.cln.call.SCMPSrvSystemCall;
import com.stabilit.sc.cln.client.ClientFactory;
import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.cln.config.ClientConfig;
import com.stabilit.sc.scmp.SCMPError;
import com.stabilit.sc.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.sim.Simulation;
import com.stabilit.sc.srv.cmd.factory.CommandFactory;
import com.stabilit.sc.srv.net.SCMPCommunicationException;
import com.stabilit.sc.unit.UnitCommandFactory;
import com.stabilit.sc.unit.test.SCTest;
import com.stabilit.sc.unit.test.SetupTestCases;
import com.stabilit.sc.unit.test.session.SuperSessionRegisterTestCase;

/**
 * @author JTraber
 */
public class WorseSCServerToClientTestCase extends SuperSessionRegisterTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public WorseSCServerToClientTestCase(String fileName) {
		super(fileName);
	}

	@Before
	@Override
	public void setup() {
		try {
			SetupTestCases.init();
			CommandFactory.setCurrentCommandFactory(new UnitCommandFactory());
			ServiceConnector.main(null);
			Simulation.main(null);
			config = new ClientConfig();
			config.load(fileName);
			ClientFactory clientFactory = new ClientFactory();
			client = clientFactory.newInstance(config.getClientConfig());
			client.connect(); // physical connect
			clnConnectBefore();
			registerServiceBefore();
			clnCreateSessionBefore();
			// needs to get disconnected (remove entry in connection registry before killing connection)
			clnDisconnectAfter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void clnDataSCServerToClientDisconnect() throws Exception {
		// disconnects simulation server from SC after sending response
		SCMPSrvSystemCall systemCall = (SCMPSrvSystemCall) SCMPCallFactory.SRV_SYSTEM_CALL.newInstance(client);
		systemCall.invoke();

		// data call should fail because connection lost to simulation server
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL.newInstance(client);
		clnDataCall.setServiceName("simulation");
		clnDataCall.setMessagInfo("asdasd");
		clnDataCall.setRequestBody("hello");
		try {
			clnDataCall.invoke();
		} catch (SCMPCommunicationException ex) {
			SCTest.verifyError((String) ex.getAttribute(SCMPHeaderAttributeKey.SC_ERROR_TEXT.getName()),
					(String) ex.getAttribute(SCMPHeaderAttributeKey.SC_ERROR_CODE.getName()),
					SCMPError.CONNECTION_LOST);
		}
		tearDownSCServerToService();
	}

	private void tearDownSCServerToService() throws Exception {
		ClientConfig config = new ClientConfig();
		config.load("sc-sim.properties");
		ClientFactory clientFactory = new ClientFactory();
		IClient tearDownClient = clientFactory.newInstance(config.getClientConfig());
		tearDownClient.connect(); // physical connect

		// disconnects server on SC to SimulatonServer
		SCMPSrvSystemCall systemCall = (SCMPSrvSystemCall) SCMPCallFactory.SRV_SYSTEM_CALL
				.newInstance(tearDownClient);
		systemCall.setRequestBody("simulation:P01_RTXS_RPRWS1");
		systemCall.invoke();
		tearDownClient.disconnect();
	}

	/**
	 * Tear down. Needs to be overridden because clnDeleteSession() from usual procedure is not possible this time
	 * - backend server already down.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@After
	@Override
	public void tearDown() throws Exception {
		client.disconnect();
	}
}