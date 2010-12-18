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

import org.serviceconnector.scmp.SCMPError;

/**
 * The Class SCServiceException. Used to notify errors on SC service level.
 * 
 * @author JTraber
 */
/**
 * @author schoeggii
 *
 */
public class SCServiceException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 75222936621899150L;

	/** The application error code. */
	private int appErrorCode;
	/** The application error text. */
	private String appErrorText;
	/** The scmp error. */
	private SCMPError scmpError;

	/**
	 * Instantiates a new SC service exception.
	 * 
	 * @param message
	 *            the message
	 * @param exception
	 *            the cause
	 */
	public SCServiceException(String message, Exception exception) {
		super(message, exception);
	}

	/**
	 * Instantiates a new SC service exception.
	 * 
	 * @param message
	 *            the message
	 */
	public SCServiceException(String message) {
		super(message);
	}

	/**
	 * Gets the application error code.
	 * 
	 * @return the application error code
	 */
	public int getAppErrorCode() {
		return this.appErrorCode;
	}

	/**
	 * Sets the application error code.
	 * 
	 * @param appErrorCode
	 *            the new application error code
	 */
	public void setAppErrorCode(int appErrorCode) {
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

	/**
	 * Gets the SCMP error.
	 * 
	 * @return the SCMP error
	 */
	public SCMPError getSCMPError() {
		return this.scmpError;
	}

	/**
	 * Sets the SCMP error.
	 * 
	 * @param errorCode
	 *            the new SCMP error
	 */
	public void setSCMPError(String errorCode) {
		this.scmpError = SCMPError.getSCMPErrorByCode(errorCode);
	}

	public void setSCMPError(SCMPError scmpError) {
		this.scmpError = scmpError;
	}
}
