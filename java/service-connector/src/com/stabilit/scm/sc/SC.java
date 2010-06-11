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

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.stabilit.scm.common.cmd.factory.CommandFactory;
import com.stabilit.scm.common.conf.IResponderConfigItem;
import com.stabilit.scm.common.conf.ResponderConfig;
import com.stabilit.scm.common.conf.ResponderConfig.ResponderConfigItem;
import com.stabilit.scm.common.log.listener.ExceptionPoint;
import com.stabilit.scm.common.res.IResponder;
import com.stabilit.scm.sc.cmd.factory.impl.ServiceConnectorCommandFactory;
import com.stabilit.scm.sc.registry.ClientRegistry;
import com.stabilit.scm.sc.registry.ServerRegistry;
import com.stabilit.scm.sc.registry.ServiceRegistry;
import com.stabilit.scm.sc.registry.SessionRegistry;
import com.stabilit.scm.sc.res.SCResponder;
import com.stabilit.scm.sc.service.ServiceLoader;

/**
 * The Class SC. Starts the core (responders) of the Service Connector.
 * 
 * @author JTraber
 */
public final class SC {

	/**
	 * Instantiates a new service connector.
	 */
	private SC() {
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		SC.run();
	}

	/**
	 * Run SC responders.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void run() throws Exception {
		ResponderConfig config = new ResponderConfig();
		config.load("sc.properties");

		// load services
		ServiceLoader.load("sc.properties");
		
		CommandFactory commandFactory = CommandFactory.getCurrentCommandFactory();
		if (commandFactory == null) {
			CommandFactory.setCurrentCommandFactory(new ServiceConnectorCommandFactory());
		}

		SC.initializeJMXStuff();

		List<ResponderConfigItem> respConfigList = config.getResponderConfigList();
		
		for (IResponderConfigItem respConfig : respConfigList) {
			IResponder resp = new SCResponder();
			resp.setResponderConfig(respConfig);
			try {
				resp.create();
				resp.runAsync();
			} catch (Exception e) {
				ExceptionPoint.getInstance().fireException(SC.class, e);
			}
		}
	}

	/**
	 * Initialize jmx stuff.
	 */
	private static void initializeJMXStuff() {
		try {
			// Necessary to make access for JMX client available
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName mxbeanNameConnReg = new ObjectName("com.stabilit.scm.registry:type=ClientRegistry");
			ObjectName mxbeanNameSessReg = new ObjectName("com.stabilit.scm.registry:type=SessionRegistry");
			ObjectName mxbeanNameServiceReg = new ObjectName("com.stabilit.scm.registry:type=ServiceRegistry");
			ObjectName mxbeanNameServerReg = new ObjectName("com.stabilit.scm.registry:type=ServerRegistry");

			// Register the Queue Sampler MXBean
			mbs.registerMBean(ClientRegistry.getCurrentInstance(), mxbeanNameConnReg);
			mbs.registerMBean(SessionRegistry.getCurrentInstance(), mxbeanNameSessReg);
			mbs.registerMBean(ServiceRegistry.getCurrentInstance(), mxbeanNameServiceReg);
			mbs.registerMBean(ServerRegistry.getCurrentInstance(), mxbeanNameServerReg);
		} catch (Throwable th) {
			ExceptionPoint.getInstance().fireException(SC.class, th);
		}
	}
}
