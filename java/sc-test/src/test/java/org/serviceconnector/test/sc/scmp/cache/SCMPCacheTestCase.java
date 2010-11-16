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
package org.serviceconnector.test.sc.scmp.cache;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.cache.SCCache;
import org.serviceconnector.cache.SCCacheException;
import org.serviceconnector.cache.SCCacheManager;
import org.serviceconnector.cache.SCCacheMessage;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.SessionService;

/**
 * The Class SCMPCacheTest.
 * 
 * @author ds
 */
public class SCMPCacheTestCase {

	private SCCacheManager scmpCacheManager;
	/**
	 * Scmp cache write test.
	 * @throws Exception 
	 * 
	 * @throws SCCacheException
	 *             the sCMP cache exception
	 */

	@Before
	public void beforeTest() throws Exception {		
       ServiceRegistry serviceRegistry = AppContext.getCurrentContext().getServiceRegistry();
	   Service service = new SessionService("dummy");
       serviceRegistry.addService("dummy", service);
	   service = new SessionService("dummy1");
       serviceRegistry.addService("dummy1", service);
	   service = new SessionService("dummy2");
       serviceRegistry.addService("dummy2", service);
	   scmpCacheManager = new SCCacheManager();
	   scmpCacheManager.initialize(null);
	}

	@After
	public void afterTest() {
		scmpCacheManager.destroy();
	}

	@Test
	public void simpleSCMPCacheWriteTest() throws SCCacheException {
		SCCache scmpCache = this.scmpCacheManager.getCache("dummy");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);
		
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		scmpCache.putSCMP(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		SCCacheMessage cacheMessage = scmpCache.getSCMP(scmpMessageRead);
		byte[] bufferRead = (byte[]) cacheMessage.getBody();
		String stringRead = new String(bufferRead);
		Assert.assertEquals(stringWrite, stringRead);
	}

	@Test
	public void duplicateSCMPCacheWriteTest() throws SCCacheException {
		SCCache scmpCache1 = this.scmpCacheManager.getCache("dummy1");
		SCCache scmpCache2 = this.scmpCacheManager.getCache("dummy2");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);
		
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		scmpCache1.putSCMP(scmpMessageWrite);
		scmpCache2.putSCMP(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		SCCacheMessage cacheMessage1 = scmpCache1.getSCMP(scmpMessageRead);
		byte[] bufferRead1 = (byte[]) cacheMessage1.getBody();
		String stringRead1 = new String(bufferRead1);
		Assert.assertEquals(stringWrite, stringRead1);
		SCCacheMessage cacheMessage2 = scmpCache2.getSCMP(scmpMessageRead);
		byte[] bufferRead2 = (byte[]) cacheMessage2.getBody();
		String stringRead2 = new String(bufferRead2);
		Assert.assertEquals(stringWrite, stringRead2);
	}

	@Test
	public void partSCMPCacheWriteTest() throws SCCacheException {
		SCCache scmpCache = this.scmpCacheManager.getCache("dummy");
		String stringWrite = "this is the part buffer nr = ";
		for (int i = 1; i <= 10; i++) {
			String partWrite = stringWrite + i;		    
		    byte[] buffer = partWrite.getBytes();
		    SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);		
		    scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, "1233/"+i);
		    scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		    scmpCache.putSCMP(scmpMessageWrite);
		}
		for (int i = 1; i <= 11; i++) {
			String partWrite = stringWrite + i;		    
		    SCMPMessage scmpMessageRead = new SCMPMessage();
		    scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, "1233/"+i);
		    scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		    SCCacheMessage cacheMessage = scmpCache.getSCMP(scmpMessageRead);
		    if (cacheMessage == null) {
		    	if (i < 11) {
		    	   Assert.fail("cacheMessage is null but should not");
		    	   continue;
		    	}
		    	break;
		    }
		    byte[] bufferRead = (byte[]) cacheMessage.getBody();
		    String stringRead = new String(bufferRead);
		    Assert.assertEquals(partWrite, stringRead);
		}
	}

}
