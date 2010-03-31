/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
package com.stabilit.sc.unit.test;

import junit.framework.Assert;

import org.junit.Test;

import com.stabilit.sc.cln.msg.impl.MaintenanceMessage;
import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPConnectCall;
import com.stabilit.sc.cln.service.SCMPDisconnectCall;
import com.stabilit.sc.cln.service.SCMPMaintenanceCall;
import com.stabilit.sc.cln.service.SCMPServiceException;
import com.stabilit.sc.common.io.SCMP;
import com.stabilit.sc.common.io.SCMPErrorCode;
import com.stabilit.sc.common.io.SCMPHeaderType;
import com.stabilit.sc.common.io.SCMPMsgType;

public class DisconnectTestCase extends SuperConnectTestCase {
	
	@Test
	public void secondConnect() throws Exception {
		SCMPConnectCall connectCall = (SCMPConnectCall) SCMPCallFactory.CONNECT_CALL.newInstance(client);

		connectCall.setVersion("1.0-00");
		connectCall.setCompression(false);
		connectCall.setKeepAliveTimeout(30);
		connectCall.setKeepAliveInterval(360);

		try {
			connectCall.invoke();
			Assert.fail("Should throw Exception!");
		} catch (SCMPServiceException e) {
			SCTest.verifyError(e.getFault(), SCMPErrorCode.ALREADY_CONNECTED, SCMPMsgType.RES_CONNECT);
		}
	}

	@Test
	public void disconnect() throws Exception {
		SCMPDisconnectCall disconnectCall = (SCMPDisconnectCall) SCMPCallFactory.DISCONNECT_CALL
				.newInstance(client);

		SCMP result = null;
		try {
			result = disconnectCall.invoke();
		} catch (SCMPServiceException e) {
			Assert.fail();
		}

		/*********************************** Verify disconnect response msg **********************************/
		Assert.assertNull(result.getBody());
		Assert.assertEquals(result.getHeader(SCMPHeaderType.MSG_TYPE.getName()), SCMPMsgType.RES_DISCONNECT
				.getResponseName());

		/*************** scmp maintenance ********/
		SCMPMaintenanceCall maintenanceCall = (SCMPMaintenanceCall) SCMPCallFactory.MAINTENANCE_CALL
				.newInstance(client);
		SCMP maintenance = maintenanceCall.invoke();
		/*********************************** Verify registry entries in SC ***********************************/
		MaintenanceMessage mainMsg = (MaintenanceMessage) maintenance.getBody();
		String scEntry = (String) mainMsg.getAttribute("connectionRegistry");
		Assert.assertEquals("", scEntry);
		super.simpleConnect();
	}

	@Test
	public void secondDisconnect() throws Exception {
		super.simpleDisconnect();
		SCMPDisconnectCall disconnectCall = (SCMPDisconnectCall) SCMPCallFactory.DISCONNECT_CALL
				.newInstance(client);
		try {
			disconnectCall.invoke();
		} catch (SCMPServiceException e) {
			SCTest.verifyError(e.getFault(), SCMPErrorCode.NOT_CONNECTED, SCMPMsgType.RES_DISCONNECT);
		}
		super.simpleConnect();
	}
}
