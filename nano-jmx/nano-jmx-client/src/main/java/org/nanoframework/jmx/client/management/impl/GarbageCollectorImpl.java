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
package org.nanoframework.jmx.client.management.impl;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;

import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.management.AbstractMXBean;
import org.nanoframework.jmx.client.management.GarbageCollectorMXBean;
import org.nanoframework.jmx.client.management.ObjectNames;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public class GarbageCollectorImpl extends AbstractMXBean implements GarbageCollectorMXBean {
	public static final String OBJECT_NAME = ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE;
	public static final String DEFAULT_OBJECT_NAME = OBJECT_NAME + ObjectNames.Other;
	public static final String NAME = "Name";
	public static final String VALID = "Valid";
	public static final String MEMORY_POOL_NAMES = "MemoryPoolNames";
	public static final String COLLECTION_COUNT = "CollectionCount";
	public static final String COLLECTION_TIME = "CollectionTime";
	
	public GarbageCollectorImpl(JmxClient client) {
		init(client, OBJECT_NAME);
	}
	
	public GarbageCollectorImpl(JmxClient client, ObjectNames name) {
		init(client, OBJECT_NAME + "," + name);
	}
	
	public GarbageCollectorImpl(JmxClient client, ObjectName objectName) {
		this.client = client;
		this.connection = client.getConnection();
		this.objectName = objectName;
	}
	
	@Override
	public ObjectName getObjectName() {
		return objectName;
	}

	@Override
	public String getName() {
		return getAttribute(NAME);
	}

	@Override
	public boolean isValid() {
		return getAttribute(VALID);
	}

	@Override
	public String[] getMemoryPoolNames() {
		return getAttribute(MEMORY_POOL_NAMES);
	}
	
	@Override
	public long getCollectionCount() {
		return getAttribute(COLLECTION_COUNT);
	}

	@Override
	public long getCollectionTime() {
		return getAttribute(COLLECTION_TIME);
	}

}
