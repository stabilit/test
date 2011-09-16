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
package org.serviceconnector.log;

import java.util.Formatter;

import org.apache.log4j.Logger;

/**
 * The Class CacheLogger.
 */
public final class CacheLogger {

	/** The Constant cacheLogger. */
	private static final Logger CACHE_LOGGER = Logger.getLogger(Loggers.CACHE.getValue());
	/** The start loading str. */
	private static String startLoadingStr = "start loading message cacheKey=%s sid=%s timeout=%sms";
	/** The try loading str. */
	private static String tryGetMsgStr = "try to get message from cache cacheKey=%s sid=%s cpn=%s";
	/** The got message str. */
	private static String gotMessageStr = "got message from cache cacheKey=%s sid=%s length=%s";
	/** The finish loading str. */
	private static String finishLoadingStr = "finish loading message cacheKey=%s sid=%s partCounter=%s";
	/** The abort loading str. */
	private static String abortLoadingStr = "abort loading message cacheKey=%s sid=%s";
	/** The put message str. */
	private static String putMessageStr = "put message into cache cacheKey=%s loading sid=%s expiration=%s length=%s";
	/** The remove msg from cache str. */
	private static String removeMsgFromCacheStr = "remove message from cache cacheKey=%s sid=%s reason=%s";
	/** The clear cache str. */
	private static String clearCacheStr = "clear cache, all messages removed.";

	/**
	 * Private constructor for singleton use.
	 */
	private CacheLogger() {
	}

	/**
	 * Checks if is enabled.
	 * 
	 * @return true, if is enabled
	 */
	public static boolean isEnabled() {
		return CACHE_LOGGER.isTraceEnabled();
	}

	/**
	 * Try to get message from cache.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 * @param requestedPart
	 *            the requested part
	 */
	public static void tryGetMessageFromCache(String cacheKey, String sessionId, String requestedPart) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(tryGetMsgStr, cacheKey, sessionId, requestedPart);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Got message from cache.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 */
	public static void gotMessageFromCache(String cacheKey, String sessionId, int length) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(gotMessageStr, cacheKey, sessionId, length);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Start loading cache message.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 * @param timeout
	 *            the loading timeout in milliseconds
	 */
	public static void startLoadingCacheMessage(String cacheKey, String sessionId, int timeout) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(startLoadingStr, cacheKey, sessionId, timeout);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Finish loading cache message.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 * @param numberOfParts
	 *            the number of parts
	 */
	public static void finishLoadingCacheMessage(String cacheKey, String sessionId, int numberOfParts) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(finishLoadingStr, cacheKey, sessionId, numberOfParts);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Abort loading message.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 */
	public static void abortLoadingMessage(String cacheKey, String sessionId) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(abortLoadingStr, cacheKey, sessionId);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Put message to cache.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param loadingSessionId
	 *            the loading session id
	 * @param expiration
	 *            the expiration
	 */
	public static void putMessageToCache(String cacheKey, String loadingSessionId, String expiration, int length) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(putMessageStr, cacheKey, loadingSessionId, expiration, length);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Removes the message from cache.
	 * 
	 * @param cacheKey
	 *            the cache key
	 * @param sessionId
	 *            the session id
	 * @param removeReason
	 *            the remove reason
	 */
	public static void removeMessageFromCache(String cacheKey, String sessionId, String removeReason) {
		if (CACHE_LOGGER.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(removeMsgFromCacheStr, cacheKey, sessionId, removeReason);
			CACHE_LOGGER.trace(format.toString());
			format.close();
		}
	}

	/**
	 * Clear cache.
	 */
	public static void clearCache() {
		if (CACHE_LOGGER.isTraceEnabled()) {
			CACHE_LOGGER.trace(clearCacheStr);
		}
	}
}