示例6: 进阶五 - 使用Mybatis进行持久层开发
====

####1、添加依赖
######1.1、修改pom.xml
```xml
<dependency>
	<groupId>org.nanoframework</groupId>
	<artifactId>nano-orm-mybatis</artifactId>
	<version>${nano-version}</version>
</dependency>
```
######1.2、在src/main/resources下添加MyBatis数据源 examples-mybatis.properties
```properties
mybatis.environment.id=mybatis-examples
mapper.package.name=org.nanoframework.examples.first.webapp.mapper

JDBC.pool.type=DRUID
JDBC.driver=org.h2.Driver
JDBC.url=jdbc:h2:~/test2
JDBC.username=test
JDBC.password=test
JDBC.autoCommit=false

# 省略连接池的属性配置
```
######1.3、修改context.properties
```properties
# mapper.package.root=examples,examples2修改为如下内容
mapper.package.root=examples,examples2,mybatis-examples
# 添加如下属性
mapper.package.jdbc.mybatis-examples=/examples-mybatis.properties
```

####2、开发基于Mybatis的服务
######2.1、在/src/main/java/org.nanoframework.examples.first.webapp.mapper下添加ExampleMapper接口
```java
public interface ExampleMapper {
	List<Test> select();
}
```
######2.2、在/src/main/resources/org.nanoframework.examples.first.webapp.mapper下添加ExampleMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="org.nanoframework.examples.first.webapp.mapper.ExampleMapper">
	<select id="select" resultType="org.nanoframework.examples.first.webapp.domain.Test">
		select id, name from t_nano_test
	</select>
</mapper>
```
######2.3、添加MybatisExampleComponent组件接口及实现类
```java
@Component
@ImplementedBy(MybatisExampleComponentImpl.class)
@RequestMapping("/mybatis")
public interface MybatisExampleComponent {
	@RequestMapping("/find/all")
	Object findAll();
}
```
```java
public class MybatisExampleComponentImpl implements MybatisExampleComponent {

	@Inject
	private ExampleMapper exampleMapper;
	
	@Override
	public Object findAll() {
		List<Test> testList = exampleMapper.select();
		Map<String, Object> map = ResultMap.create(200, "Find all Test", "SUCCESS")._getBeanToMap();
		map.put("records", testList.size());
		map.put("rows", testList);
		return map;
	}

}
```

####3、启动服务后进行以下操作
* http://ip:port/first-webapp/mybatis/find/all

####4、至此，基于Mybatis的组件服务开发完成

- [首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)
- [上一节](examples-04.md)
- [下一节](examples-06.md)