示例1: 项目搭建
====

#####1、创建Maven Webapp

#####2、pom.xml中添加依赖
```xml
  <dependency>
		<groupId>org.nanoframework</groupId>
		<artifactId>nano-webmvc</artifactId>
		<version>${nano-version}</version>
  </dependency>
```

#####3、web.xml中添加DispatcherServlet和HttpRequestFilter
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

#####4、在src/main/resources下添加属性文件context.properties(servlet中的context配置)，并添加以下内容
```properties
  context.root=/first-webapp
  context.component-scan.base-package=org.nanoframework.examples.first.webapp.component
```

#####5、修改工程的Web Project Settings，是其与context.properties属性文件中的context.root保持一致

#####6、添加第一个组件服务
######6.1、在包org.nanoframework.examples.first.webapp.component下建立HelloWorldComponent接口
```java
@Component
@ImplementedBy(HelloWorldComponentImpl.class)
@RequestMapping("/first")
public interface HelloWordComponent {
	@RequestMapping("/hello")
	Object hello();
}
```
######6.2、在包org.nanoframework.examples.first.webapp.component.impl下建立HelloWorldComponentImpl实现类
```java
public class HelloWorldComponentImpl implements HelloWordComponent {
	@Override
	public Object hello() {
		return "Hello Nano Framework!";
	}
}
```

#####7、添加web容器，运行服务，并访问http://ip:port/first-webapp/first/hello

#####8、至此最基础的服务已经搭建完成
