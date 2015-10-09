package org.nanoframework.commons.exception;

public class ClassCastException extends ExtensionRuntimeException {
	private static final long serialVersionUID = 3778728285493433413L;
	
	public ClassCastException() {

	}
	
	public ClassCastException(String message) {
		super(message);
		
	}
	
	public ClassCastException(String message, Throwable cause) {
		super(message, cause);
		
	}
	
	@Override
	public String getMessage() {
		return "加载异常: " + super.getMessage();
	}
	
}
