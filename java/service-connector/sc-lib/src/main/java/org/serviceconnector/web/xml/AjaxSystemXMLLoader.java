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

package org.serviceconnector.web.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamWriter;

import org.serviceconnector.Constants;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCFileService;
import org.serviceconnector.cache.SCCache;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.server.FileServerException;
import org.serviceconnector.service.CascadedFileService;
import org.serviceconnector.service.FileService;
import org.serviceconnector.service.Service;
import org.serviceconnector.util.CircularByteBuffer;
import org.serviceconnector.util.DateTimeUtility;
import org.serviceconnector.util.DumpUtility;
import org.serviceconnector.util.FileUtility;
import org.serviceconnector.util.SystemInfo;
import org.serviceconnector.web.IWebRequest;
import org.serviceconnector.web.WebUtil;
import org.serviceconnector.web.cmd.WebCommandException;
import org.serviceconnector.web.cmd.XMLLoaderFactory;
import org.serviceconnector.web.cmd.XSLTTransformerFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;

/**
 * The Class AjaxSystemXMLLoader.
 */
public class AjaxSystemXMLLoader extends AbstractXMLLoader {

	/** The Constant LOGS_FILE_SDF. */
	private static final SimpleDateFormat LOGS_FILE_SDF = new SimpleDateFormat(Constants.LOGS_FILE_NAME_FORMAT);

	/** The Constant LOGGER. */
	public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AjaxSystemXMLLoader.class);

	/** {@inheritDoc} */
	@Override
	public boolean isText() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void loadBody(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String action = request.getParameter("action");
		writer.writeStartElement("system");
		writer.writeStartElement("action");
		if (action != null) {
			writer.writeCData(action);
		}
		writer.writeEndElement(); // action
		try {
			if ("gc".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("run gc");
				System.gc();
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				writer.writeStartElement("messages");
				writer.writeStartElement("message");
				writer.writeCharacters("GC did run.");
				writer.writeEndElement(); // message
				writer.writeEndElement(); // messages
				return;
			}
			if ("enableService".equals(action)) {
				enableService(writer, request);
				return;
			}
			if ("disableService".equals(action)) {
				disableService(writer, request);
				return;
			}
			if ("dump".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("SC dump by user interface");
				try {
					String dumpPath = AppContext.dump();
					writer.writeStartElement("status");
					writer.writeCharacters("success");
					writer.writeEndElement();
					writer.writeStartElement("messages");
					writer.writeStartElement("message");
					writer.writeCharacters("SC dump done.");
					writer.writeEndElement(); // message
					writer.writeStartElement("message");
					writer.writeCharacters(dumpPath);
					writer.writeEndElement(); // message
					writer.writeEndElement(); // messages
				} catch (Exception e) {
					writer.writeStartElement("status");
					writer.writeCharacters("failure");
					writer.writeEndElement();
					writer.writeStartElement("messages");
					writer.writeStartElement("message");
					writer.writeCharacters(e.toString());
					writer.writeEndElement(); // message
					writer.writeEndElement(); // messages
				}
				return;
			}
			if ("terminate".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("SC terminated by user interface");
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				writer.writeStartElement("messages");
				writer.writeStartElement("message");
				writer.writeCharacters("SC has been terminated.");
				writer.writeEndElement(); // message
				writer.writeEndElement(); // messages
				System.exit(1);
			}
			if ("deleteDump".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("delete dump by user interface");
				String dumpPath = AppContext.getBasicConfiguration().getDumpPath();
				DumpUtility.deleteAllDumpFiles(dumpPath);
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				writer.writeStartElement("messages");
				writer.writeStartElement("message");
				writer.writeCharacters("Dump files have been deleted.");
				writer.writeEndElement(); // message
				writer.writeEndElement(); // messages
				return;
			}
			if ("clearCache".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("clear cache by user interface");
				SCCache cache = AppContext.getSCCache();
				cache.clearAll();
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				writer.writeStartElement("messages");
				writer.writeStartElement("message");
				writer.writeCharacters("Cache has been cleared.");
				writer.writeEndElement(); // message
				writer.writeEndElement(); // messages
				return;
			}
			if ("changeLogLevel".equals(action)) {
				changeLogLevel(writer, request);
				return;
			}
			if ("resetTranslet".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("reset translet by user interface");
				XSLTTransformerFactory.getInstance().clearTranslet();
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				writer.writeStartElement("messages");
				writer.writeStartElement("message");
				writer.writeCharacters("Translet have been reset.");
				writer.writeEndElement(); // message
				writer.writeEndElement(); // messages
				return;
			}
			if ("downloadAndReplace".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("download and replace configuration");
				downloadAndReplace(writer, request);
				writer.writeStartElement("status");
				writer.writeCharacters("success");
				writer.writeEndElement();
				return;
			}
			if ("uploadLogFiles".equals(action)) {
				XMLLoaderFactory.LOGGER.debug("upload current log files 1");
				uploadLogAndDumpFiles(writer, request);
				XMLLoaderFactory.LOGGER.debug("upload current log files 2");
				return;
			}
			// action is not valid or unknown
			writer.writeStartElement("status");
			writer.writeCharacters("failure");
			writer.writeEndElement();
			writer.writeStartElement("messages");
			writer.writeStartElement("message");
			writer.writeCharacters("Action [" + action + "] is unknwon");
			writer.writeEndElement(); // message
			writer.writeEndElement(); // messages
		} catch (Exception e) {
			writer.writeStartElement("status");
			writer.writeCharacters("failure");
			writer.writeEndElement();
			writer.writeStartElement("messages");
			writer.writeStartElement("message");
			writer.writeCharacters(e.getMessage());
			writer.writeEndElement(); // message
			writer.writeEndElement(); // messages
		} finally {
			writer.writeEndElement();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void loadBody(Writer writer, IWebRequest request) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Download and replace all selected from remote file server into our current configuration directory. If the same file already exists then the local file content will be
	 * replaced.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void downloadAndReplace(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			throw new WebCommandException("service is missing");
		}
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		Service service = serviceRegistry.getService(serviceName);
		if (service == null) {
			throw new WebCommandException("service " + serviceName + " not found");
		}
		if (service instanceof FileService == false && service instanceof CascadedFileService == false) {
			throw new WebCommandException("service " + serviceName + " is not a file or cascaded file service");
		}
		writer.writeStartElement("service");
		writer.writeCharacters(serviceName);
		writer.writeEndElement();
		List<String> fileList = request.getParameterList("file");
		writer.writeStartElement("messages");
		writer.writeStartElement("message");
		writer.writeCharacters("The following files were downloaded from file service [" + serviceName + "]:");
		writer.writeEndElement();
		if (fileList != null) {
			for (String file : fileList) {
				if (file.startsWith("fs:") && file.endsWith(":fs")) {
					try {
						file = file.substring(3, file.length() - 3);
						String configFileName = SystemInfo.getConfigFileName();
						File configFile = new File(configFileName);
						File localDestinationFile = null;
						if (configFile.isAbsolute()) {
							localDestinationFile = new File(configFile.getParent() + File.separator + file);
						} else {
							URL resourceURL = WebUtil.getResourceURL(configFileName);
							File resourceURLFile = new File(resourceURL.toURI());
							localDestinationFile = new File(resourceURLFile.getParent() + File.separator + file);
						}
						downloadAndReplaceSingleFile(writer, service, file, localDestinationFile);
					} catch (Exception e) {
						writer.writeStartElement("message");
						writer.writeCharacters(file + " did fail, " + e.getMessage());
						writer.writeEndElement();
					}
				} else {
					writer.writeStartElement("message");
					writer.writeCharacters(file + "  invalid format");
					writer.writeEndElement();
				}
			}
		}
		writer.writeEndElement();
	}

	/**
	 * Download and replace a single file.
	 *
	 * @param writer the writer
	 * @param fileServer the file server
	 * @param fileService the file service
	 * @param remoteFile the remote file
	 * @param dstFile the dst file
	 * @throws Exception the exception
	 */
	private void downloadAndReplaceSingleFile(XMLStreamWriter writer, Service service, String remoteFile, File dstFile) throws Exception {
		String status = "successful (copied)";
		if (dstFile.exists()) {
			status = "successful (replaced)";
		}
		FileOutputStream dstStream = null;
		SCClient client = null;
		try {
			// try to connect client
			client = connectClientToService(service);
			SCFileService scFileService = client.newFileService(service.getName());
			dstStream = new FileOutputStream(dstFile);
			scFileService.downloadFile(remoteFile, dstStream);
			writer.writeStartElement("message");
			writer.writeCharacters(dstFile.getName() + "  " + status);
			writer.writeEndElement();
		} catch (Exception e) {
			status = "failed";
			writer.writeStartElement("message");
			writer.writeCharacters(dstFile.getName() + "  " + status);
			writer.writeEndElement();
			throw e;
		} finally {
			if (dstStream != null) {
				try {
					dstStream.close();
				} catch (Exception e) {
				}
			}
			if (client != null) {
				client.detach();
			}
		}
	}

	/**
	 * Upload current log files to remote file server. The file server will be identified by the service name.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void uploadLogAndDumpFiles(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			throw new WebCommandException("service is missing");
		}
		String sdate = request.getParameter("date");
		Date date = DateTimeUtility.getCurrentDate();
		if (sdate != null && sdate.isEmpty() == false) {
			date = WebUtil.getXMLDateFromString(sdate);
		}
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		Service service = serviceRegistry.getService(serviceName);
		if (service == null) {
			throw new WebCommandException("service " + serviceName + " not found");
		}
		if (service instanceof FileService == false && service instanceof CascadedFileService == false) {
			throw new WebCommandException("service " + serviceName + " is not a file or cascaded file service");
		}
		writer.writeStartElement("service");
		writer.writeCharacters(serviceName);
		writer.writeEndElement();
		writer.writeStartElement("date");
		writer.writeCharacters(sdate);
		writer.writeEndElement();
		// get current log files and write them to the remote file server using a stream
		try {
			String logsFileName = this.uploadLogAndDumpFiles(service, serviceName, date);
			// String logsFileName = fileServer.uploadCurrentLogFiles(fileService, serviceName);
			writer.writeStartElement("status");
			writer.writeCharacters("success");
			writer.writeEndElement();
			writer.writeStartElement("messages");
			writer.writeStartElement("message");
			writer.writeCharacters("logs and dump file upload done!");
			writer.writeEndElement();
			writer.writeStartElement("message");
			writer.writeCharacters("file name is " + logsFileName);
			writer.writeEndElement();
			writer.writeEndElement();
		} catch (Exception e) {
			writer.writeStartElement("status");
			writer.writeCharacters("failure");
			writer.writeEndElement();
			writer.writeStartElement("messages");
			writer.writeStartElement("message");
			writer.writeCharacters("logs file upload did fail");
			writer.writeEndElement();
			writer.writeStartElement("message");
			writer.writeCharacters(e.toString());
			writer.writeEndElement();
			writer.writeEndElement();
		}
	}

	/**
	 * Upload current log and dump files.
	 *
	 * @param service the service
	 * @param serviceName the service name
	 * @return the string
	 * @throws Exception the exception
	 */
	private String uploadLogAndDumpFiles(Service service, String serviceName, Date date) throws Exception {
		if (!(service instanceof FileService || service instanceof CascadedFileService)) {
			throw new WebCommandException("upload current log and dump files, service is not a file or cascaded file service");
		}
		SCClient client = null;
		// try to connect client
		client = connectClientToService(service);
		// client gets destroyed at the end of upload inside the uploadLogFiles method
		// has to be inside because the upload works asynchronous
		if (client == null) {
			throw new WebCommandException("upload current log and dump files, client cannot connect and attach to local responder");
		}
		String fileName = this.uploadLogAndDumpFiles(client, service, serviceName, date);
		return fileName;
	}

	/**
	 * Upload log and dump files.
	 *
	 * @param client the client
	 * @param fileService the file service
	 * @param serviceName the service name
	 * @throws Exception the exception
	 */
	private String uploadLogAndDumpFiles(SCClient client, Service service, String serviceName, Date date) throws Exception {
		// get all log and dump file
		List<File> logAndDumpFiles = this.getLogAndDumpFiles(date);
		if (logAndDumpFiles.isEmpty()) {
			throw new FileServerException("upload log and dump files failed, no log or dump files found");
		}
		OutputStream os = null;
		ZipOutputStream zos = null;
		CircularByteBuffer cbb = new CircularByteBuffer();
		String remotePath = this.getUploadLogFileRemotePath(service, serviceName);
		UploadRunnable uploadRunnable = new UploadRunnable(client, remotePath, serviceName, cbb);
		Future<Integer> submit = AppContext.getSCWorkerThreadPool().submit(uploadRunnable);
		uploadRunnable.future = submit;
		@SuppressWarnings("unused")
		Integer ret;
		try {
			os = uploadRunnable.getOutputStream();
			zos = new ZipOutputStream(os);
			for (File file : logAndDumpFiles) {
				String name = file.getName();
				String path = file.getPath();
				// important:
				// get current log file size and stop reading the file after the size reached,
				// this prevents from an endless read when other logs were written in the meantime
				long logFileSize = WebUtil.getResourceSize(path);
				InputStream is = WebUtil.loadResource(path);
				if (is == null) {
					continue;
				}
				ZipEntry entry = null;
				if (name.startsWith(Constants.DUMP_FILE_NAME)) {
					entry = new ZipEntry("dumps/" + name);
					entry.setComment("dump file " + name);
				} else {
					entry = new ZipEntry("logs/" + name);
					entry.setComment("log file " + name);
				}
				zos.putNextEntry(entry);
				try {
					long totalReadBytes = 0L;
					int readBytes = -1;
					byte[] buffer = new byte[Constants.SIZE_64KB];
					while ((readBytes = is.read(buffer)) > 0) {
						if (totalReadBytes >= logFileSize) {
							break;
						}
						zos.write(buffer, 0, readBytes);
						totalReadBytes += readBytes;
					}
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (Exception e) {
							LOGGER.error(e.toString());
						}
					}
				}
				zos.closeEntry();
			}
			zos.close();
		} catch (Exception e) {
			ret = uploadRunnable.close();
			throw e;
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception e) {

				}
			}
		}
		ret = uploadRunnable.close();
		return remotePath;
	}

	/**
	 * Gets the current log file names in a list. Any distinct filenames will be ignored.
	 *
	 * @return the current log file in a list
	 */
	private List<File> getLogAndDumpFiles(Date date) {
		Date today = DateTimeUtility.getCurrentDate();
		Set<String> distinctLoggerSet = new HashSet<String>();
		List<File> logAndDumpFileList = new ArrayList<File>();
		// add log files
		Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		addLogFiles(rootLogger, logAndDumpFileList, distinctLoggerSet, date, today);
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> currentLoggers = loggerContext.getLoggerList();
		for (Logger currentLogger : currentLoggers) {
			Iterator<Appender<ILoggingEvent>> appenders = currentLogger.iteratorForAppenders();
			if (appenders.hasNext()) {
				addLogFiles(currentLogger, logAndDumpFileList, distinctLoggerSet, date, today);
			}
		}
		// add current dump files
		String dumpPath = AppContext.getBasicConfiguration().getDumpPath();
		File[] files = DumpUtility.getDumpFiles(dumpPath);
		for (File file : files) {
			if (FileUtility.belongsToDate(file, date) == false) {
				continue;
			}
			logAndDumpFileList.add(file);
		}
		return logAndDumpFileList;
	}

	/**
	 * Adds the log files for given LOGGER instance to the list. Any distinct file names will be ignored.
	 *
	 * @param logger the LOGGER
	 * @param logFileList the log file list
	 * @param distinctLoggerSet the distinct LOGGER set
	 */
	private void addLogFiles(Logger logger, List<File> logFileList, Set<String> distinctLoggerSet, Date current, Date today) {
		Iterator<Appender<ILoggingEvent>> appenders = logger.iteratorForAppenders();
		while (appenders.hasNext()) {
			Appender appender = appenders.next();
			String appenderName = appender.getName();
			if (distinctLoggerSet.contains(appenderName)) {
				continue;
			}
			distinctLoggerSet.add(appenderName);
			if (appender instanceof FileAppender) {
				FileAppender fileAppender = (FileAppender) appender;
				String sFile = fileAppender.getFile();
				if (current.before(today)) {
					sFile += "." + WebUtil.getXMLDateAsString(current);
				}
				File file = new File(sFile);
				if (file.exists() && file.isFile()) {
					logFileList.add(file);
				}
			}
		}
	}

	/**
	 * Gets the upload log file remote path.
	 *
	 * @param fileService the file service
	 * @param serviceName the service name
	 * @return the upload log file remote path
	 * @throws Exception the exception
	 */
	private String getUploadLogFileRemotePath(Service service, String serviceName) throws Exception {
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		String logsFileName = null;
		synchronized (LOGS_FILE_SDF) {
			String dateTimeString = LOGS_FILE_SDF.format(now);
			String hostName = InetAddress.getLocalHost().getHostName();
			StringBuilder sb = new StringBuilder();
			sb.append(Constants.LOGS_FILE_NAME);
			sb.append(hostName);
			sb.append("_");
			sb.append(serviceName);
			sb.append("_");
			sb.append(dateTimeString);
			sb.append(Constants.LOGS_FILE_EXTENSION);
			logsFileName = sb.toString();
		}
		return logsFileName;
	}

	/**
	 * Enable service.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void enableService(XMLStreamWriter writer, IWebRequest request) throws Exception {
		XMLLoaderFactory.LOGGER.debug("run disable service");
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			this.writeFailure(writer, "Missing service name!");
			return;
		}
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		Service service = serviceRegistry.getService(serviceName);
		if (service == null) {
			this.writeFailure(writer, "Can not enable service " + serviceName + ", not found!");
			return;
		}
		service.setEnabled(true);
		this.writeSuccess(writer, "Service " + serviceName + " has been enabled!");
		return;
	}

	/**
	 * Disable service.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void disableService(XMLStreamWriter writer, IWebRequest request) throws Exception {
		XMLLoaderFactory.LOGGER.debug("run disable service");
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			this.writeFailure(writer, "Missing service name!");
			return;
		}
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		Service service = serviceRegistry.getService(serviceName);
		if (service == null) {
			this.writeFailure(writer, "Can not disable service " + serviceName + ", not found!");
			return;
		}
		service.setEnabled(false);
		this.writeSuccess(writer, "Service " + serviceName + " has been disabled!");
		return;
	}

	/**
	 * Change log level.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception
	 */
	private void changeLogLevel(XMLStreamWriter writer, IWebRequest request) throws Exception {
		XMLLoaderFactory.LOGGER.debug("change log level");
		String logName = request.getParameter("log");
		if (logName == null) {
			this.writeFailure(writer, "Missing log name!");
			return;
		}
		String level = request.getParameter("level");
		if (level == null) {
			this.writeFailure(writer, "Missing log level!");
			return;
		}
		Logger logger = (Logger) LoggerFactory.getLogger(logName);
		if (logger == null) {
			this.writeFailure(writer, "Log name [" + logName + "] is not valid (not found)!");
			return;
		}
		Level newLevel = Level.toLevel(level);
		logger.setLevel(newLevel);
		if ("root".equals(logName)) {
			Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
			if (rootLogger != null) {
				rootLogger.setLevel(newLevel);
			}
		}
		this.writeSuccess(writer, "Log level has been changed! log name = " + logName + ", new level = " + logger.getLevel());
		return;

	}

	/**
	 * The Class UploadRunnable. Needs to be a separate thread if UI wants show a progress bar.
	 */
	public final class UploadRunnable implements Callable<Integer> {

		/** The client. */
		private SCClient client;
		/** The service name. */
		private String serviceName;
		/** The remote path. */
		private String remotePath;
		/** The cbb. */
		private CircularByteBuffer cbb;
		/** input stream */
		private InputStream is;
		/** The future. */
		private Future<Integer> future;

		/**
		 * Instantiates a new upload runnable.
		 *
		 * @param cbb the cbb
		 */
		private UploadRunnable(SCClient client, String remotePath, String serviceName, CircularByteBuffer cbb) {
			this.client = client;
			this.serviceName = serviceName;
			this.remotePath = remotePath;
			this.cbb = cbb;
			this.is = cbb.getInputStream();
			this.future = null;
		}

		/**
		 * Gets the output stream.
		 *
		 * @return the output stream
		 */
		public OutputStream getOutputStream() {
			return this.cbb.getOutputStream();
		}

		/**
		 * Close.
		 *
		 * @return the integer
		 * @throws Exception the exception
		 */
		public Integer close() throws Exception {
			this.cbb.getOutputStream().close();
			return this.future.get(5, TimeUnit.SECONDS);
		}

		/** {@inheritDoc} */
		@Override
		public Integer call() {
			try {
				SCFileService scFileService = client.newFileService(this.serviceName);
				scFileService.uploadFile(360, this.remotePath, this.is);
				// reads buffer intern until the end of output stream
				client.detach();
				return 0;
			} catch (Exception e) {
				LOGGER.error(e.toString());
				try {
					this.close();
				} catch (Exception e1) {
				}
				return -1;
			}
		}
	}

}
