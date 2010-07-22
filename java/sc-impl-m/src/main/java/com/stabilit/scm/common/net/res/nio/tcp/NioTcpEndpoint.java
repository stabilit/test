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
package com.stabilit.scm.common.net.res.nio.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.stabilit.scm.common.conf.Constants;
import com.stabilit.scm.common.factory.IFactoryable;
import com.stabilit.scm.common.listener.ExceptionPoint;
import com.stabilit.scm.common.net.res.nio.NioTcpRequestThread;
import com.stabilit.scm.common.res.EndpointAdapter;

/**
 * The Class NioTcpEndpoint. Concrete server implementation with Nio for Tcp.
 * 
 * @author JTraber
 */
public class NioTcpEndpoint extends EndpointAdapter implements Runnable {

	/** The host. */
	private String host;
	/** The port. */
	private int port;
	/** The numberOfThreads. */
	private int numberOfThreads;
	/** The server channel. */
	private ServerSocketChannel serverChannel;
	/** The pool. */
	private ThreadPoolExecutor pool;

	/**
	 * Instantiates a new NioTcpEndpoint.
	 */
	public NioTcpEndpoint() {
		this.host = null;
		this.port = 0;
		this.numberOfThreads = Constants.DEFAULT_NR_OF_THREADS;
		this.serverChannel = null;
		this.pool = null;
	}

	/** {@inheritDoc} */
	@Override
	public void create() {
		try {
			pool = new ThreadPoolExecutor(numberOfThreads, numberOfThreads, 10, TimeUnit.MICROSECONDS,
					new LinkedBlockingQueue<Runnable>());
			// Create a new blocking server socket channel
			this.serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(true);
			// Bind the server socket to the specified address and port
			InetSocketAddress isa = new InetSocketAddress(this.host, this.port);
			serverChannel.socket().bind(isa);
		} catch (IOException e) {
			ExceptionPoint.getInstance().fireException(this, e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		pool.shutdown();
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		while (true) {
			try {
				SocketChannel socketChannel;
				socketChannel = serverChannel.accept();
				pool.execute(new NioTcpRequestThread(socketChannel, this.resp));
			} catch (IOException e) {
				ExceptionPoint.getInstance().fireException(this, e);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void runAsync() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}

	/** {@inheritDoc} */
	@Override
	public void runSync() throws InterruptedException {
		this.run();
	}

	/** {@inheritDoc} */
	@Override
	public void setHost(String host) {
		this.host = host;
	}

	/** {@inheritDoc} */
	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	/** {@inheritDoc} */
	@Override
	public IFactoryable newInstance() {
		return new NioTcpEndpoint();
	}
}
