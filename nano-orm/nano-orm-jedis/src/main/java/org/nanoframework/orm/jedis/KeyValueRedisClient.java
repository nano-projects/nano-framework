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

import com.alibaba.fastjson.TypeReference;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * Key Value RedisClient.
 * @author yanghe
 * @since 1.4.10
 */
public interface KeyValueRedisClient {
    /**
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
     * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
     * 使用默认分隔符进行分隔，默认分隔符：","
     * 
     * @param key 散列Key
     * @param value 需要进行添加的内容
     * @return 追加 value 之后， key 中字符串的长度。
     */
    long append(String key, String value);

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
    long append(String key, String value, String separator);

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
    long append(String key, Object value, String separator);

    /**
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。<br>
     * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。<br>
     * 使用默认分隔符进行分隔，默认分隔符：","
     * 
     * @param key 散列Key
     * @param value 需要添加的内容对象
     * @return 追加 value 之后， key 中字符串的长度。
     */
    long append(String key, Object value);

    /**
     * 返回 key 所关联的字符串值。
     * @param key 散列Key
     * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    String get(String key);

    /**
     * 返回 key 所关联的字符串值，并且通过FastJson转换为泛型所对应的数据类型。<br>
     * 如果类型转换不匹配是则返回一个错误
     * @param <T> TypeReference type
     * @param key 散列Key
     * @param type 对象类型转换
     * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    <T> T get(String key, TypeReference<T> type);

    /**
     * 返回所有(一个或多个)给定 key 的值。
     * 
     * @param keys 散列Key动态数组
     * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    Map<String, String> get(String... keys);

    /**
     * 返回所有(一个或多个)给定 key 的值。
     * @param <T> TypeReference type
     * @param keys 散列Key的数组
     * @param type 需要转换的TypeReference泛型类型
     * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    <T> Map<String, T> get(String[] keys, TypeReference<T> type);

    /**
     * 返回所有(一个或多个)给定 key 的值。
     * @param <T> TypeReference type
     * @param keys 散列Key的List集合
     * @param type 需要转换的TypeReference泛型类型
     * @return 当 key 不存在时，返回 null ，否则，返回 key 的值。如果 key 不是字符串类型，那么返回一个错误。
     */
    <T> Map<String, T> get(List<String> keys, TypeReference<T> type);

    /**
     * 将给定 key 的值设为 value ，并返回 key 的旧值(old value)。当 key 存在但不是字符串类型时，返回一个错误。
     * @param key 散列Key
     * @param value 散列值
     * @return 返回给定 key 的旧值。当 key 没有旧值时，也即是， key 不存在时，返回 null 。
     */
    String getset(String key, String value);

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
    boolean set(String key, String value);

    /**
     * 将对象 value 关联到 key 。
     * 
     * @param key 散列Key
     * @param value 散列值
     * @return 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。<br>
     * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。<br>
     * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
     */
    boolean set(String key, Object value);

    /**
     * 同时设置一个或多个 key-value 对。
     * 
     * @param map 散列Key-Value映射表
     * @return 在 Redis 2.6.12 版本以前， SET 命令总是返回 OK 。<br>
     * 从 Redis 2.6.12 版本开始， SET 在设置操作成功完成时，才返回 OK 。<br>
     * 如果设置了 NX 或者 XX ，但因为条件没达到而造成设置操作未执行，那么命令返回空批量回复（NULL Bulk Reply）。
     */
    Map<String, Boolean> set(Map<String, Object> map);

    /**
     * 将字符串值 value 关联到 key, 当且仅当给定 key 不存在时.
     * @param key 散列Key
     * @param value 散列值
     * @return 设置成功，返回 true 。设置失败，返回 false。
     */
    boolean setByNX(String key, String value);

    /**
     * 将对象 value 关联到 key, 当且仅当给定 key 不存在时.
     * 
     * @param key 散列Key
     * @param value 散列值
     * @return 设置成功，返回 true。设置失败，返回 false。
     */
    boolean setByNX(String key, Object value);

    /**
     * 将字符串值 value 关联到 key, 当且仅当给定 key 不存在时.
     * @param key 散列Key
     * @param value 散列值
     * @param timeout 超时时间, 单位: 秒
     * @return 设置成功，返回 true。设置失败，返回 false。
     */
    boolean setByNX(String key, String value, int timeout);

    /**
     * 将字符串值 value 关联到 key, 当且仅当给定 key 不存在时.
     * @param key 散列Key
     * @param value 散列值
     * @param timeout 超时时间, 单位: 秒
     * @return 设置成功，返回 true。设置失败，返回 false。
     */
    boolean setByNX(String key, Object value, int timeout);

    /**
     * 同时设置一个或多个 key-value 对, 当且仅当所有给定 key 都不存在.
     * 即使只有一个给定 key 已存在， MSETNX 也会拒绝执行所有给定 key 的设置操作.
     * 在Cluster模式下，存在一个给定的key时，剩下不存在的key也会被设置成功.
     * @param map 散列Key-Value映射表
     * @return 设置成功，返回 true。设置失败，返回 false。返回Key与结果对应的映射表
     */
    Map<String, Boolean> setByNX(Map<String, Object> map);

    /**
     * 将字符串值 value 关联到 key, 使用默认的时间进行生命周期的设置。
     * 
     * @param key 散列Key
     * @param value 散列值
     * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
     */
    boolean setByEX(String key, String value);

    /**
     * 将字符串值 value 关联到 key, 使用默认的时间进行生命周期的设置。
     * 
     * @param key 散列Key
     * @param value 散列值
     * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
     */
    boolean setByEX(String key, Object value);

    /**
     * 将字符串值 value 关联到 key，并设置以秒为单位的生命周期。
     * 
     * @param key 散列Key
     * @param value 散列值
     * @param seconds 时间(秒)
     * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
     */
    boolean setByEX(String key, String value, int seconds);

    /**
     * 将字符串值 value 关联到 key，并设置以秒为单位的生命周期。
     * 
     * @param key 散列Key
     * @param value 散列值
     * @param seconds 时间(秒)
     * @return 设置成功时返回 OK 。当 seconds 参数不合法时，返回一个错误。
     */
    boolean setByEX(String key, Object value, int seconds);

    /**
     * 返回 key 所储存的字符串值的长度。当 key 储存的不是字符串值时，返回一个错误。
     * 
     * @param key 散列Key
     * @return 字符串值的长度。当 key 不存在时，返回 0 。
     */
    long strLen(String key);

    /**
     * 将 key 中储存的数字值增一.
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作.
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误.
     * 本操作的值限制在 64 位(bit)有符号数字表示之内.
     * @param key 自增Key
     * @return 自增后的值
     */
    long incr(String key);

    /**
     * 将 key 所储存的值加上增量 increment .
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令.
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误.
     * 本操作的值限制在 64 位(bit)有符号数字表示之内.
     * @param key 增量Key
     * @param value 增量值
     * @return 增量后的值
     */
    long incrBy(String key, long value);

    /**
     * 为 key 中所储存的值加上浮点数增量 increment .
     * 如果 key 不存在，那么 INCRBYFLOAT 会先将 key 的值设为 0 ，再执行加法操作.
     * @param key 增量key
     * @param value 增量值
     * @return 增量后的值
     */
    double incrByFloat(String key, double value);

    /**
     * 将 key 中储存的数字值减一.
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作.
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误.
     * 本操作的值限制在 64 位(bit)有符号数字表示之内.
     * @param key 自减Key
     * @return 自减后的值
     */
    long decr(String key);

    /**
     * 将 key 所储存的值减去减量 decrement .
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作.
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误.
     * 本操作的值限制在 64 位(bit)有符号数字表示之内.
     * @param key 自减Key
     * @param value 自减值
     * @return 自减后的值
     */
    long decrBy(String key, long value);

    /**
     * SCAN 命令是一个基于游标的迭代器（cursor based iterator）： SCAN 命令每次被调用之后， 都会向用户返回一个新的游标,
     * 用户在下次迭代时需要使用这个新游标作为 SCAN 命令的游标参数， 以此来延续之前的迭代过程.
     * 当 SCAN 命令的游标参数被设置为 0 时， 服务器将开始一次新的迭代， 而当服务器向用户返回值为 0 的游标时， 表示迭代已结束.
     * @param cursor 游标
     * @return Scan结果集
     */
    ScanResult<String> scan(final long cursor);

    /**
     * @see #scan(long)
     * @param cursor 游标
     * @param type TypeReference类型
     * @return Scan结果集
     */
    <T> ScanResult<T> scan(final long cursor, TypeReference<T> type);

    /**
     * @see #scan(long)
     * @param cursor 游标
     * @param params 请求参数
     * @return Scan结果集
     */
    ScanResult<String> scan(final long cursor, final ScanParams params);

    /**
     * @see #scan(long)
     * @param cursor 游标
     * @param params 请求参数
     * @param type TypeReference类型
     * @return Scan结果集
     */
    <T> ScanResult<T> scan(final long cursor, final ScanParams params, TypeReference<T> type);
}
