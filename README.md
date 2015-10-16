Java MVC + ORM框架 Nano Framework
====

	Nano Framework基于Google Guice框架进行开发，使用Guice的IoC和AOP特性可以快速的定义和开发组件及服务。
	设计初衷是减少代码量，让开发人员专注于业务层代码的开发。
	
	
环境要求
----
	Nano Framework基于JDK8进行开发，内部使用了JDK8的新特性，使用时必须使用JDK8进行开发和编译

安装
----
```shell
git clone git@github.com:nano-projects/nano-framework.git
cd nano-framework
mvn clean install -Dmaven.test.skip=true
```

使用
----
####1、添加mvc依赖
```xml
<dependency>
	<groupId>org.nanoframework</groupId>
	<artifactId>nano-webmvc</artifactId>
	<version>1.2.0-RC1</version>
</dependency>
```
####2、配置web.xml
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
####3、添加属性文件context.properties
```properties
# web根路径
context.root=/first-webapp
```
	
示例代码逐步更新中
----

- [示例1: 项目搭建](nano-examples/examples/examples-00.md)
- [示例2: 进阶一 - 增加Jetty内嵌支持](nano-examples/examples/examples-01.md)
- [示例3: 进阶二 - Restful API与带参服务](nano-examples/examples/examples-02.md)
- [示例4: 进阶三 - 添加H2数据库并使用JDBC进行持久层开发](nano-examples/examples/examples-03.md)
- [示例5: 进阶四 - 启用多数据源支持并添加多数据源事务处理](nano-examples/examples/examples-04.md)
- [示例6: 进阶五 - 使用Mybatis进行持久层开发](nano-examples/examples/examples-05.md)
- [示例7: 切面编程(AOP)](nano-examples/examples/examples-06.md)