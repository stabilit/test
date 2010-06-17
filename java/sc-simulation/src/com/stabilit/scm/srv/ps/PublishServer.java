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
package com.stabilit.scm.srv.ps;

import com.stabilit.scm.cln.service.IServiceConnector;
import com.stabilit.scm.cln.service.ServiceConnector;

public class PublishServer {

	public static void main(String[] args) {
		PublishServer.runExample();
	}

	public static void runExample() {
		IServiceConnector sc = null;
		try {
			sc = new ServiceConnector("localhost", 8080);
			sc.setAttribute("keepAliveInterval", 60);
			sc.setAttribute("keepAliveTimeout", 10);

			// connects to SC, starts observing connection
			sc.connect();
			Object data;
			String mask = "AVSD-----";
			//sc.publish("simulation", mask, data);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				sc.disconnect();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
}