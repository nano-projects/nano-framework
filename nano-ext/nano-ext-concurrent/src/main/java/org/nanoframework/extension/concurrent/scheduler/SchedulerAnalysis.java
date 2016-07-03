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
package org.nanoframework.extension.concurrent.scheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.nanoframework.extension.concurrent.scheduler.defaults.etcd.EtcdScheduler;

/**
 * @author yanghe
 * @date 2016年3月25日 上午10:07:40
 */
public class SchedulerAnalysis {
    public static final String DISABLED = "未启用统计";
    public final AtomicLong executing = new AtomicLong(0);
    public final AtomicLong beforeException = new AtomicLong(0);
    public final AtomicLong executeException = new AtomicLong(0);
    public final AtomicLong afterException = new AtomicLong(0);
    private ConcurrentMap<Cycle, AtomicLong> performCycle = new ConcurrentHashMap<>();
    {
        performCycle.put(Cycle.LESS_100, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_101_500, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_501_1000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_1001_2000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_2001_5000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_5001_10000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_10001_20000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_20001_30000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_30001_45000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_45001_60000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_60001_120000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_120001_180000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_180001_300000, new AtomicLong(0));
        performCycle.put(Cycle.BETWEEN_300001_600000, new AtomicLong(0));
        performCycle.put(Cycle.GREATER_600001, new AtomicLong(0));
    }

    private SchedulerAnalysis() {
    }

    public static SchedulerAnalysis newInstance() {
        return new SchedulerAnalysis();
    }

    public void putPerformCycle(long time) {
        Cycle cycle = Cycle.calculate(time);
        AtomicLong counter = performCycle.get(cycle);
        counter.incrementAndGet();
        performCycle.put(cycle, counter);
    }

    public String performCycle() {
        if (!EtcdScheduler.SCHEDULER_ANALYSIS_ENABLE)
            return DISABLED;

        return performCycle.get(Cycle.LESS_100).get() + ", " + performCycle.get(Cycle.BETWEEN_101_500).get() + ", "
                + performCycle.get(Cycle.BETWEEN_501_1000).get() + ", " + performCycle.get(Cycle.BETWEEN_1001_2000).get() + ", "
                + performCycle.get(Cycle.BETWEEN_2001_5000).get() + ", " + performCycle.get(Cycle.BETWEEN_5001_10000).get() + ", "
                + performCycle.get(Cycle.BETWEEN_10001_20000).get() + ", " + performCycle.get(Cycle.BETWEEN_20001_30000).get() + ", "
                + performCycle.get(Cycle.BETWEEN_30001_45000).get() + ", " + performCycle.get(Cycle.BETWEEN_45001_60000).get() + ", "
                + performCycle.get(Cycle.BETWEEN_60001_120000).get() + ", " + performCycle.get(Cycle.BETWEEN_120001_180000).get() + ", "
                + performCycle.get(Cycle.BETWEEN_180001_300000).get() + ", " + performCycle.get(Cycle.BETWEEN_300001_600000).get() + ", "
                + performCycle.get(Cycle.GREATER_600001).get();

    }

    public enum Cycle {
        LESS_100("~100"), BETWEEN_101_500("101~500"), BETWEEN_501_1000("501~1000"), BETWEEN_1001_2000("1001~2000"), BETWEEN_2001_5000(
                "2001~5000"), BETWEEN_5001_10000("5001~10000"), BETWEEN_10001_20000("10001~20000"), BETWEEN_20001_30000(
                        "20001~30000"), BETWEEN_30001_45000("30001~45000"), BETWEEN_45001_60000("45001~60000"), BETWEEN_60001_120000(
                                "60001~120000"), BETWEEN_120001_180000("120001~180000"), BETWEEN_180001_300000(
                                        "180001~300000"), BETWEEN_300001_600000("300001~600000"), GREATER_600001("600001~");

        private final String value;

        private Cycle(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        public static Cycle calculate(long time) {
            if (time <= 100) {
                return LESS_100;
            } else if (time >= 101 && time <= 500) {
                return BETWEEN_101_500;
            } else if (time >= 501 && time <= 1000) {
                return BETWEEN_501_1000;
            } else if (time >= 1001 && time <= 2000) {
                return BETWEEN_1001_2000;
            } else if (time >= 2001 && time <= 5000) {
                return BETWEEN_2001_5000;
            } else if (time >= 5001 && time <= 10_000) {
                return BETWEEN_5001_10000;
            } else if (time >= 10_001 && time <= 20_000) {
                return BETWEEN_10001_20000;
            } else if (time >= 20_001 && time <= 30_000) {
                return BETWEEN_20001_30000;
            } else if (time >= 30_001 && time <= 45_000) {
                return BETWEEN_30001_45000;
            } else if (time >= 45_001 && time <= 60_000) {
                return BETWEEN_45001_60000;
            } else if (time >= 60_001 && time <= 120_000) {
                return BETWEEN_60001_120000;
            } else if (time >= 120_001 && time <= 180_000) {
                return BETWEEN_120001_180000;
            } else if (time >= 180_001 && time <= 300_000) {
                return BETWEEN_180001_300000;
            } else if (time >= 300_001 && time <= 600_000) {
                return BETWEEN_300001_600000;
            } else {
                return GREATER_600001;
            }
        }
    }
}
