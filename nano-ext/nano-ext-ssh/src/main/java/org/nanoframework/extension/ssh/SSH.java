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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.nanoframework.extension.ssh.exception.SSHException;

import ch.ethz.ssh2.Connection;

/**
 * @author yanghe
 * @since 1.1
 */
abstract class SSH {
	protected boolean close = false;
	protected static ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	});
	
	protected GanymedSSH ssh;
	protected Connection connection;
	protected BlockingQueue<String> runnable = new LinkedBlockingQueue<>(1);
	protected BlockingQueue<String> callable = new LinkedBlockingQueue<>(1);
	
	public void connect() throws SSHException {
		connection = ssh.connect();
	}
	
	public void disconnect() throws SSHException {
		ssh.disconnect();
		connection = null;
	}
	
	protected void offer(ExecutorType type, String cmd) {
		try {
			if(type == ExecutorType.RUNNABLE)
				this.runnable.offer(cmd, 5000, TimeUnit.MILLISECONDS);
			else if(type == ExecutorType.CALLABLE)
				this.callable.offer(cmd, 5000, TimeUnit.MILLISECONDS);
			
		} catch(InterruptedException e) {
			throw new SSHException("脚本队列已满，添加队列超时");
		}
	}
	
	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
	}
	
	public enum ExecutorType {
		CALLABLE, RUNNABLE;
	}
}
