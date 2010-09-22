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
package org.serviceconnector.api.cln;

import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCService;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnCreateSessionCall;
import org.serviceconnector.call.SCMPClnDeleteSessionCall;
import org.serviceconnector.call.SCMPClnExecuteCall;
import org.serviceconnector.call.SCMPEchoCall;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.net.req.RequesterContext;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.sc.service.ISCContext;
import org.serviceconnector.sc.service.ISCMessageCallback;
import org.serviceconnector.sc.service.SCServiceException;
import org.serviceconnector.scmp.ISCMPCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.util.ITimerRun;
import org.serviceconnector.util.TimerTaskWrapper;
import org.serviceconnector.util.ValidatorUtility;


/**
 * The Class SessionService. SessionService is a remote interface in client API to a session service and provides
 * communication functions.
 * 
 * @author JTraber
 */
public class SCSessionService extends SCService implements ISessionService {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SCSessionService.class);
	/** The timer, which observes the session timeout of service. */
	private Timer timer;
	/** The timer run, runs when session need to be refreshed on SC. */
	private ITimerRun timerRun;
	/** The timer task. */
	private TimerTask timerTask;
	/** The session dead, marks state of a session. */
	private volatile boolean sessionDead;

	private int scResponseTimeMillis;

	/**
	 * Instantiates a new session service.
	 * 
	 * @param serviceName
	 *            the service name
	 * @param context
	 *            the context
	 */
	public SCSessionService(String serviceName, ISCContext context) {
		super(serviceName, context);
		this.requester = new SCRequester(new RequesterContext(context.getConnectionPool(), this.msgId));
		this.serviceContext = new ServiceContext(context, this);
		this.timerRun = null;
		this.timer = new Timer("SessionServiceTimeout");
		this.sessionDead = false;
		this.scResponseTimeMillis = Constants.OPERATION_TIMEOUT_MILLIS_SHORT;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int echoIntervalInSeconds) throws Exception {
		this.createSession(sessionInfo, echoIntervalInSeconds, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS, null);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int echoIntervalInSeconds, int timeoutInSeconds)
			throws Exception {
		this.createSession(sessionInfo, echoIntervalInSeconds, timeoutInSeconds, null);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int echoIntervalInSeconds, Object data) throws Exception {
		this.createSession(sessionInfo, echoIntervalInSeconds, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS, data);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int echoIntervalInSeconds, int timeoutInSeconds,
			Object data) throws Exception {
		if (this.callback != null) {
			throw new SCServiceException("session already created - delete session first.");
		}
		if (data != null) {
			// validate body not bigger than 60 Kb
			int length = (new SCMPMessage(data)).getBodyLength();
			if (length < 1 || length > 61440) {
				throw new SCMPValidatorException(SCMPError.HV_ERROR, "data too big - over 60Kb");
			}
		}
		ValidatorUtility.validateStringLength(1, sessionInfo, 256, SCMPError.HV_WRONG_SESSION_INFO);
		ValidatorUtility.validateInt(1, timeoutInSeconds, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
		ValidatorUtility.validateInt(1, echoIntervalInSeconds, 3600, SCMPError.HV_WRONG_ECHO_INTERVAL);
		this.msgId.reset();
		this.callback = new ServiceCallback(true);
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(this.requester, this.serviceName);
		createSessionCall.setSessionInfo(sessionInfo);
		createSessionCall.setEchoIntervalSeconds(echoIntervalInSeconds);
		createSessionCall.setRequestBody(data);
		try {
			createSessionCall.invoke(this.callback, timeoutInSeconds * Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			this.callback = null;
			throw new SCServiceException("create session failed", e);
		}
		SCMPMessage reply = this.callback.getMessageSync();
		if (reply.isFault() || reply.getHeaderFlag(SCMPHeaderAttributeKey.REJECT_SESSION)) {
			this.callback = null;
			SCServiceException ex = new SCServiceException("create session failed"
					+ reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			ex.setAppErrorCode(reply.getHeader(SCMPHeaderAttributeKey.APP_ERROR_CODE));
			ex.setAppErrorText(reply.getHeader(SCMPHeaderAttributeKey.APP_ERROR_TEXT));
			throw ex;
		}
		this.sessionId = reply.getSessionId();
		// trigger session timeout
		this.timerRun = new SessionTimeouter((int) echoIntervalInSeconds);
		this.timerTask = new TimerTaskWrapper(this.timerRun);
		this.timer.schedule(timerTask, (int) (echoIntervalInSeconds * Constants.SEC_TO_MILLISEC_FACTOR));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void deleteSession() throws Exception {
		this.deleteSession(Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void deleteSession(int timeoutInSeconds) throws Exception {
		if (this.callback == null) {
			// delete session not possible - no session on this service just ignore
			return;
		}
		if (this.pendingRequest) {
			// already executed before - reply still outstanding
			throw new SCServiceException(
					"execute not possible, there is a pending request - two pending request are not allowed.");
		}
		ValidatorUtility.validateInt(1, timeoutInSeconds, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
		this.pendingRequest = true;
		// cancel session timeout
		this.timerTask.cancel();
		this.callback = new ServiceCallback(true);
		try {
			this.msgId.incrementMsgSequenceNr();
			SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
					.newInstance(this.requester, this.serviceName, this.sessionId);
			try {
				deleteSessionCall.invoke(this.callback, timeoutInSeconds * Constants.SEC_TO_MILLISEC_FACTOR);
			} catch (Exception e) {
				if (this.sessionDead) {
					// ignore errors in state of dead session
					return;
				}
				throw new SCServiceException("delete session failed", e);
			}
			SCMPMessage reply = this.callback.getMessageSync();
			if (reply.isFault()) {
				if (this.sessionDead) {
					// ignore errors in state of dead session
					return;
				}
				throw new SCServiceException("delete session failed"
						+ reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			}
		} finally {
			this.pendingRequest = false;
			this.sessionId = null;
			this.callback = null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized SCMessage execute(SCMessage requestMsg) throws Exception {
		return this.execute(requestMsg, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
	}

	@Override
	public synchronized SCMessage execute(SCMessage requestMsg, int timeoutInSeconds) throws Exception {
		if (this.sessionDead) {
			throw new SCServiceException("execute not possible, broken session.");
		}
		if (requestMsg == null) {
			throw new InvalidParameterException("Message must be set.");
		}
		if (this.pendingRequest) {
			// already executed before - reply still outstanding
			throw new SCServiceException(
					"execute not possible, there is a pending request - two pending request are not allowed.");
		}
		ValidatorUtility.validateInt(1, timeoutInSeconds, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
		this.pendingRequest = true;
		// cancel session timeout
		this.timerTask.cancel();
		this.msgId.incrementMsgSequenceNr();
		SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(
				this.requester, this.serviceName, this.sessionId);
		String msgInfo = requestMsg.getMessageInfo();
		if (msgInfo != null) {
			// message info optional
			clnExecuteCall.setMessagInfo(msgInfo);
		}
		clnExecuteCall.setCompressed(requestMsg.isCompressed());
		clnExecuteCall.setRequestBody(requestMsg.getData());
		// invoke asynchronous
		this.callback = new ServiceCallback(true);
		try {
			clnExecuteCall.invoke(this.callback, timeoutInSeconds * Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			this.pendingRequest = false;
			throw new SCServiceException("execute failed", e);
		}
		// wait for message in callback
		SCMPMessage reply = this.callback.getMessageSync();
		this.pendingRequest = false;
		if (reply.isFault()) {
			throw new SCServiceException("execute failed" + reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		}
		// trigger session timeout
		this.timerTask = new TimerTaskWrapper(this.timerRun);
		this.timer.schedule(new TimerTaskWrapper(this.timerRun), (long) this.timerRun.getTimeoutMillis());
		SCMessage replyToClient = new SCMessage();
		replyToClient.setData(reply.getBody());
		replyToClient.setCompressed(reply.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION));
		replyToClient.setSessionId(this.sessionId);
		replyToClient.setMessageInfo(reply.getHeader(SCMPHeaderAttributeKey.MSG_INFO));
		return replyToClient;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void execute(SCMessage requestMsg, ISCMessageCallback callback) throws Exception {
		this.execute(requestMsg, callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
	}

	@Override
	public synchronized void execute(SCMessage requestMsg, ISCMessageCallback callback, int timeoutInSeconds)
			throws Exception {
		if (this.sessionDead) {
			throw new SCServiceException("execute not possible, broken session.");
		}
		if (callback == null) {
			throw new InvalidParameterException("Callback must be set.");
		}
		if (requestMsg == null) {
			throw new InvalidParameterException("Message must be set.");
		}
		if (this.pendingRequest) {
			// already executed before - reply still outstanding
			throw new SCServiceException(
					"execute not possible, there is a pending request - two pending request are not allowed.");
		}
		ValidatorUtility.validateInt(1, timeoutInSeconds, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
		this.pendingRequest = true;
		// cancel session timeout
		this.timerTask.cancel();
		this.msgId.incrementMsgSequenceNr();
		SCMPClnExecuteCall clnExecuteCall = (SCMPClnExecuteCall) SCMPCallFactory.CLN_EXECUTE_CALL.newInstance(
				this.requester, this.serviceName, this.sessionId);
		String msgInfo = requestMsg.getMessageInfo();
		if (msgInfo != null) {
			// message info optional
			clnExecuteCall.setMessagInfo(msgInfo);
		}
		clnExecuteCall.setCompressed(requestMsg.isCompressed());
		clnExecuteCall.setRequestBody(requestMsg.getData());
		ISCMPCallback scmpCallback = new ServiceCallback(this, callback);
		try {
			clnExecuteCall.invoke(scmpCallback, timeoutInSeconds * Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			this.pendingRequest = false;
			throw new SCServiceException("execute failed", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setRequestComplete() {
		super.setRequestComplete();
		// trigger session timeout
		this.timerTask = new TimerTaskWrapper(this.timerRun);
		this.timer.schedule(new TimerTaskWrapper(this.timerRun), (long) this.timerRun.getTimeoutMillis());
	}

	/** {@inheritDoc} */
	@Override
	public void setSCResponseTimeMillis(int scResponseTimeMillis) {
		this.scResponseTimeMillis = scResponseTimeMillis;
	}

	/** {@inheritDoc} */
	@Override
	public int getSCResponseTimeMillis() {
		return this.scResponseTimeMillis;
	}

	/**
	 * Echo.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private synchronized void echo() throws Exception {
		if (this.pendingRequest) {
			// an operation is running no echo necessary
			return;
		}
		this.pendingRequest = true;
		this.msgId.incrementMsgSequenceNr();
		SCMPEchoCall clnEchoCall = (SCMPEchoCall) SCMPCallFactory.ECHO_CALL.newInstance(this.requester,
				this.serviceName, this.sessionId);
		this.callback = new ServiceCallback(true);
		try {
			clnEchoCall.invoke(this.callback, this.scResponseTimeMillis);
		} catch (Exception e) {
			this.pendingRequest = false;
			throw new SCServiceException("execute failed", e);
		}
		// wait for message in callback
		SCMPMessage reply = this.callback.getMessageSync();
		this.pendingRequest = false;
		if (reply.isFault()) {
			throw new SCServiceException("echo failed" + reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		}
	}

	/**
	 * The Class SessionTimeouter. Get control at the time a session refresh is needed. Takes care of sending an echo to
	 * SC which gets the session refreshed.
	 */
	private class SessionTimeouter implements ITimerRun {

		/** The timeout in seconds. */
		private int timeoutInSeconds;

		/**
		 * Instantiates a new session timeouter.
		 * 
		 * @param timeoutInSeconds
		 *            the timeout in seconds
		 */
		public SessionTimeouter(int timeoutInSeconds) {
			this.timeoutInSeconds = timeoutInSeconds;
		}

		/**
		 * Time run out, need to send an echo to SC otherwise session gets deleted for session timeout reason.
		 */
		@Override
		public void timeout() {
			if (SCSessionService.this.pendingRequest) {
				// no echo will be sent in state of pending request
				return;
			}
			try {
				// send echo to SC
				SCSessionService.this.echo();
				// trigger session timeout
				SCSessionService.this.timerTask = new TimerTaskWrapper(SCSessionService.this.timerRun);
				SCSessionService.this.timer.schedule(new TimerTaskWrapper(SCSessionService.this.timerRun), (long) this
						.getTimeoutMillis());
			} catch (Exception e) {
				// echo failed - mark session as dead
				SCSessionService.this.sessionDead = true;
				SCSessionService.this.timer.cancel();
			}
		}

		/** {@inheritDoc} */
		@Override
		public int getTimeoutMillis() {
			return this.timeoutInSeconds;
		}
	}
}