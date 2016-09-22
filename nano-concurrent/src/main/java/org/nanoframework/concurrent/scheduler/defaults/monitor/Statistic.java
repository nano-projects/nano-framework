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
package org.nanoframework.concurrent.scheduler.defaults.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * @author yanghe
 * @since 1.3
 */
public class Statistic {
    private ConcurrentMap<String, AtomicLong> statisticMap = new ConcurrentHashMap<String, AtomicLong>();
    private LinkedBlockingQueue<List<Pointer>> pointerQueue = new LinkedBlockingQueue<List<Pointer>>();
    public static final String TOTAL = "total";
    private boolean isSetPointer = false;

    private static ReentrantLock LOCK = new ReentrantLock();

    private static Statistic DEFAULT;

    private Statistic() {
    }

    public static final Statistic getInstance() {
        if (DEFAULT == null) {
            ReentrantLock lock = LOCK;
            try {
                lock.lock();
                if (DEFAULT == null) {
                    DEFAULT = new Statistic();
                }
            } finally {
                lock.unlock();
            }
        }

        return DEFAULT;
    }

    public synchronized void setMaxPointer(int maxPointer) {
        if (!isSetPointer) {
            pointerQueue.clear();
            pointerQueue = new LinkedBlockingQueue<List<Pointer>>(maxPointer);
            isSetPointer = true;
        } else
            throw new IllegalStateException("Can not reset maxPointer again. You can restart application to reset maxPointer.");
    }

    public long incrementAndGet(String scene) {
        final ReentrantLock lock = LOCK;
        try {
            lock.lock();
            AtomicLong _val = statisticMap.get(scene);
            if (_val != null)
                _val.incrementAndGet();
            else {
                _val = new AtomicLong(0L);
                _val.incrementAndGet();
            }

            statisticMap.put(scene, _val);
            return _val.get();
        } finally {
            lock.unlock();
        }
    }

    public long get(String scene) {
        AtomicLong _val = statisticMap.get(scene);
        if (_val == null) {
            _val = new AtomicLong(0L);
            statisticMap.put(scene, _val);
        }

        return _val.get();
    }

    public List<Pointer> setAndGetPointer(int time) {
        final ReentrantLock lock = LOCK;
        try {
            lock.lock();
            List<Pointer> pointers = new ArrayList<Pointer>();
            for (Entry<String, AtomicLong> item : statisticMap.entrySet()) {
                pointers.add(Pointer.create(item.getKey(), System.currentTimeMillis(), item.getValue().get() / time));
            }

            if (!pointerQueue.offer(pointers)) {
                pointerQueue.poll();
                pointerQueue.offer(pointers);
            }

            for (AtomicLong value : statisticMap.values()) {
                value.set(0L);
            }

            statisticMap.clear();

            return pointers;
        } finally {
            lock.unlock();
        }
    }

    public List<Pointer> getPointer() {
        Iterator<List<Pointer>> iter = pointerQueue.iterator();
        if (iter.hasNext())
            return iter.next();

        return null;
    }

}
