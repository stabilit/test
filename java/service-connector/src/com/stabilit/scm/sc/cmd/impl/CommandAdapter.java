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
package com.stabilit.scm.sc.cmd.impl;

import java.net.SocketAddress;

import com.stabilit.scm.common.cmd.ICommand;
import com.stabilit.scm.common.cmd.ICommandValidator;
import com.stabilit.scm.common.cmd.NullCommandValidator;
import com.stabilit.scm.common.cmd.SCMPCommandException;
import com.stabilit.scm.common.factory.IFactoryable;
import com.stabilit.scm.common.listener.LoggerPoint;
import com.stabilit.scm.common.scmp.SCMPError;
import com.stabilit.scm.sc.registry.ClientRegistry;
import com.stabilit.scm.sc.registry.ServiceRegistry;
import com.stabilit.scm.sc.registry.SessionRegistry;
import com.stabilit.scm.sc.service.Client;
import com.stabilit.scm.sc.service.Service;
import com.stabilit.scm.sc.service.Session;

/**
 * The Class CommandAdapter.
 * 
 * @author JTraber
 */
public abstract class CommandAdapter implements ICommand {

	/** The command validator. */
	protected ICommandValidator commandValidator;

	/**
	 * Instantiates a new command adapter.
	 */
	public CommandAdapter() {
		this.commandValidator = NullCommandValidator.newInstance(); // www.refactoring.com Introduce NULL Object
	}

	/**
	 * Gets the session by id. Checks properness of session, if session is null given session id is wrong - no session
	 * found.
	 * 
	 * @param sessionId
	 *            the session id
	 * @return the session by id
	 * @throws SCMPCommandException
	 *             occurs when session is not in registry, invalid session id
	 */
	protected Session getSessionById(String sessionId) throws SCMPCommandException {
		SessionRegistry sessionRegistry = SessionRegistry.getCurrentInstance();
		Session session = sessionRegistry.getSession(sessionId);

		if (session == null) {
			// incoming session not found
			if (LoggerPoint.getInstance().isWarn()) {
				LoggerPoint.getInstance().fireWarn(this, "command error: no session found for id :" + sessionId);
			}
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.NO_SESSION_FOUND);
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
		return session;
	}

	/**
	 * Validate service. Lookup service in service registry and verify service existence.
	 * 
	 * @param serviceName
	 *            the service name
	 * @return the service
	 * @throws SCMPCommandException
	 *             the SCMP command exception
	 */
	protected Service validateService(String serviceName) throws SCMPCommandException {
		ServiceRegistry serviceRegistry = ServiceRegistry.getCurrentInstance();

		Service service = serviceRegistry.getService(serviceName);
		if (service == null) {
			// no service known with incoming serviceName
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.UNKNOWN_SERVICE);
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
		return service;
	}

	/**
	 * Validate client attached. Lookup client in client registry and verify that it has been attached correctly.
	 * 
	 * @param socketAddress
	 *            the socket address
	 * @throws SCMPCommandException
	 *             the SCMP command exception
	 */
	protected void validateClientAttached(SocketAddress socketAddress) throws SCMPCommandException {
		ClientRegistry clientRegistry = ClientRegistry.getCurrentInstance();
		Client client = clientRegistry.getClient(socketAddress);

		if (client == null) {
			if (LoggerPoint.getInstance().isWarn()) {
				LoggerPoint.getInstance().fireWarn(this,
						"command error: unknown client - client might not be properly attached");
			}
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.UNKNOWN_CLIENT);
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
	}

	/** {@inheritDoc} */
	@Override
	public ICommandValidator getCommandValidator() {
		return commandValidator;
	}
	
	/** {@inheritDoc} */
	@Override
	public IFactoryable newInstance() {
		return this;
	}
	
	@Override
	public boolean isAsynchronous() {
		return false;
	}
}
