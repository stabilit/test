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
package com.stabilit.scm.sc.service;

import java.util.UUID;

import com.stabilit.scm.common.util.ITimerRun;
import com.stabilit.scm.common.util.MapBean;

/**
 * The Class Session. Provides unique id and an attribute map to store data. A session represents virtual relation
 * between a client and a server.
 */
public class Session extends MapBean<Object>{

	/** The id. */
	private String id;
	private Server server;
	private int echoTimeout;
	private int echoInterval;
	private ITimerRun timerRun;

	/**
	 * Instantiates a new session.
	 */
	public Session() {
		UUID uuid = UUID.randomUUID();
		this.id = uuid.toString();
		this.server = null;
		this.timerRun = null;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Server getServer() {
		return this.server;
	}

	@Override
	public String toString() {
		return id + ":" + server;
	}

	public void setEchoTimeout(int echoTimeout) {
		this.echoTimeout = echoTimeout;
	}

	public void setEchoInterval(int echoInterval) {
		this.echoInterval = echoInterval;
	}

	public int getEchoTimeout() {
		return echoTimeout;
	}

	public int getEchoInterval() {
		return echoInterval;
	}

	public ITimerRun getTimerRun() {
		return this.timerRun;
	}
	
	public void setTimerRun(ITimerRun timerRun) {
		this.timerRun = timerRun;
	}
	
}
