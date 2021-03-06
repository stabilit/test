/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.net;

import org.serviceconnector.Constants;
import org.serviceconnector.scmp.SCMPHeaderKey;

/**
 * The Class SCMPFrameDecoder.
 *
 * @author JTraber
 */
public class SCMPFrameDecoder {

	/**
	 * Instantiates a new scmp frame decoder.
	 */
	protected SCMPFrameDecoder() {
	}

	/**
	 * Parses the frame size.
	 *
	 * @param buffer the buffer to parse
	 * @return the frame size
	 * @throws Exception the exception
	 */
	public static int parseFrameSize(byte[] buffer) throws FrameDecoderException {
		try {
			// check headerKey
			SCMPHeaderKey headerKey = SCMPHeaderKey.getKeyByHeadline(buffer);
			if (headerKey == SCMPHeaderKey.UNDEF) {
				throw new FrameDecoderException("invalid scmp header line:" + new String(buffer, 0, Constants.SCMP_HEADLINE_SIZE));
			}
			// parse frame size
			int scmpLength = SCMPFrameDecoder.parseMessageSize(buffer);
			return Constants.SCMP_HEADLINE_SIZE + scmpLength;
		} catch (Exception e) {
			throw new FrameDecoderException("error when parsing frame size", e);
		}
	}

	/**
	 * Parses the message size.
	 *
	 * @param buffer the buffer
	 * @return the int
	 * @throws Exception the exception
	 */
	public static int parseMessageSize(byte[] buffer) throws Exception {
		return SCMPFrameDecoder.readInt(buffer, Constants.SCMP_MSG_SIZE_START, Constants.SCMP_MSG_SIZE_END);
	}

	/**
	 * Parses the header size.
	 *
	 * @param buffer the buffer
	 * @return the int
	 * @throws Exception the exception
	 */
	public static int parseHeaderSize(byte[] buffer) throws Exception {
		return SCMPFrameDecoder.readInt(buffer, Constants.SCMP_HEADER_SIZE_START, Constants.SCMP_HEADER_SIZE_END);
	}

	/**
	 * Read int from byte buffer.
	 *
	 * @param b the b
	 * @param startOffset the start offset
	 * @param endOffset the end offset
	 * @return the int
	 * @throws FrameDecoderException the frame decoder exception
	 */
	public static int readInt(byte[] b, int startOffset, int endOffset) throws Exception {

		if (b == null) {
			throw new FrameDecoderException("invalid scmp message length");
		}

		if (endOffset <= 0 || endOffset <= startOffset) {
			throw new FrameDecoderException("invalid scmp message length");
		}

		int scmpLength = 0;
		int factor = 1;
		for (int i = endOffset; i >= startOffset; i--) {
			if (b[i] >= '0' && b[i] <= '9') {
				scmpLength += (b[i] - Constants.SCMP_ZERO) * factor;
				factor *= Constants.NUMBER_10;
			} else {
				throw new Exception("invalid scmp message length");
			}
		}
		return scmpLength;
	}
}
