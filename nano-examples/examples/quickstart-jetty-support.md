使用内嵌Jetty来替换Tomcat等容器
----

####1. 复制quickstart并重名为jetty-support

####2. 编辑 pom.xml
#####2.1. 修改依赖
```xml
<dependency>
	<groupId>org.nanoframework</groupId>
	<artifactId>nano-webmvc</artifactId>
	<version>1.3.3</version>
</dependency>
```
##### 替换为
```xml
<dependency>
	<groupId>org.nanoframework</groupId>
	<artifactId>nano-server</artifactId>
	<version>1.3.3</version>
</dependency>
```

#####2.2. 修改resource
```xml
<resource>
	<directory>src/main/webapp</directory>
</resource>
```
##### 添加 targetPath
```xml
<resource>
	<directory>src/main/webapp</directory>
	<targetPath>${project.basedir}/webRoot/</targetPath>
</resource>
```

####3. 在 src/main/webapp/WEB-INF下添加 jetty.xml 及 webdefault.xml
- [jetty.xml](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jetty-support/src/main/webapp/WEB-INF/jetty.xml)
- [webdefault.xml](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jetty-support/src/main/webapp/WEB-INF/webdefault.xml)

####4. 添加启动器
#####4.1. 在src/main/java下创建类 org.nanoframework.examples.quickstart.Bootstrap
```java
package org.nanoframework.examples.quickstart;

import org.nanoframework.server.JettyCustomServer;

public class Bootstrap {
    public static void main(String[] args) {
        JettyCustomServer.DEFAULT.bootstrap(args);
    }
}
```

####5. 修改组件实现类 HelloWorldComponentImpl
```java
@Override
public String hello() {
    return "Hello NanoFramework";
}
```
##### 修改为
```java
@Override
public String hello() {
    return "Hello NanoFramework Jetty Support";
}
```

####5. Maven Update

####6. 启动服务
#####6.1. Run Bootstrap
#####6.2. 访问服务:  http://{yourIP}:{yourPort}/quickstart/hello

源码
----
- [nano-examples-jetty-support](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jetty-support)