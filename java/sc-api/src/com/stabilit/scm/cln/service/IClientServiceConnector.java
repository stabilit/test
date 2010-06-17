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
package com.stabilit.scm.cln.service;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author JTraber
 *
 */
public interface IClientServiceConnector {
	
	/**
	 * Connects to SC.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void connect() throws Exception;

	/**
	 * Disconnects from SC.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void disconnect() throws Exception;

	/**
	 * New data session to SC.
	 * 
	 * @param serviceName
	 *            the service name
	 * @return the sc session
	 * @throws Exception
	 *             the exception
	 */
	public ISCSession newDataSession(String serviceName) throws Exception;
	
	public ISCSubscription newSubscription(String string, SCPublishMessageHandler messageHandler, String mask);

	void uploadFile(String string, String targetFileName, InputStream inStream);

	void downloadFile(String string, String sourceFileName, OutputStream outStream);
	
	/**
	 * Sets the attribute. Attributes for ServiceConnector.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public void setAttribute(String name, Object value);

	public int getNumberOfThreads();

	public void setNumberOfThreads(int numberOfThreads);

	public String getConnectionKey();

	public void setConnectionKey(String connectionKey);

	public String getHost();
	
	public int getPort();
}
