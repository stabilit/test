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
package com.stabilit.scm.cln;

import java.security.InvalidParameterException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.stabilit.scm.cln.service.ISessionService;
import com.stabilit.scm.cln.service.Service;
import com.stabilit.scm.common.call.SCMPCallFactory;
import com.stabilit.scm.common.call.SCMPClnCreateSessionCall;
import com.stabilit.scm.common.call.SCMPClnDeleteSessionCall;
import com.stabilit.scm.common.call.SCMPClnEchoCall;
import com.stabilit.scm.common.call.SCMPClnExecuteCall;
import com.stabilit.scm.common.conf.Constants;
import com.stabilit.scm.common.net.req.Requester;
import com.stabilit.scm.common.net.req.RequesterContext;
import com.stabilit.scm.common.scmp.ISCMPCallback;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.service.ISCContext;
import com.stabilit.scm.common.service.ISCMessage;
import com.stabilit.scm.common.service.ISCMessageCallback;
import com.stabilit.scm.common.service.SCMessage;
import com.stabilit.scm.common.service.SCServiceException;
import com.stabilit.scm.common.util.ITimerRun;
import com.stabilit.scm.common.util.TimerTaskWrapper;
import com.stabilit.scm.common.util.ValidatorUtility;

/**
 * The Class SessionService. SessionService is a remote interface in client API to a session service and provides
 * communication functions.
 * 
 * @author JTraber
 */
public class SessionService extends Service implements ISessionService {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SessionService.class);
	/** The timer, which observes the session timeout of service. */
	private Timer timer;
	/** The timer run, runs when session need to be refreshed on SC. */
	private ITimerRun timerRun;
	private TimerTask timerTask;
	/** The session dead, marks state of a session. */
	private volatile boolean sessionDead;

	/**
	 * Instantiates a new session service.
	 * 
	 * @param serviceName
	 *            the service name
	 * @param context
	 *            the context
	 */
	public SessionService(String serviceName, ISCContext context) {
		super(serviceName, context);
		this.requester = new Requester(new RequesterContext(context.getConnectionPool(), this.msgId));
		this.serviceContext = new ServiceContext(context, this);
		this.timerRun = null;
		this.timer = new Timer("SessionServiceTimeout");
		this.sessionDead = false;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int timeoutInSeconds, int echoIntervalInSeconds,
			Object data) throws Exception {
		if (this.callback != null) {
			throw new SCServiceException("session already created - delete session first.");
		}
		ValidatorUtility.validateStringLength(1, sessionInfo, 256, SCMPError.HV_WRONG_SESSION_INFO);
		ValidatorUtility.validateInt(0, timeoutInSeconds, 3600, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
		ValidatorUtility.validateInt(0, echoIntervalInSeconds, 3600, SCMPError.HV_WRONG_ECHO_INTERVAL);
		this.msgId.reset();
		this.callback = new ServiceCallback(true);
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(this.requester, this.serviceName);
		createSessionCall.setSessionInfo(sessionInfo);
		createSessionCall.setEchoIntervalSeconds(echoIntervalInSeconds);
		createSessionCall.setRequestBody(data);
		try {
			createSessionCall.invoke(this.callback, timeoutInSeconds);
		} catch (Exception e) {
			this.callback = null;
			throw new SCServiceException("create session failed", e);
		}
		SCMPMessage reply = this.callback.getMessageSync();
		if (reply.isFault()) {
			this.callback = null;
			SCServiceException ex = new SCServiceException("create session failed"
					+ reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			ex.setAppErrorCode(reply.getHeader(SCMPHeaderAttributeKey.APP_ERROR_CODE));
			ex.setAppErrorText(reply.getHeader(SCMPHeaderAttributeKey.APP_ERROR_TEXT));
			throw ex;
		}
		this.sessionId = reply.getSessionId();
		// trigger session timeout
		this.timerRun = new SessionTimeouter((int) (echoIntervalInSeconds * 0.75));
		this.timerTask = new TimerTaskWrapper(this.timerRun);
		this.timer.schedule(timerTask, (int) (echoIntervalInSeconds * 0.75 * Constants.SEC_TO_MILISEC_FACTOR));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void createSession(String sessionInfo, int timeoutInSeconds, int echoIntervalInSeconds)
			throws Exception {
		this.createSession(sessionInfo, timeoutInSeconds, echoIntervalInSeconds, null);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void deleteSession() throws Exception {
		if (this.callback == null) {
			// delete session not possible - no session on this service just ignore
			return;
		}
		if (this.pendingRequest) {
			// already executed before - reply still outstanding
			throw new SCServiceException(
					"execute not possible, there is a pending request - two pending request are not allowed.");
		}
		this.pendingRequest = true;
		// cancel session timeout
		this.timerTask.cancel();
		this.callback = new ServiceCallback(true);
		try {
			this.msgId.incrementMsgSequenceNr();
			SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
					.newInstance(this.requester, this.serviceName, this.sessionId);
			try {
				deleteSessionCall.invoke(this.callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
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
	public synchronized SCMessage execute(ISCMessage requestMsg) throws Exception {
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
			clnExecuteCall.invoke(this.callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
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
		this.timer.schedule(new TimerTaskWrapper(this.timerRun), this.timerRun.getTimeoutSeconds()
				* Constants.SEC_TO_MILISEC_FACTOR);
		SCMessage replyToClient = new SCMessage();
		replyToClient.setData(reply.getBody());
		replyToClient.setCompressed(reply.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION));
		replyToClient.setSessionId(this.sessionId);
		replyToClient.setMessageInfo(reply.getHeader(SCMPHeaderAttributeKey.MSG_INFO));
		return replyToClient;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void execute(ISCMessage requestMsg, ISCMessageCallback messageCallback) throws Exception {
		if (this.sessionDead) {
			throw new SCServiceException("execute not possible, broken session.");
		}
		if (messageCallback == null) {
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
		ISCMPCallback scmpCallback = new ServiceCallback(this, messageCallback);
		try {
			clnExecuteCall.invoke(scmpCallback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
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
		this.timer.schedule(new TimerTaskWrapper(this.timerRun), this.timerRun.getTimeoutSeconds()
				* Constants.SEC_TO_MILISEC_FACTOR);

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
		SCMPClnEchoCall clnEchoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(this.requester,
				this.serviceName, this.sessionId);
		this.callback = new ServiceCallback(true);
		try {
			clnEchoCall.invoke(this.callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS);
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
			if (SessionService.this.pendingRequest) {
				// no echo will be sent in state of pending request
				return;
			}
			try {
				// send echo to SC
				SessionService.this.echo();
				// trigger session timeout
				SessionService.this.timerTask = new TimerTaskWrapper(SessionService.this.timerRun);
				SessionService.this.timer.schedule(new TimerTaskWrapper(SessionService.this.timerRun), this
						.getTimeoutSeconds()
						* Constants.SEC_TO_MILISEC_FACTOR);
			} catch (Exception e) {
				// echo failed - mark session as dead
				SessionService.this.sessionDead = true;
				SessionService.this.timer.cancel();
			}
		}

		/** {@inheritDoc} */
		@Override
		public int getTimeoutSeconds() {
			return this.timeoutInSeconds;
		}
	}
}