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
package org.nanoframework.server;

import java.io.IOException;
import java.util.Map;

import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nanoframework.commons.util.MapBuilder;
import org.nanoframework.core.globals.Globals;
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.inject.Injector;


/**
 *
 * @author yanghe
 * @since 1.4.3
 */
public class TomcatReadonlyTests {
    private final static ThreadLocal<TomcatCustomServer> SERVER = new ThreadLocal<>();
    
    @BeforeClass
    public static void initServer() throws Throwable {
        final TomcatCustomServer serv = TomcatCustomServer.server();
        SERVER.set(serv);
        serv.startServerDaemon();
        while (!Boolean.parseBoolean(System.getProperty(TomcatCustomServer.READY, "false"))) {}
    }
    
    @Test
    public void putTest() throws IOException {
        final HttpClient client = Globals.get(Injector.class).getInstance(HttpClient.class);
        HttpResponse res = client.put("http://localhost:7000/tomcat/put/hello", MapBuilder.<String, String>create().put("spec", "&").put("value", "world").build());
        Map<String, String> result = JSON.parseObject(res.entity, new TypeReference<Map<String, String>>() { });
        Assert.assertEquals(result.get("message"), "hello&world");
    }
    
    @Test
    public void putArrayTest() throws IOException {
        final HttpClient client = Globals.get(Injector.class).getInstance(HttpClient.class);
        HttpResponse res = client.put("http://localhost:7000/tomcat/put/arr/hello", "values[]=1&values[]=2&values[]=3", ContentType.APPLICATION_FORM_URLENCODED);
        Map<String, String> result = JSON.parseObject(res.entity, new TypeReference<Map<String, String>>() { });
        Assert.assertEquals(result.get("message"), "hello=[\"1\",\"2\",\"3\"]");
    }
    
    @Test
    public void putJsonTest() throws IOException {
        final HttpClient client = Globals.get(Injector.class).getInstance(HttpClient.class);
        final String json = JSON.toJSONString(MapBuilder.<String, String[]>create().put("value", new String[]{"1", "2", "3"}).build());
        HttpResponse res = client.put("http://localhost:7000/tomcat/put/json/hello", json);
        Map<String, String> result = JSON.parseObject(res.entity, new TypeReference<Map<String, String>>() { });
        Assert.assertEquals(result.get("message"), "hello=[\"1\",\"2\",\"3\"]");
    }
    
    @Test
    public void deleteTest() throws IOException {
        final HttpClient client = Globals.get(Injector.class).getInstance(HttpClient.class);
        HttpResponse res = client.delete("http://localhost:7000/tomcat/del/hello", MapBuilder.<String, String>create().put("spec", " del ").put("value", "world").build());
        Map<String, String> result = JSON.parseObject(res.entity, new TypeReference<Map<String, String>>() { });
        Assert.assertEquals(result.get("message"), "hello del world");
    }
    
    @Test
    public void postTest() throws IOException {
        final HttpClient client = Globals.get(Injector.class).getInstance(HttpClient.class);
        HttpResponse res = client.post("http://localhost:7000/tomcat/post/hello", MapBuilder.<String, String>create().put("spec", "&").put("value", "world").build());
        Map<String, String> result = JSON.parseObject(res.entity, new TypeReference<Map<String, String>>() { });
        Assert.assertEquals(result.get("message"), "hello&world");
    }
    
    @AfterClass
    public static void shutdown() throws Throwable {
        final TomcatCustomServer serv = SERVER.get();
        serv.stopServer();
        SERVER.remove();
        System.setProperty(TomcatCustomServer.READY, "false");
    }
}
