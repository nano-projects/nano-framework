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

/**
 * 
 * @author yanghe
 * @date 2016年3月22日 下午5:14:25
 */
public interface EtcdSchedulerOperate {
	final EtcdSchedulerOperate EMPTY = new EtcdSchedulerOperate() {
		@Override
		public void stopping(String group, String id) { }
		
		@Override
		public void stopped(String group, String id, boolean isRemove) { }
		
		@Override
		public void start(String group, String id) { }
		
	};
	
	public void start(String group, String id);
	public void stopping(String group, String id);
	public void stopped(String group, String id, boolean isRemove);
}
