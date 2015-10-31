package org.nanoframework.commons.format;

public enum Pattern {
	DATE("yyyy-MM-dd"), TIME("HH:mm:ss"), DATETIME("yyyy-MM-dd HH:mm:ss"), TIMESTAMP("yyyy-MM-dd HH:mm:ss.SSS");

	private String pattern;

	private Pattern(String pattern) {
		this.pattern = pattern;
	}

	public String get() {
		return pattern;
	}

}