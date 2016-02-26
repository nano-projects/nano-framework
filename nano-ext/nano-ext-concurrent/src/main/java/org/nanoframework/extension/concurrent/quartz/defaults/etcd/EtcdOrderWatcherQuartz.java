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
package org.nanoframework.extension.concurrent.quartz.defaults.etcd;

import static org.nanoframework.extension.concurrent.quartz.QuartzFactory.DEFAULT_QUARTZ_NAME_PREFIX;
import static org.nanoframework.extension.concurrent.quartz.QuartzFactory.threadFactory;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.util.CollectionUtils;
import org.nanoframework.commons.util.StringUtils;
import org.nanoframework.extension.concurrent.exception.QuartzException;
import org.nanoframework.extension.concurrent.quartz.BaseQuartz;
import org.nanoframework.extension.concurrent.quartz.CronExpression;
import org.nanoframework.extension.concurrent.quartz.QuartzConfig;
import org.nanoframework.extension.concurrent.quartz.QuartzFactory;
import org.nanoframework.extension.concurrent.queue.BlockingQueueFactory;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdKeysResponse.EtcdNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

public class EtcdOrderWatcherQuartz extends BaseQuartz {
	
	public static final String ORDER = EtcdQuartz.DIR + "/Order.list";
	
	private BlockingQueue<Object> nodesQueue = BlockingQueueFactory.getInstance().getQueue(EtcdOrderWatcherQuartz.class.getName());
	
	private final EtcdClient etcd;
	
	private EtcdOrderExecuteQuartz etcdOrderExecuteQuartz;
	
	private EtcdOrderFetchQuartz etcdOrderFetchQuartz;
	
	public EtcdOrderWatcherQuartz(EtcdClient etcd) {
		this.etcd = etcd;
		
		QuartzConfig config = new QuartzConfig();
		config.setId("EtcdOrderWatcherQuartz-0");
		config.setName(DEFAULT_QUARTZ_NAME_PREFIX + "EtcdOrderWatcherQuartz-0");
		config.setGroup("EtcdOrderWatcherQuartz");
		threadFactory.setBaseQuartz(this);
		config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
		config.setTotal(1);
		config.setDaemon(true);
		config.setBeforeAfterOnly(true);
		setConfig(config);
		setClose(false);
	}
	
	@Override
	public void before() throws QuartzException {
		if(etcdOrderExecuteQuartz == null) {
			synchronized (this) {
				if(etcdOrderExecuteQuartz == null) {
					etcdOrderExecuteQuartz = this.new EtcdOrderExecuteQuartz();
					etcdOrderExecuteQuartz.getConfig().getService().execute(etcdOrderExecuteQuartz);
				}
			}
		}
		
		if(etcdOrderFetchQuartz == null) {
			synchronized (this) {
				if(etcdOrderFetchQuartz == null) {
					etcdOrderFetchQuartz = this.new EtcdOrderFetchQuartz();
					etcdOrderFetchQuartz.getConfig().getService().execute(etcdOrderFetchQuartz);
				}
			}
		}
	}

	@Override
	public void execute() throws QuartzException {
		try {
			etcd.get(ORDER).recursive().sorted().waitForChange().send().get();
			etcdOrderFetchQuartz.active();
			
		} catch (Exception e) {
			LOG.error("waitForChange error: " + e.getMessage());
			return ;
		} 
		
	}
	
	@Override
	public void after() throws QuartzException {

	}

	@Override
	public void destroy() throws QuartzException {

	}
	
	private class EtcdOrderFetchQuartz extends BaseQuartz {
		private boolean active = false;
		private int count = 0;
		
		public EtcdOrderFetchQuartz() {
			QuartzConfig config = new QuartzConfig();
			config.setId("EtcdOrderFetchQuartz-0");
			config.setName(DEFAULT_QUARTZ_NAME_PREFIX + "EtcdOrderFetchQuartz-0");
			config.setGroup("EtcdOrderFetchQuartz");
			threadFactory.setBaseQuartz(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			config.setTotal(1);
			config.setDaemon(true);
			try { config.setCron(new CronExpression("* * * * * ?")); } catch (ParseException e) { }
			setConfig(config);
			
		}
		
		@Override
		public void before() throws QuartzException { }

		@Override
		public void execute() throws QuartzException {
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
		public void after() throws QuartzException { }

		@Override
		public void destroy() throws QuartzException { }
		
	}

	private class EtcdOrderExecuteQuartz extends BaseQuartz {
		private TypeReference<EtcdOrder> type = new TypeReference<EtcdOrder>() {};
		private QuartzFactory FACTORY = QuartzFactory.getInstance();
		private String value;
		
		public EtcdOrderExecuteQuartz() {
			QuartzConfig config = new QuartzConfig();
			config.setId("EtcdOrderExecuteQuartz-0");
			config.setName(DEFAULT_QUARTZ_NAME_PREFIX + "EtcdOrderExecuteQuartz-0");
			config.setGroup("EtcdOrderExecuteQuartz");
			threadFactory.setBaseQuartz(this);
			config.setService((ThreadPoolExecutor) Executors.newFixedThreadPool(1, threadFactory));
			config.setTotal(1);
			config.setDaemon(true);
			setConfig(config);
			setClose(false);
		}
		
		@Override
		public void before() throws QuartzException {
			try {
				value = (String) nodesQueue.poll(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) { }
		}

		@Override
		public void execute() throws QuartzException {
			if(!StringUtils.isEmpty(value)) {
				try {
					EtcdOrder order = JSON.parseObject(CryptUtil.decrypt(value, EtcdQuartz.SYSTEM_ID), type);
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
								FACTORY.removeQuartz(FACTORY.find(order.getId()));
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
		public void after() throws QuartzException {
			value = null;
		}

		@Override
		public void destroy() throws QuartzException {
			
		}
		
	}
}
