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
package org.nanoframework.extension.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.nanoframework.commons.util.CollectionUtils;

import io.netty.channel.group.ChannelGroup;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
@Deprecated
public class ChannelGroupSupport {

	public static final ConcurrentMap<String, List<ChannelGroupSupport>> GROUP = new ConcurrentHashMap<>();
	private String key;
	private ChannelGroup group;

	public ChannelGroupSupport(String key, ChannelGroup group) {
		this.key = key;
		this.group = group;

	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ChannelGroup getGroup() {
		return group;
	}

	public void setGroup(ChannelGroup group) {
		this.group = group;
	}

	public static List<ChannelGroupSupport> get(String key) {
		return GROUP.get(key);
	}
	
	public static void put(ChannelGroupSupport group) {
		put(group, false);
	}
	
	public static void put(ChannelGroupSupport group, boolean ifClear) {
		List<ChannelGroupSupport> groups = get(group.getKey());
		if(groups == null)
			groups = new ArrayList<>();
		else if(ifClear) {
			groups.clear();
		}
		
		groups.add(group);
		GROUP.put(group.getKey(), groups);
	}
	
	public static boolean remove(ChannelGroupSupport group) {
		List<ChannelGroupSupport> groups = get(group.getKey());
		if(!CollectionUtils.isEmpty(groups)) {
			if(groups.contains(group))
				return groups.remove(group);
		}
		
		return false;
	}
}
