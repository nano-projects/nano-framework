/**
 * Copyright 2015 the original author or authors.
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
package org.nanoframework.core.chain;

import org.junit.Test;

/**
 * @author yanghe
 * @date 2015年12月3日 下午1:28:05
 */
public class ChainTest {

	@Test
	public void test0() {
		Chain chain0 = new PlusChain();
		Chain chain1 = new SubtractChain();
		Chain chain2 = new MultiplyChain();
		Chain chain3 = new DivideChain();
		
		chain0.setChain(chain1.setChain(chain2.setChain(chain3)));
		System.out.println(chain0.execute(1L));
	}
	
}
