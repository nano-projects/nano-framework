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
package org.nanoframework.orm.kafka;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.google.inject.Inject;

/**
 *
 * @author yanghe
 * @since 1.4.9
 */
@Ignore
public class KafkaTests extends AbstractTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTests.class);

    @Inject
    private Producer<Object, Object> producer;

    @Test
    public void createProducerTest() {
        injects();
        Assert.assertNotNull(producer);
    }

    @Test
    public void sendTest() throws InterruptedException, ExecutionException {
        injects();
        final Future<RecordMetadata> future = producer.send(new ProducerRecord<>("test", "Hello! Kafka"));
        final RecordMetadata metadata = future.get();
        LOGGER.debug("offset: {}", metadata.offset());
    }
}
