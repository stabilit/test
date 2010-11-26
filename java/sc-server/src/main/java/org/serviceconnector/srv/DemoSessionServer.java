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
package org.serviceconnector.srv;

import org.apache.log4j.Logger;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.srv.SCServer;
import org.serviceconnector.api.srv.SCSessionServer;
import org.serviceconnector.api.srv.SCSessionServerCallback;

public class DemoSessionServer extends Thread {
	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DemoSessionServer.class);
	
	/**
	 * Main method if you like to start in debug mode.
	 */
	public static void main(String[] args) throws Exception {
		DemoSessionServer sessionServer = new DemoSessionServer();
		sessionServer.run();
	}

	public SCSessionServerCallback newSrvCallback(SCSessionServer server) {
		SCSessionServerCallback cbk = new SrvCallback(server);
		return cbk;
	}

	@Override
	public void run() {

		SCServer sc = new SCServer("localhost", 9000, 9002); // regular, defaults documented in javadoc
		//sc = new SCServer("localhost", 9000, 9002, ConnectionType.NETTY_TCP); // alternative with connection type

		try {
			sc.setKeepAliveIntervalInSeconds(10); // can be set before register
			sc.setImmediateConnect(true); // can be set before register
			sc.startListener(); // regular
			
			String serviceName = "session-1";
			SCSessionServer server = sc.newSessionServer(serviceName); // no other params possible
			int maxSess = 10;
			int maxConn = 5;
			SCSessionServerCallback cbk = newSrvCallback(server);
			try {
				server.registerServer(maxSess, maxConn, cbk); // regular
				// server.registerServer(10, maxSess, maxConn, cbk); // alternative with operation timeout
			} catch (Exception e) {
				logger.error("runSessionServer", e);
				server.deregisterServer();
				// server.deregisterServer(10);
			}
			// server.destroy();
		} catch (Exception e) {
			logger.error("runSessionServer", e);
		} finally {
			// sc.stopListener();
		}
	}

	class SrvCallback extends SCSessionServerCallback {
		private SCSessionServer scSessionServer;
		
		public SrvCallback(SCSessionServer server) {
			this.scSessionServer = server;
		}

		@Override
		public SCMessage createSession(SCMessage request, int operationTimeoutInMillis) {
			logger.info("Session created");
			return request;
		}

		@Override
		public void deleteSession(SCMessage request, int operationTimeoutInMillis) {
			logger.info("Session deleted");
		}

		@Override
		public void abortSession(SCMessage request, int operationTimeoutInMillis) {
			logger.info("Session aborted");
		}

		@Override
		public SCMessage execute(SCMessage request, int operationTimeoutInMillis) {
			Object data = request.getData();

			// watch out for kill server message
			if (data.getClass() == String.class) {
				String dataString = (String) data;
				if (dataString.equals("kill server")) {
					KillThread kill = new KillThread(this.scSessionServer);
					kill.start();
				} else {
					logger.info("Message received: " + data);
				}
			}
			return request;
		}
	}

	protected class KillThread extends Thread {
		private SCSessionServer server;
		public KillThread(SCSessionServer server) {
			this.server = server;
		}

		@Override
		public void run() {
			// sleep for 2 seconds before killing the server
			try {
				Thread.sleep(2000);
				this.server.deregisterServer();
				//SCServer sc = server.getSCServer().stopListener();
			} catch (Exception e) {
				logger.error("run", e);
			}
		}
	}
}