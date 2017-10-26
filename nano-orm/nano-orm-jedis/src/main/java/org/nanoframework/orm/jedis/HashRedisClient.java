/*
 * Copyright 2015-2017 the original author or authors.
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
package org.nanoframework.orm.jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.TypeReference;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * Hash RedisClient.
 * @author yanghe
 * @since 1.4.10
 */
public interface HashRedisClient {
    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略。
     * 
     * @param key 哈希表Key
     * @param fields 哈希域动态数组
     * @return 被成功移除的域的数量，不包括被忽略的域。
     */
    long hdel(String key, String... fields);

    /**
     * 查看哈希表 key 中，给定域 field 是否存在。
     * 
     * @param key 哈希表Key
     * @param field 哈希域
     * @return 如果哈希表含有给定域，返回 true 。如果哈希表不含有给定域，或 key 不存在，返回 false 。
     */
    boolean hexists(String key, String field);

    /**
     * 返回哈希表 key 中给定域 field 的值。
     * 
     * @param key 哈希表Key
     * @param field 哈希域
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
     */
    String hget(String key, String field);

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。<br>
     * 如果给定的域不存在于哈希表，那么返回一个 nil 值。<br>
     * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。<br>
     * 
     * @param key 哈希表Key
     * @param fields 哈希域
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    Map<String, String> hmget(String key, String... fields);

    /**
     * 返回哈希表 key 中给定域 field 的值。并将值转换成泛型对象。
     * @param <T> TypeReference type
     * @param key 哈希表Key
     * @param field 哈希域
     * @param type TypeReference的泛型对象
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
     */
    <T> T hget(String key, String field, TypeReference<T> type);

    /**
     * 返回哈希表 key 中，一个或多个给定域的值。<br>
     * 如果给定的域不存在于哈希表，那么返回一个 nil 值。<br>
     * 因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。<br>
     * @param <T> TypeReference type
     * @param key 哈希表Key
     * @param fields 哈希域数组
     * @param type TypeReference的泛型对象
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 null 。
     */
    <T> Map<String, T> hmget(String key, String[] fields, TypeReference<T> type);

    /**
     * 返回哈希表 key 中，所有的域和值。<br>
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * 
     * @param key 哈希表Key
     * @return 以Map形式返回哈希表的域和域的值。若 key 不存在，返回空Map。
     */
    Map<String, String> hgetAll(String key);

    /**
     * 返回哈希表 key 中，所有的域和值。<br>
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * @param <T> TypeReference type
     * @param key 哈希表Key
     * @param type TypeReference的泛型对象
     * @return 以Map形式返回哈希表的域和域的值。若 key 不存在，返回空Map。
     */
    <T> Map<String, T> hgetAll(String key, TypeReference<T> type);

    /**
     * 返回哈希表 key 中的所有域。
     * 
     * @param key 哈希表Key
     * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
     */
    Set<String> hkeys(String key);

    <T> Set<T> hkeys(String key, TypeReference<T> type);

    /**
     * 返回哈希表 key 中域的数量。
     * 
     * @param key 哈希表Key
     * @return 哈希表中域的数量。当 key 不存在时，返回 0 。
     */
    long hlen(String key);

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
    boolean hset(String key, String field, String value);

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
    boolean hset(String key, String field, Object value);

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。<br>
     * 此命令会覆盖哈希表中已存在的域。<br>
     * 如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。<br>
     * 
     * @param key 哈希表Key
     * @param map 数据对象映射表
     * @return Return OK or Exception if hash is empty
     */
    boolean hmset(String key, Map<String, Object> map);

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
    boolean hsetByNX(String key, String field, String value);

    /**
     * 将哈希表 key 中的域 field 的值设置为 value ，当且仅当域 field 不存在。<br>
     * 若域 field 已经存在，该操作无效。<br>
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。<br>
     * 
     * @param key the key
     * @param field the field
     * @param value the value
     * @return 设置成功，返回 true 。如果给定域已经存在且没有操作被执行，返回 false 。
     */
    boolean hsetByNX(String key, String field, Object value);

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中 ，当且仅当域 field 不存在。<br>
     * 若域 field 已经存在，该操作无效。<br>
     * 如果 key 不存在，一个新哈希表被创建并执行 HSETNX 命令。<br>
     * 在Cluster模式下，即使存在一个给定的field，其他field也将写入成功.
     * @param key the key
     * @param map the map
     * @return 设置成功，返回 true 。如果给定域已经存在且没有操作被执行，返回 false 。
     */
    Map<String, Boolean> hsetByNX(String key, Map<String, Object> map);

    /**
     * 返回哈希表 key 中所有域的值。
     * 
     * @param key 哈希表Key
     * @return 一个包含哈希表中所有值的表。当 key 不存在时，返回一个空表。
     */
    List<String> hvals(String key);

    <T> List<T> hvals(String key, TypeReference<T> type);

    /**
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @return 结果集
     */
    ScanResult<Entry<String, String>> hscan(String key, long cursor);

    /**
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param type FastJSON TypeReference
     * @return 结果集
     */
    <T> ScanResult<Entry<String, T>> hscan(String key, long cursor, TypeReference<T> type);

    /**
     * 
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param params 参数
     * @return 结果集
     */
    ScanResult<Entry<String, String>> hscan(String key, long cursor, ScanParams params);

    /**
     * 
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param params 参数
     * @param type FastJSON TypeReference
     * @return 结果集
     */
    <T> ScanResult<Entry<String, T>> hscan(String key, long cursor, ScanParams params, TypeReference<T> type);

    /**
     * 为哈希表 key 中的域 field 的值加上增加1.
     * 增量也可以为负数，相当于对给定域进行减法操作.
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令.
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0.
     * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误.
     * @param key Hash Key
     * @param field Hash Field
     * @return 自增后的值
     */
    long hincr(String key, String field);

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment.
     * 增量也可以为负数，相当于对给定域进行减法操作.
     * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令.
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0.
     * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误.
     * @param key Hash Key
     * @param field Hash Field
     * @param value 增量值
     * @return 自增后的值
     */
    long hincrBy(String key, String field, long value);
    
    /**
     * 为哈希表 key 中的域 field 加上浮点数增量 increment.
     * 如果哈希表中没有域 field ，那么 HINCRBYFLOAT 会先将域 field 的值设为 0 ，然后再执行加法操作.
     * 如果键 key 不存在，那么 HINCRBYFLOAT 会先创建一个哈希表，再创建域 field ，最后再执行加法操作.
     * 当以下任意一个条件发生时，返回一个错误:
     *   域 field 的值不是字符串类型(因为 redis 中的数字和浮点数都以字符串的形式保存，所以它们都属于字符串类型）
     *   域 field 当前的值或给定的增量 increment 不能解释(parse)为双精度浮点数(double precision floating point number)
     * @param key Hash Key
     * @param field Hash Field
     * @param value 增量值
     * @return 自增后的值
     */
    double hincrByFloat(String key, String field, double value);
}
