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
package com.stabilit.scm.common.service;

import org.apache.log4j.Logger;

/**
 * The Class SCMessageFault. A SCMessageFault is the basic transport unit to communicate with a Service Connector
 * in case of an error situation.
 * 
 * @author JTraber
 */
public class SCMessageFault extends SCMessage {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCMessageFault.class);
	
	/** The application error code. */
	private String appErrorCode;
	/** The application error text. */
	private String appErrorText;

	/** {@inheritDoc} */
	@Override
	public boolean isFault() {
		return true;
	}

	/**
	 * Gets the application error code.
	 * 
	 * @return the application error code
	 */
	public String getAppErrorCode() {
		return appErrorCode;
	}

	/**
	 * Sets the application error code.
	 * 
	 * @param appErrorCode
	 *            the new application error code
	 */
	public void setAppErrorCode(String appErrorCode) {
		this.appErrorCode = appErrorCode;
	}

	/**
	 * Gets the application error text.
	 * 
	 * @return the application error text
	 */
	public String getAppErrorText() {
		return appErrorText;
	}

	/**
	 * Sets the application error text.
	 * 
	 * @param appErrorText
	 *            the new application error text
	 */
	public void setAppErrorText(String appErrorText) {
		this.appErrorText = appErrorText;
	}
}