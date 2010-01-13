package com.stabilit.sc.context;

import java.net.URL;

import com.stabilit.sc.util.ConsoleUtil;

public class ClientApplicationContext extends ApplicationContext {
	public ClientApplicationContext() {
	}

	public URL getURL() {
		URL url = (URL) this.getAttribute("url");
		return url;
	}
	
	public String getConnection() {
		return (String)this.getAttribute("con");
	}

	public void setArgs(String[] args) throws Exception {
		super.setArgs(args);
		String sURL = ConsoleUtil.getArg(args, "-url");
		if (sURL == null) {
			sURL = "http://localhost:80/";
		}
		try {
			URL url = new URL(sURL);
			String urlPath = url.getPath();
			if (urlPath == null || urlPath.length() <= 0) {
				sURL += "/";
				url = new URL(sURL);
			}
			this.setAttribute("url", url);
		} catch (Exception e) {
		}
		String con = ConsoleUtil.getArg(args, "-con");
		if (con != null) {
		   this.setAttribute("con", con);
		}
		if (con == null) {
		   throw new Exception("invalid arguments");
		}
	}

}
