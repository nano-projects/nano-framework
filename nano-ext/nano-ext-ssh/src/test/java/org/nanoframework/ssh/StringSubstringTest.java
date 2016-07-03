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
package org.nanoframework.ssh;

import org.junit.Test;

/**
 * @author yanghe
 * @date 2015年8月20日 下午3:24:06
 * @since 1.1
 */
public class StringSubstringTest {

	@Test
	public void test0() {
		String cmd = "/home/yanghe/test/nano-manager-quartz-server-1.0.0-SNAPSHOT/bin/startup.sh";
		String path = cmd.substring(0, cmd.lastIndexOf("/"));
		String file = cmd.substring(path.length(), cmd.length());
		
		System.out.println(path);
		System.out.println(file);
	}
}
