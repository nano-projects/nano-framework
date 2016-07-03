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
package org.nanoframework.jmx.agent;

import javax.management.ObjectInstance;

import org.nanoframework.jmx.agent.mbean.HelloWorld;

import com.alibaba.fastjson.JSON;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:05:43
 */
public class JmxAgentTest {
	
	public static void main(String... args) {
		ObjectInstance instance = JmxAgentFactory.register(new HelloWorld(), "org.nanoframework:type=HelloWorld");
		System.out.println(("Object Instance: " + JSON.toJSONString(instance)));
	}
}
