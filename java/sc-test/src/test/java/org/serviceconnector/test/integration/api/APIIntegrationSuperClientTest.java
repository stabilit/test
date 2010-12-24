/*
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
 */
package org.serviceconnector.test.integration.api;

import org.junit.After;
import org.junit.Before;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.api.cln.SCSessionService;

public class APIIntegrationSuperClientTest extends APIIntegrationSuperTest {

	protected SCClient client;
	protected SCSessionService sessionService = null;
	protected SCPublishService publishService = null;
	protected static boolean messageReceived = false;
	
	@Before
	public void beforeOneTest() throws Exception {
		sessionService = null;
		publishService = null;
		super.beforeOneTest();
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			client.detach();
		} catch (Exception e) {}
		client = null;
		super.afterOneTest();
	}
}
