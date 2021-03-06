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
package org.serviceconnector;

public class TestConstants {
	public static final String HOST = "localhost";
	public static final int PORT_SC0_HTTP = 7000;
	public static final int PORT_SC0A_HTTP = 3000;
	public static final int PORT_SC1_HTTP = 7001;
	public static final int PORT_SC2_HTTP = 7002;
	public static final int PORT_SC0_TCP = 9100;
	public static final int PORT_SC0A_TCP = 5000;
	public static final int PORT_SC1_TCP = 9101;
	public static final int PORT_SC2_TCP = 9102;

	public static final int PORT_SC0_MGMT = 49070;
	public static final int PORT_SC1_MGMT = 49071;
	public static final int PORT_SC2_MGMT = 49072;
	public static final int PORT_MIN = 1;
	public static final int PORT_MAX = 65535;
	public static final int PORT_SES_SRV_TCP = 30000;
	public static final int PORT_SES_SRVA_TCP = 50000;
	public static final int PORT_PUB_SRV_TCP = 30001;
	public static final int PORT_PUB_SRVA_TCP = 50001;
	public static final int PORT_PUB_SRV2_TCP = 30002;
	public static final int PORT_SES_SRV_HTTP = 40000;
	public static final int PORT_PUB_SRV_HTTP = 40001;

	public static final String COMMUNICATOR_TYPE_SESSION = "session";
	public static final String COMMUNICATOR_TYPE_PUBLISH = "publish";

	public static final String SC01Properties = "sc0.properties";
	public static final String SC1Properties = "sc1.properties";
	public static final String SC2Properties = "sc2.properties";
	public static final String logbackSC0 = "logback-sc0.xml";
	public static final String logbackSC0A = "logback-sc0A.xml";
	public static final String logbackSC1 = "logback-sc1.xml";
	public static final String logbackSC2 = "logback-sc2.xml";

	public static final String logbackCln = "logback-cln.xml";
	public static final String logbackSrv = "logback-srv.xml";
	public static final String SC0Properties = "sc0.properties";
	public static final String SC0AProperties = "sc0A.properties";
	public static final String SCNoInterfacesProperties = "sc-nointerfaces.properties";

	public static final String sesServiceName1 = "session-1";
	public static final String sesServiceName2 = "session-2";
	public static final String sesServerName1 = "session-1";
	public static final String sesServerName1A = "session-1A";
	public static final String pubServiceName1 = "publish-1";
	public static final String pubServiceName2 = "publish-2";
	public static final String pubServerName1 = "publish-1";
	public static final String pubServerName1A = "publish-1A";
	public static final String pubServerName2 = "publish-2";
	public static final String cacheGuardian1 = "cacheGuardian1";
	public static final String cacheGuardian2 = "cacheGuardian2";
	public static final String cacheGuardian1A = "cacheGuardian1A";
	public static final String filServiceName1 = "file-1";
	public static final String filServiceLocation1 = "up-download/";
	public static final String filServiceName2 = "file-2";
	public static final String filServiceLocation2 = "up-download/";
	public static final String sesServiceName1Casc = "session-1-casc";

	public static final String killServerCmd = "kill";
	public static final String rejectCmd = "reject";
	public static final String doNothingCmd = "doNothing";
	public static final String publish1RemoveMsgCmd = "publish1Remove";
	public static final String publish3AppendixMsgCmd = "publish3Appendix";
	public static final String publish1InitialMsgCmd = "publish1Initial";
	public static final String publish1LargeInitialMsgCmd = "publish1LargeInitial";
	public static final String publish10MBInitialMsgCmd = "publish10MBInitial";
	public static final String publish3LargeAppendix1InitialMsgCmd = "publish3LargeAppendix1Initial";
	public static final String publish3LargeAppendix1RemoveMsgCmd = "publish3LargeAppendix1Remove";
	public static final String publish50LargeAppendixMsgCmd = "publish50LargeAppendix";
	public static final String publish10MBAppendixMsgCmd = "publish10MBAppendix";
	public static final String publish50MBAppendixMsgCmd = "publish50MBAppendix";
	public static final String publish3LargeAppendixMsgCmd = "publish3LargeAppendix";
	public static final String publish3LargeAppendix1LargeInitialMsgCmd = "publish3LargeAppendix1LargeInitial";
	public static final String publish1Initial3LargeAppendixMsgCmd = "publish1Initial3LargeAppendix";
	public static final String publishCompressedMsgCmd = "publishMessagesCompressed";
	public static final String publishUncompressedMsgCmd = "publishMessagesUncompressed";
	public static final String publishMsgWithDelayCmd = "publishMessagesWithDelay";
	public static final String publishAppendsWithDelayCmd = "publishAppendsWithDelayCmd";
	public static final String publishLargeMsgCmd = "publishLargeMessage";
	public static final String echoCmd = "echoMessage";
	public static final String raiseExceptionCmd = "raiseException";
	public static final String echoAppErrorCmd = "echoAppError";
	public static final String echoAppError1Cmd = "echoAppError1";
	public static final String echoAppError2Cmd = "echoAppError2";
	public static final String echoAppError3Cmd = "echoAppError3";
	public static final String echoAppError4Cmd = "echoAppError4";
	public static final String largeResponseCmd = "largeResponse";
	public static final String largeResponse10MBCmd = "largeResponse10MB";
	public static final String sleepCmd = "sleep";
	public static final String cacheCmd = "cache";

	public static final String pangram = "The quick brown fox jumps over a lazy dog.";
	public static final String stringLength32 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength33 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength128 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength256 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
	public static final String stringLength257 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

	public static final int dataLength60kB = 61440;
	public static final int dataLength1MB = 1048576;
	public static final String mask = "0000121ABCDEFGHIJKLMNO-----------X-----------";
	public static final String mask1 = "0000635KLMNOPQRSTVWXYZ-----------X-----------";
	public static final String maskSrv = "0000121%%%%%%%%%%%%%%%-----------X-----------";
	public static final String noRecvMask = "                                             ";
	public static final String combinedMask = "0000%%%%%%%%%%%%%%%%%%-----------X-----------";
	public static final String appErrorText = "application error text";
	public static final int appErrorCode = 45234;

	public static final String logDirectoryToken = "logPath";
	public static final String configPortToken = "sc-tcp.port";
	public static final String scRunable = "sc.jar";
	public static final String serverRunable = "test-client-server.jar";
	public static final String clientRunable = "test-client-server.jar";

	// SC names
	public static final String SC0 = "sc0";
	public static final String SC0A = "sc0A";
	public static final String SC1 = "sc1";
	public static final String SC2 = "sc2";
	public static final String RemoteNodeName = "TestRemoteNode";
}
