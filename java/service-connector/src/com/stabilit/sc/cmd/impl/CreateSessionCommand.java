package com.stabilit.sc.cmd.impl;

import java.net.SocketAddress;

import com.stabilit.sc.cmd.CommandAdapter;
import com.stabilit.sc.cmd.CommandException;
import com.stabilit.sc.cmd.ICommandValidator;
import com.stabilit.sc.cmd.SCMPValidatorException;
import com.stabilit.sc.ctx.IRequestContext;
import com.stabilit.sc.factory.IFactoryable;
import com.stabilit.sc.io.IRequest;
import com.stabilit.sc.io.IResponse;
import com.stabilit.sc.io.IpAddressList;
import com.stabilit.sc.io.SCMP;
import com.stabilit.sc.io.SCMPHeaderType;
import com.stabilit.sc.io.SCMPMsgType;
import com.stabilit.sc.io.SCMPReply;
import com.stabilit.sc.io.Session;
import com.stabilit.sc.msg.impl.CreateSessionMessage;
import com.stabilit.sc.registry.ServiceRegistry;
import com.stabilit.sc.registry.ServiceRegistryItem;
import com.stabilit.sc.registry.SessionRegistry;
import com.stabilit.sc.util.ValidatorUtility;

public class CreateSessionCommand extends CommandAdapter {

	public CreateSessionCommand() {
		this.commandValidator = new CreateSessionCommandValidator();
	}

	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.REQ_CREATE_SESSION;
	}

	@Override
	public ICommandValidator getCommandValidator() {
		return super.getCommandValidator();
	}

	@Override
	public void run(IRequest request, IResponse response) throws CommandException {
		IRequestContext requestContext = request.getContext();
		SocketAddress socketAddress = requestContext.getSocketAddress();
		try {
			// get free service
			SCMP scmp = request.getSCMP();
			String serviceName = scmp.getHeader(SCMPHeaderType.SERVICE_NAME.getName());
			ServiceRegistry serviceRegistry = ServiceRegistry.getCurrentInstance();
			ServiceRegistryItem serviceRegistryItem = serviceRegistry.allocate(serviceName);
			if (serviceRegistryItem == null) {
				// throw
			}
			SessionRegistry sessionRegistry = SessionRegistry.getCurrentInstance();
			// create session
			Session session = new Session();
			session.setAttribute(ServiceRegistryItem.class.getName(), serviceRegistryItem);
			sessionRegistry.put(session.getId(), session);
			SCMPReply scmpReply = new SCMPReply();
			scmpReply.setMessageType(SCMPMsgType.REQ_CREATE_SESSION.getResponseName());
			scmpReply.setSessionId(session.getId());
			response.setSCMP(scmpReply);
		} catch (Exception e) {
		}
	}

	@Override
	public IFactoryable newInstance() {
		return this;
	}

	public class CreateSessionCommandValidator implements ICommandValidator {

		@Override
		public void validate(IRequest request, IResponse response) throws SCMPValidatorException {
			SCMP scmp = request.getSCMP();

			try {
				// TODO msg in body??
				CreateSessionMessage msg = (CreateSessionMessage) scmp.getBody();

				// ipAddressList
				String ipAddressListString = (String) msg.getAttribute(SCMPHeaderType.IP_ADDRESS_LIST
						.getName());
				IpAddressList ipAddressList = ValidatorUtility.validateIpAddressList(ipAddressListString);
				request.setAttribute(SCMPHeaderType.IP_ADDRESS_LIST.getName(), ipAddressList);

			} catch (Throwable e) {
				SCMPValidatorException validatorException = new SCMPValidatorException();
				validatorException.setMessageType(SCMPMsgType.REQ_CREATE_SESSION.getResponseName());
				throw validatorException;
			}
		}
	}

}
