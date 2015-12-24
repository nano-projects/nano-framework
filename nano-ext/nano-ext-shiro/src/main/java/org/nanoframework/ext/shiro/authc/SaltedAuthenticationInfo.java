package org.nanoframework.ext.shiro.authc;

import org.nanoframework.ext.shiro.util.ByteSource;

public interface SaltedAuthenticationInfo {
	ByteSource getCredentialsSalt();
}
