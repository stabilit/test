package com.stabilit.sc.example.server;

import org.apache.log4j.Logger;

import com.stabilit.sc.io.SCMP;
import com.stabilit.sc.msg.ClientListener;
import com.stabilit.sc.msg.impl.GetDataMessage;
import com.stabilit.sc.pool.IPoolConnection;

public class ServerRRListener extends ClientListener {
	
	Logger log = Logger.getLogger(ServerRRListener.class);
	
	@Override
	public void messageReceived(IPoolConnection conn, SCMP scmp) throws Exception {
		super.messageReceived(conn, scmp);
	
		log.debug("Messages received " + scmp.getMessageId() + " on TcpRRServerListener");
		
		if (scmp.getMessageId().equals("getData")) {
			log.debug("GetDataMessage sent.");
			scmp.setBody(new GetDataMessage("DatenStringRRR"));
			conn.send(scmp);
		}
	}
}
