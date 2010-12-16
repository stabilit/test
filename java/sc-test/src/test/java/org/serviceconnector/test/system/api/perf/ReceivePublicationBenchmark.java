package org.serviceconnector.test.system.api.perf;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCMessageCallback;
import org.serviceconnector.api.SCService;
import org.serviceconnector.api.SCSubscribeMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.service.SCServiceException;

@SuppressWarnings("unused")
public class ReceivePublicationBenchmark {

	/** The Constant testLogger. */
	protected static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ReceivePublicationBenchmark.class);

	private static boolean messageReceived = false;
	private static ProcessesController ctrl;
	private ProcessCtx scCtx;
	private ProcessCtx srvCtx;
	private SCClient client;
	private SCPublishService service;
	private int threadCount = 0;
	long start= 0;
	long stop = 0;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void beforeOneTest() throws Exception {
		threadCount = Thread.activeCount();
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		srvCtx = ctrl.startServer(TestConstants.SERVER_TYPE_PUBLISH, TestConstants.log4jSrvProperties,
				TestConstants.pubServerName1, TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, 10,
				TestConstants.pubServiceName1);
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client.attach();
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			service.unsubscribe();
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
		testLogger.info("Number of threads :" + Thread.activeCount() + " created :" + (Thread.activeCount() - threadCount));
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		ctrl = null;
	}

	/**
	 * Description: receive one message (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void benchmark_10000_msg_compressed() throws Exception {
		service = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		SCSubscribeMessage subMsgResponse = null;
		MsgCallback cbk = new MsgCallback(service);
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo("publishMessages");
		int nrMessages = 10000;
		subMsgRequest.setData(Integer.toString(nrMessages));
		cbk.expectedMessages = nrMessages;
		start = System.currentTimeMillis();
		subMsgResponse = service.subscribe(subMsgRequest, cbk);
		waitForMessage(30);
		stop = System.currentTimeMillis();
		long perf = nrMessages * 1000 / (stop - start);
		testLogger.info(nrMessages + "msg � 128 byte performance : " + perf + " msg/sec.");
		service.unsubscribe();
	}
	
	private void waitForMessage(int nrSeconds) throws Exception {
		for (int i = 0; i < (nrSeconds * 10); i++) {
			if (messageReceived) {
				return;
			}
			Thread.sleep(100);
		}
		throw new TimeoutException("No message received within " + nrSeconds + " seconds timeout.");
	}

	private class MsgCallback extends SCMessageCallback {
		
		private SCMessage response = null;
		private int messageCounter = 0;
		private int expectedMessages = 0;

		public MsgCallback(SCService service) {
			super(service);
			ReceivePublicationBenchmark.messageReceived = false;
			response = null;
			messageCounter = 0;
			expectedMessages = 0;
		}

		@Override
		public void receive(SCMessage msg) {
			response = msg;
			messageCounter++;
			if (((messageCounter+1) % 1000) == 0) {
				ReceivePublicationBenchmark.testLogger.info("Receiving message nr. " + (messageCounter+1));
			}
			if ( expectedMessages == messageCounter) {
				stop = System.currentTimeMillis();
				ReceivePublicationBenchmark.messageReceived = true;
			}
		}

		@Override
		public void receive(Exception e) {
			logger.error("receive error: " + e.getMessage());
			if (e instanceof SCServiceException) {
				SCMPError scError = ((SCServiceException) e).getSCMPError();
				logger.info("SC error received code:" + scError.getErrorCode() + " text:" + scError.getErrorText());
			}
			response = null;
			ReceivePublicationBenchmark.messageReceived = true;
		}
	}
}
