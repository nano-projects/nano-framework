示例4: 进阶三 - 添加H2数据库并使用JDBC进行持久层开发
====

####1、添加H2数据库依赖
######1.1、修改pom.xml
```xml
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<version>${h2-version}</version>
</dependency>
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid</artifactId>
	<version>${druid-version}</version>
</dependency>
```
######1.2、添加监听类(org.nanoframework.examples.first.webapp.listener.H2DBServerListener)
```java
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.h2.tools.Server;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

public class H2DBServerListener implements ServletContextListener {

	private Logger LOG = LoggerFactory.getLogger(H2DBServerListener.class);
	private Server server;
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {  
			LOG.info("正在启动H2数据库...");
			server = Server.createTcpServer().start(); 
			LOG.info("H2数据库启动完成");
		} catch (SQLException e) {  
			throw new RuntimeException("启动H2数据库出错：" + e.getMessage(), e);  
		}  
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if(server != null) {
			server.stop();
			server = null;
		}
 	}

}
```
######1.3、修改web.xml，添加listener
```xml
<listener>
	<listener-class>org.nanoframework.examples.first.webapp.listener.H2DBServerListener</listener-class>
</listener>
```
######1.4、启动服务并访问 http://ip:port/first-webapp/console
######1.5、添加数据库并新建测试表
```properties
JDBC.url=jdbc:h2:~/test
JDBC.username=test
JDBC.password=test
```
```sql
create table t_nano_test (
id int primary key,
name varchar(255)
)
```

####2、开发基于JDBC的服务
######2.1、添加数据源属性文件examples-jdbc.properties(参照nano-orm-jdbc/src/main/resources/jdbc-templet.properties模板)
```properties
JDBC.environment.id=examples

JDBC.pool.type=DRUID
JDBC.driver=org.h2.Driver
JDBC.url=jdbc:h2:~/test
JDBC.username=test
JDBC.password=test
JDBC.autoCommit=false

#######################################################################################
####################        DRUID连接池配置        ######################################
#######################################################################################
# 初始化连接数量
druid.initialSize=1

# 最大并发连接数
druid.maxActive=5

# 最大空闲连接数
druid.maxIdle=5

# 最小空闲连接数
druid.minIdle=1

# 配置获取连接等待超时的时间
druid.maxWait=30000

# 超过时间限制是否回收 
druid.removeAbandoned=true

# 超过时间限制多长 
druid.removeAbandonedTimeout=180

# 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 
druid.timeBetweenEvictionRunsMillis=10000

# 配置一个连接在池中最小生存的时间，单位是毫秒
druid.minEvictableIdleTimeMillis=60000

# 用来检测连接是否有效的sql，要求是一个查询语句
druid.validationQuery=select 1

# 申请连接的时候检测
druid.testWhileIdle=true

# 申请连接时执行validationQuery检测连接是否有效，配置为true会降低性能
druid.testOnBorrow=false

# 归还连接时执行validationQuery检测连接是否有效，配置为true会降低性能
druid.testOnReturn=false

# 打开PSCache，并且指定每个连接上PSCache的大小
druid.poolPreparedStatements=true

druid.maxPoolPreparedStatementPerConnectionSize=20

# 属性类型是字符串，通过别名的方式配置扩展插件，
# 常用的插件有： 
#	监控统计用的filter:stat 
#	日志用的filter:log4j  
# 	防御SQL注入的filter:wall
druid.filters=stat
```
######2.2、添加Domain
```java
import org.nanoframework.commons.entity.BaseEntity;

public class Test extends BaseEntity {
	private Integer id;
	private String name;

	public Test() {}
	public Test(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
```
######2.3、添加Dao接口与实现
```java
import org.nanoframework.examples.first.webapp.dao.impl.JdbcExamplesDaoImpl;
import org.nanoframework.examples.first.webapp.domain.Test;
import com.google.inject.ImplementedBy;
...

@ImplementedBy(JdbcExamplesDaoImpl.class)
public interface JdbcExamplesDao {
	long insert(Test test) throws SQLException;
	List<Test> select() throws SQLException;
	Test select(int id) throws SQLException;
}
```
```java
import static org.nanoframework.orm.jdbc.binding.GlobalJdbcManager.get;
import org.nanoframework.examples.first.webapp.constant.DataSource;
import org.nanoframework.examples.first.webapp.dao.JdbcExamplesDao;
import org.nanoframework.examples.first.webapp.domain.Test;
import org.nanoframework.orm.jdbc.jstl.Result;
...

public class JdbcExamplesDaoImpl implements JdbcExamplesDao {

	private final String insert = "INSERT INTO T_NANO_TEST(ID, NAME) VALUES (?, ?) ";
	private final String select = "SELECT ID, NAME FROM T_NANO_TEST ";
	private final String selectById = "SELECT ID, NAME FROM T_NANO_TEST WHERE ID = ? ";
	
	@Override
	public long insert(Test test) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(test.getId());
		values.add(test.getName());
		return get(DataSource.EXAMPLES.value()).executeUpdate(insert);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Test> select() throws SQLException {
		Result result = get(DataSource.EXAMPLES.value()).executeQuery(select);
		if(result.getRowCount() > 0) {
			List<Test> tests = new ArrayList<>();
			Arrays.asList(result.getRows()).forEach(row -> tests.add(Test._getMapToBean(row, Test.class)));
		} 
		
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Test select(int id) throws SQLException {
		List<Object> values = new ArrayList<>();
		values.add(id);
		Result result = get(DataSource.EXAMPLES.value()).executeQuery(selectById, values);
		if(result.getRowCount() > 0) {
			return Test._getMapToBean(result.getRows()[0], Test.class);
		}
		
		return null;
	}

}
```
######2.4、添加Component接口与实现
```java
@Component
@ImplementedBy(JdbcExamplesComponentImpl.class)
@RequestMapping("/jdbc")
public interface JdbcExamplesComponent {
	@RequestMapping("/persist")
	Object persist(@RequestParam(name = "id") Integer id, @RequestParam(name = "name") String name);
	
	@RequestMapping("/find/all")
	Object findAll();
	
	@RequestMapping("/find/{id}")
	Object findById(@PathVariable("id") Integer id);
}
```
```java
public class JdbcExamplesComponentImpl implements JdbcExamplesComponent {
	private Logger LOG = LoggerFactory.getLogger(JdbcExamplesComponentImpl.class);
	
	@Inject
	private JdbcExamplesDao examplsDao;
	
	@JdbcTransactional(envId = DataSource.EXAMPLES_STRING)
	@Override
	public Object persist(Integer id, String name) {
		Test test = new Test(id, name);
		try { 
			long changed = examplsDao.insert(test);
			if(changed > 0)
				return ResultMap.create(200, "写入数据库成功", "SUCCESS");
			else 
				return ResultMap.create(200, "写入数据库失败", "ERROR");
		} catch(Exception e) {
			LOG.error("写入数据库异常: " + e.getMessage(), e);
			return ResultMap.create(500, "写入数据库异常: " + e.getMessage(), e.getClass().getName());
		}
	}

	@Override
	public Object findAll() {
		try {
			List<Test> testList = examplsDao.select();
			Map<String, Object> map = ResultMap.create(200, "OK", "SUCCESS")._getBeanToMap();
			map.put("records", testList.size());
			map.put("rows", testList);
			return map;
		} catch(Exception e) {
			LOG.error("查询数据异常: " + e.getMessage(), e);
			return ResultMap.create(500, "查询数据异常: " + e.getMessage() , e.getClass().getName());
		}
	}

	@Override
	public Object findById(Integer id) {
		try {
			Test test = examplsDao.select(id);
			Map<String, Object> map = ResultMap.create(200, "OK", "SUCCESS")._getBeanToMap();
			map.put("data", test);
			return map;
		} catch(Exception e) {
			LOG.error("查询数据异常: " + e.getMessage(), e);
			return ResultMap.create(500, "查询数据异常: " + e.getMessage() , e.getClass().getName());
		}
	}

}
```

####3、启动服务后进行以下操作
* http://ip:port/first-webapp/jdbc/persist?id=1&name=test
* http://ip:port/first-webapp/jdbc/find/all
* http://ip:port/first-webapp/jdbc/find/1

####4、至此，最基本的JDBC示例开发就完成了

- [首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)
- [上一节](examples-02.md)
- [下一节](examples-04.md)

