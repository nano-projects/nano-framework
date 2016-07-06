/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.ssh;

import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.extension.ssh.exception.SSHException;

import ch.ethz.ssh2.Connection;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class GanymedSSH {
	private String host;
	private Integer port;
	private String username;
	private String passwd;
	private ThreadLocal<Connection> connection = new ThreadLocal<>();

	private GanymedSSH(String host, String username, String passwd) {
		Assert.hasLength(host);
		Assert.hasLength(username);
		Assert.hasLength(passwd);
		this.host = host;
		this.username = username;
		this.passwd = CryptUtil.decrypt(passwd);
	}

	private GanymedSSH(String host, Integer port, String username, String passwd) {
		this(host, username, passwd);
		this.port = port;

	}
	
	public static final GanymedSSH newInstance(String host, String username, String passwd) {
		return new GanymedSSH(host, username, passwd);
	}
	
	public static final GanymedSSH newInstance(String host, Integer port, String username, String passwd) {
		return new GanymedSSH(host, port, username, passwd);
	}

	protected Connection connect() throws SSHException {
		if (port != null)
			connection.set(new Connection(host, port));
		else
			connection.set(new Connection(host));

		try {
			connection.get().connect();
			boolean isAuthenticated = connection.get().authenticateWithPassword(username, passwd);
			if (isAuthenticated == false) {
				connection.get().close();
				connection = null;
				throw new SSHException("登陆远程服务器失败");
			}
			
			return connection.get();
		} catch (Exception e) {
			throw new SSHException("连接远程服务器失败：" + e.getMessage(), e);
		}
	}
	
	protected void disconnect() throws SSHException {
		if(connection.get() != null) {
			connection.get().close();
			connection.remove();
		}
	}

}
