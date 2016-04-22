创建quickstart工程
----

####1. Create maven-archetype-webapp

####2. 编辑 pom.xml
#####2.1. 在pom.xml的dependencies标签中添加dependency依赖。此依赖包中包含web项目开发所必须的依赖，导入此包后将自动导入其他所需的依赖包
```xml
<dependency>
	<groupId>org.nanoframework</groupId>
	<artifactId>nano-webmvc</artifactId>
	<version>1.3.4</version>
</dependency>
```
#####2.2. 在pom.xml的build标签中添加resources
```xml
<resources>
	<resource>
		<directory>src/main/webapp</directory>
	</resource>
	<resource>
		<directory>src/main/resources</directory>
	</resource>
</resources>
```
#####2.3. 在项目目录下添加以下目录。新建项目不能自动创建以下资源包，需要手动添加
- src/main/java
- src/test/java
- src/test/resources

#####2.4. Maven Update

####3. 编辑 web.xml
#####3.1. 添加DispatcherServlet 与 HttpRequestFilter
##### DispatcherServlet为服务启动加载组件，必须设置为启动时自动加载，加载过程会依次加载Properties、Module、Plugin、Component
##### HttpRequestFilter为服务调用入口，所有组件资源全部通过此Filter进入
```xml
<filter>
	<filter-name>httpRequestFilter</filter-name>
	<filter-class>org.nanoframework.web.server.filter.HttpRequestFilter</filter-class>
</filter>

<filter-mapping>
	<filter-name>httpRequestFilter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>

<servlet>
	<servlet-name>Dispatcher-Servlet</servlet-name>
	<servlet-class>org.nanoframework.web.server.servlet.DispatcherServlet</servlet-class>
	<init-param>
		<param-name>context</param-name>
		<param-value>/context.properties</param-value>
	</init-param>
	<load-on-startup>1</load-on-startup>
</servlet>

<servlet-mapping>
	<servlet-name>Dispatcher-Servlet</servlet-name>
	<url-pattern>/dispatcher/*</url-pattern>
</servlet-mapping>
```

####4. 添加 context.properties 属性文件
#####4.1. 在src/main/resources下添加context.properties属性文件，并添加以下属性配置
```properties
# 服务根资源路径
context.root=/quickstart

# 组件扫描配置，指定组件所在包路径后启动项目将自动进行组件加载
context.component-scan.base-package=org.nanoframework.examples.quickstart.component
```

####5. 修改项目属性
#####5.1. 修改工程的Web Project Settings，使其与context.properties属性文件中的context.root保持一致

####6. 创建HelloWorld组件
#####6.1. 在src/main/java下创建接口 org.nanoframework.examples.quickstart.component.HelloWorldComponent
```java
package org.nanoframework.examples.quickstart.component;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.core.component.stereotype.bind.RequestMapping;
import org.nanoframework.examples.quickstart.component.impl.HelloWorldComponentImpl;
import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(HelloWorldComponentImpl.class)
public interface HelloWorldComponent {
    @RequestMapping("/hello")
    String hello();
}
```

#####6.2. 在src/java/main下创建类 org.nanoframework.examples.quickstart.component.impl.HelloWorldComponentImpl 并实现 HelloWorldComponent接口
```java
package org.nanoframework.examples.quickstart.component.impl;

import org.nanoframework.examples.quickstart.component.HelloWorldComponent;

public class HelloWorldComponentImpl implements HelloWorldComponent {
    @Override
    public String hello() {
        return "Hello NanoFramework";
    }
}
```

####7. 添加Web容器(Tomcat, Jetty等)
#####7.1. 启动项目
#####7.2. 访问服务:  http://localhost:8080/quickstart/hello

源码
----
- [nano-examples-quickstart](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-quickstart)
