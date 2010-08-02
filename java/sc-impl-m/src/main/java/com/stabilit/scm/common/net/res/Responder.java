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
package com.stabilit.scm.common.net.res;

import com.stabilit.scm.common.conf.ICommunicatorConfig;
import com.stabilit.scm.common.res.IEndpoint;
import com.stabilit.scm.common.res.IResponder;

/**
 * The Class Responder. Abstracts responder functionality from a application view. It is not the technical
 * representation of a responder connection.
 * 
 * @author JTraber
 */
public class Responder implements IResponder {

	/** The responder configuration. */
	private ICommunicatorConfig respConfig;
	/** The endpoint connection. */
	private IEndpoint endpoint;

	public Responder() {
	}

	public Responder(ICommunicatorConfig respConfig) {
		this.respConfig = respConfig;
	}

	/** {@inheritDoc} */
	@Override
	public void create() throws Exception {
		EndpointFactory endpointFactory = EndpointFactory.getCurrentInstance();
		this.endpoint = endpointFactory.newInstance(this.respConfig.getConnectionType());
		this.endpoint.setResponder(this);
		this.endpoint.setHost(this.respConfig.getHost());
		this.endpoint.setPort(this.respConfig.getPort());
		this.endpoint.create();
	}

	/** {@inheritDoc} */
	@Override
	public void runAsync() throws Exception {
		endpoint.runAsync();
	}

	/** {@inheritDoc} */
	@Override
	public void runSync() throws Exception {
		endpoint.runSync();
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		endpoint.destroy();
	}

	@Override
	public ICommunicatorConfig getResponderConfig() {
		return this.respConfig;
	}

	@Override
	public void setResponderConfig(ICommunicatorConfig respConfig) {
		this.respConfig = respConfig;
	}
}
