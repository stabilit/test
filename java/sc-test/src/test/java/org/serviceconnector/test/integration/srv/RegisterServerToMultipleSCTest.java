/*
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
 */
package org.serviceconnector.test.integration.srv;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.srv.SCSessionServerCallback;
import org.serviceconnector.api.srv.SCSessionServer;
import org.serviceconnector.ctrl.util.ProcessesController;


public class RegisterServerToMultipleSCTest {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(RegisterServerToMultipleSCTest.class);
	
	private int threadCount = 0;
	private SCSessionServer server;

	private static ProcessesController ctrl;
	private static Process scProcess;
	private static Process r;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
			r = ctrl.startSC(TestConstants.log4jSCcascadedProperties, TestConstants.SCcascadedProperties);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl.stopProcess(scProcess, TestConstants.log4jSCProperties);
		ctrl.stopProcess(r, TestConstants.log4jSCcascadedProperties);
		ctrl = null;
		scProcess = null;
		r = null;
	}

	@Before
	public void setUp() throws Exception {
//		threadCount = Thread.activeCount();
		server = new SCSessionServer();
	}

	@After
	public void tearDown() throws Exception {
		server.destroy();
		server = null;
//		assertEquals("number of threads", threadCount, Thread.activeCount());
	}

	@Test
	public void registerServer_onMultipleSCs_registeredOnBoth() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.publishServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
		server.deregister(TestConstants.sessionServiceName);
		assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
		server.deregister(TestConstants.publishServiceName);
		assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
	}

	@Test
	public void registerServer_withDifferentConnectionTypesHttpFirst_registeredThenNot()
			throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerServer(TestConstants.HOST, TestConstants.PORT_HTTP, TestConstants.sessionServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		((SCSessionServer) server).setConnectionType("netty.tcp");
		server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.publishServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
		server.deregister(TestConstants.sessionServiceName);
		server.deregister(TestConstants.publishServiceName);
		assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
	}

	@Test
	public void registerServer_withDifferentConnectionTypesTcpFirst_registeredThenNot()
			throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerServer(TestConstants.HOST, TestConstants.PORT_MIN, TestConstants.publishServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
		server.deregister(TestConstants.sessionServiceName);
		server.deregister(TestConstants.publishServiceName);
		assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
	}

	@Test
	public void registerServer_httpConnectionType_registeredThenNot() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		server.registerServer(TestConstants.HOST, TestConstants.PORT_HTTP, TestConstants.sessionServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		server.registerServer(TestConstants.HOST, TestConstants.PORT_MIN, TestConstants.publishServiceName, 1, 1, new CallBack());
		assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
		server.deregister(TestConstants.sessionServiceName);
		server.deregister(TestConstants.publishServiceName);
		assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
		assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
	}

	@Test
	public void registerServerDeregisterServer_onTwoSCsHttp_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		((SCSessionServer) server).setConnectionType("netty.http");
		for (int i = 0; i < 100; i++) {
			server.registerServer(TestConstants.HOST, TestConstants.PORT_HTTP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MIN, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		}
	}

	@Test
	public void registerServerDeregisterServer_onTwoSCsTcp_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		for (int i = 0; i < 100; i++) {
			server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		}
	}
	
	//TODO verify with jan - service name must be unique!!!
//	@Test
	public void registerServerDeregisterServer_onTwoSCsBoth_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		for (int i = 0; i < 100; i++) {
			((SCSessionServer) server).setConnectionType("netty.tcp");
			server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.http");
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MIN, TestConstants.sessionServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.sessionServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			System.out.println(i);
		}
	}

	@Test
	public void registerServerDeregisterServer_onTwoSCsChangingConnectionTypes_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		for (int i = 0; i < 50; i++) {
			server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.http");
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MIN, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
			server.registerServer(TestConstants.HOST, TestConstants.PORT_HTTP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			((SCSessionServer) server).setConnectionType("netty.tcp");
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
			((SCSessionServer) server).setConnectionType("netty.tcp");
		}
	}

	@Test
	public void registerServerDeregisterServer_onTwoSCsChangingServices_periodicallyRegistered() throws Exception {
		server.startListener(TestConstants.HOST, TestConstants.PORT_LISTENER, 0);
		for (int i = 0; i < 50; i++) {
			server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.sessionServiceName, 1, 1, new CallBack());
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
			server.registerServer(TestConstants.HOST, TestConstants.PORT_MAX, TestConstants.sessionServiceName, 1, 1, new CallBack());
			server.registerServer(TestConstants.HOST, TestConstants.PORT_TCP, TestConstants.publishServiceName, 1, 1, new CallBack());
			assertEquals(true, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(true, server.isRegistered(TestConstants.publishServiceName));
			server.deregister(TestConstants.sessionServiceName);
			server.deregister(TestConstants.publishServiceName);
			assertEquals(false, server.isRegistered(TestConstants.sessionServiceName));
			assertEquals(false, server.isRegistered(TestConstants.publishServiceName));
		}
	}

	private class CallBack extends SCSessionServerCallback {

		@Override
		public SCMessage execute(SCMessage message, int operationTimeoutInMillis) {
			return null;
		}
	}
}
