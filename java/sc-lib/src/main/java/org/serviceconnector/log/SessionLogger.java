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
package org.serviceconnector.log;

import java.util.Formatter;

import org.apache.log4j.Logger;


public class SessionLogger {

	private static final Logger sessionLogger = Logger.getLogger(Loggers.SESSION.getValue());
	private static final SessionLogger instance = new SessionLogger();

	private String CREATE_SESSION_STR = "create session:%s";
	private String DELETE_SESSION_STR = "delete session:%s";
	private String ABORT_SESSION_STR = "abort session:%s";
	private String TIMEOUT_SESSION_STR = "timeout session:%s";

	/**
	 * Private constructor for singleton use.
	 */
	private SessionLogger() {
	}

	public static SessionLogger getInstance() {
		return SessionLogger.instance;
	}

	/**
	 * @param className
	 * @param sessionId
	 */
	public synchronized void logCreateSession(String className, String sessionId) {
		if (sessionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(CREATE_SESSION_STR, sessionId);
			sessionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @param className
	 * @param sessionId
	 */
	public synchronized void logDeleteSession(String className, String sessionId) {
		if (sessionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(DELETE_SESSION_STR, sessionId);
			sessionLogger.debug(format.toString());
			format.close();
		}
	}
	
	/**
	 * @param className
	 * @param sessionId
	 */
	public synchronized void logTimeoutSession(String className, String sessionId) {
		if (sessionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(TIMEOUT_SESSION_STR, sessionId);
			sessionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @param className
	 * @param sessionId
	 */
	public synchronized void logAbortSession(String className, String sessionId) {
		if (sessionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(ABORT_SESSION_STR, sessionId);
			sessionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return sessionLogger.isTraceEnabled();
	}
}
