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
/**
 * 
 */
package com.stabilit.sc.service;

import java.util.Map;

import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.cln.service.ISCMPCall;
import com.stabilit.sc.cln.service.SCMPCallAdapter;
import com.stabilit.sc.common.io.SCMPHeaderType;
import com.stabilit.sc.common.io.SCMPMsgType;

/**
 * @author JTraber
 * 
 */
public class SCMPAllocateSessionCall extends SCMPCallAdapter {

	public SCMPAllocateSessionCall() {
		this(null);
	}

	public SCMPAllocateSessionCall(IClient client) {
		this.client = client;
	}

	@Override
	public ISCMPCall newInstance(IClient client) {
		return new SCMPAllocateSessionCall(client);
	}
	
	public void setSessionId(String sessionId) {
		call.setHeader(SCMPHeaderType.SESSION_ID.getName(), sessionId);
	}

	public void setHeader(Map<String, String> header) {
		this.call.setHeader(header);
	}
	
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.REQ_ALLOCATE_SESSION;
	}
}
