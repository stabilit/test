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
package com.stabilit.sc.cln;

import com.stabilit.sc.cln.SCClient;
import com.stabilit.sc.cln.service.ISCClient;
import com.stabilit.sc.cln.service.ISessionService;
import com.stabilit.sc.common.service.ISCMessage;
import com.stabilit.sc.common.service.SCMessage;

public class SCSyncSessionServiceExample {

	public static void main(String[] args) {
		SCSyncSessionServiceExample.runExample();
	}

	public static void runExample() {
		ISCClient sc = null;
		try {
			sc = new SCClient();
			sc.setMaxConnections(100);

			// connects to SC, checks connection to SC
			sc.attach("localhost", 8080);

			ISessionService sessionServiceA = sc.newSessionService("simulation");
			// creates a session
			sessionServiceA.createSession("sessionInfo", 300, 60);

			ISCMessage requestMsg = new SCMessage();
			requestMsg.setData("Hello World");
			requestMsg.setCompressed(false);
			ISCMessage responseMsg = sessionServiceA.execute(requestMsg);

			System.out.println(responseMsg.getData().toString());
			// deletes the session
			sessionServiceA.deleteSession();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				sc.detach();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
}