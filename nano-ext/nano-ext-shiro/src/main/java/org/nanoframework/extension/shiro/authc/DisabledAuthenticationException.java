package org.nanoframework.extension.shiro.authc;

import org.apache.shiro.authc.AuthenticationException;

public class DisabledAuthenticationException extends AuthenticationException {
	private static final long serialVersionUID = -4703042566287167334L;

	/**
     * Creates a new DisabledAuthenticationException.
     */
    public DisabledAuthenticationException() {
        super();
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param message the reason for the exception
     */
    public DisabledAuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public DisabledAuthenticationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DisabledAuthenticationException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public DisabledAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
