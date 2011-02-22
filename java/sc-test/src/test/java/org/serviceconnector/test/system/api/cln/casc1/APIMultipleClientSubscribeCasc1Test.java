/*
 * Copyright � 2010 STABILIT Informatik AG, Switzerland *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License"); *
 * you may not use this file except in compliance with the License. *
 * You may obtain a copy of the License at *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0 *
 * *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS, *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and *
 * limitations under the License. *
 */
package org.serviceconnector.test.system.api.cln.casc1;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestUtil;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ServerDefinition;
import org.serviceconnector.ctrl.util.ServiceConnectorDefinition;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.test.system.SystemSuperTest;

public class APIMultipleClientSubscribeCasc1Test extends SystemSuperTest {

	public APIMultipleClientSubscribeCasc1Test() {
		APIMultipleClientSubscribeCasc1Test.setUp1CascadedServiceConnectorAndServer();
	}

	public static void setUpServiceConnectorAndServer() {
		// SC definitions
		List<ServiceConnectorDefinition> sc0Defs = new ArrayList<ServiceConnectorDefinition>();
		ServiceConnectorDefinition sc0Def = new ServiceConnectorDefinition(TestConstants.SC0, TestConstants.SC0Properties,
				TestConstants.log4jSC0Properties);
		sc0Defs.add(sc0Def);

		// server definitions
		List<ServerDefinition> srvToSC0Defs = new ArrayList<ServerDefinition>();
		ServerDefinition srv1ToSC0Def = new ServerDefinition(TestConstants.COMMUNICATOR_TYPE_PUBLISH,
				TestConstants.log4jSrvProperties, TestConstants.pubServerName1, TestConstants.PORT_PUB_SRV_TCP,
				TestConstants.PORT_SC_TCP, 10, 5, TestConstants.pubServerName1);
		ServerDefinition srv2ToSC0Def = new ServerDefinition(TestConstants.COMMUNICATOR_TYPE_PUBLISH,
				TestConstants.log4jSrvProperties, TestConstants.pubServiceName2, 30002, TestConstants.PORT_SC_TCP, 10, 5,
				TestConstants.pubServiceName2);
		srvToSC0Defs.add(srv1ToSC0Def);
		srvToSC0Defs.add(srv2ToSC0Def);

		SystemSuperTest.scDefs = sc0Defs;
		SystemSuperTest.srvDefs = srvToSC0Defs;
	}

	public static void setUp1CascadedServiceConnectorAndServer() {
		List<ServiceConnectorDefinition> scCascDefs = new ArrayList<ServiceConnectorDefinition>();
		ServiceConnectorDefinition sc0CascDef = new ServiceConnectorDefinition(TestConstants.SC0_CASC,
				TestConstants.SC0CASCProperties, TestConstants.log4jSC0CASCProperties);
		ServiceConnectorDefinition sc1CascDef = new ServiceConnectorDefinition(TestConstants.SC1_CASC,
				TestConstants.SC1CASC1Properties, TestConstants.log4jSC1CASCProperties);
		scCascDefs.add(sc0CascDef);
		scCascDefs.add(sc1CascDef);

		// server definitions
		List<ServerDefinition> srvToSC0CascDefs = new ArrayList<ServerDefinition>();
		ServerDefinition srv1ToSC0CascDef = new ServerDefinition(TestConstants.COMMUNICATOR_TYPE_PUBLISH,
				TestConstants.log4jSrvProperties, TestConstants.pubServerName1, TestConstants.PORT_PUB_SRV_TCP,
				TestConstants.PORT_SC0_CASC_TCP, 10, 5, TestConstants.pubServerName1);
		ServerDefinition srv2ToSC0CascDef = new ServerDefinition(TestConstants.COMMUNICATOR_TYPE_PUBLISH,
				TestConstants.log4jSrvProperties, TestConstants.pubServiceName2, 30002, TestConstants.PORT_SC0_CASC_TCP, 10, 5,
				TestConstants.pubServiceName2);
		srvToSC0CascDefs.add(srv1ToSC0CascDef);
		srvToSC0CascDefs.add(srv2ToSC0CascDef);

		SystemSuperTest.scDefs = scCascDefs;
		SystemSuperTest.srvDefs = srvToSC0CascDefs;
	}

	/**
	 * Description: 2 clients Subscribe, receive 10000 message and unsubscribe<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_2ClientsReceiving10000Messages() throws Exception {
		int numberOfClients = 2;
		ProcessCtx[] clientCtxs = new ProcessCtx[numberOfClients];

		for (int i = 0; i < clientCtxs.length; i++) {
			ProcessCtx clientCtx = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client" + i, TestConstants.HOST,
					TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
					"f_subscribeReceive10000Unsubscribe");
			clientCtxs[i] = clientCtx;
		}
		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtxs);
		// dont't check message.log might be an EXC because of broken CRP
		TestUtil.checkLogFile(TestConstants.log4jClnProperties, "client.log");
	}

	/**
	 * Description: 2 clients Subscribe, receive 500/10000 message and unsubscribe, different services<br>
	 * Expectation: passes
	 */
	@Test
	public void t02_2ClientsReceiving500_10000MessagesDiffService() throws Exception {

		ProcessCtx clientCtx = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client1", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
				"f_subscribeReceive500Unsubscribe");

		ProcessCtx clientCtx2 = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client2", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServiceName2, 50,
				"f_subscribeReceive10000Unsubscribe");

		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtx);
		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtx2);
		// dont't check message.log might be an EXC because of broken CRP
		TestUtil.checkLogFile(TestConstants.log4jClnProperties, "client.log");
	}

	/**
	 * Description: 2 clients Subscribe, receive 20 message and unsubscribe, same service different noDataInterval which is smaller
	 * than message sending delay on server<br>
	 * Expectation: passes
	 */
	@Test
	public void t05_2ClientsReceiving20MessagesDelayed() throws Exception {
		int noDataInterval1 = 10;
		int noDataInterval2 = 11;

		ProcessCtx clientCtx = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client1", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, noDataInterval1,
				"f_subscribeReceive20_12SecUnsubscribe");

		ProcessCtx clientCtx2 = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client2", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServiceName1, noDataInterval2,
				"f_subscribeReceive20_12SecUnsubscribe");

		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtx);
		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtx2);
		// dont't check message.log might be an EXC because of broken CRP
		TestUtil.checkLogFile(TestConstants.log4jClnProperties, "client.log");
	}

	/**
	 * Description: 10 clients Subscribe, receive 10000 message and unsubscribe<br>
	 * Expectation: passes
	 */
	@Test
	public void t10_10ClientsReceiving10000Messages() throws Exception {
		int numberOfClients = 10;
		ProcessCtx[] clientCtxs = new ProcessCtx[numberOfClients];

		for (int i = 0; i < clientCtxs.length - 1; i++) {
			ProcessCtx clientCtx = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client" + i, TestConstants.HOST,
					TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
					"f_subscribeUnsubscribe");
			clientCtxs[i] = clientCtx;
		}
		ProcessCtx clientCtx10 = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client10", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
				"f_subscribeReceive10000Unsubscribe");
		clientCtxs[9] = clientCtx10;
		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtxs);
		// dont't check message.log might be an EXC because of broken CRP
		TestUtil.checkLogFile(TestConstants.log4jClnProperties, "client.log");
	}

	/**
	 * Description: 3 clients Subscribe, 1 receives 10000, 2 receives 500 message and unsubscribe<br>
	 * Expectation: passes
	 */
	@Test
	public void t11_3ClientsReceivingMessages() throws Exception {
		int numberOfClients = 2;
		ProcessCtx[] clientCtxs = new ProcessCtx[numberOfClients];

		ProcessCtx clientCtx3 = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client0", TestConstants.HOST,
				TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
				"f_subscribeReceive10000Unsubscribe");
		clientCtxs[0] = clientCtx3;
		for (int i = 1; i < clientCtxs.length; i++) {
			ProcessCtx clientCtx = ctrl.startPublishClient(TestConstants.log4jClnProperties, "client" + i, TestConstants.HOST,
					TestConstants.PORT_SC_TCP, ConnectionType.NETTY_TCP, 10, 0, TestConstants.pubServerName1, 50,
					"f_subscribeReceive500Unsubscribe");
			clientCtxs[i] = clientCtx;
		}
		APIMultipleClientSubscribeCasc1Test.ctrl.waitForClientTermination(clientCtxs);
		// dont't check message.log might be an EXC because of broken CRP
		TestUtil.checkLogFile(TestConstants.log4jClnProperties, "client.log");
	}

}