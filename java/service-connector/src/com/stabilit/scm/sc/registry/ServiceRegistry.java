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
package com.stabilit.scm.sc.registry;

import com.stabilit.scm.common.registry.Registry;
import com.stabilit.scm.sc.service.Service;

/**
 * The Class ServiceRegistry. Registry stores entries for properly registered services.
 * 
 * @author JTraber
 */
public final class ServiceRegistry extends Registry {

	/** The instance. */
	private static ServiceRegistry instance = new ServiceRegistry();

	/**
	 * Instantiates a new service registry.
	 */
	private ServiceRegistry() {
	}

	/**
	 * Gets the current instance.
	 * 
	 * @return the current instance
	 */
	public static ServiceRegistry getCurrentInstance() {
		return instance;
	}

	public void addService(Object key, Service service) {
		super.put(key, service);
	}

	public Service getService(String serviceName) {
		return (Service) this.get(serviceName);
	}

	public void removeService(Service service) {
		this.removeService(service.getServiceName());
	}

	public void removeService(Object key) {
		super.remove(key);
	}
}
