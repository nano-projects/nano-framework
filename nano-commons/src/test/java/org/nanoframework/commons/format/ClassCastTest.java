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
package org.nanoframework.commons.format;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.nanoframework.commons.entity.UseEntity;

import com.alibaba.fastjson.JSON;

/**
 * @author yanghe
 * @date 2015年10月8日 下午2:50:09
 */
public class ClassCastTest {
	@Test
	public void castByStringTest() {
		Object val = ClassCast.cast("1", Integer.class.getName());
		assertEquals(1, val);
		
		val = ClassCast.cast("1", Long.class.getName());
		assertEquals(1L, val);
		
		val = ClassCast.cast("1.0", Double.class.getName());
		assertEquals(1D, val);
		
		val = ClassCast.cast("1.0", Float.class.getName());
		assertEquals(1F, val);
		
		String now = "2015-01-01 12:00:00.000";
		val = ClassCast.cast(String.valueOf(now), Timestamp.class.getName());
		assertEquals(Timestamp.valueOf(now), val);
		
		Map<String, String> map = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L; {
			put("id", "id0");
			put("name", "name0");
		}};
		
		val = ClassCast.cast(JSON.toJSONString(map), UseEntity.class.getName());
		assertEquals(UseEntity.class.getName(), val.getClass().getName());
		assertEquals("id0", ((UseEntity) val).getId());
		assertEquals("name0", ((UseEntity) val).getName());
	}
	
	@Test
	public void castByObjectTest() {
		Object val = ClassCast.cast(new BigDecimal(1), Integer.class.getName());
		assertEquals(1, val);
		
		val = ClassCast.cast(new BigDecimal(1), Long.class.getName());
		assertEquals(1L, val);
		
		val = ClassCast.cast(new BigDecimal(1), Double.class.getName());
		assertEquals(1D, val);
		
		val = ClassCast.cast(new BigDecimal(1), Float.class.getName());
		assertEquals(1F, val);
		
		Date now = new Timestamp(System.currentTimeMillis());
		val = ClassCast.cast(DateFormatUtils.format(now, Pattern.TIMESTAMP.get()), Timestamp.class.getName());
		assertEquals(now.getTime(), ((Timestamp) val).getTime());
	}
	
}
