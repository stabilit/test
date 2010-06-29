package com.stabilit.scm.common.net;

import com.stabilit.scm.common.scmp.SCMPMessage;


public interface ISCMPCallback {

	public abstract void callback(SCMPMessage scmpReply);
	
	public abstract void callback(Throwable th);
	
}
