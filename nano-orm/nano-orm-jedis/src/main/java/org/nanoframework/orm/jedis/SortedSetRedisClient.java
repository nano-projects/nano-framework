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

import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.TypeReference;

/**
 *
 * @author yanghe
 * @since 1.4.10
 */
public interface SortedSetRedisClient {

    /**
     * 将一个或多个 member 元素及其 score 值加入到有序集 key 当中。<br>
     * 如果某个 member 已经是有序集的成员，那么更新这个 member 的 score 值，并通过重新插入这个 member 元素，来保证该 member 在正确的位置上。<br>
     * score 值可以是整数值或双精度浮点数。<br>
     * 如果 key 不存在，则创建一个空的有序集并执行 ZADD 操作。<br>
     * 当 key 存在但不是有序集类型时，返回一个错误。
     * 
     * @param key the key
     * @param score the score
     * @param member the member
     * @return 被成功添加的新成员的数量，不包括那些被更新的、已经存在的成员。
     */
    long zadd(String key, double score, String member);

    long zadd(String key, double score, Object member);

    long zadd(String key, Map<Object, Double> values);

    long zcard(String key);

    long zcount(String key, double min, double max);

    long zcount(String key);

    long zcount(String key, String min, String max);

    long zlexcount(String key, String min, String max);

    /**
     * 为有序集 key 的成员 member 的 score 值加上增量 increment 。<br>
     * 可以通过传递一个负数值 increment ，让 score 减去相应的值，比如 ZINCRBY key -5 member ，就是让 member 的 score 值减去 5 。<br>
     * 当 key 不存在，或 member 不是 key 的成员时， ZINCRBY key increment member 等同于 ZADD key increment member 。<br>
     * 当 key 不是有序集类型时，返回一个错误。<br>
     * score 值可以是整数值或双精度浮点数。<br>
     * 
     * @param key the key
     * @param increment the increment
     * @param member the member
     * @return member 成员的新 score 值
     */
    double zincrby(String key, double increment, String member);

    <T> double zincrby(String key, double increment, T member);

    /**
     * 返回有序集 key 中，指定区间内的成员。<br>
     * 其中成员的位置按 score 值递增(从小到大)来排序。<br>
     * 具有相同 score 值的成员按字典序(lexicographical order )来排列。<br>
     * 如果你需要成员按 score 值递减(从大到小)来排列，请使用 ZREVRANGE 命令。<br>
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
     * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
     * 超出范围的下标并不会引起错误。<br>
     * 比如说，当 start 的值比有序集的最大下标还要大，或是 start &gt; stop 时， ZRANGE 命令只是简单地返回一个空列表。<br>
     * 另一方面，假如 stop 参数的值比有序集的最大下标还要大，那么 Redis 将 stop 当作最大下标来处理。<br>
     * 
     * @param key the key
     * @param start the start
     * @param end the end
     * @return 指定区间内，有序集成员的列表。
     */
    Set<String> zrange(String key, long start, long end);

    Set<String> zrange(String key, long end);

    Set<String> zrange(String key);

    <T> Set<T> zrange(String key, long start, long end, TypeReference<T> type);

    <T> Set<T> zrange(String key, long end, TypeReference<T> type);

    <T> Set<T> zrange(String key, TypeReference<T> type);

    Set<String> zrangeByLex(String key, String min, String max, int offset, int count);

    <T> Set<T> zrangeByLex(String key, String min, String max, int offset, int count, TypeReference<T> type);

    <T> Map<T, Double> zrangeWithScores(String key, long start, long end, TypeReference<T> type);

    <T> Map<T, Double> zrangeWithScores(String key, long end, TypeReference<T> type);

    <T> Map<T, Double> zrangeWithScores(String key, TypeReference<T> type);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<String> zrangeByScore(String key, String min, String max);

    Set<String> zrangeByScore(String key, String min, String max, int offset, int count);

    <T> Set<T> zrangeByScore(String key, double min, double max, TypeReference<T> type);

    <T> Set<T> zrangeByScore(String key, double min, double max, int offset, int count, TypeReference<T> type);

    <T> Set<T> zrangeByScore(String key, String min, String max, TypeReference<T> type);

    <T> Set<T> zrangeByScore(String key, String min, String max, int offset, int count, TypeReference<T> type);

    <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, TypeReference<T> type);

    <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, TypeReference<T> type);

    <T> Map<T, Double> zrangeByScoreWithScores(String key, double min, double max, int offset, int count, TypeReference<T> type);

    <T> Map<T, Double> zrangeByScoreWithScores(String key, String min, String max, int offset, int count, TypeReference<T> type);

    long zrank(String key, String member);

    <T> long zrank(String key, T member);

    long zrem(String key, String... members);

    <T> long zrem(String key, @SuppressWarnings("unchecked") T... members);

    long zremrangeByLex(String key, String min, String max);

    /**
     * 移除有序集 key 中，指定排名(rank)区间内的所有成员。<br>
     * 区间分别以下标参数 start 和 stop 指出，包含 start 和 stop 在内。<br>
     * 下标参数 start 和 stop 都以 0 为底，也就是说，以 0 表示有序集第一个成员，以 1 表示有序集第二个成员，以此类推。<br>
     * 你也可以使用负数下标，以 -1 表示最后一个成员， -2 表示倒数第二个成员，以此类推。<br>
     * 
     * @param key the key
     * @param start the start
     * @param end the end
     * @return 被移除成员的数量。
     */
    long zremrangeByRank(String key, long start, long end);

    long zremrangeByScore(String key, double min, double max);

    long zremrangeByScore(String key, String start, String end);

    Set<String> zrevrange(String key, long start, long end);

    Set<String> zrevrange(String key, long end);

    Set<String> zrevrange(String key);

    <T> Set<T> zrevrange(String key, long start, long end, TypeReference<T> type);

    <T> Set<T> zrevrange(String key, long end, TypeReference<T> type);

    <T> Set<T> zrevrange(String key, TypeReference<T> type);

    Set<String> zrevrangeByLex(String key, String max, String min);

    <T> Set<T> zrevrangeByLex(String key, String max, String min, TypeReference<T> type);

    Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count);

    <T> Set<T> zrevrangeByLex(String key, String max, String min, int offset, int count, TypeReference<T> type);

    <T> Map<T, Double> zrevrangeWithScores(String key, long start, long end, TypeReference<T> type);

    <T> Map<T, Double> zrevrangeWithScores(String key, long end, TypeReference<T> type);

    <T> Map<T, Double> zrevrangeWithScores(String key, TypeReference<T> type);

    Set<String> zrevrangeByScore(String key, double max, double min);

    Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count);

    <T> Set<T> zrevrangeByScore(String key, double max, double min, TypeReference<T> type);

    <T> Set<T> zrevrangeByScore(String key, double max, double min, int offset, int count, TypeReference<T> type);

    <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, TypeReference<T> type);

    <T> Map<T, Double> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count, TypeReference<T> type);

    long zrevrank(String key, String member);

    <T> long zrevrank(String key, T member);

    double zscore(String key, String member);

    <T> double zscore(String key, T member);
}
