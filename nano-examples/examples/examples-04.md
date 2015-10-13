示例5: 进阶四 - 启用多数据源支持并添加多数据源事务处理
====

####1、修改配置并添加新的数据源配置
######1.1、修改context.properties
```properties
# 将属性mapper.package.jdbc=/examples-jdbc.properties修改为如下配置
mapper.package.root=examples,examples2
mapper.package.jdbc.examples=/examples-jdbc.properties
mapper.package.jdbc.examples2=/examples2-jdbc.properties
```
######1.2、添加examples2-jdbc.properties
```properties
JDBC.environment.id=examples2

JDBC.pool.type=DRUID
JDBC.driver=org.h2.Driver
JDBC.url=jdbc:h2:~/test2
JDBC.username=test
JDBC.password=test
JDBC.autoCommit=false

# 省略连接池的属性配置
```

####2、新建数据库及表结构
######2.1、启动服务并访问 http://ip:port/first-webapp/console，使用以下信息登录
```properties
JDBC.url=jdbc:h2:~/test2
JDBC.username=test
JDBC.password=test
```
######2.2、建表
```sql
create table t_nano_test (
id int primary key,
name varchar(255)
)
```

####3、添加多数据源操作代码
######3.1、修改JdbcExamplesDao及JdbcExamplesDaoImpl，添加delete方法
```java
long delete(int id) throws SQLException;
```
```java
private final String deleteById = "DELETE FROM T_NANO_TEST WHERE ID = ? ";

@Override
public long delete(int id) throws SQLException {
	return get(DataSource.EXAMPLES.value()).executeUpdate(deleteById, new ArrayList<Object>() {
		private static final long serialVersionUID = 1L; { 
		add(id); 
	}});
}
```
######3.2、增加JdbcExamplesMoveDao及JdbcExamplesMoveDaoImpl
```java
@ImplementedBy(JdbcExamplesMoveDaoImpl.class)
public interface JdbcExamplesMoveDao { 
	long insert(Test test) throws SQLException;
}
```
```java
public class JdbcExamplesMoveDaoImpl implements JdbcExamplesMoveDao {

	private final String insert = "INSERT INTO T_NANO_TEST(ID, NAME) VALUES (?, ?) ";
	
	@Override
	public long insert(Test test) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(test.getId());
		values.add(test.getName());
		return get(DataSource.EXAMPLES2).executeUpdate(insert, values);
	}
}
```
######3.3、添加Component实现，修改JdbcExamplesComponent及JdbcExamplesComponentImpl
```java
@RequestMapping("/persist/move/{id}")
Object move(@PathVariable("id") Integer id);
```
```java
@Inject
private JdbcExamplesMoveDao examplesMoveDao;

@JdbcTransactional(envId = {DataSource.EXAMPLES, DataSource.EXAMPLES2})
@Override
public Object move(Integer id) {
	try {
		Test test = examplsDao.select(id);
		if(test == null) {
			return ResultMap.create(200, "Not Found Data", "WARNING");
		} else {
			if(examplesMoveDao.insert(test) > 0) {
				examplsDao.delete(id);
			}
		}
	} catch(Exception e) {
		throw new ComponentInvokeException(e.getMessage(), e);
	}
	
	return ResultMap.create(200, "OK", "SUCCESS");
}
```

####4、启动服务后进行以下操作
* http://ip:port/first-webapp/jdbc/persist/move/1
* http://ip:port/first-webapp/console并登陆test2库查询迁移数据

####5、至此，多数据源及多数据源事务的示例就开发完了

- [首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)
- [上一节](examples-03.md)
- [下一节](examples-05.md)

