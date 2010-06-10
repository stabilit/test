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
package com.stabilit.scm.sc;

import java.net.SocketAddress;

import com.stabilit.scm.common.scmp.SCMPMessage;
import com.stabilit.scm.common.util.MapBean;

/**
 * The Class Client. Represents an attached client.
 * 
 * @author JTraber
 */
public class Client extends MapBean<Object> {

	/** The scmp received when client attached on SC. */
	private SCMPMessage scmp;
	/** The socket address of the client, unique identifier. */
	private SocketAddress socketAddress;

	/**
	 * Instantiates a new client.
	 * 
	 * @param socketAddress
	 *            the socket address
	 * @param scmp
	 *            the scmp
	 */
	public Client(SocketAddress socketAddress, SCMPMessage scmp) {
		this.scmp = scmp;
		this.socketAddress = socketAddress;
	}

	public Object getSocketAddress() {
		return socketAddress;
	}

	public SCMPMessage getScmp() {
		return scmp;
	}
}
