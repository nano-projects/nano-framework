package org.nanoframework.extension.shiro.authc;

import org.nanoframework.extension.shiro.util.ByteSource;

public interface SaltedAuthenticationInfo {
	ByteSource getCredentialsSalt();
}
