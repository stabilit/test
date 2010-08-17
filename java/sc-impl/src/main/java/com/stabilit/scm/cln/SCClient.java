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

import com.stabilit.scm.cln.service.IFileService;
import com.stabilit.scm.cln.service.IPublishService;
import com.stabilit.scm.cln.service.ISCClient;
import com.stabilit.scm.cln.service.ISessionService;
import com.stabilit.scm.common.call.SCMPAttachCall;
import com.stabilit.scm.common.call.SCMPCallFactory;
import com.stabilit.scm.common.call.SCMPDetachCall;
import com.stabilit.scm.common.conf.Constants;
import com.stabilit.scm.common.net.req.ConnectionFactory;
import com.stabilit.scm.common.net.req.ConnectionPool;
import com.stabilit.scm.common.net.req.IConnectionPool;
import com.stabilit.scm.common.net.req.IRequester;
import com.stabilit.scm.common.net.req.Requester;
import com.stabilit.scm.common.net.req.RequesterContext;
import com.stabilit.scm.common.service.ISCContext;
import com.stabilit.scm.common.service.SCServiceException;
import com.stabilit.scm.common.util.SynchronousCallback;

/**
 * The Class SCClient. Client to an SC.
 * 
 * @author JTraber
 */
public class SCClient implements ISCClient {

	/** The host of the SC. */
	private String host;
	/** The port of the SC. */
	private int port;
	/** The keep alive interval. */
	private int keepAliveInterval;
	/** The connection pool. */
	private IConnectionPool connectionPool;
	/** Identifies low level component to use for communication default for clients is "netty.http". */
	private String conType;
	/** The requester. */
	protected IRequester requester;
	/** The context. */
	private ServiceConnectorContext context;
	/** The callback. */
	private SCClientCallback callback;

	/**
	 * Instantiates a new service connector.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	public SCClient(String host, int port) {
		this(host, port, Constants.DEFAULT_CLIENT_CON, Constants.DEFAULT_KEEP_ALIVE_INTERVAL);
	}

	/**
	 * Instantiates a new service connector.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param connectionType
	 *            the connection type
	 */
	public SCClient(String host, int port, String connectionType) {
		this(host, port, connectionType, Constants.DEFAULT_KEEP_ALIVE_INTERVAL);
	}

	/**
	 * Instantiates a new service connector.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param connectionType
	 *            the connection type
	 * @param keepAliveInterval
	 *            the keep alive interval
	 */
	public SCClient(String host, int port, String connectionType, int keepAliveInterval) {
		this.host = host;
		this.port = port;
		this.conType = connectionType;
		this.keepAliveInterval = keepAliveInterval;
		this.connectionPool = new ConnectionPool(this.host, this.port, this.conType, keepAliveInterval);
		this.context = new ServiceConnectorContext();
		this.callback = null;
	}

	/** {@inheritDoc} */
	@Override
	public ISCContext getContext() {
		return context;
	}

	/** {@inheritDoc} */
	@Override
	public void attach() throws Exception {
		if (this.callback != null) {
			throw new SCServiceException(
					"already attached before - detach first, attaching in sequence is not allowed.");
		}
		this.requester = new Requester(new RequesterContext(this.context.getConnectionPool(), null));
		SCMPAttachCall attachCall = (SCMPAttachCall) SCMPCallFactory.ATTACH_CALL.newInstance(this.requester);
		this.callback = new SCClientCallback();
		attachCall.invoke(this.callback);
		this.callback.getMessageSync();
	}

	/** {@inheritDoc} */
	@Override
	public void detach() throws Exception {
		if (this.callback == null) {
			throw new SCServiceException("detach not possible - client not attached.");
		}
		SCMPDetachCall detachCall = (SCMPDetachCall) SCMPCallFactory.DETACH_CALL.newInstance(this.requester);
		detachCall.invoke(this.callback);
		this.callback.getMessageSync();
		this.callback = null;
		// destroy connection pool
		this.connectionPool.destroy();
		// destroy connection resource
		ConnectionFactory.shutdownConnectionFactory();
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionType() {
		return conType;
	}

	/**
	 * Sets the connection type. Should only be used if you really need to change low level technology careful.
	 * 
	 * @param conType
	 *            the new connection type, identifies low level communication technology
	 */
	public void setConnectionType(String conType) {
		this.conType = conType;
	}

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return host;
	}

	/** {@inheritDoc} */
	@Override
	public int getPort() {
		return port;
	}

	/** {@inheritDoc} */
	@Override
	public int getKeepAliveInterval() {
		return this.keepAliveInterval;
	}

	/** {@inheritDoc} */
	@Override
	public IFileService newFileService(String serviceName) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public ISessionService newSessionService(String serviceName) {
		return new SessionService(serviceName, this.context);
	}

	/** {@inheritDoc} */
	@Override
	public IPublishService newPublishService(String serviceName) {
		return new PublishService(serviceName, this.context);
	}

	/** {@inheritDoc} */
	@Override
	public void setMaxConnections(int maxConnections) {
		this.connectionPool.setMaxConnections(maxConnections);
	}
	
	/** {@inheritDoc} */
	@Override
	public int getMaxConnections() {
		return this.connectionPool.getMaxConnections();
	}

	/**
	 * The Class ServiceConnectorContext.
	 */
	class ServiceConnectorContext implements ISCContext {

		/** {@inheritDoc} */
		@Override
		public IConnectionPool getConnectionPool() {
			return connectionPool;
		}

		/** {@inheritDoc} */
		@Override
		public ISCClient getServiceConnector() {
			return SCClient.this;
		}
	}

	/**
	 * The Class SCClientCallback.
	 */
	private class SCClientCallback extends SynchronousCallback {
		// nothing to implement in this case - everything is done by super-class
	}
}
