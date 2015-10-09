package org.nanoframework.commons.exception;

public class DateFormatException extends RuntimeException {
	private static final long serialVersionUID = 4883777612565681931L;

	public DateFormatException() {
		super();
	}

	public DateFormatException(String message) {
		super(message);
	}

	public DateFormatException(Throwable cause) {
		super(cause);
	}

	public DateFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String getMessage() {
		return "时间转换异常: " + super.getMessage();
	}
}