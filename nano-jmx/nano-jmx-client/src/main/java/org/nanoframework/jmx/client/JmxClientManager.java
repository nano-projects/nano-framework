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
package org.nanoframework.jmx.client;

import static org.nanoframework.jmx.client.JmxClient.DEFAULT_CONTENT;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.jmx.client.exception.MXBeanException;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class JmxClientManager {
	private static ConcurrentMap<String, JmxClient> jmxClientMap = new ConcurrentHashMap<>();
	
	public static final JmxClient get(String address) {
		return get(address, DEFAULT_CONTENT);
	}
	
	public static final JmxClient get(String address, long timeout) {
		return get(address, DEFAULT_CONTENT, timeout);
	}
	
	public static final JmxClient get(String address, String content) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		JmxClient client = jmxClientMap.get(address + '/' + content);
		if(client == null) {
			String[] addr = address.split(":");
			if(addr.length != 2) throw new MXBeanException("无效的Key");
			try {
				put(address, content, client = new JmxClient(addr[0], Integer.valueOf(addr[1]), content));
			} catch(NumberFormatException e) {
				throw new MXBeanException("无效的Key");
			}
		} else if(client.isClosed()) {
			client.connect();
		}
		
		return client;
	}
	
	public static final JmxClient get(String address, String content, long timeout) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		JmxClient client = jmxClientMap.get(address + '/' + content);
		if(client == null) {
			String[] addr = address.split(":");
			if(addr.length != 2) throw new MXBeanException("无效的Key");
			try {
				put(address, content, client = new JmxClient(addr[0], Integer.valueOf(addr[1]), content, timeout));
			} catch(NumberFormatException e) {
				throw new MXBeanException("无效的Key");
			}
		} else if(client.isClosed()) {
			client.connect(timeout);
		}
		
		return client;
	}
	
	public static final JmxClient put(String address, String content, JmxClient jmxClient) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		Assert.notNull(jmxClient);
		return jmxClientMap.put(address + '/' + content, jmxClient);
	}
	
	public static final JmxClient putIfAbsent(String address, String content, JmxClient jmxClient) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		Assert.notNull(jmxClient);
		return jmxClientMap.putIfAbsent(address + '/' + content, jmxClient);
	}
	
	public static final JmxClient put(String address, JmxClient jmxClient) {
		return put(address, DEFAULT_CONTENT, jmxClient);
	}
	
	public static final JmxClient putIfAbsent(String address, JmxClient jmxClient) {
		return putIfAbsent(address, DEFAULT_CONTENT, jmxClient);
	}
	
	public static final JmxClient remove(String address, String content) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		return jmxClientMap.remove(address + '/' + content);
	}
	
	public static final JmxClient remove(String address) {
		return remove(address, DEFAULT_CONTENT);
	}
	
	public static final boolean remove(String address, String content, JmxClient jmxClient) {
		Assert.hasLength(address);
		Assert.hasLength(content);
		Assert.notNull(jmxClient);
		return jmxClientMap.remove(address + '/' + content, jmxClient);
	}
	
	public static final boolean remove(String address, JmxClient jmxClient) {
		return remove(address, DEFAULT_CONTENT, jmxClient);
	}
	
}
