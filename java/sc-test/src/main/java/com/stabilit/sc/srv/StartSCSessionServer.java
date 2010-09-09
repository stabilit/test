package com.stabilit.sc.srv;

import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.stabilit.sc.ctrl.util.TestEnvironmentController;
import com.stabilit.scm.common.service.ISCMessage;
import com.stabilit.scm.srv.ISCServer;
import com.stabilit.scm.srv.ISCSessionServerCallback;
import com.stabilit.scm.srv.SCServer;

public class StartSCSessionServer {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(StartSCSessionServer.class);

	private ISCServer scSrv = null;
	private String startFile = null;
	private String[] serviceNames;
	private int port = 9000;
	private int listenerPort = 30000;
	private int maxCons = 10;

	public static void main(String[] args) throws Exception {
		StartSCSessionServer sessionServer = new StartSCSessionServer();
		
		sessionServer.runSessionServer(args);
	}

	public void runSessionServer(String[] args) {
		try {
			this.scSrv = new SCServer();

			try {
				this.listenerPort = Integer.parseInt(args[0]);
				this.port = Integer.parseInt(args[1]);
				this.maxCons = Integer.parseInt(args[2]);
				this.startFile = args[3];
				this.serviceNames = new String[args.length - 4];
				System.arraycopy(args, 4, serviceNames, 0, args.length - 4);
			} catch (Exception e) {
				logger.error("incorrect parameters", e);
				throw e;
			}
			
			// connect to SC as server
			this.scSrv.setImmediateConnect(true);
			this.scSrv.startListener("localhost", listenerPort, 0);

			SrvCallback srvCallback = new SrvCallback(new SessionServerContext());
			
			for (int i = 0; i < serviceNames.length; i++) {
				this.scSrv.registerService("localhost", port, serviceNames[i], 1000, getMaxCons(),
						srvCallback);
			}

			//for testing whether the server already started 
			new TestEnvironmentController().createFile(startFile);
			
			String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
			long pid = Long.parseLong(processName.split("@")[0]);
			FileWriter fw = null;
			try {
				File pidFile = new File(startFile);
				fw = new FileWriter(pidFile);
				fw.write("pid: " + pid);
				fw.flush();
				fw.close();
			} finally {
				if (fw != null) {
					fw.close();
				}
			}
			
		} catch (Exception e) {
			logger.error("runSessionServer", e);
			this.shutdown();
		}
	}

	private void shutdown() {
		try {
			for (int i = 0; i < serviceNames.length; i++) {
				this.scSrv.deregisterService(serviceNames[i]);
			}
		} catch (Exception e) {
			this.scSrv = null;
		}
	}

	public void setMaxCons(int maxCons) {
		this.maxCons = maxCons;
	}

	public int getMaxCons() {
		return maxCons;
	}

	class SrvCallback implements ISCSessionServerCallback {

		private SessionServerContext outerContext;

		public SrvCallback(SessionServerContext context) {
			this.outerContext = context;
		}

		@Override
		public ISCMessage createSession(ISCMessage message) {
			logger.debug("SessionServer.SrvCallback.createSession()");
			return message;
		}

		@Override
		public void deleteSession(ISCMessage message) {
			logger.debug("SessionServer.SrvCallback.deleteSession()");
		}

		@Override
		public void abortSession(ISCMessage message) {
			logger.debug("SessionServer.SrvCallback.abortSession()");
		}

		@Override
		public ISCMessage execute(ISCMessage request) {
			Object data = request.getData();

			if (data != null) {
				// watch out for kill server message
				if (data.getClass() == String.class) {
					String dataString = (String) data;
					if (dataString.equals("kill server")) {
						try {
							KillThread kill = new KillThread(this.outerContext.getServer());
							kill.start();
						} catch (Exception e) {
							logger.error("execute", e);
						}
					}
				}
			}
			return request;
		}
	}

	private class SessionServerContext {
		public ISCServer getServer() {
			return scSrv;
		}
	}

	private class KillThread extends Thread {

		private ISCServer server;

		public KillThread(ISCServer server) {
			this.server = server;
		}

		@Override
		public void run() {
			// sleep before killing the server
			try {
				Thread.sleep(100);
				for (int i = 0; i < serviceNames.length; i++) {
					this.server.deregisterService(serviceNames[i]);
				}
				this.server.destroyServer();
			} catch (Exception e) {
				logger.error("run", e);
			}
		}
	}
}
