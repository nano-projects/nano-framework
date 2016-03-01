/**
 * Copyright 2015- the original author or authors.
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
package org.nanoframework.orm.jedis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;
import com.google.inject.ImplementedBy;


/**
 * 针对Jedis池的使用而基础的封装，主要针对Sharding模式进行接口API的定义<br>
 * 如果Hosts配置为单节点时则无需特别注意，但是配置了多个节点时，
 * 则无法使用部分功能，这些功能主要因分布式而无法保证使用，
 * 但是部分操作可以进行遍历Sharding节点来进行操作，这也是可以满足操作的<br>
 * 
 * @author yanghe
 * @date 2015年6月11日 下午9:53:41 
 *
 */
@ImplementedBy(RedisClientImpl.class)
public interface RedisClient {
	public enum Mark {
		/** 从列表的左端读取元素 */
		LPOP, 
		
		/** 从列表的右端读取元素 */
		RPOP, 
		
		/** 将元素写入列表的左端 */
		LPUSH, 
		
		/** 将元素写入列表的右端 */
		RPUSH, 
		
		/** linsert position on before */
		BEFORE, 
		
		/** linsert position on after */
		AFTER,
		
		/** PUSH列表时的策略，以KEY为基准 */
		KEY,
		
		/** PUSH列表时的策略，以VALUE为基准 */
		VALUE;
	}
	
	/** 默认分隔符 */
	String DEFAULT_SEPARATOR = ",";
	
	/** 部分操作的返回结果，表示操作成功 */
	String OK = "OK";
	
	/** 部分操作的返回结果，表示操作成功 */
	long SUCCESS = 1;
	
	String INF0 = "-inf";
	String INF1 = "+inf";
	
	/**
	 * 删除给定的一个或多个 key 。
	 * 
	 * @param keys key动态数组
	 */
	public long del(String... keys);
	
	/**
	 * 删除给定列表的所有key 。
	 * @param keys key列表
	 */
	public long del(List<String> keys);
	
	/**
	 * 检查给定 key 是否存在。
	 * 
	 * @param key 散列Key
	 * @return 如果存在则返回true，否则返回false
	 */
	public boolean exists(String key);
	
	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * 
	 * @param key 散列Key
	 * @param seconds 过期时间，单位：秒
	 * @return 设置成功返回 1 。当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
	 */
	public long expire(String key, int seconds);
	
	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * 使用RedisConfig中的过期时间进行设置
	 * 
	 * @param key 散列key
	 * @return 设置成功返回 1 。当 key 不存在或者不能为 key 设置生存时间时(比如在低于 2.1.3 版本的 Redis 中你尝试更新 key 的生存时间)，返回 0 。
	 * 
	 * @see RedisConfig#getExpireTime()
	 */
	public long expire(String key);
	
	/**
	 * 为给定 key 设置生存时间，当 key 过期时(生存时间为 0 )，它会被自动删除。
	 * 
	 * @param key 散列Key
	 * @param timestamp 过期时间，时间戳，自动将毫秒制转换成秒制
	 * @return 如果生存时间设置成功，返回 1 。当 key 不存在或没办法设置生存时间，返回 0 。
	 */
	public long expireat(String key, long timestamp);
	
	/**
	 * 以秒为单位，返回给定 key 的剩余生存时间(TTL, time to live)。
	 * 
	 * @param key 元素Key
	 * @return 当 key 不存在时，返回 -2 。<br>
	 * 当 key 存在但没有设置剩余生存时间时，返回 -1 。<br>
	 * 否则，以秒为单位，返回 key 的剩余生存时间。
	 * 
	 */
	public long ttl(String key);
	
	/**
	 * 查找所有符合给定模式 pattern 的 key 。
	 * 
	 * @param pattern 匹配规则
	 * @return 符合给定模式的 key 列表。
	 */
	public Set<String> keys(String pattern);
	
	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
	 * 使用默认分隔符进行分隔，默认分隔符：","
	 * 
	 * @param key 散列Key
	 * @param value 需要进行添加的内容
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	public long append(String key, String value);
	
	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
	 * 使用指定的分隔符进行分隔<br>
	 * 
	 * @param key 散列Key
	 * @param value 需要添加的内容
	 * @param separator 分隔符
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	public long append(String key, String value, String separator);
	
	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
	 * 存入的value值自动转换为JSON字符串<br>
	 * 使用指定的分隔符进行分隔<br>
	 * 
	 * @param key 散列Key
	 * @param value 需要添加的内容对象
	 * @param separator 分隔符
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	public long append(String key, Object value, String separator);
	
	/**
	 * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
	 * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
	 * 使用默认分隔符进行分隔，默认分隔符：","
	 * 
	 * @param key 散列Key
	 * @param value 需要添加的内容对象
	 * @return 追加 value 之后， key 中字符串的长度。
	 */
	public long append(String key, Object value);
	
	/**
	 * 返回 key 所关联的字符串值。
	 * 
	 * @param key散列Key
	 * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
	 */
	public String get(String key);
	
	/**
	 * 返回 key 所关联的字符串值，并且通过FastJson转换为泛型所对应的数据类型。<br>
	 * 如果类型转换不匹配是则返回一个错误
	 * 
	 * @param key 散列Key
	 * @param type 对象类型转换
	 * @return当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
	 */
	public <T> T get(String key, TypeReference<T> type);
	
	/**
	 * 返回所有(一个或多个)给定 key 的值。
	 * 
	 * @param keys 散列Key动态数组
	 * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
	 */
	public Map<String, String> get(String... keys);
	
	/**
	 * 返回所有(一个或多个)给定 key 的值。
	 * 
	 * @param keys 散列Key的数组
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
	 */
	public <T> Map<String, T> get(String[] keys, TypeReference<T> type);
	
	/**
	 * 返回所有(一个或多个)给定 key 的值。
	 * 
	 * @param keys 散列Key的List集合
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
	 */
	public <T> Map<String, T> get(List<String> keys, TypeReference<T> type);
	
	/**
	 * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。当 key 存在但不是字符串类型时，返回一个错误。
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 返回给定 key 的旧值。当 key 没有旧值时，也即是， key 不存在时，返回 null 。
	 */
	public String getset(String key, String value);
	
	/**
	 * 将字符串值 value 关联到 key 。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。<br>
	 * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。<br>
	 * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
	 * 
	 */
	public boolean set(String key, String value);
	
	/**
	 * 将对象 value 关联到 key 。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。<br>
	 * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。<br>
	 * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
	 */
	public boolean set(String key, Object value);
	
	/**
	 * 同时设置一个或多个 key-value 对。
	 * 
	 * @param map 散列Key-Value映射表
	 * @return 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。<br>
	 * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。<br>
	 * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
	 */
	public Map<String, Boolean> set(Map<String, Object> map);
	
	/**
	 * 将字符串值 value 关联到 key, 当且仅当给定 key 不存在时.
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 设置成功，返回 1 。设置失败，返回 0 。
	 */
	public long setByNX(String key, String value);
	
	/**
	 * 将对象 value 关联到 key, 当且仅当给定 key 不存在时.
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 设置成功，返回 1 。设置失败，返回 0 。
	 */
	public long setByNX(String key, Object value);
	
	/**
	 * 同时设置一个或多个 key-value 对, 当且仅当所有给定 key 都不存在。
	 * 即使只有一个给定 key 已存在， MSETNX 也会拒绝执行所有给定 key 的设置操作。
	 * 
	 * @param map 散列Key-Value映射表
	 * @return 设置成功，返回 1 。设置失败，返回 0 。返回Key与结果对应的映射表
	 * 
	 */
	public Map<String, Long> setByNX(Map<String, Object> map);
	
	/**
	 * 将字符串值 value 关联到 key, 使用默认的时间进行生命周期的设置。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
	 */
	public boolean setByEX(String key, String value);
	
	/**
	 * 将字符串值 value 关联到 key, 使用默认的时间进行生命周期的设置。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
	 */
	public boolean setByEX(String key, Object value);
	
	/**
	 * 将字符串值 value 关联到 key，并设置以秒为单位的生命周期。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @param seconds 时间(秒)
	 * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
	 */
	public boolean setByEX(String key, String value, int seconds);
	
	/**
	 * 将字符串值 value 关联到 key，并设置以秒为单位的生命周期。
	 * 
	 * @param key 散列Key
	 * @param value 散列值
	 * @param seconds 时间(秒)
	 * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
	 */
	public boolean setByEX(String key, Object value, int seconds);
	
	/**
	 * 返回 key 所储存的字符串值的长度。当 key 储存的不是字符串值时，返回一个错误。
	 * 
	 * @param key 散列Key
	 * @return 字符串值的长度。当 key 不存在时，返回 0 。
	 */
	public long strLen(String key);
	
	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
	 * 
	 * @param key 哈希表Key
	 * @param fields 哈希域动态数组
	 * @return 被成功移除的域的数量，不包括被忽略的域。
	 */
	public long hdel(String key, String... fields);
	
	/**
	 * 查看哈希表 key 中，给定域 field 是否存在。
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @return 如果哈希表含有给定域，返回 true 。如果哈希表不含有给定域，或 key 不存在，返回 false 。
	 */
	public boolean hexists(String key, String field);
	
	/**
	 * 返回哈希表 key 中给定域 field 的值。
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
	 */
	public String hget(String key, String field);
	
	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。<br>
	 * 如果给定的域不存在于哈希表，那么返回一个 nil 值。<br>
	 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。<br>
	 * 
	 * @param key 哈希表Key
	 * @param fields 哈希域
	 * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
	 */
	public Map<String, String> hmget(String key, String... fields);
	
	/**
	 * 返回哈希表 key 中给定域 field 的值。并将值转换成泛型对象。
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @param type TypeReference的泛型对象
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
	 */
	public <T> T hget(String key, String field, TypeReference<T> type);
	
	/**
	 * 返回哈希表 key 中，一个或多个给定域的值。<br>
	 * 如果给定的域不存在于哈希表，那么返回一个 nil 值。<br>
	 * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。<br>
	 * 
	 * @param key 哈希表Key
	 * @param fields 哈希域数组
	 * @param type TypeReference的泛型对象
	 * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
	 */
	public <T> Map<String, T> hmget(String key, String[] fields, TypeReference<T> type);
	
	/**
	 * 返回哈希表 key 中，所有的域和值。<br>
	 * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
	 * 
	 * @param key 哈希表Key
	 * @return 以Map形式返回哈希表的域和域的值。若 key 不存在，返回空Map。
	 */
	public Map<String, String> hgetAll(String key);
	
	/**
	 * 返回哈希表 key 中，所有的域和值。<br>
	 * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
	 * 
	 * @param key 哈希表Key
	 * @param type TypeReference的泛型对象
	 * @return 以Map形式返回哈希表的域和域的值。若 key 不存在，返回空Map。
	 */
	public <T> Map<String, T> hgetAll(String key, TypeReference<T> type);
	
	/**
	 * 返回哈希表 key 中的所有域。
	 * 
	 * @param key 哈希表Key
	 * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
	 */
	public Set<String> hkeys(String key);
	
	public <T> Set<T> hkeys(String key, TypeReference<T> type);
	
	/**
	 * 返回哈希表 key 中域的数量。
	 * 
	 * @param key 哈希表Key
	 * @return 哈希表中域的数量。当 key 不存在时，返回 0 。
	 */
	public long hlen(String key);
	
	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。<br>
	 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。<br>
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。<br>
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @param value 数据内容
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 true 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 false 。
	 * 
	 */
	public boolean hset(String key, String field, String value);
	
	/**
	 * 将哈希表 key 中的域 field 的值设为 value 。<br>
	 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。<br>
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖。<br>
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @param value 数据对象内容
	 * @return 如果 field 是哈希表中的一个新建域，并且值设置成功，返回 true 。如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 false 。
	 */
	public boolean hset(String key, String field, Object value);
	
	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中。<br>
	 * 此命令会覆盖哈希表中已存在的域。<br>
	 * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。<br>
	 * 
	 * @param key 哈希表Key
	 * @param map 数据对象映射表
	 * @return Return OK or Exception if hash is empty
	 */
	public boolean hmset(String key, Map<String, Object> map);
	
	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。<br>
	 * 若域 field 已经存在，该操作无效。<br>
	 * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。<br>
	 * 
	 * @param key 哈希表Key
	 * @param field 哈希域
	 * @param value 数据内容
	 * @return 设置成功，返回 true 。如果给定域已经存在且没有操作被执行，返回 false 。
	 */
	public boolean hsetByNX(String key, String field, String value);
	
	/**
	 * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。<br>
	 * 若域 field 已经存在，该操作无效。<br>
	 * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。<br>
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return 设置成功，返回 true 。如果给定域已经存在且没有操作被执行，返回 false 。
	 */
	public boolean hsetByNX(String key, String field, Object value);
	
	/**
	 * 同时将多个 field-value (域-值)对设置到哈希表 key 中 ，当且仅当域 field 不存在。<br>
	 * 若域 field 已经存在，该操作无效。<br>
	 * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。<br>
	 * 
	 * @param key
	 * @param map
	 * @return 设置成功，返回 true 。如果给定域已经存在且没有操作被执行，返回 false 。
	 */
	public Map<String, Boolean> hsetByNX(String key, Map<String, Object> map);
	
	/**
	 * 返回哈希表 key 中所有域的值。
	 * 
	 * @param key 哈希表Key
	 * @return 一个包含哈希表中所有值的表。当 key 不存在时，返回一个空表。
	 */
	public List<String> hvals(String key);
	
	public <T> List<T> hvals(String key, TypeReference<T> type);
	
	/**
	 * BLPOP/BRPOP 是列表的阻塞式(blocking)弹出原语。<br>
	 * 它是 LPOP/RPOP 命令的阻塞版本，当给定列表内没有任何元素可供弹出的时候，连接将被 BLPOP/BRPOP 命令阻塞，直到等待超时或发现可弹出元素为止。<br>
	 * 当给定多个 key 参数时，按参数 key 的先后顺序依次检查各个列表，弹出第一个非空列表的头元素。<br>
	 * <br>
	 * 非阻塞行为 <br>
	 * 当 BLPOP/BRPOP 被调用时，如果给定 key 内至少有一个非空列表，那么弹出遇到的第一个非空列表的头元素，并和被弹出元素所属的列表的名字一起，组成结果返回给调用者。<br>
	 * 当存在多个给定 key 时， BLPOP/BRPOP 按给定 key 参数排列的先后顺序，依次检查各个列表。<br>
	 * 假设现在有 job 、 command 和 request 三个列表，其中 job 不存在， command 和 request 都持有非空列表。考虑以下命令：<br>
	 * BLPOP job command request 0<br>
	 * BLPOP 保证返回的元素来自 command ，因为它是按”查找 job -> 查找 command -> 查找 request “这样的顺序，第一个找到的非空列表。<br>
	 * 
	 * Timeout默认为0
	 * 
	 * @param key 列表Key
	 * @param pop 读取方式
	 * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
	 * 
	 * @see #LPOP
	 * @see #RPOP
	 */
	public String bpop(String key, Mark pop);
	
	/**
	 * 
	 * @param key 列表Key
	 * @param pop 读取方式
	 * @param type TypeReference的泛型对象
	 * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
	 * 
	 * @see #bpop(String, Mark)
	 */
	public <T> T bpop(String key, Mark pop, TypeReference<T> type);
	
	/**
	 * 
	 * @param key 列表Key
	 * @param timeout 超时时间(秒)
	 * @param pop 读取方式
	 * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
	 * 
	 * @see #bpop(String, Mark)
	 */
	public String bpop(String key, int timeout, Mark pop);
	
	/**
	 * 
	 * @param key 列表Key
	 * @param timeout 超时时间(秒)
	 * @param pop 读取方式
	 * @param type TypeReference的泛型对象
	 * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
	 */
	public <T> T bpop(String key, int timeout, Mark pop, TypeReference<T> type);
	
	/**
	 * 
	 * @param keys 列表Key的数组
	 * @param pop 读取方式
	 * @return 返回被弹出元素的非空值与列表Key的映射表
	 * 
	 * @see #bpop(String, Mark)
	 */
	public Map<String, String> bpop(String[] keys, Mark pop);
	
	/**
	 * 
	 * @param keys 列表Key的数组
	 * @param pop 读取方式
	 * @param type TypeReference的泛型对象
	 * @return 返回被弹出元素的非空值与列表Key的映射表
	 * 
	 * @see #bpop(String, Mark)
	 */
	public <T> Map<String, T> bpop(String[] keys, Mark pop, TypeReference<T> type);
	
	/**
	 * 
	 * @param keys 列表Key的数组
	 * @param timeout 超时时间(秒)
	 * @param pop 读取方式
	 * @return 返回被弹出元素的非空值与列表Key的映射表
	 * 
	 * @see #bpop(String, Mark)
	 */
	public Map<String, String> bpop(String[] keys, int timeout, Mark pop);
	
	/**
	 * 
	 * @param keys 列表Key的数组
	 * @param timeout 超时时间(秒)
	 * @param pop 读取方式
	 * @param type TypeReference的泛型对象
	 * @return 返回被弹出元素的非空值与列表Key的映射表
	 * 
	 * @see #bpop(String, Mark)
	 */
	public <T> Map<String, T> bpop(String[] keys, int timeout, Mark pop, TypeReference<T> type);
	
	/**
	 * BRPOPLPUSH 是 RPOPLPUSH 的阻塞版本，当给定列表 source 不为空时， BRPOPLPUSH 的表现和 RPOPLPUSH 一样。<br>
	 * 当列表 source 为空时， BRPOPLPUSH 命令将阻塞连接，直到等待超时，或有另一个客户端对 source 执行 LPUSH 或 RPUSH 命令为止。<br>
	 * 超时参数 timeout 接受一个以秒为单位的数字作为值。超时参数设为 0 表示阻塞时间可以无限期延长(block indefinitely) 。<br>
	 * Timeout默认为0
	 * 
	 * @param source
	 * @param destination
	 * @return
	 */
	public String brpoplpush(String source, String destination);
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @param type
	 * @return
	 * 
	 * @see #brpoplpush(String, String)
	 */
	public <T> T brpoplpush(String source, String destination, TypeReference<T> type);
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @param timeout
	 * @return
	 * 
	 * @see #brpoplpush(String, String)
	 */
	public String brpoplpush(String source, String destination, int timeout);
	
	/**
	 * 
	 * @param source
	 * @param destination
	 * @param timeout
	 * @param type
	 * @return
	 * 
	 * @see #brpoplpush(String, String)
	 */
	public <T> T brpoplpush(String source, String destination, int timeout, TypeReference<T> type);
	
	/**
	 * 返回列表 key 中，下标为 index 的元素。<br>
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
	 * 如果 key 不是列表类型，返回一个错误。<br>
	 * 
	 * @param key
	 * @param index
	 * @return
	 */
	public String lindex(String key, int index);
	
	/**
	 * 
	 * @param key
	 * @param index
	 * @param type
	 * @return
	 * 
	 * @see #lindex(String, int)
	 */
	public <T> T lindex(String key, int index, TypeReference<T> type);
	
	/**
	 * LINSERT key BEFORE|AFTER pivot value <br>
	 * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。<br>
	 * 当 pivot 不存在于列表 key 时，不执行任何操作。<br>
	 * 当 key 不存在时， key 被视为空列表，不执行任何操作。<br>
	 * 如果 key 不是列表类型，返回一个错误。<br>
	 * 
	 * @param key
	 * @param pivot
	 * @param value
	 * @param position
	 * @return 
	 */
	public long linsert(String key, String pivot, String value, Mark position);
	
	/**
	 * 
	 * @param key
	 * @param pivot
	 * @param value
	 * @param position
	 */
	public long linsert(String key, String pivot, Object value, Mark position);
	
	/**
	 * LLEN key <br>
	 * 返回列表 key 的长度。 <br>
	 * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 . <br>
	 * 如果 key 不是列表类型，返回一个错误。
	 * 
	 * @param key
	 * @return
	 */
	public long llen(String key);
	
	/**
	 * 移除并返回列表 key 的头元素。
	 * 
	 * @param key
	 * @return
	 */
	public String pop(String key, Mark pop);
	
	/**
	 * 移除并返回列表 key 的{count}个元素。<br>
	 * {count < 0}时使用RPOP，{count >= 0}时使用LPOP
	 * 
	 * @param key 列表Key
	 * @param count 数量
	 * @param pop 标记位
	 * @return
	 */
	public List<String> pop(String key, int count);
	
	/**
	 * 移除并返回列表 key 的头元素。
	 * 
	 * @param key 列表Key
	 * @param type 泛型对象
	 * @return
	 */
	public <T> T pop(String key, Mark pop, TypeReference<T> type);
	
	/**
	 * 移除并返回列表 key 的{count}个元素。<br>
	 * {count < 0}时使用RPOP，{count >= 0}时使用LPOP
	 * 
	 * @param key 列表Key
	 * @param count 数量
	 * @param pop 标记位
	 * @param type 泛型对象
	 * @return
	 */
	public <T> List<T> pop(String key, int count, TypeReference<T> type);
	
	/**
	 * LPUSH/RPUSH key value [value ...] <br>
	 * 将一个或多个值 value 插入到列表 key 的表头 <br>
	 * 如果有多个 value 值，那么各个 value 值按从左到右的顺序依次插入到表头：  <br>
	 * 比如说，对空列表 mylist 执行命令 LPUSH/RPUSH mylist a b c ，列表的值将是 c b a ，这等同于原子性地执行 LPUSH/RPUSH mylist a 、 
	 * LPUSH/RPUSH mylist b 和 LPUSH mylist c 三个命令。
	 * 如果 key 不存在，一个空列表会被创建并执行 LPUSH/RPUSH 操作。当 key 存在但不是列表类型时，返回一个错误。 <br>
	 * 
	 * @param key
	 * @param values
	 * @param push
	 * @return 
	 */
	public boolean push(String key, String[] values, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param values
	 * @param push
	 */
	public boolean push(String key, String value, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param values
	 * @param push
	 * 
	 * @see #push(String, String[], Mark)
	 */
	public boolean push(String key, Object[] values, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param push
	 */
	public boolean push(String key, Object value, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param values
	 * @param push
	 * @return
	 */
	public boolean push(String key, List<Object> values, Mark push);
	
	/**
	 * 根据策略(policy)存储列表(key-scanKey或者key-value)与散列(scanKey-value) <br>
	 * 根据写入方式(push)存储列表数据 <br>
	 * 
	 * @param key 列表Key
	 * @param scanKey 散列Key
	 * @param value 元素值
	 * @param push 写入方式(Mark.LPUSH / Mark.RPUSH)
	 * @param policy 策略(Mark.KEY / Makr.VALUE)
	 * @return 如果写入成功则返回true， 写入失败返回false
	 */
	public boolean push(String key, String scanKey, String value, Mark push, Mark policy);
	
	/**
	 * 根据策略(policy)存储列表(key-scanKey或者key-value)与散列(scanKey-value) <br>
	 * 根据写入方式(push)存储列表数据 <br>
	 * 
	 * @param key 列表Key
	 * @param scanKey 散列Key
	 * @param value 元素值
	 * @param push 写入方式(Mark.LPUSH / Mark.RPUSH)
	 * @param policy 策略(Mark.KEY / Makr.VALUE)
	 * @return 如果写入成功则返回true， 写入失败返回false
	 */
	public boolean push(String key, String scanKey, Object value, Mark push, Mark policy);
	
	/**
	 * 根据策略(policy)存储列表(key-scanKey或者key-value)与散列(scanKey-value)
	 * 根据写入方式(push)存储列表数据
	 * 
	 * @param key 列表Key
	 * @param scanMap 散列Key-Value集合
	 * @param push 写入方式(Mark.LPUSH / Mark.RPUSH)
	 * @param policy 策略(Mark.KEY / Makr.VALUE)
	 * @return 如果写入成功则返回true， 写入失败返回false
	 */
	public Map<String, Boolean> push(String key, Map<String, Object> scanMap, Mark push, Mark policy);
	
	/**
	 * 
	 * @param key
	 * @param values
	 * @param push
	 */
	public void pushx(String key, String[] values, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param push
	 */
	public void pushx(String key, String value, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param values
	 * @param push
	 */
	public void pushx(String key, Object[] values, Mark push);
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param push
	 */
	public void pushx(String key, Object value, Mark push);
	
	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。<br>
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
	 * 
	 * @param key 列表Key
	 * @param start 起始位置
	 * @param end 结束位置
	 * @return
	 */
	public List<String> lrange(String key, int start, int end);
	
	/**
	 * 返回列表 key 中0到count之间的所有的元素。
	 * 
	 * @param key 列表Key
	 * @param count 数量
	 * @return
	 */
	public List<String> lrange(String key, int count);
	
	/**
	 * 返回列表 key 中所有的元素
	 * 
	 * @param key 列表Key
	 * @return
	 */
	public List<String> lrange(String key);
	
	/**
	 * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。<br>
	 * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
	 * 
	 * @param key 列表Key
	 * @param start 起始位置
	 * @param end 结束位置
	 * @param type 泛型对象
	 * @return
	 * 
	 */
	public <T> List<T> lrange(String key, int start, int end, TypeReference<T> type);
	
	/**
	 * 返回列表 key 中0到count之间的所有的元素。
	 * 
	 * @param key 列表Key
	 * @param count 数量
	 * @param type 泛型对象
	 * @return
	 */
	public <T> List<T> lrange(String key, int count, TypeReference<T> type);
	
	/**
	 * 
	 * @param key
	 * @param type
	 * @return
	 */
	public <T> List<T> lrange(String key, TypeReference<T> type);
	
	/**
	 * 列表队列的读取方法
	 * 
	 * @param key
	 * @param count
	 * @return
	 */
	public List<String> lrangeltrim(String key, int count);
	
	/**
	 * 列表队列的读取方法
	 * @param key
	 * @param count
	 * @param type
	 * @return
	 */
	public <T> List<T> lrangeltrim(String key, int count, TypeReference<T> type);
	
	/**
	 * 根据参数 count 的值，移除列表中与参数 value 相等的元素。<br>
	 * count 的值可以是以下几种：<br>
	 * count > 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。<br>
	 * count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。<br>
	 * count = 0 : 移除表中所有与 value 相等的值。<br>
	 * 
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 */
	public long lrem(String key, int count, String value);
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 * 
	 * @see #lrem(String, int, String)
	 */
	public long lrem(String key, String value);
	
	/**
	 * 
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 * 
	 * @see #lrem(String, int, String)
	 */
	public long lrem(String key, int count, Object value);
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 * 
	 * @see #lrem(String, int, String)
	 */
	public long lrem(String key, Object value);
	
	/**
	 * 将列表 key 下标为 index 的元素的值设置为 value 。<br>
	 * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。<br>
	 * 
	 * @param key
	 * @param index
	 * @param value
	 * @return 
	 */
	public String lset(String key, int index, String value);
	
	/**
	 * 
	 * @param key
	 * @param index
	 * @param value
	 * @param type
	 * 
	 * @see #lset(String, int, String)
	 */
	public <T> T lset(String key, int index, T value, TypeReference<T> type);
	
	/**
	 * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
	 * 
	 * @param key
	 * @param start
	 * @param stop
	 * @return 
	 */
	public String ltrim(String key, int start, int stop);
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。<br>
	 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。<br>
	 * 当 key 不是集合类型时，返回一个错误。<br>
	 * 
	 * @param key 集合Key
	 * @param members 元素值动态数组
	 * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
	 */
	public long sadd(String key, String... members);
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。<br>
	 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。<br>
	 * 当 key 不是集合类型时，返回一个错误。<br>
	 * 
	 * @param key 集合Key
	 * @param members 元素对象动态数组
	 * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
	 */
	public long sadd(String key, Object... members);
	
	/**
	 * 替换原有的值
	 * 
	 * @param key 集合Key
	 * @param oldMembers 旧的元素
	 * @param newMembers 新的元素
	 * @return
	 */
	public long sreplace(String key, String[] oldMembers, String[] newMembers);
	
	/**
	 * 
	 * @param key
	 * @param oldMembers
	 * @param newMembers
	 * @return
	 */
	public long sreplace(String key, Object[] oldMembers, Object[] newMembers);
	
	/**
	 * 
	 * @param key
	 * @param oldMembers
	 * @param newMembers
	 * @return
	 */
	public long sreplace(String key, Collection<Object> oldMembers, Collection<Object> newMembers);
	
	/**
	 * 返回集合 key 的基数(集合中元素的数量)。
	 * 
	 * @param key 集合Key
	 * @return 集合的基数。当 key 不存在时，返回 0 。
	 */
	public long scard(String key);
	
	/**
	 * 返回集合 key 的基数(集合中元素的数量)。
	 * 
	 * @param keys 集合Key动态数组
	 * @return 集合的基数。当 key 不存在时，返回 0 。
	 */
	public Map<String, Long> scard(String... keys);
	
	/**
	 * 返回集合 key 的基数(集合中元素的数量)。
	 * @param keys 集合Key列表
	 * @return 集合的基数。当 key 不存在时，返回 0 。
	 */
	public Map<String, Long> scard(Collection<String> keys);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
	 * 
	 * @param keys 集合Key动态数组
	 * @return 一个包含差集成员的列表。
	 */
	public Set<String> sdiff(String... keys);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
	 * 
	 * @param keys 集合Key动态数组
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 一个包含差集成员的列表。
	 */
	public <T> Set<T> sdiff(String[] keys, TypeReference<T> type);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
	 * 
	 * @param keys 集合Key动态数组
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 一个包含差集成员的列表。
	 */
	public <T> Set<T> sdiff(Collection<String> keys, TypeReference<T> type);
	
	/**
	 * 这个命令的作用和 SDIFF 类似，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
	 * 如果 destination 集合已经存在，则将其覆盖。<br>
	 * destination 可以是 key 本身。<br>
	 * 
	 * @param destination 目标集合Key
	 * @param keys 集合Key动态数组
	 * @return 结果集中的元素数量。
	 */
	public long sdiffstore(String destination, String... keys);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
	 * 不存在的 key 被视为空集。<br>
	 * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
	 * @param keys 集合Key动态数组
	 * @return 交集成员的列表。
	 */
	public Set<String> sinter(String... keys);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
	 * 不存在的 key 被视为空集。<br>
	 * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
	 * @param keys 集合Key数组
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 交集成员的列表。
	 */
	public <T> Set<T> sinter(String[] keys, TypeReference<T> type);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
	 * 不存在的 key 被视为空集。<br>
	 * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
	 * @param keys 集合Key列表
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 交集成员的列表。
	 */
	public <T> Set<T> sinter(Collection<String> keys, TypeReference<T> type);
	
	/**
	 * 这个命令类似于 SINTER 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
	 * 如果 destination 集合已经存在，则将其覆盖。<br>
	 * destination 可以是 key 本身。<br>
	 * 
	 * @param destination 目标集合Key
	 * @param keys 集合Key动态数组
	 * @return 结果集中的成员数量。
	 */
	public long sinterstore(String destination, String... keys);
	
	/**
	 * 判断 member 元素是否集合 key 的成员。
	 * 
	 * @param key 集合Key
	 * @param member 元素值
	 * @return 如果 member 元素是集合的成员，返回 true 。如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
	 */
	public boolean sismember(String key, String member);
	
	/**
	 * 判断 member 元素是否集合 key 的成员。
	 * 
	 * @param key 集合Key
	 * @param member 元素对象
	 * @return 如果 member 元素是集合的成员，返回 true 。如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
	 */
	public boolean sismember(String key, Object member);
	
	/**
	 * 返回集合 key 中的所有成员。不存在的 key 被视为空集合。
	 * 
	 * @param key 集合Key
	 * @return 集合中的所有成员。
	 */
	public Set<String> smembers(String key);
	
	/**
	 * 返回集合 key 中的所有成员。不存在的 key 被视为空集合。
	 * 
	 * @param key 集合Key
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 集合中的所有成员。
	 */
	public <T> Set<T> smembers(String key, TypeReference<T> type);
	
	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。 
	 * 
	 * @param source 源集合Key
	 * @param destination 目标集合Key
	 * @param member 元素值
	 * @return 如果 member 元素被成功移除，返回 true 。<br>
	 * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
	 */
	public boolean smove(String source, String destination, String member);
	
	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。
	 * 
	 * @param source 源集合Key
	 * @param destination 目标集合Key
	 * @param members 元素值动态数组
	 * @return 如果 member 元素被成功移除，返回 true 。<br>
	 * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
	 */
	public Map<String, Boolean> smove(String source, String destination, String... members);
	
	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。
	 * 
	 * @param source 源集合Key
	 * @param destination 目标集合Key
	 * @param members 元素对象
	 * @return 如果 member 元素被成功移除，返回 true 。<br>
	 * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
	 */
	public boolean smove(String source, String destination, Object member);
	
	/**
	 * 将 member 元素从 source 集合移动到 destination 集合。
	 * 
	 * @param source 源集合Key
	 * @param destination 目标集合Key
	 * @param members 元素对象动态数组
	 * @return 如果 member 元素被成功移除，返回 true 。<br>
	 * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
	 */
	public Map<Object, Boolean> smove(String source, String destination, Object... members);
	
	/**
	 * 移除并返回集合中的一个随机元素。<br>
	 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
	 * 
	 * @param key 集合Key
	 * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
	 */
	public String spop(String key);
	
	/**
	 * 移除并返回集合中的一个随机元素。<br>
	 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
	 * 
	 * @param key 集合Key
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
	 */
	public <T> T spop(String key, TypeReference<T> type);
	
	/**
	 * 移除并返回集合中的{count}个随机元素。<br>
	 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
	 * 
	 * @param key 集合Key
	 * @param count 获取元素的数量
	 * @return 被移除的随机元素集合。当 key 不存在或 key 是空集时，返回 nil 。
	 */
	public Set<String> spop(String key, int count);
	
	/**
	 * 移除并返回集合中的{count}个随机元素。<br>
	 * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
	 * 
	 * @param key 集合Key
	 * @param count 获取元素的数量
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 被移除的随机元素集合。当 key 不存在或 key 是空集时，返回 nil 。
	 */
	public <T> Set<T> spop(String key, int count, TypeReference<T> type);
	
	/**
	 * 返回集合中的一个随机元素
	 * 
	 * @param key 集合Key
	 * @return 返回一个元素；如果集合为空，返回 null
	 */
	public String srandmember(String key);
	
	/**
	 * 返回集合中的一个随机元素
	 * 
	 * @param key 集合Key
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 返回一个元素；如果集合为空，返回 null
	 */
	public <T> T srandmember(String key, TypeReference<T> type);
	
	/**
	 * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。<br>
	 * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。<br>
	 * 
	 * @param key 集合Key
	 * @param count 获取元素的数量
	 * @return 返回一个List集合；如果集合为空，返回空集合。
	 */
	public List<String> srandmember(String key, int count);
	
	/**
	 * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。<br>
	 * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。<br>
	 * 
	 * @param key 集合Key
	 * @param count 获取元素的数量
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 返回一个List集合；如果集合为空，返回空集合。
	 */
	public <T> List<T> srandmember(String key, int count, TypeReference<T> type);
	
	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
	 * 
	 * @param key 集合Key
	 * @param members 元素值动态数组
	 * @return
	 */
	public long srem(String key, String... members);
	
	/**
	 * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
	 * 
	 * @param key 集合Key
	 * @param members 元素对象动态数组
	 * @return
	 */
	public long srem(String key, Object... members);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的并集。<br>
	 * 不存在的 key 被视为空集。<br>
	 * @param keys 集合Key动态数组
	 * @return 并集成员的列表。
	 */
	public Set<String> sunion(String... keys);
	
	/**
	 * 返回一个集合的全部成员，该集合是所有给定集合的并集。<br>
	 * 不存在的 key 被视为空集。<br>
	 * @param keys 集合Key数组
	 * @param type 需要转换的TypeReference泛型类型
	 * @return 并集成员的列表。
	 */
	public <T> Set<T> sunion(String[] keys, TypeReference<T> type);
	
	/**
	 * 这个命令类似于 SUNION 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
	 * 如果 destination 已经存在，则将其覆盖。<br>
	 * destination 可以是 key 本身。<br>
	 * 
	 * @param destination 目标集合Key
	 * @param keys 集合Key动态数组
	 * @return 结果集中的元素数量。
	 */
	public long sunionstore(String destination, String... keys);
	
	/**
	 * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。<br>
	 * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。<br>
	 * score 值可以是整数值或双精度浮点数。<br>
	 * 如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。<br>
	 * 当 key 存在但不是有序集类型时，返回一个错误。
	 * 
	 * @param key
	 * @param score
	 * @param member
	 * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
	 */
	public long zadd(String key, double score, String member);
	
	public <T> long zadd(String key, double score, T member);
	
	public <T> long zadd(String key, Map<T, Double> values);
	
	public long zcard(String key);
	
	/**
	 * 返回有序集 key 中， score 值在 min 和 max 之间(默认包括 score 值等于 min 或 max )的成员的数量。
	 * @param key
	 * @param min
	 * @param max
	 * @return score 值在 min 和 max 之间的成员的数量。
	 */
	public long zcount(String key, double min, double max);
	
	public long zcount(String key);
	
	public long zcount(String key, String min, String max);
	
	public long zlexcount(String key, String min, String max);
	
	/**
	 * 为有序集 key 的成员 member 的 score 值加上增量 increment 。<br>
	 * 可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。<br>
	 * 当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。<br>
	 * 当 key 不是有序集类型时，返回一个错误。<br>
	 * score 值可以是整数值或双精度浮点数。<br>
	 * 
	 * @param key
	 * @param increment
	 * @param member
	 * @return member 成员的新 score 值
	 */
	public double zincrby(String key, double increment, String member);
	
	public <T> double zincrby(String key, double increment, T member);
	
	/**
	 * 返回有序集 key 中，指定区间内的成员。<br>
	 * 其中成员的位置按 score 值递增(从小到大)来排序。<br>
	 * 具有相同 score 值的成员按字典序(lexicographical order )来排列。<br>
	 * 如果你需要成员按 score 值递减(从大到小)来排列，请使用 ZREVRANGE 命令。<br>
	 * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
	 * 超出范围的下标并不会引起错误。<br>
	 * 比如说，当 start 的值比有序集的最大下标还要大，或是 start > stop 时， ZRANGE 命令只是简单地返回一个空列表。<br>
	 * 另一方面，假如 stop 参数的值比有序集的最大下标还要大，那么 Redis 将 stop 当作最大下标来处理。<br>
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return 指定区间内，有序集成员的列表。
	 */
	public Set<String> zrange(String key, long start, long end);
	
	public Set<String> zrange(String key, long end);
	
	public Set<String> zrange(String key);
	
	public <T> Set<T> zrange(String key, long start, long end, TypeReference<T> type);
	
	public <T> Set<T> zrange(String key, long end, TypeReference<T> type);
	
	public <T> Set<T> zrange(String key, TypeReference<T> type);
	
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count);
	
	public <T> Set<T> zrangeByLex(String key, String min, String max, int offset, int count, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeWithScores(String key, long start, long end, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeWithScores(String key, long end, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeWithScores(String key, TypeReference<T> type);
	
	public Set<String> zrangeByScore(String key, double min, double max);
	
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count);
	
	public Set<String> zrangeByScore(String key, String min, String max);
	
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count);
	
	public <T> Set<T> zrangeByScore(String key, double min, double max, TypeReference<T> type);
	
	public <T> Set<T> zrangeByScore(String key, double min, double max, int offset, int count, TypeReference<T> type);
	
	public <T> Set<T> zrangeByScore(String key, String min, String max, TypeReference<T> type);
	
	public <T> Set<T> zrangeByScore(String key, String min, String max, int offset, int count, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, int offset, int count, TypeReference<T> type);
	
	public <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, int offset, int count, TypeReference<T> type);
	
	/**
	 * 返回有序集 key 中成员 member 的排名。其中有序集成员按 score 值递增(从小到大)顺序排列。<br>
	 * 排名以 0 为底，也就是说， score 值最小的成员排名为 0 。<br>
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public long zrank(String key, String member);
	
	public <T> long zrank(String key, T member);
	
	/**
	 * 移除有序集 key 中的一个或多个成员，不存在的成员将被忽略。<br>
	 * 当 key 存在但不是有序集类型时，返回一个错误。<br>
	 * 
	 * @param key
	 * @param members
	 * @return 被成功移除的成员的数量，不包括被忽略的成员。
	 */
	public long zrem(String key, String... members);
	
	public <T> long zrem(String key, @SuppressWarnings("unchecked") T... members);
	
	public long zremrangeByLex(String key, String min, String max);
	
	/**
	 * 移除有序集 key 中，指定排名(rank)区间内的所有成员。<br>
	 * 区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。<br>
	 * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
	 * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return 被移除成员的数量。
	 */
	public long zremrangeByRank(String key, long start, long end);
	
	/**
	 * 移除有序集 key 中，所有 score 值介于 min 和 max 之间(包括等于 min 或 max )的成员。
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return 被移除成员的数量。
	 */
	public long zremrangeByScore(String key, double min, double max);
	
	public long zremrangeByScore(String key, String min, String max);
	
	public Set<String> zrevrange(String key, long start, long end);
	
	public Set<String> zrevrange(String key, long end);
	
	public Set<String> zrevrange(String key);
	
	public <T> Set<T> zrevrange(String key, long start, long end, TypeReference<T> type);
	
	public <T> Set<T> zrevrange(String key, long end, TypeReference<T> type);
	
	public <T> Set<T> zrevrange(String key, TypeReference<T> type);
	
	public Set<String> zrevrangeByLex(String key, String max, String min);
	
	public <T> Set<T> zrevrangeByLex(String key, String max, String min, TypeReference<T> type);
	
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);
	
	public <T> Set<T> zrevrangeByLex(String key, String max, String min, int offset, int count, TypeReference<T> type);
	
	public <T> Map<T, Double> zrevrangeWithScores(String key, long start, long end, TypeReference<T> type);
	
	public <T> Map<T, Double> zrevrangeWithScores(String key, long end, TypeReference<T> type);
	
	public <T> Map<T, Double> zrevrangeWithScores(String key, TypeReference<T> type);
	
	public Set<String> zrevrangeByScore(String key, double max, double min);
	
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);
	
	public <T> Set<T> zrevrangeByScore(String key, double max, double min, TypeReference<T> type);
	
	public <T> Set<T> zrevrangeByScore(String key, double max, double min, int offset, int count, TypeReference<T> type);
	
	public <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, TypeReference<T> type);
	
	public <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count, TypeReference<T> type);
	
	public long zrevrank(String key, String member);
	
	public <T> long zrevrank(String key, T member);
	
	public double zscore(String key, String member);
	
	public <T> double zscore(String key, T member);
}
