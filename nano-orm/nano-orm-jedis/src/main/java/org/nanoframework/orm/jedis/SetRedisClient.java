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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.alibaba.fastjson.TypeReference;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * Set RedisClient
 * @author yanghe
 * @since 1.4.10
 */
public interface SetRedisClient {

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。<br>
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。<br>
     * 当 key 不是集合类型时，返回一个错误。<br>
     * 
     * @param key 集合Key
     * @param members 元素值动态数组
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    long sadd(String key, String... members);

    /**
     * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略。<br>
     * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。<br>
     * 当 key 不是集合类型时，返回一个错误。<br>
     * 
     * @param key 集合Key
     * @param members 元素对象动态数组
     * @return 被添加到集合中的新元素的数量，不包括被忽略的元素。
     */
    long sadd(String key, Object... members);

    /**
     * 替换原有的值
     * 
     * @param key 集合Key
     * @param oldMembers 旧的元素
     * @param newMembers 新的元素
     * @return replace size
     */
    long sreplace(String key, String[] oldMembers, String[] newMembers);

    long sreplace(String key, Object[] oldMembers, Object[] newMembers);

    long sreplace(String key, Collection<Object> oldMembers, Collection<Object> newMembers);

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     * 
     * @param key 集合Key
     * @return 集合的基数。当 key 不存在时，返回 0 。
     */
    long scard(String key);

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     * 
     * @param keys 集合Key动态数组
     * @return 集合的基数。当 key 不存在时，返回 0 。
     */
    Map<String, Long> scard(String... keys);

    /**
     * 返回集合 key 的基数(集合中元素的数量)。
     * @param keys 集合Key列表
     * @return 集合的基数。当 key 不存在时，返回 0 。
     */
    Map<String, Long> scard(Collection<String> keys);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
     * 
     * @param keys 集合Key动态数组
     * @return 一个包含差集成员的列表。
     */
    Set<String> sdiff(String... keys);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
     * @param <T> TypeReference type
     * @param keys 集合Key动态数组
     * @param type 需要转换的TypeReference泛型类型
     * @return 一个包含差集成员的列表。
     */
    <T> Set<T> sdiff(String[] keys, TypeReference<T> type);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合之间的差集。不存在的 key 被视为空集。
     * @param <T> TypeReference type
     * @param keys 集合Key动态数组
     * @param type 需要转换的TypeReference泛型类型
     * @return 一个包含差集成员的列表。
     */
    <T> Set<T> sdiff(Collection<String> keys, TypeReference<T> type);

    /**
     * 这个命令的作用和 SDIFF 类似，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
     * 如果 destination 集合已经存在，则将其覆盖。<br>
     * destination 可以是 key 本身。<br>
     * 
     * @param destination 目标集合Key
     * @param keys 集合Key动态数组
     * @return 结果集中的元素数量。
     */
    long sdiffstore(String destination, String... keys);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
     * 不存在的 key 被视为空集。<br>
     * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
     * @param keys 集合Key动态数组
     * @return 交集成员的列表。
     */
    Set<String> sinter(String... keys);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
     * 不存在的 key 被视为空集。<br>
     * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
     * @param <T> TypeReference type
     * @param keys 集合Key数组
     * @param type 需要转换的TypeReference泛型类型
     * @return 交集成员的列表。
     */
    <T> Set<T> sinter(String[] keys, TypeReference<T> type);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合的交集。<br>
     * 不存在的 key 被视为空集。<br>
     * 当给定集合当中有一个空集时，结果也为空集(根据集合运算定律)。<br>
     * @param <T> TypeReference type
     * @param keys 集合Key列表
     * @param type 需要转换的TypeReference泛型类型
     * @return 交集成员的列表。
     */
    <T> Set<T> sinter(Collection<String> keys, TypeReference<T> type);

    /**
     * 这个命令类似于 SINTER 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
     * 如果 destination 集合已经存在，则将其覆盖。<br>
     * destination 可以是 key 本身。<br>
     * 
     * @param destination 目标集合Key
     * @param keys 集合Key动态数组
     * @return 结果集中的成员数量。
     */
    long sinterstore(String destination, String... keys);

    /**
     * 判断 member 元素是否集合 key 的成员。
     * 
     * @param key 集合Key
     * @param member 元素值
     * @return 如果 member 元素是集合的成员，返回 true 。如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
     */
    boolean sismember(String key, String member);

    /**
     * 判断 member 元素是否集合 key 的成员。
     * 
     * @param key 集合Key
     * @param member 元素对象
     * @return 如果 member 元素是集合的成员，返回 true 。如果 member 元素不是集合的成员，或 key 不存在，返回 false 。
     */
    boolean sismember(String key, Object member);

    /**
     * 返回集合 key 中的所有成员。不存在的 key 被视为空集合。
     * 
     * @param key 集合Key
     * @return 集合中的所有成员。
     */
    Set<String> smembers(String key);

    /**
     * 返回集合 key 中的所有成员。不存在的 key 被视为空集合。
     * @param <T> TypeReference type
     * @param key 集合Key
     * @param type 需要转换的TypeReference泛型类型
     * @return 集合中的所有成员。
     */
    <T> Set<T> smembers(String key, TypeReference<T> type);

    /**
     * 将 member 元素从 source 集合移动到 destination 集合。 
     * 
     * @param source 源集合Key
     * @param destination 目标集合Key
     * @param member 元素值
     * @return 如果 member 元素被成功移除，返回 true 。<br>
     * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
     */
    boolean smove(String source, String destination, String member);

    /**
     * 将 member 元素从 source 集合移动到 destination 集合。
     * 
     * @param source 源集合Key
     * @param destination 目标集合Key
     * @param members 元素值动态数组
     * @return 如果 member 元素被成功移除，返回 true 。<br>
     * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
     */
    Map<String, Boolean> smove(String source, String destination, String... members);

    /**
     * 将 member 元素从 source 集合移动到 destination 集合。
     * 
     * @param source 源集合Key
     * @param destination 目标集合Key
     * @param member 元素对象
     * @return 如果 member 元素被成功移除，返回 true 。<br>
     * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
     */
    boolean smove(String source, String destination, Object member);

    /**
     * 将 member 元素从 source 集合移动到 destination 集合。
     * 
     * @param source 源集合Key
     * @param destination 目标集合Key
     * @param members 元素对象动态数组
     * @return 如果 member 元素被成功移除，返回 true 。<br>
     * 如果 member 元素不是 source 集合的成员，并且没有任何操作对 destination 集合执行，那么返回 false 。
     */
    Map<Object, Boolean> smove(String source, String destination, Object... members);

    /**
     * 移除并返回集合中的一个随机元素。<br>
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
     * 
     * @param key 集合Key
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    String spop(String key);

    /**
     * 移除并返回集合中的一个随机元素。<br>
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
     * @param <T> TypeReference type
     * @param key 集合Key
     * @param type 需要转换的TypeReference泛型类型
     * @return 被移除的随机元素。当 key 不存在或 key 是空集时，返回 nil 。
     */
    <T> T spop(String key, TypeReference<T> type);

    /**
     * 移除并返回集合中的{count}个随机元素。<br>
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
     * 
     * @param key 集合Key
     * @param count 获取元素的数量
     * @return 被移除的随机元素集合。当 key 不存在或 key 是空集时，返回 nil 。
     */
    Set<String> spop(String key, int count);

    /**
     * 移除并返回集合中的{count}个随机元素。<br>
     * 如果只想获取一个随机元素，但不想该元素从集合中被移除的话，可以使用 SRANDMEMBER 命令。<br>
     * @param <T> TypeReference type
     * @param key 集合Key
     * @param count 获取元素的数量
     * @param type 需要转换的TypeReference泛型类型
     * @return 被移除的随机元素集合。当 key 不存在或 key 是空集时，返回 nil 。
     */
    <T> Set<T> spop(String key, int count, TypeReference<T> type);

    /**
     * 返回集合中的一个随机元素
     * 
     * @param key 集合Key
     * @return 返回一个元素；如果集合为空，返回 null
     */
    String srandmember(String key);

    /**
     * 返回集合中的一个随机元素
     * @param <T> TypeReference type
     * @param key 集合Key
     * @param type 需要转换的TypeReference泛型类型
     * @return 返回一个元素；如果集合为空，返回 null
     */
    <T> T srandmember(String key, TypeReference<T> type);

    /**
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。<br>
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。<br>
     * 
     * @param key 集合Key
     * @param count 获取元素的数量
     * @return 返回一个List集合；如果集合为空，返回空集合。
     */
    List<String> srandmember(String key, int count);

    /**
     * 如果 count 为正数，且小于集合基数，那么命令返回一个包含 count 个元素的数组，数组中的元素各不相同。如果 count 大于等于集合基数，那么返回整个集合。<br>
     * 如果 count 为负数，那么命令返回一个数组，数组中的元素可能会重复出现多次，而数组的长度为 count 的绝对值。<br>
     * @param <T> TypeReference type
     * @param key 集合Key
     * @param count 获取元素的数量
     * @param type 需要转换的TypeReference泛型类型
     * @return 返回一个List集合；如果集合为空，返回空集合。
     */
    <T> List<T> srandmember(String key, int count, TypeReference<T> type);

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * 
     * @param key 集合Key
     * @param members 元素值动态数组
     * @return rem member size
     */
    long srem(String key, String... members);

    /**
     * 移除集合 key 中的一个或多个 member 元素，不存在的 member 元素会被忽略。
     * 
     * @param key 集合Key
     * @param members 元素对象动态数组
     * @return rem member size
     */
    long srem(String key, Object... members);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合的并集。<br>
     * 不存在的 key 被视为空集。<br>
     * @param keys 集合Key动态数组
     * @return 并集成员的列表。
     */
    Set<String> sunion(String... keys);

    /**
     * 返回一个集合的全部成员，该集合是所有给定集合的并集。<br>
     * 不存在的 key 被视为空集。<br>
     * @param <T> TypeReference type
     * @param keys 集合Key数组
     * @param type 需要转换的TypeReference泛型类型
     * @return 并集成员的列表。
     */
    <T> Set<T> sunion(String[] keys, TypeReference<T> type);

    /**
     * 这个命令类似于 SUNION 命令，但它将结果保存到 destination 集合，而不是简单地返回结果集。<br>
     * 如果 destination 已经存在，则将其覆盖。<br>
     * destination 可以是 key 本身。<br>
     * 
     * @param destination 目标集合Key
     * @param keys 集合Key动态数组
     * @return 结果集中的元素数量。
     */
    long sunionstore(String destination, String... keys);

    /**
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @return 结果集
     */
    ScanResult<String> sscan(String key, long cursor);

    /**
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param type FastJSON TypeReference
     * @return 结果集
     */
    <T> ScanResult<T> sscan(String key, long cursor, TypeReference<T> type);

    /**
     * 
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param params 参数
     * @return 结果集
     */
    ScanResult<String> sscan(String key, long cursor, ScanParams params);

    /**
     * 
     * @param key 哈希表Key
     * @param cursor 迭代游标量
     * @param params 参数
     * @param type FastJSON TypeReference
     * @return 结果集
     */
    <T> ScanResult<T> sscan(String key, long cursor, ScanParams params, TypeReference<T> type);
}
