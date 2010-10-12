/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package org.serviceconnector.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The Class Service.
 * 
 * @author JTraber
 */
public abstract class Service {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(Service.class);

	/** The type. */
	private ServiceType type;
	/** The state. */
	private ServiceState state;
	/** The name. */
	private String name;
	/** The location. */
	private String location;
	/** The server index. */
	protected int serverIndex;
	/** The list of servers. */
	protected List<Server> listOfServers;

	/**
	 * Instantiates a new service.
	 * 
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 */
	public Service(String name, ServiceType type) {
		this.name = name;
		this.location = null;
		this.serverIndex = 0;
		this.type = type;
		// synchronize the sever list
		this.listOfServers = Collections.synchronizedList(new ArrayList<Server>());
	}

	/**
	 * Gets the service name.
	 * 
	 * @return the service name
	 */
	public String getServiceName() {
		return name;
	}

	/**
	 * Adds the server.
	 * 
	 * @param server
	 *            the server
	 */
	public void addServer(Server server) {
		this.listOfServers.add(server);
	}

	/**
	 * Removes the server.
	 * 
	 * @param server
	 *            the server
	 */
	public void removeServer(Server server) {
		this.listOfServers.remove(server);
	}

	/**
	 * Gets the server list.
	 *
	 * @return the server list
	 */
	public List<Server> getServerList() {
		return Collections.unmodifiableList(this.listOfServers);
	}
	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 * 
	 * @param location
	 *            the new location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public ServiceType getType() {
		return type;
	}

	/**
	 * Gets the current service state
	 * 
	 * @return
	 */
	public ServiceState getState() {
		return state;
	}

	/**
	 * @param state
	 */
	public void setState(ServiceState state) {
		this.state = state;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.serverIndex);
		for (Server server : this.listOfServers) {
			sb.append(" - ");
			sb.append(server);
		}
		return sb.toString();
	}

	public int getCountServers() {
		return listOfServers.size();
	}
	/**
	 * Gets the count allocated sessions.
	 * 
	 * @return the count allocated sessions
	 */
	public int getCountAllocatedSessions() {
		int allocatedSessions = 0;

		for (Server server : listOfServers) {
			allocatedSessions += server.getSessions().size();
		}
		return allocatedSessions;
	}

	/**
	 * Gets the count available sessions.
	 * 
	 * @return the count available sessions
	 */
	public int getCountAvailableSessions() {
		int availableSessions = 0;

		for (Server server : listOfServers) {
			availableSessions += server.getMaxSessions();
		}
		return availableSessions;
	}
	

}
