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
package com.stabilit.scm.common.net.res.netty.tcp;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.stabilit.scm.common.cmd.IAsyncCommand;
import com.stabilit.scm.common.cmd.ICommand;
import com.stabilit.scm.common.cmd.ICommandValidator;
import com.stabilit.scm.common.cmd.IPassThroughPartMsg;
import com.stabilit.scm.common.cmd.factory.CommandFactory;
import com.stabilit.scm.common.listener.ExceptionPoint;
import com.stabilit.scm.common.listener.LoggerPoint;
import com.stabilit.scm.common.listener.PerformancePoint;
import com.stabilit.scm.common.net.IResponderCallback;
import com.stabilit.scm.common.net.res.ResponderRegistry;
import com.stabilit.scm.common.net.res.SCMPSessionCompositeRegistry;
import com.stabilit.scm.common.net.res.netty.NettyTcpRequest;
import com.stabilit.scm.common.net.res.netty.NettyTcpResponse;
import com.stabilit.scm.common.scmp.HasFaultResponseException;
import com.stabilit.scm.common.scmp.IRequest;
import com.stabilit.scm.common.scmp.IResponse;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.common.scmp.SCMPFault;
import com.stabilit.scm.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.scmp.SCMPMessageId;
import com.stabilit.scm.common.scmp.SCMPMsgType;
import com.stabilit.scm.common.scmp.internal.SCMPCompositeReceiver;
import com.stabilit.scm.common.scmp.internal.SCMPCompositeSender;
import com.stabilit.scm.common.scmp.internal.SCMPPart;

/**
 * The Class NettyTcpResponderRequestHandler. This class is responsible for handling Tcp requests. Is called from the
 * Netty framework by catching events (message received, exception caught). Functionality to handle large messages is
 * also inside.
 * 
 * @author JTraber
 */
@ChannelPipelineCoverage("one")
public class NettyTcpResponderRequestHandler extends SimpleChannelUpstreamHandler implements IResponderCallback {

	private final static SCMPSessionCompositeRegistry compositeRegistry = SCMPSessionCompositeRegistry
			.getCurrentInstance();

	/** {@inheritDoc} */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		NettyTcpResponse response = new NettyTcpResponse(event);
		Channel channel = ctx.getChannel();
		InetSocketAddress localSocketAddress = (InetSocketAddress) channel.getLocalAddress();
		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
		IRequest request = new NettyTcpRequest(event, localSocketAddress, remoteSocketAddress);
		SCMPMessage scmpReq = request.getMessage();
		String sessionId = scmpReq.getSessionId();
		SCMPMessageId messageId = NettyTcpResponderRequestHandler.compositeRegistry.getSCMPMessageId(sessionId);

		if (scmpReq == null) {
			// no scmp protocol used - nothing to return
			return;
		}
		if (scmpReq.isKeepAlive()) {
			scmpReq.setIsReply(true);
			response.setSCMP(scmpReq);
			response.write();
			return;
		}
		try {
			// needs to set a key in thread local to identify thread later and get access to the responder
			ResponderRegistry respRegistry = ResponderRegistry.getCurrentInstance();
			respRegistry.setThreadLocal(channel.getParent().getId());

			request.read();
			// gets the command
			ICommand command = CommandFactory.getCurrentCommandFactory().getCommand(request);
			if (command == null) {
				this.sendUnknownRequestError(response, scmpReq);
				return;
			}

			if ((command instanceof IPassThroughPartMsg) == false) {
				// large messages needs to be handled
				SCMPCompositeSender compositeSender = NettyTcpResponderRequestHandler.compositeRegistry
						.getSCMPCompositeSender(sessionId);

				if (compositeSender != null && scmpReq.isPart()) {
					// sending of a large response has already been started and incoming scmp is a pull request
					if (compositeSender.hasNext()) {
						// there are still parts to send to complete request
						SCMPMessage nextSCMP = compositeSender.getNext();
						response.setSCMP(nextSCMP);
						// handling messageId
						if (SCMPMessageId.necessaryToWrite(nextSCMP.getMessageType())) {
							if (compositeSender.hasNext()) {
								// there are more parts to send - just increment part number
								messageId.incrementPartSequenceNr();
							} else {
								// last part to send - will be a RES message increment message number
								messageId.incrementMsgSequenceNr();
							}
							nextSCMP.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, messageId.getCurrentMessageID());
						}
						response.write();
						return;
					}
					NettyTcpResponderRequestHandler.compositeRegistry.removeSCMPCompositeSender(sessionId);
				}
				// command needs buffered message - buffer message
				SCMPCompositeReceiver compositeReceiver = this.getCompositeReceiver(request, response);

				if (compositeReceiver != null && compositeReceiver.isComplete() == false) {
					// request is not complete yet
					SCMPMessage message = response.getSCMP();
					// handling messageId
					if (SCMPMessageId.necessaryToWrite(message.getMessageType())) {
						messageId.incrementPartSequenceNr();
						message.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, messageId.getCurrentMessageID());
					}
					response.write();
					return;
				}
				// removes compositeReceiver - request is complete don't need to know preceding messages any more
				NettyTcpResponderRequestHandler.compositeRegistry.removeSCMPCompositeReceiver(sessionId);
			}

			// validate request and run command
			ICommandValidator commandValidator = command.getCommandValidator();
			try {
				commandValidator.validate(request);
				if (LoggerPoint.getInstance().isDebug()) {
					LoggerPoint.getInstance().fireDebug(this, "Run command [" + command.getKey() + "]");
				}
				PerformancePoint.getInstance().fireBegin(command, "run");
				if (command.isAsynchronous()) {
					((IAsyncCommand) command).run(request, response, this);
					return;
				}
				command.run(request, response);
				PerformancePoint.getInstance().fireEnd(command, "run");
			} catch (HasFaultResponseException ex) {
				// exception carries response inside
				ExceptionPoint.getInstance().fireException(this, ex);
				ex.setFaultResponse(response);
			}
		} catch (Throwable th) {
			ExceptionPoint.getInstance().fireException(this, th);
			SCMPFault scmpFault = new SCMPFault(SCMPError.SERVER_ERROR);
			scmpFault.setMessageType(SCMPMsgType.UNDEFINED);
			scmpFault.setLocalDateTime();
			response.setSCMP(scmpFault);
		}

		if (response.isLarge()) {
			// response is large, create a large response for reply
			SCMPCompositeSender compositeSender = new SCMPCompositeSender(response.getSCMP());
			SCMPMessage firstSCMP = compositeSender.getFirst();
			response.setSCMP(firstSCMP);
			// handling messageId
			if (SCMPMessageId.necessaryToWrite(firstSCMP.getMessageType())) {
				// override messageId now - because parts need to be sent
				messageId.incrementPartSequenceNr();
				firstSCMP.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, messageId.getCurrentMessageID());
			}
			// adding compositeReceiver to the composite registry
			NettyTcpResponderRequestHandler.compositeRegistry.addSCMPCompositeSender(sessionId, compositeSender);
		}
		response.write();
	}

	/** {@inheritDoc} */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		NettyTcpResponse response = new NettyTcpResponse(e);
		ExceptionPoint.getInstance().fireException(this, e.getCause());
		Throwable th = e.getCause();
		if (th instanceof HasFaultResponseException) {
			((HasFaultResponseException) th).setFaultResponse(response);
			response.write();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void callback(IRequest request, IResponse response) {
		try {
			SCMPMessage scmpRequest = request.getMessage();
			String sessionId = scmpRequest.getSessionId();
			if (response.isLarge()) {
				SCMPMessageId messageId = NettyTcpResponderRequestHandler.compositeRegistry.getSCMPMessageId(sessionId);
				// response is large, create a large response for reply
				SCMPCompositeSender compositeSender = new SCMPCompositeSender(response.getSCMP());
				SCMPMessage firstSCMP = compositeSender.getFirst();
				response.setSCMP(firstSCMP);
				// handling messageId
				if (SCMPMessageId.necessaryToWrite(firstSCMP.getMessageType())) {
					// override messageId now - because parts need to be sent
					messageId.incrementPartSequenceNr();
					firstSCMP.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, messageId.getCurrentMessageID());
				}
				// adding compositeReceiver to the composite registry
				NettyTcpResponderRequestHandler.compositeRegistry.addSCMPCompositeSender(sessionId, compositeSender);
			}
			response.write();
		} catch (Throwable th) {
			this.callback(response, th);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void callback(IResponse response, Throwable th) {
		ExceptionPoint.getInstance().fireException(this, th);
		if (th instanceof HasFaultResponseException) {
			((HasFaultResponseException) th).setFaultResponse(response);
		} else {
			SCMPFault scmpFault = new SCMPFault(SCMPError.SERVER_ERROR);
			scmpFault.setMessageType(SCMPMsgType.UNDEFINED);
			scmpFault.setLocalDateTime();
			response.setSCMP(scmpFault);
		}
		try {
			response.write();
		} catch (Throwable thr) {
		}
	}

	private void sendUnknownRequestError(IResponse response, SCMPMessage scmpReq) throws Exception {
		SCMPFault scmpFault = new SCMPFault(SCMPError.REQUEST_UNKNOWN);
		scmpFault.setMessageType(scmpReq.getMessageType());
		scmpFault.setLocalDateTime();
		response.setSCMP(scmpFault);
		response.write();
	}

	private SCMPCompositeReceiver getCompositeReceiver(IRequest request, IResponse response) throws Exception {
		SCMPMessage scmpReq = request.getMessage();
		String sessionId = scmpReq.getSessionId();
		SCMPCompositeReceiver compositeReceiver = compositeRegistry.getSCMPCompositeReceiver(scmpReq.getSessionId());

		if (compositeReceiver == null) {
			// no compositeReceiver used before
			if (scmpReq.isPart() == false) {
				// request not chunk
				return compositeReceiver;
			}
			// first part of a large request received - introduce composite receiver
			compositeReceiver = new SCMPCompositeReceiver(scmpReq, (SCMPMessage) scmpReq);
			// add compositeReceiver to the registry
			NettyTcpResponderRequestHandler.compositeRegistry.addSCMPCompositeReceiver(sessionId, compositeReceiver);
			// need to increment message number here
			SCMPMessageId messageId = NettyTcpResponderRequestHandler.compositeRegistry.getSCMPMessageId(sessionId);
			messageId.incrementMsgSequenceNr();
		} else {
			// next part of a large request received - add to composite receiver
			compositeReceiver.add(scmpReq);
		}

		if (scmpReq.isPart()) {
			// received message part - request not complete yet
			compositeReceiver.uncomplete();
			// set up pull request
			SCMPMessage scmpReply = new SCMPPart();
			scmpReply.setIsReply(true);
			scmpReply.setMessageType(scmpReq.getMessageType());
			response.setSCMP(scmpReply);
		} else {
			// last message of a chunk message received - request complete now
			compositeReceiver.complete();
			request.setMessage(compositeReceiver);
		}
		return compositeReceiver;
	}
}
