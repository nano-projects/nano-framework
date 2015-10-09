package org.nanoframework.commons.exception;

public class StringFormatException extends ExtensionRuntimeException {
	private static final long serialVersionUID = 2104634926671632268L;

	public StringFormatException() {

	}
	
	public StringFormatException(String message) {
		super(message);
		
	}
	
	public StringFormatException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "字符处理异常: " + super.getMessage();
	}
	
}
