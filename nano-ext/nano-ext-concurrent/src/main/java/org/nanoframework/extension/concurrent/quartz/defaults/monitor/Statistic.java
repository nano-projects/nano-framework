package org.nanoframework.extension.concurrent.quartz.defaults.monitor;

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
 * move to {@link org.nanoframework.extension.concurrent.scheduler.defaults.monitor.Statistic}
 * The next version will be removed
 * 
 * @author yanghe
 * @date 2016年3月23日 上午9:31:58
 */
@Deprecated
public class Statistic {
	private ConcurrentMap<String , AtomicLong> statisticMap = new ConcurrentHashMap<String , AtomicLong>();
	private LinkedBlockingQueue<List<Pointer>> pointerQueue = new LinkedBlockingQueue<List<Pointer>>();
	public static final String TOTAL = "total";
	private boolean isSetPointer = false;
	
	private static ReentrantLock LOCK = new ReentrantLock();
	
	private static Statistic DEFAULT;
	
	private Statistic() { }
	
	public static final Statistic getInstance() {
		if(DEFAULT == null) {
			ReentrantLock lock = LOCK;
			try {
				lock.lock();
				if(DEFAULT == null) {
					DEFAULT = new Statistic();
				}
			} finally {
				lock.unlock();
			}
		}
		
		return DEFAULT;
	}
	
	public synchronized void setMaxPointer(int maxPointer) {
		if(!isSetPointer) {
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
			if(_val != null)
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
		if(_val == null) {
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
			for(Entry<String, AtomicLong> item : statisticMap.entrySet()) {
				pointers.add(Pointer.create(item.getKey(), System.currentTimeMillis(), item.getValue().get() / time));
			}
			
			if(!pointerQueue.offer(pointers)) {
				pointerQueue.poll();
				pointerQueue.offer(pointers);
			}
			
			for(AtomicLong value : statisticMap.values()) {
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
		if(iter.hasNext())
			return iter.next();
		
		return null;
	}
	
}
