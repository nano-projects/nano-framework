/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.orm.jedis.atomic;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.StringUtils;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;
import org.nanoframework.orm.jedis.RedisClientException;

/**
 * @author yanghe
 * @date 2016年3月24日 上午8:53:21
 */
public class AtomicInteger extends Number implements java.io.Serializable {
	private static final long serialVersionUID = -3659807697944522620L;

	protected final static String DEFAULT_ATOMIC_INTEGER_KEY = "nanoframework.AtomicInteger";
	
	private static final ConcurrentMap<String, ReentrantLock> LOCK = new ConcurrentHashMap<>();
	
	private final RedisClient redisClient;
	
	private final String key;
	
	private final String lockKey;
	
	public AtomicInteger(RedisClient redisClient) {
		this(redisClient, DEFAULT_ATOMIC_INTEGER_KEY);
	}
	
	public AtomicInteger(String redisType) {
		this(GlobalRedisClient.get(redisType), DEFAULT_ATOMIC_INTEGER_KEY);
	}
	
	public AtomicInteger(String redisType, String key) {
		this(GlobalRedisClient.get(redisType), key);
	}
	
	public AtomicInteger(RedisClient redisClient, String key) {
		Assert.notNull(redisClient, "RedisClient must be not null.");
		Assert.hasText(key, "The key must be not empty.");
		this.redisClient = redisClient;
		this.key = key;
		this.lockKey = redisClient.toString() + "@" + key;
		LOCK.putIfAbsent(this.lockKey, new ReentrantLock());
	}
	
    public final int get() {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
    	try {
    		lock.lock();
	    	String value = this.redisClient.get(key);
	    	if(value == null) {
	    		if(!this.redisClient.set(key, 0)) {
	    			throw new RedisClientException("Set newValue to Redis error!");
	    		}
	    		
	    		return 0;
	    	}
	    	
	        return Integer.parseInt(value);
    	} finally {
    		lock.unlock();
    	}
    }

    public final void set(int newValue) {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
    	try {
    		lock.lock();
    		if(!this.redisClient.set(key, newValue)) {
    			throw new RedisClientException("Set newValue to Redis error!");
    		}
    	} finally {
    		lock.unlock();
    	}
    }

    public final int getAndSet(int newValue) {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
    	try {
    		lock.lock();
	    	String value = this.redisClient.getset(key, String.valueOf(newValue));
	    	if(value == null) {
	    		return 0;
	    	}
	    	
	    	return Integer.parseInt(value);
    	} finally {
    		lock.unlock();
    	}
    }

    public final boolean compareAndSet(int expect, int update) {
        final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(StringUtils.equals(value, String.valueOf(expect))) {
        		if(!this.redisClient.set(key, String.valueOf(update))) {
        			throw new RedisClientException("Update value to Redis error!");
        		}
        		
        		return true;
        	}
        	
        	return false;
        } finally {
        	lock.unlock();
        }
    }

    public final int getAndIncrement() {
        final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, 1)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return 0;
        	}
        	
        	int oldValue;
        	if(!this.redisClient.set(key, String.valueOf((oldValue = Integer.parseInt(value)) + 1))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return oldValue;
        } finally { 
        	lock.unlock();
        }
    }

    public final int getAndDecrement() {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, -1)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return 0;
        	}
        	
        	int oldValue;
        	if(!this.redisClient.set(key, String.valueOf((oldValue = Integer.parseInt(value)) - 1))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return oldValue;
        } finally { 
        	lock.unlock();
        }
    }

    public final int getAndAdd(int delta) {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, delta)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return 0;
        	}
        	
        	int oldValue;
        	if(!this.redisClient.set(key, String.valueOf((oldValue = Integer.parseInt(value)) + delta))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return oldValue;
        } finally { 
        	lock.unlock();
        }
    }

    public final int incrementAndGet() {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, 1)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return 1;
        	}
        	
        	int newValue;
        	if(!this.redisClient.set(key, String.valueOf(newValue = Integer.parseInt(value) + 1))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return newValue;
        } finally { 
        	lock.unlock();
        }
    }

    public final int decrementAndGet() {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, -1)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return -1;
        	}
        	
        	int newValue;
        	if(!this.redisClient.set(key, String.valueOf(newValue = Integer.parseInt(value) - 1))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return newValue;
        } finally { 
        	lock.unlock();
        }
    }

    public final int addAndGet(int delta) {
    	final ReentrantLock lock = LOCK.get(this.lockKey);
        try {
        	lock.lock();
        	String value = this.redisClient.get(key);
        	if(value == null) {
        		if(!this.redisClient.set(key, delta)) {
        			throw new RedisClientException("Increment to Redis error!");
        		}
        		
        		return delta;
        	}
        	
        	int newValue;
        	if(!this.redisClient.set(key, String.valueOf(newValue = Integer.parseInt(value) + delta))) {
        		throw new RedisClientException("Increment to Redis error!");
        	}
        	
        	return newValue;
        } finally { 
        	lock.unlock();
        }
    }

    public String toString() {
        return Integer.toString(get());
    }

    public int intValue() {
        return get();
    }

    public long longValue() {
        return (long)get();
    }

    public float floatValue() {
        return (float)get();
    }

    public double doubleValue() {
        return (double)get();
    }

}
