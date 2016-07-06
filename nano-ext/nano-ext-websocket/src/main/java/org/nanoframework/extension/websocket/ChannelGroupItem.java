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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.ObjectCompare;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class ChannelGroupItem {
	private static final Logger LOG = LoggerFactory.getLogger(ChannelGroupItem.class);
	public static final ConcurrentMap<String, ChannelGroupItem> GROUP = new ConcurrentHashMap<>();
	private static final ChannelGroup DEFAULT = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private ConcurrentMap<String, Channel> items;

	public ChannelGroupItem() {
		this.items = new ConcurrentHashMap<>();
	}

	public ConcurrentMap<String, Channel> getItems() {
		return items;
	}
	
	public ChannelGroup getGroup() {
	    ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	    if(!CollectionUtils.isEmpty(items)) {
	        items.forEach((itemId, channel) -> channelGroup.add(channel));
	    }
	    
	    return channelGroup;
	}
	
	public ChannelGroup getGroup(String... itemIds) {
	    if(ArrayUtils.isEmpty(itemIds)) {
	        return DEFAULT;
	    }
	    
	    ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        if(!CollectionUtils.isEmpty(items)) {
            items.entrySet().stream().filter(entry -> ObjectCompare.isInList(entry.getKey(), itemIds)).forEach(entry -> channelGroup.add(entry.getValue()));
        }
        
        return channelGroup;
	}

	public static ChannelGroupItem get(String groupId) {
		return GROUP.get(groupId);
	}
	
	public static void put(String groupId, String itemId, Channel channel) {
		ChannelGroupItem group = get(groupId);
		if(group != null) {
			if(!group.items.containsKey(itemId)) {
				group.items.put(itemId, channel);
			}
		} else {
			group = new ChannelGroupItem();
			group.items.put(itemId, channel);
		}
		
		GROUP.put(groupId, group);
	}
	
	public static void remove(String groupId, String itemId) {
		ChannelGroupItem group = get(groupId);
		if(group != null) {
			if(group.items.remove(itemId) != null) {
				LOG.warn("Remove ChannelGroup Items: {}", itemId);
			}
		}
	}
}
