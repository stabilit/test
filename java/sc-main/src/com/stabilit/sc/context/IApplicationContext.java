package com.stabilit.sc.context;

import java.util.Properties;

public interface IApplicationContext extends IContext {
	
	public void setArgs(String[] args) throws Exception;
	
	public void setProps(Properties props);
}
