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
package org.serviceconnector.cmd.sc;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.cache.Cache;
import org.serviceconnector.cache.CacheComposite;
import org.serviceconnector.cache.CacheException;
import org.serviceconnector.cache.CacheManager;
import org.serviceconnector.cache.CacheComposite.CACHE_STATE;
import org.serviceconnector.cmd.SCMPCommandException;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.ServiceType;
import org.serviceconnector.service.StatefulService;
import org.serviceconnector.util.URLString;
import org.serviceconnector.util.ValidatorUtility;
import org.serviceconnector.web.NotFoundException;

/**
 * The Class InspectCommand. Responsible for validation and execution of inspect command. Inspect command is used for
 * testing/maintaining reasons. Returns dumps of internal stuff to requester.
 * 
 * @author JTraber
 */
public class InspectCommand extends CommandAdapter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(InspectCommand.class);

	/**
	 * Instantiates a new InspectCommand.
	 */
	public InspectCommand() {
	}

	/** {@inheritDoc} */
	@Override
	public final SCMPMsgType getKey() {
		return SCMPMsgType.INSPECT;
	}

	/** {@inheritDoc} */
	@Override
	public final void run(final IRequest request, final IResponse response, final IResponderCallback responderCallback)
			throws Exception {
		SCMPMessage reqMsg = request.getMessage();
		String bodyString = (String) reqMsg.getBody();

		URLString urlRequestString = new URLString();
		urlRequestString.parseRequestURLString(bodyString);
		String callKey = urlRequestString.getCallKey();
		String serviceName = urlRequestString.getParamValue(Constants.SERVICE_NAME);

		SCMPMessage scmpReply = new SCMPMessage();
		scmpReply.setIsReply(true);
		InetAddress localHost = InetAddress.getLocalHost();
		scmpReply.setHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST, localHost.getHostAddress());
		scmpReply.setMessageType(getKey());

		if (Constants.CC_CMD_STATE.equalsIgnoreCase(callKey)) {
			// state for service requested
			LOGGER.debug("state request for service=" + serviceName);
			try {
				// get state of all services
				scmpReply.setBody(this.getStateOfServicesString(serviceName));
			} catch (Exception e) {
				LOGGER.debug("service=" + serviceName + " not found");
				scmpReply = new SCMPMessageFault(SCMPError.SERVICE_NOT_FOUND, serviceName);
			}
			response.setSCMP(scmpReply);
			// initiate responder to send reply
			responderCallback.responseCallback(request, response);
			return;
		}
		if (Constants.CC_CMD_SESSIONS.equalsIgnoreCase(callKey)) {
			// state for service requested
			LOGGER.debug("sessions request for service: " + serviceName);
			try {
				// get sessions of all services
				scmpReply.setBody(this.getSessionsOfServicesString(serviceName));
			} catch (Exception e) {
				LOGGER.debug("service=" + serviceName + " not found");
				scmpReply = new SCMPMessageFault(SCMPError.SERVICE_NOT_FOUND, serviceName);
			}
			response.setSCMP(scmpReply);
			// initiate responder to send reply
			responderCallback.responseCallback(request, response);
			return;
		}
		if (Constants.CC_CMD_INSPECT_CACHE.equalsIgnoreCase(callKey)) {
			String cacheId = urlRequestString.getParamValue("cacheId");
			LOGGER.debug("cache inspect for serviceName=" + serviceName + ", cacheId=" + cacheId);
			String cacheInspectString = getCacheInspectString(serviceName, cacheId);
			scmpReply.setBody(cacheInspectString);
			response.setSCMP(scmpReply);
			// initiate responder to send reply
			responderCallback.responseCallback(request, response);
			return;
		}
		LOGGER.error("wrong inspect command body=" + bodyString); // body has bad syntax
		scmpReply = new SCMPMessageFault(SCMPError.V_WRONG_INSPECT_COMMAND, bodyString);
		response.setSCMP(scmpReply);
		// initiate responder to send reply
		responderCallback.responseCallback(request, response);
	}

	/** {@inheritDoc} */
	@Override
	public final void validate(final IRequest request) throws Exception {
		try {
			SCMPMessage message = request.getMessage();
			// ipAddressList mandatory
			String ipAddressList = message.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);
			ValidatorUtility.validateIpAddressList(ipAddressList);
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			LOGGER.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}

	/**
	 * Gets the cache inspect string for given serviceName and cacheId.
	 * 
	 * @param serviceName
	 *            the service name
	 * @param cacheId
	 *            the cache id
	 * @return the cache inspect string
	 * @throws SCMPCommandException
	 *             the SCMP command exception
	 * @throws UnsupportedEncodingException
	 *             encoding response string failed
	 */
	private String getCacheInspectString(final String serviceName, final String cacheId) throws SCMPCommandException,
			UnsupportedEncodingException {
		CacheManager cacheManager = AppContext.getCacheManager();
		if (cacheManager == null) {
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_MANAGER_ERROR,
					"no cache manager (null)");
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
		Cache cache = cacheManager.getCache(serviceName);
		if (cache == null) {
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR, serviceName);
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
		try {
			CacheComposite cacheComposite = cache.getComposite(cacheId);
			if (cacheComposite == null) {
				return URLString.toURLResponseString(Constants.CACHE_ID, cacheId, "return", "notfound");
			}
			CACHE_STATE cacheState = cacheComposite.getCacheState();
			// Date creationTime = cacheComposite.getCreationTime();
			// Date lastModifiedTime = cacheComposite.getLastModifiedTime();
			String expirationDateTime = cacheComposite.getExpiration();
			int size = cacheComposite.getSize();
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("return", "success");
			parameters.put(Constants.CACHE_ID, cacheId);
			parameters.put("cacheState", cacheState.toString());
			parameters.put("cacheSize", String.valueOf(size));
			parameters.put("cacheExpiration", expirationDateTime);
			return URLString.toURLResponseString(parameters);
		} catch (CacheException e) {
			SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.CACHE_ERROR, e.toString());
			scmpCommandException.setMessageType(getKey());
			throw scmpCommandException;
		}
	}

	/**
	 * Gets the sessions of services string.
	 * 
	 * @return the sessions of services string
	 * @throws NotFoundException
	 */
	private String getSessionsOfServicesString(String serviceNameRegex) throws NotFoundException {
		boolean found = false;
		StringBuilder sb = new StringBuilder();

		Service[] services = this.serviceRegistry.getServices();
		int counter = 0;
		for (Service service : services) {
			if (service.getType() != ServiceType.PUBLISH_SERVICE && service.getType() != ServiceType.SESSION_SERVICE) {
				continue;
			}
			if (service.getName().matches(serviceNameRegex) == false) {
				// pattern does not match
				continue;
			}
			switch (service.getType()) {
			case SESSION_SERVICE:
			case PUBLISH_SERVICE:
				if (counter != 0) {
					sb.append(Constants.AMPERSAND_SIGN);
				}
				counter++;
				StatefulService statefulService = (StatefulService) service;
				sb.append(statefulService.getName());
				sb.append(Constants.EQUAL_SIGN);
				sb.append(statefulService.getCountAllocatedSessions());
				sb.append("/");
				sb.append(statefulService.getCountAvailableSessions());
				found = true;
				break;
			default:
				continue;
			}
		}
		if (found == false) {
			throw new NotFoundException("no service found pattern=" + serviceNameRegex);
		}
		return sb.toString();
	}

	/**
	 * Gets the state of services string.
	 * 
	 * @return the state of services string
	 * @throws NotFoundException
	 */
	private String getStateOfServicesString(String serviceNameRegex) throws NotFoundException {
		boolean found = false;
		StringBuilder sb = new StringBuilder();

		Service[] services = this.serviceRegistry.getServices();
		int counter = 0;
		for (Service service : services) {
			if (service.getName().matches(serviceNameRegex) == false) {
				// pattern does not match
				continue;
			}
			switch (service.getType()) {
			case SESSION_SERVICE:
			case PUBLISH_SERVICE:
			case FILE_SERVICE:
				if (counter != 0) {
					sb.append(Constants.AMPERSAND_SIGN);
				}
				counter++;
				sb.append(service.getName());
				sb.append(Constants.EQUAL_SIGN);
				if (service.isEnabled() == true) {
					sb.append("enabled");
				} else {
					sb.append("disabled");
				}
				found = true;
				break;
			default:
				continue;
			}
		}
		if (found == false) {
			throw new NotFoundException("no service found pattern=" + serviceNameRegex);
		}
		return sb.toString();
	}
}