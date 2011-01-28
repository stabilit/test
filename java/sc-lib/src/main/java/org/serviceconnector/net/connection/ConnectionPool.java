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
package org.serviceconnector.net.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPKeepAlive;
import org.serviceconnector.util.SynchronousCallback;

/**
 * The Class ConnectionPool. Concrete implementation of connection pooling.<br>
 * <br>
 * This connection pool takes care of following listed points:<br>
 * - creating / destroying of connections<br>
 * - observing the max numbers of connections<br>
 * - keeping a minimum of connections active<br>
 * - disconnect connection after Constants.DEFAULT_NR_OF_KEEP_ALIVES_TO_CLOSE <br>
 * - destroying connection pool, destroys all connections <br>
 * <br>
 * optional functions:<br>
 * - closing connection after getting it back<br>
 * - initializing pool by starting a minimum of connections immediately<br>
 * - observing connection idle timeout and sending keep alive messages to refresh firewall<br>
 * - force closing of a specific connection, very useful if connection has a curious state
 * 
 * @author JTraber
 */
public class ConnectionPool {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ConnectionPool.class);

	/** The port. */
	private int port;
	/** The host. */
	private String host;
	/** The connection type. */
	private String connectionType;
	/** The maximum connections. */
	private int maxConnections;
	/** The minimum connections. */
	private int minConnections;
	/** The close on free. */
	private boolean closeOnFree;
	/** The keep alive interval. */
	private int keepAliveIntervalSeconds;
	/** The free connections. */
	private List<IConnection> freeConnections;
	/** The used connections. */
	private List<IConnection> usedConnections;
	/** The connection factory. */
	private ConnectionFactory connectionFactory;

	/**
	 * Instantiates a new connection pool.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param conType
	 *            the connection type
	 * @param keepAliveIntervalSeconds
	 *            the keep alive interval
	 */
	public ConnectionPool(String host, int port, String conType, int keepAliveIntervalSeconds) {
		this.host = host;
		this.port = port;
		this.connectionType = conType;
		// default = false connection will not be closed at the time they are freed
		this.closeOnFree = false;
		this.maxConnections = Constants.DEFAULT_MAX_CONNECTION_POOL_SIZE;
		// the minimum of connection is 1 per default - means there is always one connection active
		this.minConnections = 1;
		this.freeConnections = Collections.synchronizedList(new ArrayList<IConnection>());
		this.usedConnections = Collections.synchronizedList(new ArrayList<IConnection>());
		this.connectionFactory = AppContext.getConnectionFactory();
		this.keepAliveIntervalSeconds = keepAliveIntervalSeconds;
	}

	/**
	 * Instantiates a new connection pool.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param conType
	 *            the connection type
	 */
	public ConnectionPool(String host, int port, String conType) {
		this(host, port, conType, Constants.DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS);
	}

	/**
	 * Instantiates a new connection pool.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @param keepAliveInterval
	 *            the keep alive interval
	 */
	public ConnectionPool(String host, int port, int keepAliveInterval) {
		this(host, port, ConnectionType.DEFAULT_CLIENT_CONNECTION_TYPE.getValue(), keepAliveInterval);
	}

	/**
	 * Instantiates a new connection pool.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	public ConnectionPool(String host, int port) {
		this(host, port, ConnectionType.DEFAULT_CLIENT_CONNECTION_TYPE.getValue(), Constants.DEFAULT_KEEP_ALIVE_INTERVAL_SECONDS);
	}

	/**
	 * Gets a connection of the pool.
	 * 
	 * @return the connection
	 * @throws Exception
	 *             the exception
	 */
	public synchronized IConnection getConnection() throws Exception {
		IConnection connection = null;

		if (freeConnections.size() > 0) {
			int freeConNumber = freeConnections.size();
			for (int index = 0; index < freeConNumber; index++) {
				// loop through free connections
				connection = freeConnections.remove(0);
				if (connection.isConnected()) {
					// found an active connection quit loop
					break;
				} else {
					// null connection is a dead one
					connection.destroy();
					connection = null;
				}
			}
		}

		if (connection == null) {
			// no free connection available - try to create a new one
			connection = this.createNewConnection();
		}
		this.usedConnections.add(connection);
		return connection;
	}

	/**
	 * Creates the new connection.
	 * 
	 * @return the i connection
	 * @throws Exception
	 *             the exception
	 */
	private synchronized IConnection createNewConnection() throws Exception {
		IConnection connection = null;
		if (usedConnections.size() + freeConnections.size() >= maxConnections) {
			// we can't create a new one - limit reached
			throw new ConnectionPoolBusyException("Unable to create new connection - limit of : " + maxConnections + " reached!");
		}
		// we create a new one
		connection = connectionFactory.createConnection(this.connectionType);
		connection.setHost(this.host);
		connection.setPort(this.port);
		connection.setIdleTimeoutSeconds(this.keepAliveIntervalSeconds);
		IIdleConnectionCallback idleCallback = new IdleCallback();
		ConnectionContext connectionContext = new ConnectionContext(connection, idleCallback, this.keepAliveIntervalSeconds);
		connection.setContext(connectionContext);
		try {
			connection.connect(); // can throw an exception
		} catch (Exception ex) {
			logger.debug("Unable to establish new connection.", ex);
			throw new ConnectionPoolConnectException("Unable to establish new connection.", ex);
		}
		return connection;
	}

	/**
	 * Free connection. Gives connection back for other interested parties.
	 * 
	 * @param connection
	 *            the connection
	 * @throws Exception
	 *             the exception
	 */
	public synchronized void freeConnection(IConnection connection) {
		if (this.usedConnections.remove(connection) == false) {
			logger.warn("connection does not exist in pool - not possible to free");
			return;
		}
		if (closeOnFree && (this.freeConnections.size() + this.usedConnections.size() >= this.minConnections)) {
			// do not add the connection to free pool array - just close it immediately! Keep minimum connections alive
			// don't forget current connection is not included in above size calculation "=>"
			this.disconnectConnection(connection);
			return;
		}
		connection.resetNrOfIdles();
		// insert used connection at first position of list
		this.freeConnections.add(0, connection);
	}

	/**
	 * Sets the max connections for the pool.
	 * 
	 * @param maxConnections
	 *            the new max connections
	 */
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	/**
	 * Destroy the pool.
	 */
	public synchronized void destroy() {
		this.destroyConnections(this.usedConnections);
		this.destroyConnections(this.freeConnections);
	}

	/**
	 * Destroy connections.
	 * 
	 * @param connections
	 *            the connections
	 */
	private void destroyConnections(List<IConnection> connections) {
		IConnection connection;
		int size = connections.size();
		for (int index = 0; index < size; index++) {
			connection = connections.remove(0);
			this.destroyConnection(connection);
		}
	}

	/**
	 * Destroy connection. Careful in use - to be called only if pool gets destroyed. Destroying a single connection may affect
	 * others because of shared stuff (timer) etc.
	 * 
	 * @param connection
	 *            the connection
	 */
	private void destroyConnection(IConnection connection) {
		try {
			connection.disconnect();
		} catch (Exception ex) {
			logger.error("destroy disconnect", ex);
		} finally {
			connection.destroy();
		}
	}

	/**
	 * Disconnect connection.
	 * 
	 * @param connection
	 *            the connection
	 */
	private void disconnectConnection(IConnection connection) {
		try {
			connection.disconnect();
		} catch (Exception ex) {
			logger.error("disconnect", ex);
		}
	}

	/**
	 * Force closing specific connection.
	 * 
	 * @param connection
	 *            the connection
	 * @throws Exception
	 *             the exception
	 */
	public synchronized void forceClosingConnection(IConnection connection) {
		// make sure connection is not registered
		this.usedConnections.remove(connection);
		this.freeConnections.remove(connection);

		try {
			connection.disconnect();
		} catch (Exception ex) {
			logger.error("force disconnect", ex);
		}
	}

	/**
	 * Sets the close on free. Indicates that connection should be closed at the time they get freed.
	 * 
	 * @param closeOnFree
	 *            the new close on free
	 */
	public void setCloseOnFree(boolean closeOnFree) {
		this.closeOnFree = closeOnFree;
	}

	/**
	 * Sets the minimum connections for the pool.
	 * 
	 * @param minConnections
	 *            the new minimum connections
	 */
	public void setMinConnections(int minConnections) {
		this.minConnections = minConnections;
	}

	/**
	 * Initiates the minimum connections. The minimum of connections gets active immediately.
	 */
	public synchronized void initMinConnections() {
		IConnection connection = null;
		int con = usedConnections.size() + freeConnections.size();
		for (int countCon = con; countCon < this.minConnections; countCon++) {
			try {
				connection = this.createNewConnection();
				if (connection == null) {
					// connection null at the time maxConnections is reached - stop creating
					return;
				}
			} catch (Exception ex) {
				logger.error("create connection", ex);
				return;
			}
			this.freeConnections.add(connection);
		}
	}

	/**
	 * Gets the max connections.
	 * 
	 * @return the max connections
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * Gets the min connections.
	 * 
	 * @return the min connections
	 */
	public int getMinConnections() {
		return minConnections;
	}

	/**
	 * Gets the number of busy connections at this time.
	 * 
	 * @return the busy connections
	 */
	public int getBusyConnections() {
		return this.usedConnections.size();
	}

	/**
	 * Checks for free connections in the pool.
	 * 
	 * @return true, if successful
	 */
	public synchronized boolean hasFreeConnections() {
		if (this.freeConnections.size() > 0) {
			// we have free connections left
			return true;
		}
		if (this.usedConnections.size() < maxConnections) {
			// we can create new connections if necessary
			return true;
		}
		return false;
	}

	/**
	 * Connection idle. Process idle event of connection.
	 * 
	 * @param connection
	 *            the connection
	 * @throws Exception
	 *             the exception
	 */
	public synchronized void connectionIdle(IConnection connection) {
		if (this.freeConnections.remove(connection) == false) {
			// this connection is no more free - no keep alive necessary
			return;
		}
		if (connection.getNrOfIdlesInSequence() > Constants.DEFAULT_NR_OF_KEEP_ALIVES_TO_CLOSE) {
			// connection has been idle for the DEFAULT_NR_OF_KEEP_ALIVES_TO_CLOSE times
			if ((this.freeConnections.size() + this.usedConnections.size()) > this.minConnections) {
				// there are still enough (totalCons > minConnections) free - disconnect this one
				this.disconnectConnection(connection);
				return;
			}
		}
		// send a keep alive message
		SCMPKeepAlive keepAliveMessage = new SCMPKeepAlive();
		try {
			ConnectionPoolCallback callback = new ConnectionPoolCallback(true);
			connection.send(keepAliveMessage, callback);
			callback.getMessageSync(AppContext.getBasicConfiguration().getKeepAliveOTIMillis());
			connection.incrementNrOfIdles();
			this.freeConnections.add(connection);
		} catch (Exception ex) {
			logger.error("send keepalive failed - connection gets destroyed", ex);
			this.forceClosingConnection(connection);
		}
	}

	/**
	 * Gets the keep alive interval the pool is observing. 0 means disabled.
	 * 
	 * @return the keep alive interval
	 */
	public int getKeepAliveInterval() {
		return this.keepAliveIntervalSeconds;
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return this.host;
	}

	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Gets the free connections.
	 * 
	 * @return the free connections
	 */
	public List<IConnection> getFreeConnections() {
		return Collections.unmodifiableList(freeConnections);
	}

	/**
	 * Gets the used connections.
	 * 
	 * @return the used connections
	 */
	public List<IConnection> getUsedConnections() {
		return Collections.unmodifiableList(usedConnections);
	}

	/**
	 * The Class IdleCallback. Gets informed when connection runs into an idle timeout.
	 */
	private class IdleCallback implements IIdleConnectionCallback {

		/** {@inheritDoc} */
		@Override
		public void connectionIdle(IConnection connection) {
			ConnectionPool.this.connectionIdle(connection);
		}
	}

	/**
	 * The Class ConnectionPoolCallback.
	 */
	private class ConnectionPoolCallback extends SynchronousCallback {

		public ConnectionPoolCallback(boolean synchronous) {
			this.synchronous = synchronous;
		}
		// nothing to implement in this case everything is done in super-class
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		this.destroy();
	}
}
