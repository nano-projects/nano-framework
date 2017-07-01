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
package org.nanoframework.concurrent.queue;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 阻塞队列工厂类.
 * @author yanghe
 * @since 1.0
 */
public class BlockingQueueFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockingQueueFactory.class);
    private static BlockingQueueFactory FACTORY;
    private static final Object LOCK = new Object();
    public static final int DEFAULT_QUEUE_SIZE = 10000;

    private ConcurrentMap<String, BlockingQueue<Object>> queueMap;

    private BlockingQueueFactory() {
        queueMap = Maps.newConcurrentMap();
    }

    public static BlockingQueueFactory getInstance() {
        if (FACTORY == null) {
            synchronized (LOCK) {
                if (FACTORY == null) {
                    FACTORY = new BlockingQueueFactory();
                }
            }
        }

        return FACTORY;
    }

    /**
     * 根据Key获取阻塞队列.
     * @param key 队列Key
     * @return 返回阻塞队列
     * @throws RuntimeException 运行时异常
     */
    public BlockingQueue<Object> getQueue(final String key) throws RuntimeException {
        if (!queueMap.containsKey(key)) {
            synchronized (LOCK) {
                if (!queueMap.containsKey(key)) {
                    initQueue(key);
                }
            }
        }

        return queueMap.get(key);
    }

    /**
     * 初始化队列.
     * @param key 队列Key
     * @param size 队列大小
     */
    public void initQueue(final String key, final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("队列大小不能小于等于0, 队列Key: " + key);
        }

        final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(size);
        queueMap.put(key, queue);
        LOGGER.debug("初始化队列: {}, 大小: {}", key, size);
    }

    /**
     * 初始化队列.
     * @param key 队列Key
     */
    public void initQueue(final String key) {
        final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE);
        queueMap.put(key, queue);
        LOGGER.debug("初始化队列: {}, 大小: {} <默认值>", key, DEFAULT_QUEUE_SIZE);
    }

    /**
     * 向工厂中添加队列.
     * @param key 队列Key
     * @param queue 队列
     */
    public void setQueue(final String key, final ArrayBlockingQueue<Object> queue) {
        if (getQueue(key) != null) {
            final BlockingQueue<Object> theQueue = getQueue(key);
            theQueue.addAll(queue);
        } else {
            queueMap.put(key, queue);
        }
    }

    /**
     * 向队列中添加元素.
     * @param key 队列Key
     * @param bean 对象
     * @throws InterruptedException 中断异常
     */
    public void put(final String key, final Object bean) throws InterruptedException {
        getQueue(key).put(bean);
    }

    /**
     * 从队列中获取元素.
     * @param key 队列Key
     * @return 返回对象
     * @throws InterruptedException 中断异常
     */
    public Object take(final String key) throws InterruptedException {
        return getQueue(key).take();
    }

    /**
     * 向队列中添加元素.
     * @param key 队列Key
     * @param bean 对象
     * @return 返回添加结果
     */
    public boolean offer(final String key, final Object bean) {
        return getQueue(key).offer(bean);

    }

    /**
     * 在设定时间内向队列中添加元素，超时抛出中断异常.
     * @param key 队列Key
     * @param bean 对象
     * @param time 超时时间
     * @param unit 时间类型
     * @return 返回添加结果
     * @throws InterruptedException 中断异常
     */
    public boolean offer(final String key, final Object bean, final Long time, final TimeUnit unit) throws InterruptedException {
        return getQueue(key).offer(bean, time, unit);
    }

    /**
     * 从队列中获取元素.
     * @param <T> the value type
     * @param key 队列key
     * @return T
     */
    @SuppressWarnings("unchecked")
    public <T> T poll(final String key) {
        return (T) getQueue(key).poll();
    }

    /**
     * 在设定时间内从队列中获取元素，超时抛出中断异常.
     * @param <T> the value type
     * @param key 队列Key
     * @param time 超时时间
     * @param unit 时间类型
     * @return 返回队列元素
     * @throws InterruptedException 中断异常
     */
    @SuppressWarnings("unchecked")
    public <T> T poll(final String key, final Long time, final TimeUnit unit) throws InterruptedException {
        return (T) getQueue(key).poll(time, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> poll(final String key, final int size, final long time, final TimeUnit unit) {
        final List<T> batch = Lists.newArrayList();
        try {
            while (batch.size() < size) {
                batch.add((T) getQueue(key).poll(time, unit));
            }
        } catch (final InterruptedException e) {
            // ignore
        }

        return batch;
    }

    public static final int howManyElementInQueues() {
        final BlockingQueueFactory factory = getInstance();
        if (factory.queueMap != null) {
            if (factory.queueMap.size() == 0) {
                return 0;
            } else {
                final AtomicInteger size = new AtomicInteger();
                factory.queueMap.values().stream().filter(queue -> queue != null && queue.size() > 0).forEach(queue -> size.addAndGet(queue.size()));
                return size.get();
            }
        }

        return 0;
    }

}
