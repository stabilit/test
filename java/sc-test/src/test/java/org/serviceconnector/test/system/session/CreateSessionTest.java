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
package org.serviceconnector.test.system.session;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.test.system.perf.SessionBenchmark;

public class CreateSessionTest {

	/** The Constant testLogger. */
	protected static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(CreateSessionTest.class);

	private static ProcessesController ctrl;
	private ProcessCtx scCtx;
	private ProcessCtx srvCtx;
	private SCClient client;
	private SCSessionService service;
	private int threadCount = 0;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void beforeOneTest() throws Exception {
		threadCount = Thread.activeCount();
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		srvCtx = ctrl.startServer(TestConstants.SERVER_TYPE_SESSION, TestConstants.log4jSrvProperties,
				TestConstants.sesServerName1, TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, 10,
				TestConstants.sesServiceName1);
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client.attach();
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			service.deleteSession();
		} catch (Exception e1) {
		}
		service = null;
		try {
			client.detach();
		} catch (Exception e) {
		}
		client = null;
		try {
			ctrl.stopServer(srvCtx);
		} catch (Exception e) {
		}
		srvCtx = null;
		try {
			ctrl.stopSC(scCtx);
		} catch (Exception e) {
		}
		scCtx = null;
		testLogger.info("Number of threads :" + Thread.activeCount() + " created :"+(Thread.activeCount() - threadCount));
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		ctrl = null;
	}

	/**
	 * Description: Create session with service which has been disabled<br>
	 * Expectation: throws SCMPValidatorException
	 */
	@Test (expected = SCMPValidatorException.class )
	public void t01_disabledService() throws Exception {
		// disable service
		SCMgmtClient clientMgmt = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_TCP);
		clientMgmt.attach();
		clientMgmt.disableService(TestConstants.sesServiceName1);
		clientMgmt.detach();
		
		SCMessage request = null;
		@SuppressWarnings("unused")
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		response = service.createSession(request);
		service.deleteSession();
	}




	@Test
	public void createSession_disabledService_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);

		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("something");
		sessionService.createSession( 10, scMessage);
		
		assertNotNull("the session ID is null", sessionService.getSessionId());
		sessionService.deleteSession();
		assertNull("the session ID is NOT null after deleteSession()", sessionService.getSessionId());
	}

	@Test
	public void deleteSession_beforeCreateSession_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void deleteSession_afterValidNewSessionService_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void createSession_twice_throwsExceptioin() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		try {
			sessionService.createSession( 10, scMessage);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_twiceWithDifferentSessionServices_differentSessionIds() throws Exception {
		SCSessionService sessionService0 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService sessionService1 = client.newSessionService(TestConstants.pubServiceName1);

		assertEquals(true, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage0 = new SCMessage();
		scMessage0.setSessionInfo("sessionInfo");
		sessionService0.createSession( 10, scMessage0);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		try {
			SCMessage scMessage1 = new SCMessage();
			scMessage1.setSessionInfo("sessionInfo");
			sessionService1.createSession( 10, scMessage1);
		} catch (SCServiceException e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		sessionService0.deleteSession();
		sessionService1.deleteSession();
	}

	@Test
	public void createSession_10000times_passes() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		for (int i = 1; i < 10001; i++) {
			if ((i % 500) == 0)
				testLogger.info("createSession_10000times cycle:\t" + i + " ...");
			SCMessage scMessage = new SCMessage();
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
			assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		}
	}


	@Test
	public void createSession_echoInterval1_sessionIdCreated() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession(10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}


	@Test (expected= SCMPValidatorException.class)
	public void createSession_timeout0_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 0, scMessage);
	}

	@Test (expected= SCMPValidatorException.class)
	public void createSession_timeoutMinus1_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( -1, scMessage);
	}

	@Test
	public void createSession_timeout1_sessionIdCreated() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 1, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_timeoutIntMin_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( Integer.MIN_VALUE, scMessage);
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_timeoutIntMax_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( Integer.MAX_VALUE, scMessage);
	}

	@Test
	public void createSession_timeout3600_sessionIdCreated() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 3600, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test (expected= SCMPValidatorException.class)
	public void createSession_timeout3601_sessionIdCreated() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);

		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 3601, scMessage);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void createSession_allInvalidParams_throwsSCMPValidatorException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		try {
			sessionService.createSession(-1, null);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCMPValidatorException);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_emptySessionServiceNameDataNull_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService("");
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_whiteSpaceSessionServiceNameDataNull_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(" ");
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_arbitrarySessionServiceNameNotInSCPropsDataNull_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.pangram);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_whiteSpaceSessionInfoDataNull_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo(" ");
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_arbitrarySpaceSessionInfoDataNull_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("The quick brown fox jumps over a lazy dog.");
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_256LongSessionInfoDataNull_sessionIdIsNotEmpty() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 256; i++) {
			sb.append('a');
		}
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo(sb.toString());
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_257LongSessionInfoDataNull_throwsException() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 257; i++) {
			sb.append('a');
		}
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		try {
			SCMessage scMessage = new SCMessage();
			scMessage.setSessionInfo(sb.toString());
			sessionService.createSession( 10, scMessage);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCMPValidatorException);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void deleteSession_afterValidCreateSessionDataNull_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test (expected = SCMPValidatorException.class)
	public void deleteSession_whiteSpaceSessionInfoDataNull_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage();
		scMessage.setSessionInfo(" ");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
	}

	@Test
	public void createSession_twiceDataNull_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage0 = new SCMessage();
		scMessage0.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage0);
		try {
			SCMessage scMessage1 = new SCMessage();
			scMessage1.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage1);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_1000timesDataNull_passes() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		for (int i = 1; i < 1001; i++) {
			if ((i % 100) == 0)
				testLogger.info("createSession_1000times cycle:\t" + i + " ...");
			SCMessage scMessage = new SCMessage();
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
			assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		}
	}



	@Test(expected = SCMPValidatorException.class)
	public void createSession_emptySessionInfoDataWhiteSpace_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo("");
		sessionService.createSession( 10, scMessage);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_whiteSpaceSessionInfoDataWhiteSpace_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo(" ");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
	}

	@Test
	public void createSession_arbitrarySpaceSessionInfoDataWhiteSpace_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo("The quick brown fox jumps over a lazy dog.");
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_256LongSessionInfoDataWhiteSpace_sessionIdIsNotEmpty() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 256; i++) {
			sb.append('a');
		}
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo(sb.toString());
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void deleteSession_afterValidCreateSessionDataWhiteSpace_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}


	@Test
	public void createSession_twiceDataWhiteSpace_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(" ");
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		try {
			SCMessage scMessage1 = new SCMessage();
			scMessage1.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage1);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_twiceWithDifferentSessionServicesDataWhiteSpace_differentSessionIds() throws Exception {
		SCSessionService sessionService0 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService sessionService1 = client.newSessionService(TestConstants.sesServiceName1);

		assertEquals(true, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage0 = new SCMessage(" ");
		scMessage0.setSessionInfo("sessionInfo");
		sessionService0.createSession( 10, scMessage0);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage1 = new SCMessage(" ");
		scMessage1.setSessionInfo("sessionInfo");
		sessionService1.createSession( 10, scMessage1);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(false, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		assertEquals(false, sessionService0.getSessionId().equals(sessionService1.getSessionId()));

		sessionService0.deleteSession();
		sessionService1.deleteSession();
	}

	@Test
	public void createSession_1000timesDataWhiteSpace_passes() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		for (int i = 1; i < 1001; i++) {
			if ((i % 100) == 0)
				testLogger.info("createSession_1000times cycle:\t" + i + " ...");
			SCMessage scMessage = new SCMessage(" ");
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
			assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		}
	}





	@Test(expected = SCMPValidatorException.class)
	public void createSession_emptySessionInfoDataOneChar_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo("");
		sessionService.createSession( 10, scMessage);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}



	@Test
	public void createSession_arbitrarySpaceSessionInfoDataOneChar_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo("The quick brown fox jumps over a lazy dog.");
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_256LongSessionInfoDataOneChar_sessionIdIsNotEmpty() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 256; i++) {
			sb.append('a');
		}
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo(sb.toString());
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_257LongSessionInfoDataOneChar_throwsException() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 257; i++) {
			sb.append('a');
		}
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		try {
			SCMessage scMessage = new SCMessage("a");
			scMessage.setSessionInfo(sb.toString());
			sessionService.createSession( 10, scMessage);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCMPValidatorException);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void deleteSession_afterValidCreateSessionDataOneChar_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test (expected = SCMPValidatorException.class )
	public void deleteSession_whiteSpaceSessionInfoDataOneChar_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo(" ");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void createSession_twiceDataOneChar_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage("a");
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		try {
			SCMessage scMessage1 = new SCMessage();
			scMessage1.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage1);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_twiceWithDifferentSessionServicesDataOneChar_differentSessionIds() throws Exception {
		SCSessionService sessionService0 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService sessionService1 = client.newSessionService(TestConstants.sesServiceName1);

		assertEquals(true, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage0 = new SCMessage("a");
		scMessage0.setSessionInfo("sessionInfo");
		sessionService0.createSession( 10, scMessage0);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage1 = new SCMessage("a");
		scMessage1.setSessionInfo("sessionInfo");
		sessionService1.createSession( 10, scMessage1);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(false, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		assertEquals(false, sessionService0.getSessionId().equals(sessionService1.getSessionId()));

		sessionService0.deleteSession();
		sessionService1.deleteSession();
	}

	@Test
	public void createSession_1000times_passes() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		for (int i = 1; i < 1001; i++) {
			if ((i % 100) == 0)
				testLogger.info("createSession_1000times cycle:\t" + i + " ...");
			SCMessage scMessage = new SCMessage("a");
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
			assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		}
	}

	@Test (expected = SCMPValidatorException.class)
	public void createSession_emptySessionServiceNameData60kBByteArray_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService("");

		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);

		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}




	@Test(expected = SCMPValidatorException.class)
	public void createSession_emptySessionInfoData60kBByteArray_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("");
		sessionService.createSession( 10, scMessage);
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_arbitrarySpaceSessionInfoData60kBByteArray_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("The quick brown fox jumps over a lazy dog.");
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_256LongSessionInfoData60kBByteArray_sessionIdIsNotEmpty() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo(TestConstants.stringLength256);
		sessionService.createSession( 10, scMessage);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void deleteSession_afterValidCreateSessionData60kBByteArray_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test (expected = SCMPValidatorException.class)
	public void deleteSession_whiteSpaceSessionInfoData60kBByteArray_noSessionId() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo(" ");
		sessionService.createSession( 10, scMessage);
		sessionService.deleteSession();
		assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
	}

	@Test
	public void createSession_twiceData60kBByteArray_throwsException() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("sessionInfo");
		sessionService.createSession( 10, scMessage);
		try {
			sessionService.createSession( 10, scMessage);
		} catch (Exception e) {
			ex = e;
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		sessionService.deleteSession();
	}

	@Test
	public void createSession_twiceWithDifferentSessionServicesData60kBByteArray_differentSessionIds() throws Exception {
		SCSessionService sessionService0 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService sessionService1 = client.newSessionService(TestConstants.sesServiceName1);

		assertEquals(true, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage.setSessionInfo("sessionInfo");
		sessionService0.createSession( 10, scMessage);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(true, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		SCMessage scMessage1 = new SCMessage(new byte[TestConstants.dataLength60kB]);
		scMessage1.setSessionInfo("sessionInfo");
		sessionService1.createSession( 10, scMessage1);

		assertEquals(false, sessionService0.getSessionId() == null || sessionService0.getSessionId().isEmpty());
		assertEquals(false, sessionService1.getSessionId() == null || sessionService1.getSessionId().isEmpty());

		assertEquals(false, sessionService0.getSessionId().equals(sessionService1.getSessionId()));

		sessionService0.deleteSession();
		sessionService1.deleteSession();
	}

	@Test
	public void createSession_1000timesData60kBByteArray_passes() throws Exception {
		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		for (int i = 1; i < 1001; i++) {
			if ((i % 100) == 0)
				testLogger.info("createSession_1000times cycle:\t" + i + " ...");
			SCMessage scMessage = new SCMessage(new byte[TestConstants.dataLength60kB]);
			scMessage.setSessionInfo("sessionInfo");
			sessionService.createSession( 10, scMessage);
			assertEquals(false, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
			sessionService.deleteSession();
			assertEquals(true, sessionService.getSessionId() == null || sessionService.getSessionId().isEmpty());
		}
	}

	@Test
	public void createSession_1000SessionsAtOnce_acceptAllOfThem() throws Exception {
		int i = 0;
		int sessionsCount = 10;
		String[] sessions = new String[sessionsCount];
		SCSessionService[] sessionServices = new SCSessionService[sessionsCount];
		try {
			for (i = 1; i < sessionsCount+1; i++) {
				if ((i % 100) == 0)
					testLogger.info("createSession_1000times cycle:\t" + i + " ...");
				sessionServices[i] = client.newSessionService(TestConstants.sesServiceName1);
				sessionServices[i].createSession( 10, new SCMessage());
				sessions[i] = sessionServices[i].getSessionId();
			}
		} catch (Exception ex) {
			assertEquals("Error on create "+i+" session, "+ex.getMessage(), true, false);
		}
		for (i = 0; i < sessionsCount; i++) {
			sessionServices[i].deleteSession();
			sessionServices[i] = null;
		}
		sessionServices = null;

		Arrays.sort(sessions);
		boolean duplicates = false;

		for (i = 1; i < sessionsCount; i++) {
			if (sessions[i].equals(sessions[i - 1])) {
				duplicates = true;
				break;
			}
		}
		assertEquals(false, duplicates);
	}

	@Test
	public void createSession_1001SessionsAtOnce_exceedsConnectionsLimitThrowsException() throws Exception {
		int sessionsCount = 100;
		int ctr = 0;
		String[] sessions = new String[sessionsCount];
		SCSessionService[] sessionServices = new SCSessionService[sessionsCount];
		try {
			for (int i = 1; i < sessionsCount+1; i++) {
				if ((i % 100) == 0)
					testLogger.info("createSession_1001times cycle:\t" + i + " ...");
				sessionServices[i] = client.newSessionService(TestConstants.sesServiceName1);
				sessionServices[i].createSession( 10, new SCMessage());
				sessions[i] = sessionServices[i].getSessionId();
				ctr++;
			}
		} catch (Exception e) {
			ex = e;
		}

		for (int i = 0; i < ctr; i++) {
			sessionServices[i].deleteSession();
			sessionServices[i] = null;
		}
		sessionServices = null;

		String[] successfulSessions = new String[ctr];
		System.arraycopy(sessions, 0, successfulSessions, 0, ctr);

		Arrays.sort(successfulSessions);
		boolean duplicates = false;

		for (int i = 1; i < ctr; i++) {
			if (successfulSessions[i].equals(successfulSessions[i - 1])) {
				duplicates = true;
				break;
			}
		}
		assertEquals(true, ex instanceof SCServiceException);
		assertEquals(sessionsCount - 1, ctr);
		assertEquals(false, duplicates);
	}

	@Test
	public void createSession_overBothConnectionTypes_passes() throws Exception {
		SCMgmtClient client2 = new SCMgmtClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client2.attach();

		SCSessionService session1 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService session2 = client2.newSessionService(TestConstants.sesServiceName1);

		session1.createSession( 10, new SCMessage());
		session2.createSession( 10, new SCMessage());

		assertEquals(false, session1.getSessionId().equals(session2.getSessionId()));

		session1.deleteSession();
		session2.deleteSession();

		assertEquals(session1.getSessionId(), session2.getSessionId());
		client2.detach();
		client2 = null;
	}

	@Test
	public void createSession_overBothConnectionTypesDifferentServices_passes() throws Exception {
		SCClient client2 = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client2.attach();

		SCSessionService session1 = client.newSessionService(TestConstants.sesServiceName1);
		SCSessionService session2 = client2.newSessionService(TestConstants.pubServiceName1);

		session1.createSession( 10, new SCMessage());
		session2.createSession( 10, new SCMessage());

		assertEquals(false, session1.getSessionId().equals(session2.getSessionId()));

		session1.deleteSession();
		session2.deleteSession();

		assertEquals(session1.getSessionId(), session2.getSessionId());
		client2.detach();
		client2 = null;
	}

	@Test
	public void sessionId_uniqueCheckFor10000IdsByOneClient_allSessionIdsAreUnique() throws Exception {
		int clientsCount = 10000;

		SCSessionService sessionService = client.newSessionService(TestConstants.sesServiceName1);
		String[] sessions = new String[clientsCount];

		for (int i = 1; i < clientsCount+1; i++) {
			if ((i % 500) == 0)
				testLogger.info("createSession_10000times cycle:\t" + i + " ...");
			sessionService.createSession( 60, new SCMessage());
			sessions[i] = sessionService.getSessionId();
			sessionService.deleteSession();
		}

		Arrays.sort(sessions);
		boolean duplicates = false;

		for (int i = 1; i < clientsCount; i++) {
			if (sessions[i].equals(sessions[i - 1])) {
				duplicates = true;
				break;
			}
		}
		assertEquals(false, duplicates);
	}
}