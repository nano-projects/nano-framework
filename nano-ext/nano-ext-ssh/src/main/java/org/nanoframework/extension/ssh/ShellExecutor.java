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

import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.extension.ssh.exception.SSHException;

/**
 * @author yanghe
 * @since 1.1
 */
public final class ShellExecutor extends SSHExecutor {
	protected ShellExecutor(GanymedSSH ssh) {
		super(ssh);
	}

	public static final ShellExecutor newInstance(GanymedSSH ssh) {
		return new ShellExecutor(ssh);
	}

	@Override
	public void addCmd(String source, String cmd) {
		Assert.hasLength(cmd);
		if(StringUtils.isNotBlank(source)) {
			if(!source.startsWith("/") || cmd.contains(" ") || cmd.contains(";"))
				throw new SSHException("无效的环境变量路径");
		}
		
		if(!cmd.startsWith("/") || !cmd.endsWith(".sh") || cmd.contains(" ") || cmd.contains(";"))
			throw new SSHException("无效的脚本文件路径");
		
		String path = cmd.substring(0, cmd.lastIndexOf('/'));
		String file = '.' + cmd.substring(path.length(), cmd.length());
		
		offer(TYPE, "source " + source + "; cd " + path + "; sh " + file);
	}

	@Override
	public synchronized void execute(String source, String cmd) {
		addCmd(source, cmd);
		execute();
	}

}
