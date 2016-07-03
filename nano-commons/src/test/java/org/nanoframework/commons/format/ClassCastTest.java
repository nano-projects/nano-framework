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
package org.nanoframework.commons.format;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    public void castNullTest() {
        final Object value = ClassCast.cast(null, "java.lang.String");
        assertEquals(value, null);
    }
    
    @Test
    public void castStringTest() {
        final Object value = ClassCast.cast("cast", "java.lang.String");
        assertEquals("cast", value);
    }
    
    @Test
    public void castBaseTypeTest() {
        try {
            ClassCast.cast("1", "int");
        } catch (final Throwable e) {
            assertEquals(e instanceof org.nanoframework.commons.exception.ClassCastException, true);
        }
    }
    
    @Test
    public void castDateTest() {
        final Object value = ClassCast.cast("2016-07-03 20:35:00", "java.util.Date");
        assertEquals(value.getClass(), java.util.Date.class);
    }
    
    @Test
    public void castObjectEmptyTypeTest() {
        try {
            ClassCast.cast(1, "");
        } catch (final Throwable e) {
            assertEquals(e instanceof IllegalArgumentException, true);
        }
    }
    
    @Test
    public void castObjectNullTest() {
        final Object value = ClassCast.cast((Object) null, "java.lang.String");
        assertEquals(value, null);
    }
    
    @Test
    public void castObjectIntegerTest() {
        final Object value = ClassCast.cast(1, "java.lang.Integer");
        assertEquals(value instanceof Integer, true);
        
        final Object value2 = ClassCast.cast(new BigDecimal(1), "java.lang.Integer");
        assertEquals(value2 instanceof Integer, true);
        
        final Object value3 = ClassCast.cast(1.5, "java.lang.Integer");
        assertEquals(value3 instanceof Integer, true);
    }
    
    @Test
    public void castObjectIntegerArrayTest() {
        final Object value = ClassCast.cast(new Object[]{1.0, 2L, "0.6", null, "" }, "[Ljava.lang.Integer;");
        assertEquals(value instanceof Integer[], true);
        assertEquals(((Integer[]) value)[0], (Integer) 1);
        assertEquals(((Integer[]) value)[1], (Integer) 2);
        assertEquals(((Integer[]) value)[2], (Integer) 0); 
    }
    
    @Test
    public void castObjectLongTest() {
        final Object value = ClassCast.cast(1L, "java.lang.Long");
        assertEquals(value instanceof Long, true);
        assertEquals((Long) value, (Long) 1L);
        
        final Object value2 = ClassCast.cast(new BigDecimal(1), "java.lang.Long");
        assertEquals(value2 instanceof Long, true);
        assertEquals((Long) value2, (Long) 1L);
        
        final Object value3 = ClassCast.cast(1.5, "java.lang.Long");
        assertEquals(value3 instanceof Long, true);
        assertEquals((Long) value3, (Long) 1L);
    }
    
    @Test
    public void castObjectLongArrayTest() {
        final Object value = ClassCast.cast(new Object[]{1.0, 2, "0.6", null, "" }, "[Ljava.lang.Long;");
        assertEquals(value instanceof Long[], true);
        assertEquals(((Long[]) value)[0], (Long) 1L);
        assertEquals(((Long[]) value)[1], (Long) 2L);
        assertEquals(((Long[]) value)[2], (Long) 0L); 
    }
    
    @Test
    public void castObjectDoubleTest() {
        final Object value = ClassCast.cast(1.5, "java.lang.Double");
        assertEquals(value instanceof Double, true);
        assertEquals((Double) value, (Double) 1.5);
        
        final Object value2 = ClassCast.cast(new BigDecimal(1), "java.lang.Double");
        assertEquals(value2 instanceof Double, true);
        assertEquals((Double) value2, (Double) 1D);
        
        final Object value3 = ClassCast.cast(1L, "java.lang.Double");
        assertEquals(value3 instanceof Double, true);
        assertEquals((Double) value3, (Double) 1D);
    }
    
    @Test
    public void castObjectDoubleArrayTest() {
        final Object value = ClassCast.cast(new Object[]{1.0, 2, "0.6", null, "" }, "[Ljava.lang.Double;");
        assertEquals(value instanceof Double[], true);
        assertEquals(((Double[]) value)[0], (Double) 1D);
        assertEquals(((Double[]) value)[1], (Double) 2D);
        assertEquals(((Double[]) value)[2], (Double) 0.6); 
    }
    
    @Test
    public void castObjectFloatTest() {
        final Object value = ClassCast.cast(1.5F, "java.lang.Float");
        assertEquals(value instanceof Float, true);
        assertEquals((Float) value, (Float) 1.5F);
        
        final Object value2 = ClassCast.cast(new BigDecimal(1.5), "java.lang.Float");
        assertEquals(value2 instanceof Float, true);
        assertEquals((Float) value2, (Float) 1.5F);
        
        final Object value3 = ClassCast.cast(1L, "java.lang.Float");
        assertEquals(value3 instanceof Float, true);
        assertEquals((Float) value3, (Float) 1F);
    }
    
    @Test
    public void castObjectFloatArrayTest() {
        final Object value = ClassCast.cast(new Object[]{1.0, 2, "0.6", null, "" }, "[Ljava.lang.Float;");
        assertEquals(value instanceof Float[], true);
        assertEquals(((Float[]) value)[0], (Float) 1F);
        assertEquals(((Float[]) value)[1], (Float) 2F);
        assertEquals(((Float[]) value)[2], (Float) 0.6F); 
    }
    
    @Test
    public void castObjectBooleanTest() {
        final Object value = ClassCast.cast(true, "java.lang.Boolean");
        assertEquals((boolean) value, true);
     
        final Object value2 = ClassCast.cast("1", "java.lang.Boolean");
        assertEquals((boolean) value2, true);
        
        final Object value3 = ClassCast.cast("YES", "java.lang.Boolean");
        assertEquals((boolean) value3, true);
        
        final Object value4 = ClassCast.cast("0", "java.lang.Boolean");
        assertEquals((boolean) value4, false);
        
        final Object value5 = ClassCast.cast("NO", "java.lang.Boolean");
        assertEquals((boolean) value5, false);
        
        final Object value6 = ClassCast.cast(1, "java.lang.Boolean");
        assertEquals((boolean) value6, false);
    }
    
    @Test
    public void castObjectDateTest() {
        final Object value = ClassCast.cast((Object) "2016-07-03 22:05:00", "java.util.Date");
        assertEquals(value instanceof Date, true);
        
        final Object value2 = ClassCast.cast(new Date(), "java.util.Date");
        assertEquals(value2 instanceof Date, true);
    }
    
    @Test
    public void castObjectTimestampTest() {
        final Object value = ClassCast.cast((Object) "2016-07-03 22:05:00", "java.sql.Timestamp");
        assertEquals(value instanceof Timestamp, true);
        
        final Object value2 = ClassCast.cast(new Date(), "java.sql.Timestamp");
        assertEquals(value2 instanceof Timestamp, true);
    }
    
    @Test
    public void castObjectUnsupportedTest() {
        try {
            ClassCast.cast(1, "int");
        } catch (final Throwable e) {
            assertEquals(e instanceof org.nanoframework.commons.exception.ClassCastException, true);
        }
    }
    
    @Test
    public void castObjectDefaultTest() {
        final Object value = ClassCast.cast((Object) "[\"1\",\"2\"]", "java.util.List");
        assertEquals(value instanceof List, true);
        
        final Object value2 = ClassCast.cast((Object) "[\"1\",\"2\"]", "[Ljava.lang.String;");
        assertEquals(value2 instanceof String[], true);
        
        final Object value3 = ClassCast.cast((Object) new String[]{"[\"1\",\"2\"]", "[1, 2]"}, "java.util.List");
        assertEquals(value3 instanceof Object[], true);
        assertEquals(((Object[]) value3)[0] instanceof List, true);
        assertEquals(((List<?>) ((Object[]) value3)[0]).get(0), "1");
        assertEquals(((List<?>) ((Object[]) value3)[1]).get(0), 1);
        
        final Object value4 = ClassCast.cast((Object) new String[]{"[\"1\",\"2\"]", "[1, 2]"}, "[Ljava.lang.String;");
        assertEquals(value4 instanceof Object[], true);
        assertEquals(((Object[]) value3)[0] instanceof List, true);
        assertEquals(((List<?>) ((Object[]) value3)[0]).get(0), "1");
        assertEquals(((List<?>) ((Object[]) value3)[1]).get(0), 1);
    }
    
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
            private static final long serialVersionUID = 1L;
            {
                put("id", "id0");
                put("name", "name0");
            }
        };

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
