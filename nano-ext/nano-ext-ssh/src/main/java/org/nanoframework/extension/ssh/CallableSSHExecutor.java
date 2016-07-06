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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.commons.util.Charsets;
import org.nanoframework.extension.ssh.exception.SSHException;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author yanghe
 * @since 1.1
 */
abstract class CallableSSHExecutor<V> extends SSH implements Callable<V> {
	protected Logger LOG = LoggerFactory.getLogger(CallableSSHExecutor.class);
	protected final ExecutorType TYPE = ExecutorType.CALLABLE;
	
	protected CallableSSHExecutor(GanymedSSH ssh) {
		Assert.notNull(ssh);
		this.ssh = ssh;
	}
	
	@Override
	public V call() throws Exception {
		return callable();
	}

	@SuppressWarnings("unchecked")
	protected V callable() throws SSHException {
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
    		List<String> lines = new ArrayList<>();
    		String line;
            while(!close && (line = reader.readLine()) != null) {
            	lines.add(line);
            }
              
            LOG.info("Exit");
            return (V) lines;
    	} catch(Exception e) {
			LOG.error("Exec Exception: " + e.getMessage(), e);
			
		} finally {
			if(reader != null) 
				try { reader.close(); } catch(Exception e) { }
			
			if(stdout != null)
				try { stdout.close(); } catch(Exception e) { }
			
			disconnect();
		}
		
		return (V) Collections.emptyList();
	}

	public Future<V> submit() {
		return service.submit(this);
	}
	
	public abstract void addCmd(String cmd);
	public abstract void addCmd(String source, String cmd);
	
	public abstract Future<V> submit(String cmd);
	public abstract Future<V> submit(String source, String cmd);
	
	public abstract Object get(Future<V> future, long timeout, TimeUnit unit, boolean ifTimeoutToClose);
	
}
