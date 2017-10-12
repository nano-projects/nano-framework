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

import org.nanoframework.orm.jedis.RedisClient.Mark;

import com.alibaba.fastjson.TypeReference;

/**
 * List RedisClient.
 * @author yanghe
 * @since 1.4.10
 */
public interface ListRedisClient {
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
     * BLPOP 保证返回的元素来自 command ，因为它是按 "查找 job -&gt; 查找 command -&gt; 查找 request" 这样的顺序，第一个找到的非空列表。<br>
     * 
     * Timeout默认为0
     * 
     * @param key 列表Key
     * @param pop 读取方式
     * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
     * 
     * @see Mark#LPOP
     * @see Mark#RPOP
     */
    String bpop(String key, Mark pop);

    /**
     * @param <T> TypeReference type
     * @param key 列表Key
     * @param pop 读取方式
     * @param type TypeReference的泛型对象
     * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
     * 
     * @see #bpop(String, Mark)
     */
    <T> T bpop(String key, Mark pop, TypeReference<T> type);

    /**
     * 
     * @param key 列表Key
     * @param timeout 超时时间(秒)
     * @param pop 读取方式
     * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
     * 
     * @see #bpop(String, Mark)
     */
    String bpop(String key, int timeout, Mark pop);

    /**
     * @param <T> TypeReference type
     * @param key 列表Key
     * @param timeout 超时时间(秒)
     * @param pop 读取方式
     * @param type TypeReference的泛型对象
     * @return 如果列表为空，返回一个 null 。否则，返回被弹出元素的值。
     */
    <T> T bpop(String key, int timeout, Mark pop, TypeReference<T> type);

    /**
     * 
     * @param keys 列表Key的数组
     * @param pop 读取方式
     * @return 返回被弹出元素的非空值与列表Key的映射表
     * 
     * @see #bpop(String, Mark)
     */
    Map<String, String> bpop(String[] keys, Mark pop);

    /**
     * @param <T> TypeReference type
     * @param keys 列表Key的数组
     * @param pop 读取方式
     * @param type TypeReference的泛型对象
     * @return 返回被弹出元素的非空值与列表Key的映射表
     * 
     * @see #bpop(String, Mark)
     */
    <T> Map<String, T> bpop(String[] keys, Mark pop, TypeReference<T> type);

    /**
     * 
     * @param keys 列表Key的数组
     * @param timeout 超时时间(秒)
     * @param pop 读取方式
     * @return 返回被弹出元素的非空值与列表Key的映射表
     * 
     * @see #bpop(String, Mark)
     */
    Map<String, String> bpop(String[] keys, int timeout, Mark pop);

    /**
     * @param <T> TypeReference type
     * @param keys 列表Key的数组
     * @param timeout 超时时间(秒)
     * @param pop 读取方式
     * @param type TypeReference的泛型对象
     * @return 返回被弹出元素的非空值与列表Key的映射表
     * 
     * @see #bpop(String, Mark)
     */
    <T> Map<String, T> bpop(String[] keys, int timeout, Mark pop, TypeReference<T> type);

    /**
     * BRPOPLPUSH 是 RPOPLPUSH 的阻塞版本，当给定列表 source 不为空时， BRPOPLPUSH 的表现和 RPOPLPUSH 一样。<br>
     * 当列表 source 为空时， BRPOPLPUSH 命令将阻塞连接，直到等待超时，或有另一个客户端对 source 执行 LPUSH 或 RPUSH 命令为止。<br>
     * 超时参数 timeout 接受一个以秒为单位的数字作为值。超时参数设为 0 表示阻塞时间可以无限期延长(block indefinitely) 。<br>
     * Timeout默认为0
     * 
     * @param source the source
     * @param destination the destination
     * @return String
     */
    String brpoplpush(String source, String destination);

    <T> T brpoplpush(String source, String destination, TypeReference<T> type);

    String brpoplpush(String source, String destination, int timeout);

    <T> T brpoplpush(String source, String destination, int timeout, TypeReference<T> type);

    /**
     * 返回列表 key 中，下标为 index 的元素。<br>
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
     * 如果 key 不是列表类型，返回一个错误。<br>
     * 
     * @param key the key
     * @param index the index
     * @return list对应index下标的数据
     */
    String lindex(String key, int index);

    <T> T lindex(String key, int index, TypeReference<T> type);

    /**
     * LINSERT key BEFORE|AFTER pivot value <br>
     * 将值 value 插入到列表 key 当中，位于值 pivot 之前或之后。<br>
     * 当 pivot 不存在于列表 key 时，不执行任何操作。<br>
     * 当 key 不存在时， key 被视为空列表，不执行任何操作。<br>
     * 如果 key 不是列表类型，返回一个错误。<br>
     * 
     * @param key the key
     * @param pivot the pivot
     * @param value the value
     * @param position the position
     * @return long
     */
    long linsert(String key, String pivot, String value, Mark position);

    long linsert(String key, String pivot, Object value, Mark position);

    /**
     * LLEN key <br>
     * 返回列表 key 的长度。 <br>
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 . <br>
     * 如果 key 不是列表类型，返回一个错误。
     * 
     * @param key the key
     * @return list长度
     */
    long llen(String key);

    /**
     * 移除并返回列表 key 的头/尾元素。
     * @param key the key
     * @param pop the pop
     * @return 头或尾的元素
     */
    String pop(String key, Mark pop);

    List<String> pop(String key, int count);

    <T> T pop(String key, Mark pop, TypeReference<T> type);

    <T> List<T> pop(String key, int count, TypeReference<T> type);

    boolean push(String key, String[] values, Mark push);

    boolean push(String key, String value, Mark push);

    boolean push(String key, Object[] values, Mark push);

    boolean push(String key, Object value, Mark push);

    boolean push(String key, List<Object> values, Mark push);

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
    boolean push(String key, String scanKey, String value, Mark push, Mark policy);

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
    boolean push(String key, String scanKey, Object value, Mark push, Mark policy);

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
    Map<String, Boolean> push(String key, Map<String, Object> scanMap, Mark push, Mark policy);

    long pushx(String key, String[] values, Mark push);

    long pushx(String key, String value, Mark push);

    long pushx(String key, Object[] values, Mark push);

    long pushx(String key, Object value, Mark push);

    /**
     * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。<br>
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
     * 
     * @param key 列表Key
     * @param start 起始位置
     * @param end 结束位置
     * @return 元素集合
     */
    List<String> lrange(String key, int start, int end);

    List<String> lrange(String key, int count);

    List<String> lrange(String key);

    /**
     * 返回列表 key 中指定区间内的元素，区间以偏移量 start 和 end 指定。<br>
     * 下标(index)参数 start 和 stop 都以 0 为底，也就是说，以 0 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。<br>
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。<br>
     * @param <T> TypeReference type
     * @param key 列表Key
     * @param start 起始位置
     * @param end 结束位置
     * @param type 泛型对象
     * @return 元素集合
     * 
     */
    <T> List<T> lrange(String key, int start, int end, TypeReference<T> type);

    /**
     * 返回列表 key 中0到count之间的所有的元素。
     * @param <T> TypeReference type
     * @param key 列表Key
     * @param count 数量
     * @param type 泛型对象
     * @return 元素集合
     */
    <T> List<T> lrange(String key, int count, TypeReference<T> type);

    <T> List<T> lrange(String key, TypeReference<T> type);

    List<String> lrangeltrim(String key, int count);

    <T> List<T> lrangeltrim(String key, int count, TypeReference<T> type);

    /**
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素。<br>
     * count 的值可以是以下几种：<br>
     * count &gt; 0 : 从表头开始向表尾搜索，移除与 value 相等的元素，数量为 count 。<br>
     * count &lt; 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。<br>
     * count = 0 : 移除表中所有与 value 相等的值。<br>
     * 
     * @param key the key
     * @param count the count
     * @param value the value
     * @return delete size
     */
    long lrem(String key, int count, String value);

    long lrem(String key, String value);

    long lrem(String key, int count, Object value);

    long lrem(String key, Object value);

    /**
     * 将列表 key 下标为 index 的元素的值设置为 value 。<br>
     * 当 index 参数超出范围，或对一个空列表( key 不存在)进行 LSET 时，返回一个错误。<br>
     * 
     * @param key the key
     * @param index the index
     * @param value the value
     * @return set value successful
     */
    boolean lset(String key, int index, String value);

    boolean lset(String key, int index, Object value);

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     * 
     * @param key the key
     * @param start the start
     * @param end the end
     * @return trim successful
     */
    boolean ltrim(String key, int start, int end);

}
