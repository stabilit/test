/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.web;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.web.ctx.WebContext;

/**
 * The Class AbstractWebRequest.
 */
public abstract class AbstractWebRequest implements IWebRequest {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebRequest.class);

	/** The attr map. */
	private Map<String, Object> attrMap;

	/** The local address. */
	private InetSocketAddress localAddress;

	/** The remote address. */
	private InetSocketAddress remoteAddress;

	/** The encoded session id. */
	protected String encodedSessionId;

	/**
	 * Instantiates a new abstract web request.
	 *
	 * @param localAddress the local address
	 * @param remoteAddress the remote address
	 */
	public AbstractWebRequest(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
		this.localAddress = localAddress;
		this.remoteAddress = remoteAddress;
		attrMap = new HashMap<String, Object>();
		this.encodedSessionId = null;
	}

	/**
	 * Gets the attribute.
	 *
	 * @param key the key
	 * @return the attribute {@inheritDoc}
	 */
	@Override
	public Object getAttribute(String key) {
		return this.attrMap.get(key);
	}

	/**
	 * Sets the attribute.
	 *
	 * @param key the key
	 * @param value the value {@inheritDoc}
	 */
	@Override
	public void setAttribute(String key, Object value) {
		this.attrMap.put(key, value);

	}

	/** {@inheritDoc} */
	@Override
	public InetSocketAddress getLocalAddress() {
		return this.localAddress;
	}

	/** {@inheritDoc} */
	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * Sets the local address.
	 *
	 * @param localAddress the new local address
	 */
	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}

	/**
	 * Sets the remote address.
	 *
	 * @param remoteAddress the new remote address
	 */
	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return this.getLocalAddress().getHostName();
	}

	/** {@inheritDoc} */
	@Override
	public String getRemoteHost() {
		return this.getRemoteAddress().getHostName();
	}

	/** {@inheritDoc} */
	@Override
	public int getPort() {
		return this.getLocalAddress().getPort();
	}

	/** {@inheritDoc} */
	@Override
	public int getRemotePort() {
		return this.getRemoteAddress().getPort();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebSession getSession(boolean create) {
		if (this.encodedSessionId != null) {
			WebSession webSession = WebContext.getWebSessionRegistry().getSession(encodedSessionId);
			if (webSession != null) {
				// check if web session belongs to same local port, client and user agent
				if (this.isMySession(webSession)) {
					return webSession;
				}
			}
		}
		if (create == true) {
			return WebContext.getWebSessionRegistry().createSession();
		}
		return null;
	}

	/**
	 * Checks if is my session.
	 *
	 * @param webSession the web session
	 * @return true, if is my session
	 */
	private boolean isMySession(WebSession webSession) {
		String userAgent = webSession.getUserAgent();
		String webLocalHost = webSession.getHost();
		String webRemoteHost = webSession.getRemoteHost();
		int webLocalPort = webSession.getPort();
		// int webRemotePort = webSession.getRemotePort();
		if (userAgent == null) {
			return false;
		}
		if (userAgent.equals(this.getHeader("User-Agent")) == false) {
			return false;
		}
		if (webLocalHost == null || webRemoteHost == null) {
			return false;
		}
		if (webLocalHost.equals(this.getHost()) == false) {
			return false;
		}
		if (webRemoteHost.equals(this.getRemoteHost()) == false) {
			return false;
		}
		if (webLocalPort != this.getPort()) {
			return false;
		}
		return true;
	}

}
