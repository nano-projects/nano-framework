/**
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.concurrent.scheduler.defaults.etcd;

import static org.nanoframework.extension.concurrent.scheduler.SchedulerFactory.DEFAULT_SCHEDULER_NAME_PREFIX;
import static org.nanoframework.extension.concurrent.scheduler.SchedulerFactory.threadFactory;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.concurrent.queue.BlockingQueueFactory;
import org.nanoframework.extension.concurrent.scheduler.BaseScheduler;
import org.nanoframework.extension.concurrent.scheduler.CronExpression;
import org.nanoframework.extension.concurrent.scheduler.SchedulerConfig;
import org.nanoframework.extension.concurrent.scheduler.SchedulerFactory;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

/**
 * 
 * @author yanghe
 * @date 2016年3月22日 下午5:13:33
 */
public class EtcdOrderWatcherScheduler extends BaseScheduler {
	
	public static final String ORDER = EtcdScheduler.DIR + "/Order.list";
	
	private BlockingQueue<Object> nodesQueue = BlockingQueueFactory.getInstance().getQueue(EtcdOrderWatcherScheduler.class.getName());
	
	private final EtcdClient etcd;
	
	private EtcdOrderExecuteScheduler etcdOrderExecuteScheduler;
	
	private EtcdOrderFetchScheduler etcdOrderFetchScheduler;
	
	public EtcdOrderWatcherScheduler(EtcdClient etcd) {
		this.etcd = etcd;
		
		SchedulerConfig config = new SchedulerConfig();
		config.setId("EtcdOrderWatcherScheduler-0");
		config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "EtcdOrderWatcherScheduler-0");
		config.setGroup("EtcdOrderWatcherScheduler");
		threadFactory.setBaseScheduler(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
		config.setTotal(1);
		config.setDaemon(Boolean.TRUE);
		config.setBeforeAfterOnly(Boolean.TRUE);
		setConfig(config);
		setClose(false);
	}
	
	@Override
	public void before() {
		if(etcdOrderExecuteScheduler == null) {
			synchronized (this) {
				if(etcdOrderExecuteScheduler == null) {
					etcdOrderExecuteScheduler = this.new EtcdOrderExecuteScheduler();
					etcdOrderExecuteScheduler.getConfig().getService().execute(etcdOrderExecuteScheduler);
				}
			}
		}
		
		if(etcdOrderFetchScheduler == null) {
			synchronized (this) {
				if(etcdOrderFetchScheduler == null) {
					etcdOrderFetchScheduler = this.new EtcdOrderFetchScheduler();
					etcdOrderFetchScheduler.getConfig().getService().execute(etcdOrderFetchScheduler);
				}
			}
		}
	}

	@Override
	public void execute() {
		try {
			etcd.get(ORDER).recursive().sorted().waitForChange().send().get();
			etcdOrderFetchScheduler.active();
			
		} catch (Exception e) {
			LOG.error("waitForChange error: " + e.getMessage());
			return ;
		} 
		
	}
	
	@Override
	public void after() {

	}

	@Override
	public void destroy() {

	}
	
	private class EtcdOrderFetchScheduler extends BaseScheduler {
		private boolean active = false;
		private int count = 0;
		
		public EtcdOrderFetchScheduler() {
			SchedulerConfig config = new SchedulerConfig();
			config.setId("EtcdOrderFetchScheduler-0");
			config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "EtcdOrderFetchScheduler-0");
			config.setGroup("EtcdOrderFetchScheduler");
			threadFactory.setBaseScheduler(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			config.setTotal(1);
			config.setDaemon(Boolean.TRUE);
			config.setBeforeAfterOnly(Boolean.TRUE);
			try { config.setCron(new CronExpression("* * * * * ?")); } catch (ParseException e) { }
			setConfig(config);
			
		}
		
		@Override
		public void before() { }

		@Override
		public void execute() {
			if(active) {
				try {
					EtcdKeysResponse response = etcd.get(ORDER).sorted().send().get();
					List<EtcdNode> nodes = response.node.nodes;
					if(!CollectionUtils.isEmpty(nodes)) {
						for(EtcdNode node : nodes) {
							fetch(node);
						}
					}
				} catch(Exception e) {
					LOG.error("get Order Error: {}", e.getMessage());
				} 
				
				count ++;
			}
			
			if(count == 3) {
				active = false;
				count = 0;
				thisWait();
			}
		}
		
		private void fetch(EtcdNode node) {
			if(node != null) {
				nodesQueue.add(node.value);
				try {
					etcd.delete(node.key).send().get();
				} catch(Exception e) {
					LOG.error("Delete Order.list item error: " + e.getMessage());
				}
			}
		}
		
		public void active() {
			this.count = 0;
			this.active = true;
			thisNotify();
		}

		@Override
		public void after() { }

		@Override
		public void destroy() { }
		
	}

	private class EtcdOrderExecuteScheduler extends BaseScheduler {
		private TypeReference<EtcdOrder> type = new TypeReference<EtcdOrder>() {};
		private SchedulerFactory FACTORY = SchedulerFactory.getInstance();
		private String value;
		
		public EtcdOrderExecuteScheduler() {
			SchedulerConfig config = new SchedulerConfig();
			config.setId("EtcdOrderExecuteScheduler-0");
			config.setName(DEFAULT_SCHEDULER_NAME_PREFIX + "EtcdOrderExecuteScheduler-0");
			config.setGroup("EtcdOrderExecuteScheduler");
			threadFactory.setBaseScheduler(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			config.setTotal(1);
			config.setDaemon(Boolean.TRUE);
			setConfig(config);
			setClose(false);
		}
		
		@Override
		public void before() {
			try {
				value = (String) nodesQueue.poll(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) { }
		}

		@Override
		public void execute() {
			if(!StringUtils.isEmpty(value)) {
				try {
					EtcdOrder order = JSON.parseObject(CryptUtil.decrypt(value, EtcdScheduler.SYSTEM_ID), type);
					if(order != null && order.valid()) {
						switch(order.getAction()) {
							case APPEND: 
								FACTORY.append(order.getGroup(), order.getSize(), order.getAutoStart());
								break;
							case START: 
								FACTORY.start(order.getId());
								break;
							case START_GROUP: 
								FACTORY.startGroup(order.getGroup());
								break;
							case START_ALL: 
								FACTORY.startAll();
								break;
							case STOP: 
								FACTORY.close(order.getId());
								break;
							case STOP_GROUP:
								FACTORY.closeGroup(order.getGroup());
								break;
							case STOP_ALL:
								FACTORY.closeAll();
								break;
							case REMOVE: 
								FACTORY.removeScheduler(FACTORY.find(order.getId()));
								break;
							case REMOVE_GROUP: 
								FACTORY.removeGroup(order.getGroup());
								break;
								
						}
						
						
					}
				} catch(Exception e) {
					LOG.error("Order process error: " + e.getMessage());
				}
			}
		}

		@Override
		public void after() {
			value = null;
		}

		@Override
		public void destroy() {
			
		}
		
	}
}
