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
package org.nanoframework.jmx.client.management;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.nanoframework.commons.util.Assert;
import org.nanoframework.jmx.client.JmxClient;
import org.nanoframework.jmx.client.exception.AttributeException;
import org.nanoframework.jmx.client.exception.InvokeException;
import org.nanoframework.jmx.client.exception.MXBeanException;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public abstract class AbstractMXBean {
	public static final String CONNECT_REFUSED = "Connection refused: connect";
	
	protected JmxClient client;
	protected MBeanServerConnection connection;
	protected ObjectName objectName;
	
	public void init(JmxClient client, String objectName) {
		Assert.notNull(client);
		Assert.notNull(objectName);
		
		this.client = client;
		this.connection = client.getConnection();
		
		try {
			this.objectName = new ObjectName(objectName);
		} catch(MalformedObjectNameException e) {
			throw new MXBeanException(e.getMessage(), e);
		}
	}
	
	public void init(JmxClient client, ObjectName objectName) {
		Assert.notNull(client);
		Assert.notNull(objectName);
		
		this.client = client;
		this.connection = client.getConnection();
		this.objectName = objectName;
	}
	
	public Set<ObjectName> queryNames() {
		try {
			Assert.notNull(connection, CONNECT_REFUSED);
			Assert.notNull(objectName);
			return connection.queryNames(objectName, null);
		} catch(IOException e) {
			throw new AttributeException(e.getMessage(), e);
		}
	}
	
	public Object getAttribute0(String attribute) throws AttributeException {
		try {
			Assert.notNull(connection, CONNECT_REFUSED);
			Assert.notNull(objectName);
			Assert.hasLength(attribute);
			return connection.getAttribute(objectName, attribute);
		} catch (AttributeNotFoundException | InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
			throw new AttributeException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String attribute) throws AttributeException {
		return (T) getAttribute0(attribute);
	}
	
	public List<Attribute> getAttributes0(String... attributes) throws AttributeException {
		try {
			Assert.notNull(connection, CONNECT_REFUSED);
			Assert.notNull(objectName);
			Assert.notEmpty(attributes);
			AttributeList attrList = connection.getAttributes(objectName, attributes);
			if(attrList != null)
				return attrList.asList();
			
			return Collections.emptyList();
		} catch (InstanceNotFoundException | ReflectionException | IOException e) {
			throw new AttributeException(e.getMessage(), e);
		}
	}
	
	public Map<String, Object> getAttributes(String... attributes) throws AttributeException {
		List<Attribute> attributeList = getAttributes0(attributes);
		Map<String, Object> values = new HashMap<>();
		attributeList.forEach(attribute -> values.put(attribute.getName(), attribute.getValue()));
		return values;
	}
	
	public void setAttribute(String name, Object value) {
		try {
			Assert.hasLength(name);
			connection.setAttribute(objectName, new Attribute(name, value));
		} catch(IOException | InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException e) {
			throw new AttributeException(e.getMessage(), e);
		}
	}
	
	public void setAttribute(AttributeList attributeList) {
		try {
			Assert.notEmpty(attributeList);
			connection.setAttributes(objectName, attributeList);
		} catch(IOException | InstanceNotFoundException | ReflectionException e) {
			throw new AttributeException(e.getMessage(), e);
		}
	}
	
	public Object invoke0(String operationName, Object[] params, String[] signature) throws InvokeException {
		try {
			Assert.notNull(connection, CONNECT_REFUSED);
			Assert.notNull(objectName);
			Assert.hasLength(operationName);
			return connection.invoke(objectName, operationName, params, signature);
		} catch (InstanceNotFoundException | MBeanException | ReflectionException | IOException e) {
			throw new InvokeException(e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T invoke(String operationName, Object[] params, String[] signature) throws InvokeException {
		return (T) invoke0(operationName, params, signature);
	}
	
	public Object invoke0(String operationName) throws InvokeException {
		return invoke0(operationName, null, null);
	}
	
	public <T> T invoke(String operationName) throws InvokeException {
		return invoke(operationName, null, null);
	}
	
	public void close() {
		Assert.notNull(client);
		if(!client.isClosed()) {
			client.close();
			connection = null;
		}
	}
	
	public void connect() {
		Assert.notNull(client);
		connection = client.getConnection();
	}
	
	public void reConnect() {
		close();
		connect();
	}
	
}
