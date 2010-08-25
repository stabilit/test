package com.stabilit.scm.common.log;

/**
 * The Enum Loggers. All available Loggers beside class loggers in SCM.
 */
public enum Loggers {

	CONNECTION("connection"), //
	SESSION("session");

	/** The value. */
	private String value;

	/**
	 * Instantiates a new logger.
	 * 
	 * @param value
	 *            the value
	 */
	private Loggers(String value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
