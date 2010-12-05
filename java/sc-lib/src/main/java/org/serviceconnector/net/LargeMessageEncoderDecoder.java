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
package org.serviceconnector.net;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.Deflater;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.MessageLogger;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPHeadlineKey;
import org.serviceconnector.scmp.SCMPInternalStatus;
import org.serviceconnector.scmp.SCMPMessage;

/**
 * The Class LargeMessageEncoderDecoder. Defines large SCMP encoding/decoding of object into/from stream.
 */
public class LargeMessageEncoderDecoder extends MessageEncoderDecoderAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(LargeMessageEncoderDecoder.class);

	/** The Constant messageLogger. */
	private final static MessageLogger messageLogger = MessageLogger.getInstance();

	/**
	 * Instantiates a new large message encoder decoder.
	 */
	LargeMessageEncoderDecoder() {
	}

	/** {@inheritDoc} */
	@Override
	public void encode(OutputStream os, Object obj) throws Exception {
		OutputStreamWriter osw = new OutputStreamWriter(os, CHARSET);
		BufferedWriter bw = new BufferedWriter(osw);
		SCMPMessage scmpMsg = (SCMPMessage) obj;

		if (scmpMsg.isGroup() == false) {
			// no group call reset internal status, if group call internal
			// status already set
			scmpMsg.setInternalStatus(SCMPInternalStatus.NONE);
		}

		// evaluate right headline key from SCMP type
		SCMPHeadlineKey headerKey = SCMPHeadlineKey.UNDEF;
		if (scmpMsg.isReply()) {
			if (scmpMsg.isFault()) {
				headerKey = SCMPHeadlineKey.EXC;
			} else {
				if (scmpMsg.isPart()) {
					if (scmpMsg.isPollRequest()) {
						headerKey = SCMPHeadlineKey.PAC;
					} else {
						headerKey = SCMPHeadlineKey.PRS;
					}
				} else {
					headerKey = SCMPHeadlineKey.RES;
				}
			}
		} else {
			if (scmpMsg.isPart() || scmpMsg.isComposite()) {
				if (scmpMsg.isPollRequest()) {
					headerKey = SCMPHeadlineKey.PAC;
				} else {
					headerKey = SCMPHeadlineKey.PRQ;
				}
			} else {
				headerKey = SCMPHeadlineKey.REQ;
			}
		}

		StringBuilder sb = this.writeHeader(scmpMsg.getHeader());
		// write body depends on body type
		Object body = scmpMsg.getBody();
		int headerSize = sb.length();
		try {
			if (body != null) {
				if (byte[].class == body.getClass()) {
					byte[] ba = (byte[]) body;
					byte[] output = null;
					// int bodyLength = ba.length;
					int bodyLength = scmpMsg.getBodyLength();
					int bodyOffset = scmpMsg.getBodyOffset();
					if (scmpMsg.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION) && AppContext.isScEnvironment() == false) {
						// message compression required
						output = new byte[bodyLength];
						Deflater compresser = new Deflater();
						// compresser.setInput(ba);
						compresser.setInput(ba, bodyOffset, bodyLength);
						compresser.finish();
						ByteArrayOutputStream baos = new ByteArrayOutputStream(output.length);
						bodyLength = 0;
						while (!compresser.finished()) {
							int numCompressedBytes = compresser.deflate(output);
							bodyLength += numCompressedBytes;
							if (numCompressedBytes > 0) {
								baos.write(output, 0, numCompressedBytes);
								baos.flush();
							}
						}
						baos.close();
						ba = baos.toByteArray();
					}
					this.writeHeadLine(bw, headerKey, bodyLength + sb.length(), headerSize);
					bw.write(sb.toString());
					bw.flush();
					if (scmpMsg.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION) && AppContext.isScEnvironment() == false) {
						// message compression required
						os.write(ba);
					} else {
						os.write(ba, bodyOffset, bodyLength);
					}
					os.flush();
					// set internal status to save communication state
					scmpMsg.setInternalStatus(SCMPInternalStatus.getInternalStatus(headerKey));
					if (messageLogger.isEnabled()) {
						messageLogger.logMessage(this.getClass().getSimpleName(), scmpMsg);
					}
					return;
				}
				if (String.class == body.getClass()) {
					String t = (String) body;
					byte[] output = null;
					// int bodyLength = t.length();
					int bodyLength = scmpMsg.getBodyLength();
					// gets the offset of body - body of part message is written
					int bodyOffset = scmpMsg.getBodyOffset();
					if (scmpMsg.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION) && AppContext.isScEnvironment() == false) {
						// message compression required
						byte[] ba = t.getBytes();
						output = new byte[ba.length];
						Deflater compresser = new Deflater();
						compresser.setInput(ba, bodyOffset, bodyLength);
						compresser.finish();
						ByteArrayOutputStream baos = new ByteArrayOutputStream(output.length);
						bodyLength = 0;
						while (!compresser.finished()) {
							int numCompressedBytes = compresser.deflate(output, 0, output.length);
							bodyLength += numCompressedBytes;
							if (numCompressedBytes > 0) {
								baos.write(output, 0, numCompressedBytes);
								baos.flush();
							}
						}
						baos.close();
						output = baos.toByteArray();
					}
					this.writeHeadLine(bw, headerKey, bodyLength + sb.length(), headerSize);
					bw.write(sb.toString()); // write header
					bw.flush();
					if (scmpMsg.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION) && AppContext.isScEnvironment() == false) {
						// message compression required
						os.write(output);
						os.flush();
					} else {
						bw.write(t, bodyOffset, bodyLength);
						bw.flush();
					}
					// set internal status to save communication state
					scmpMsg.setInternalStatus(SCMPInternalStatus.getInternalStatus(headerKey));
					if (messageLogger.isEnabled()) {
						messageLogger.logMessage(this.getClass().getSimpleName(), scmpMsg);
					}
					return;
				}
				if (body instanceof InputStream) {
					InputStream inStream = (InputStream) body;
					byte[] buffer = new byte[Constants.MAX_MESSAGE_SIZE];
					int bodyLength = inStream.read(buffer);
					if (scmpMsg.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION) && AppContext.isScEnvironment() == false) {
						// message compression required
						byte[] output = new byte[bodyLength];
						Deflater compresser = new Deflater();
						compresser.setInput(buffer, 0, bodyLength);
						compresser.finish();
						bodyLength = compresser.deflate(output);
						buffer = output;
					}
					this.writeHeadLine(bw, headerKey, bodyLength + sb.length(), headerSize);
					bw.write(sb.toString());
					bw.flush();
					os.write(buffer, 0, bodyLength);
					os.flush();
					// set internal status to save communication state
					scmpMsg.setInternalStatus(SCMPInternalStatus.getInternalStatus(headerKey));
					if (messageLogger.isEnabled()) {
						messageLogger.logMessage(this.getClass().getSimpleName(), scmpMsg);
					}
					return;
				}
				// set internal status to save communication state
				scmpMsg.setInternalStatus(SCMPInternalStatus.FAILED);
				throw new EncodingDecodingException("unsupported large message body type");
			} else {
				writeHeadLine(bw, headerKey, headerSize, headerSize);
				bw.write(sb.toString());
				bw.flush();
			}
		} catch (IOException ex) {
			logger.error("encode", ex);
			scmpMsg.setInternalStatus(SCMPInternalStatus.FAILED);
			throw new EncodingDecodingException("io error when decoding message", ex);
		}
		// set internal status to save communication state
		scmpMsg.setInternalStatus(SCMPInternalStatus.getInternalStatus(headerKey));
		if (messageLogger.isEnabled()) {
			messageLogger.logMessage(this.getClass().getSimpleName(), scmpMsg);
		}
		return;
	}
}
