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
package org.serviceconnector.test.unit.cache;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.cache.Cache;
import org.serviceconnector.cache.CacheComposite;
import org.serviceconnector.cache.CacheException;
import org.serviceconnector.cache.CacheId;
import org.serviceconnector.cache.CacheManager;
import org.serviceconnector.cache.CacheMessage;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.SessionService;
import org.serviceconnector.util.DateTimeUtility;
import org.serviceconnector.util.TimeMillis;


/**
 * The class CacheExpirationTest tests the cache expiration functionality.
 * 
 * @author ds
 */
public class CacheExpirationTest {

	private CacheManager cacheManager;
	
	/**
	 * Run before each test and setup the dummy environment (services and cache manager).
	 * <br/>
	 * 
	 * @throws Exception
	 * 
	 */
	@Before
	public void beforeTest() throws Exception {
	   AppContext.setSCEnvironment(true);
       ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
	   Service service = new SessionService("dummy");
       serviceRegistry.addService("dummy", service);
	   service = new SessionService("dummy1");
       serviceRegistry.addService("dummy1", service);
	   service = new SessionService("dummy2");
       serviceRegistry.addService("dummy2", service);
	   cacheManager = new CacheManager();
	   cacheManager.initialize();
	}

	@After
	public void afterTest() {
		cacheManager.destroy();
	}

	/**
	 * Description: Simple cache write test, not expired<br>
	 * Write a message into the cache using a dummy id and nr and read the message from cache again, checking if both contents
	 * (body) equals. Verify if cache size is 1.<br>
	 *  
	 * Expectation: passes
	 */
	@Test
	public void t01_notExpiredCacheWriteTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);		
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		Date now = new Date();
		Date expirationDate = DateTimeUtility.getIncrementTimeInMillis(now, TimeMillis.HOUR.getMillis());	
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_EXPIRATION_DATETIME, DateTimeUtility.getTimeAsString(expirationDate));
		CacheId msgCacheId = scmpCache.putMessage(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, msgCacheId.getFullCacheId());
		CacheMessage cacheMessage = scmpCache.getMessage(scmpMessageRead.getCacheId());
		byte[] bufferRead = (byte[]) cacheMessage.getBody();
		String stringRead = new String(bufferRead);
		Assert.assertEquals(stringWrite, stringRead);
		// get composite cache of given id
		CacheComposite cacheComposite = scmpCache.getComposite(msgCacheId.getCacheId());
		int size = cacheComposite.getSize();
		Assert.assertEquals(1, size);
		Assert.assertEquals(false, cacheComposite.isExpired());
	}

	/**
	 * Description: Simple cache write test, expired<br>
	 * Write a message into the cache using a dummy id and nr. Set the expiration date and time one hour to the past.
	 * Try to read the message from its cache again but this should fail because the message has been expired.<br>
	 *  
	 * Expectation: passes
	 */
	@Test
	public void t02_expiredCacheWriteTest() throws CacheException {
		Cache scmpCache = this.cacheManager.getCache("dummy");
		String stringWrite = "this is the buffer";
		byte[] buffer = stringWrite.getBytes();
		SCMPMessage scmpMessageWrite = new SCMPMessage(buffer);		
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_ID, "dummy.cache.id");
		Date now = new Date();
		Date expirationDate = DateTimeUtility.getIncrementTimeInMillis(now, -TimeMillis.HOUR.getMillis());	
		scmpMessageWrite.setHeader(SCMPHeaderAttributeKey.CACHE_EXPIRATION_DATETIME, DateTimeUtility.getTimeAsString(expirationDate));
		CacheId msgCacheId = scmpCache.putMessage(scmpMessageWrite);
		SCMPMessage scmpMessageRead = new SCMPMessage();
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, 1233);
		scmpMessageRead.setHeader(SCMPHeaderAttributeKey.CACHE_ID, msgCacheId.getFullCacheId());
		CacheMessage cacheMessage = scmpCache.getMessage(scmpMessageRead.getCacheId());
		if (cacheMessage != null) {
		    Assert.fail("cache should be expired but is not");
		}
		CacheComposite cacheComposite = scmpCache.getComposite(msgCacheId.getCacheId());
		int size = cacheComposite.getSize();
		Assert.assertEquals(1, size);
		Assert.assertEquals(true, cacheComposite.isExpired());
	}

}
