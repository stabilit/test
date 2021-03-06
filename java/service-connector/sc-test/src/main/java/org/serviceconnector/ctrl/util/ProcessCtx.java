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
package org.serviceconnector.ctrl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.net.ConnectionType;

public class ProcessCtx {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCtx.class);

	private Process process = null;
	private String runableName = null;
	private String propertyFileName = null;
	private String logbackFileName = null;
	private String pidFileName = null;
	private boolean running = false;
	private String serviceNames;
	private String communicatorType;
	private String processName;
	private int scPort;
	private ConnectionType connectionType;

	public ProcessCtx() {
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		if (process != null) {
			this.running = true;
		}
		this.process = process;
	}

	public String getRunableName() {
		return runableName;
	}

	public void setRunableName(String runableName) {
		this.runableName = runableName;
	}

	public String getPropertyFileName() {
		return propertyFileName;
	}

	public void setPropertyFileName(String propertyFileName) {
		this.propertyFileName = propertyFileName;
	}

	public String getLogbackFileName() {
		return logbackFileName;
	}

	public void setLogbackFileName(String logbackFileName) {
		this.logbackFileName = logbackFileName;
	}

	public String getPidFileName() {
		return pidFileName;
	}

	public void setPidFileName(String pidFileName) {
		this.pidFileName = pidFileName;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getServiceNames() {
		return serviceNames;
	}

	public void setServiceNames(String serviceNames) {
		this.serviceNames = serviceNames;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getSCPort() {
		return scPort;
	}

	public void setSCPort(int scPort) {
		this.scPort = scPort;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public String getCommunicatorType() {
		return communicatorType;
	}

	public void setCommunicatorType(String communicatorType) {
		this.communicatorType = communicatorType;
	}

}
