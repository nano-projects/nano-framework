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
package org.nanoframework.core.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.core.plugins.defaults.module.SPIModule;
import org.nanoframework.core.spi.test.SpiLazyService;
import org.nanoframework.core.spi.test.SpiService;
import org.nanoframework.core.spi.test.impl.TestLazyServiceImpl;
import org.nanoframework.core.spi.test.impl.TestService2Impl;
import org.nanoframework.core.spi.test.impl.TestServiceImpl;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

/**
 *
 * @author yanghe
 * @since 1.4.8
 */
public class SPITests {
    private List<String> spiFiles = Lists.newArrayList("org.nanoframework.core.spi.test.SpiService", "org.nanoframework.core.spi.test.SpiLazyService",
            "org.nanoframework.core.spi.test.NotSpiService", "org.nanoframework.core.spi.test.SpiNotImplService");

    @Inject
    @Named("testService")
    private static SpiService testService;

    @Inject
    @Named("testService2")
    private static SpiService testService2;

    @Inject
    @Named("testLazyService")
    private static SpiLazyService testLazyService;

    @Test
    public void getResourcesTest() throws IOException, URISyntaxException {
        final SPILoader loader = new SPILoader();
        final Enumeration<URL> urls = loader.getResources();
        Assert.assertTrue(urls.hasMoreElements());
    }

    @Test
    public void getSPIFilesTest() throws URISyntaxException, IOException {
        final SPILoader loader = new SPILoader();
        final SPIResource resource = loader.getSPIResource(loader.getResources());
        Assert.assertTrue(resource != SPIResource.EMPTY);
        final List<File> files = resource.getFiles();
        for (final File spiFile : files) {
            Assert.assertTrue(spiFiles.contains(spiFile.getName()));
        }

        final Map<String, List<InputStream>> streams = resource.getStreams();
        Assert.assertTrue(streams.isEmpty());
    }

    @Test
    public void spiTest() {
        final Injector injector = Guice.createInjector();
        Globals.set(Injector.class, injector);
        injector.createChildInjector(new SPIModule(), binder -> binder.requestStaticInjection(SPITests.class));

        Assert.assertNotNull(testService);
        Assert.assertTrue(testService.getClass() == TestServiceImpl.class);
        Assert.assertEquals(testService.echo(), "Echo TestService");

        Assert.assertNotNull(testService2);
        Assert.assertTrue(testService2.getClass() == TestService2Impl.class);
        Assert.assertEquals(testService2.echo(), "Echo TestService 2");

        Assert.assertNotNull(testLazyService);
        Assert.assertTrue(testLazyService.getClass() == TestLazyServiceImpl.class);
        Assert.assertEquals(testLazyService.echo(), "Echo Lazy TestService");
    }
}
