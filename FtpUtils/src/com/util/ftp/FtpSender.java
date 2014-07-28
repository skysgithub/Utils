package com.util.ftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * 可同時傳送一個或多個檔案
 * 
 * @author sean
 *
 */
public class FtpSender {

	private FTPClient ftp = new FTPClient();

	private String ipAddress;
	/**
	 * <li>ex: / 根目錄 <li>ex: /abc/abc/ 從根目錄開始 <li>ex: abc/abc/ 從當前目錄開始
	 * */
	private String directoryPath = "";

	private String account = "";

	private String password = "";

	/**
	 * <li>[0]:fileName String <li>[1]:file byte[]
	 * */
	private ArrayList<Object[]> files = new ArrayList<Object[]>();

	private StringBuilder logs = new StringBuilder();

	public FtpSender(String ipAddress, String account, String password) {
		this.ipAddress = ipAddress;
		this.account = account;
		this.password = password;
	}

	/** 須指定目錄用 */
	public FtpSender(String ipAddress, String directoryPath, String account,
			String password) {
		this.ipAddress = ipAddress;
		this.account = account;
		this.password = password;
		this.directoryPath = directoryPath;
	}

	public void setPort(int port) {
		ftp.setDefaultPort(port);
	}

	/**
	 * 加入檔案
	 * 
	 * @param fileName
	 *            含副檔名
	 * @param file
	 * @return
	 */
	public boolean addFile(String fileName, byte[] file) {
		if (fileName == null || fileName.isEmpty())
			return false;
		if (file == null || file.length == 0)
			return false;
		return files.add(new Object[] { fileName, file });
	}

	public void send() {
		login();
		changeDirectory();
		upload();
		logout();
	}

	private void login() {
		try {
			ftp.connect(ipAddress);
			ftp.login(account, password);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			logFTPReply("login success");
		} catch (Exception e) {
			logFTPReply("login failure::" + e.getMessage());
		}
	}

	private boolean changeDirectory() {
		try {
			if (directoryPath.isEmpty())
				return true;
			boolean changed = ftp.changeWorkingDirectory(directoryPath);
			logFTPReply("change directory success");
			return changed;
		} catch (Exception e) {
			logFTPReply("change directory failure::" + e.getMessage());
			return false;
		}
	}

	private void upload() {
		for (Object[] ary : files) {
			String fileName = ary[0].toString();
			byte[] file = (byte[]) ary[1];
			InputStream is = new ByteArrayInputStream(file);
			try {
				ftp.storeFile(fileName, is);
				logFTPReply("upload success::fileName " + fileName);
			} catch (Exception e) {
				logFTPReply("upload failure::fileName " + fileName + " "
						+ e.getMessage());
			}
		}
	}

	private void logout() {
		try {
			ftp.logout();
			ftp.disconnect();
			logFTPReply("logout success");
		} catch (Exception e) {
			logFTPReply("logout failure::" + e.getMessage());
		}
	}

	private void logFTPReply(String info) {
		if (info != null && info.isEmpty() == false)
			logs.append(" \n\n " + info);
		for (String msg : ftp.getReplyStrings())
			logs.append(" \n " + msg);
	}

	public String getLogs() {
		return logs.toString();
	}

}
