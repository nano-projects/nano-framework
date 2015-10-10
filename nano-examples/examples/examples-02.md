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

@RequestMapping(value = "/hello/param/{value}", method = RequestMethod.GET)
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
* 启动服务并访问http://ip:port/first-webapp/first/hello/param/test?name=hello
* 返回结果: Hello Nano Framework by Restful API and GET method, this value is test and name is hello

####3、对PUT、DELETE、PATCH等RequestMethod的支持
######3.1、PUT示例
```java
@RequestMapping(value = "/hello/{value}", method = RequestMethod.PUT)
Object byRestfulApiByPut(@PathVariable("value") String value, @RequestParam(name = "name") String name);
```
```java
@Override
public Object byRestfulApiByPut(String value, String name) {
	return ResultMap.create(200, "Hello Nano Framework by Restful API and PUT method, this value is " + value + " and name is " + name, "SUCCESS");
}
```
######3.2、在index.jsp中添加ajax请求代码
```html
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript">
$.ajax({
	url : '/first-webapp/first/hello/123' , 
	type : "PUT" ,
	contentType: "application/x-www-form-urlencoded; charset=utf-8" , 
	data : {
		name: 'postName'
	},
	success : function(data) {
		console.log(data);
		$('#context')[0].innerHTML = JSON.stringify(data);
	}
});

</script>
<body>
	<div id="context"></div>
</body>
</html>
```

######3.3、验证PUT请求
* 启动服务并访问http://ip:port/first-webapp
* 返回结果: {"info":"SUCCESS","message":"Hello Nano Framework by Restful API and PUT method, this value is 123 and name is postName","status":200}


[首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)

[上一节](examples-01.md)

[下一节](examples-03.md)
