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
package org.nanoframework.orm.jedis;

import java.util.List;
import java.util.Set;

import org.nanoframework.commons.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 * @author yanghe
 * @since 1.3.10
 */
public class RedisClientExtImpl extends RedisClientImpl {
    private final String specSource = config.getExtendProperties().getProperty("redis.specSource");
    
    public RedisClientExtImpl(String type) {
        super(type);
    }
    
    public RedisClientExtImpl(RedisConfig config) {
        super(config);
    }
    
    public RedisClient getSpecSource() {
        return GlobalRedisClient.get(specSource);
    }

    @Override
    public List<String> lrange(String key, int start, int end) {
        Set<String> linkedSet = Sets.newLinkedHashSet();
        
        List<String> range = super.lrange(key, start, end);
        if(!CollectionUtils.isEmpty(range)) {
            range.forEach(item -> linkedSet.add(item));
        }
        
        if(this.specSource != null) {
            range = this.getSpecSource().lrange(key, start, end);
            range.forEach(item -> linkedSet.add(item));
        }
        
        return Lists.newArrayList(linkedSet.iterator());
    }
    
    @Override
    public long lrem(String key, int count, String value) {
        long rem = super.lrem(key, count, value);
        rem += getSpecSource().lrem(key, count, value);
        return rem;
    }
    
    @Override
    public boolean push(String key, String[] values, Mark push) {
        boolean success = super.push(key, values, push);
        return success && getSpecSource().push(key, values, push);
    }
    
}
