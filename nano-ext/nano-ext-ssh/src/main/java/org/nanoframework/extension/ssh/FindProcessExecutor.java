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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.Charsets;
import org.nanoframework.extension.ssh.exception.SSHException;
import org.nanoframework.extension.ssh.exception.UnSupportedSSHException;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author yanghe
 * @since 1.1
 */
public final class FindProcessExecutor extends CallableSSHExecutor<Boolean> {
	protected FindProcessExecutor(GanymedSSH ssh) {
		super(ssh);
	}
	
	public static final FindProcessExecutor newInstance(GanymedSSH ssh) {
		return new FindProcessExecutor(ssh);
	}
	
	
	protected Boolean callable() throws SSHException {
		BufferedReader reader = null;
		InputStream stdout = null;
		try {
			String cmd;
			Assert.hasLength(cmd = callable.poll(1000, TimeUnit.MILLISECONDS));
			connect();
    		Session session = connection.openSession();
			session.execCommand(cmd);
			
    		stdout = new StreamGobbler(session.getStdout()); 
            reader = new BufferedReader(new InputStreamReader(stdout, Charsets.UTF_8));
    		String line;
            while(!close && (line = reader.readLine()) != null) {
            	if(LOG.isDebugEnabled()) {
            		LOG.debug(line);
            	}
            	
            	return Boolean.TRUE;
            }
              
            LOG.info("Exit");
    	} catch(Exception e) {
			throw new SSHException(e);
			
		} finally {
			if(reader != null) {
				try { reader.close(); } catch(Exception e) { }
			}
			
			if(stdout != null) {
				try { stdout.close(); } catch(Exception e) { }
			}
			
			disconnect();
		}
		
		return Boolean.FALSE;
	}

	@Override
	public void addCmd(String source, String cmd) {
		throw new UnSupportedSSHException("不支持此调用");
	}

	@Override
	public void addCmd(String cmd) {
		try {
			Integer.parseInt(cmd);
		} catch(NumberFormatException e) {
			throw new SSHException("无效的端口号");
		}
		
		offer(TYPE, "ps -ef | grep " + cmd + " | grep -v grep ");
	}
	
	@Override
	public Future<Boolean> submit(String cmd) {
		addCmd(cmd);
		return submit();
	}

	@Override
	public Future<Boolean> submit(String source, String cmd) {
		throw new UnSupportedSSHException("不支持此调用");
	}

	@Override
	public Boolean get(Future<Boolean> future, long timeout, TimeUnit unit, boolean ifTimeoutToClose)  {
		Assert.notNull(future);
		try {
			return future.get(timeout, unit);
		} catch(InterruptedException | ExecutionException | TimeoutException e) {
			if(ifTimeoutToClose) {
				setClose(true);
				try { return future.get(timeout, unit); } catch(InterruptedException | ExecutionException | TimeoutException  ex) { 
					throw new SSHException(e.getMessage(), e);
				}
			}
		}
		
		return Boolean.FALSE;
	}

}
