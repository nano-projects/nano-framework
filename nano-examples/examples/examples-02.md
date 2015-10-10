示例3: 进阶二 - Restful API与带参服务
====

####1、修改HelloWorldComponent，添加Restful风格服务
######1.1、添加接口
```java
import org.nanoframework.core.component.stereotype.bind.PathVariable;
import org.nanoframework.core.component.stereotype.bind.RequestMethod;
...

@RequestMapping(value = "/hello/{value}", method = RequestMethod.GET)
Object byRestfulApiByGet(@PathVariable("value") String value);
```

######1.2、添加实现
```java
@Override
public Object byRestfulApiByGet(String value) {
	return "Hello Nano Framework by Restful API and GET method, this value is " + value;
}
```

######1.3、验证Restful
* 启动服务并访问http://ip:port/first-webapp/first/hello/test
* 返回结果: Hello Nano Framework by Restful API and GET method, this value is test

####2、修改HelloWorldComponent，添加带参数的Restful风格服务
######2.1、添加接口
```java
import org.nanoframework.core.component.stereotype.bind.RequestParam;
...

@RequestMapping(value = "/hello/{value}", method = RequestMethod.GET)
Object byRestfulApiByGetParam(@PathVariable("value") String value, @RequestParam(name = "name") String name);
```

######2.2、添加实现
```java
@Override
public Object byRestfulApiByGetParam(String value, String name) {
	return "Hello Nano Framework by Restful API and GET method, this value is " + value + " and name is " + name;
}
```
######2.3、验证Restful
* 启动服务并访问http://ip:port/first-webapp/first/hello/test?name=hello
* 返回结果: Hello Nano Framework by Restful API and GET method, this value is test and name is hello

[首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)

[上一节](examples-01.md)

[下一节](examples-03.md)
