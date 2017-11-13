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
package org.nanoframework.extension.dubbo;

import org.nanoframework.extension.dubbo.service.HelloWorld2Service;
import org.nanoframework.extension.dubbo.service.HelloWorldService;

import com.alibaba.dubbo.config.annotation.Reference;
import com.google.inject.Inject;

/**
 *
 * @author yanghe
 * @since 1.4.1
 */
public class HelloWorldProxy implements HelloWorldService, HelloWorld2Service {
    private static final String SERVER = "localhost:20880";

    private HelloWorldService helloWorldService;
    private HelloWorld2Service helloWorld2Service;

    @Inject
    @Reference(check = false, url = "dubbo://" + SERVER + "/org.nanoframework.extension.dubbo.service.HelloWorldService")
    public HelloWorldService getHelloWorldService() {
        return helloWorldService;
    }

    public void setHelloWorldService(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    @Inject
    @Reference(check = false, url = "dubbo://" + SERVER + "/org.nanoframework.extension.dubbo.service.HelloWorld2Service")
    public HelloWorld2Service getHelloWorld2Service() {
        return helloWorld2Service;
    }

    public void setHelloWorld2Service(HelloWorld2Service helloWorld2Service) {
        this.helloWorld2Service = helloWorld2Service;
    }

    @Override
    public String say(final String who) {
        return "Proxy: " + helloWorldService.say(who);
    }

    @Override
    public String say2(final String who) {
        return "Proxy: " + helloWorld2Service.say2(who);
    }
}
