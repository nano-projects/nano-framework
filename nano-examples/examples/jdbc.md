JDBC起步
====

  基于前篇Restful，我们将原有的内存存储数据的模式修改为数据库的模式，这里我们使用NanoFramework中对JDBC的封装模块 nano-orm-jdbc 的使用进行介绍

####1. 前期准备

###### 为了简化示例代码，这里使用了H2来作为数据库。我们只需要在pom文件中增加h2的依赖并添加启动类即可
###### 按照惯例，我们使用前一个示例版本作为本示例的基础， 复制 nano-examples-restful 并重名为 nano-examples-jdbc

##### 1.1. 修改artifactId
```xml
<artifactId>nano-examples-restful</artifactId>
```
###### 修改为
```xml
<artifactId>nano-examples-jdbc</artifactId>
```

##### 1.2. 在pom.xml中添加H2依赖
```xml
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<version>1.4.191</version>
</dependency>
```

###### 并在 src/main/java 下编写H2数据库的启动监听类
###### org.nanoframework.examples.quickstart.listener.H2DBServerListener
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

###### 同时我们还需要在web.xml中添加H2的listener和servlet
```xml
<listener>
	<listener-class>org.nanoframework.examples.quickstart.listener.H2DBServerListener</listener-class>
</listener>
```

```xml
<servlet>
	<servlet-name>H2Console</servlet-name>
	<servlet-class>org.h2.server.web.WebServlet</servlet-class>
	<init-param>
		<param-name>webAllowOthers</param-name>
		<param-value></param-value>
	</init-param>
	<init-param>
		<param-name>trace</param-name>
		<param-value></param-value>
	</init-param>
	<load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
	<servlet-name>H2Console</servlet-name>
	<url-pattern>/console/*</url-pattern>
</servlet-mapping>
```

###### 现在我们可以启动服务并为H2数据库添加数据库和数据表
###### 和之前一样，我们使用Bootstrap类来启动应用
###### 启动完成后，我们就可以访问 http://localhost:8080/quickstart/console 来配置我们的数据库了
###### 我们使用默认的配置， 密码直接用test即可，当然也可以自行设置，但是不能为空
###### 登陆后我们就可以创建我们的测试表了，表结构如下
```sql
create table elements (
id long primary key auto_increment not null,
text varchar(256) not null
);
```

##### 1.3. 添加数据库连接池

###### 在NanoFramework中，集成了2种数据库连接池，druid和c3p0，默认使用druid，在此我们需要在pom.xml中添加druid的依赖以支持jdbc的连接池处理
```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid</artifactId>
	<version>1.0.15</version>
</dependency>
```

#### 2. 正式开始我们的JDBC开发
###### 首先我们需要为我们的应用添加数据库的连接池配置(即数据源), 我们需要在 src/main/resources 下添加 quickstart.jdbc.properties 属性文件, 内容如下
```properties
JDBC.environment.id=quickstart

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
###### 并修改 context.properties, 增加如下属性
```properties
mapper.package.jdbc=/quickstart.jdbc.properties
```
###### 多数据源支持配置示例，在本示例中请不要添加以下属性，可以自行进行试验
```properties
mapper.package.root=quickstart0,quickstart1
mapper.package.jdbc.quickstart0=/quickstart0.jdbc.properties
mapper.package.jdbc.quickstart1=/quickstart1.jdbc.properties
```

#### 3. 开发我们的ORM层代码
###### 为了规范化代码结构，我们需要创建Dao和对应的实现类来完成对数据库的操作，如下
###### 创建接口 org.nanoframework.examples.quickstart.dao.ElementsDao
```java
import java.util.List;
import org.nanoframework.examples.quickstart.dao.impl.ElementsDaoImpl;
import org.nanoframework.examples.quickstart.domain.Element;
import com.google.inject.ImplementedBy;

@ImplementedBy(ElementsDaoImpl.class)
public interface ElementsDao {
    /**
     * 获取所有Element对象
     * @return All Element
     */
    List<Element> findAll();
    
    /**
     * 根据元素Id获取Element对象
     * 
     * @param id The Element ID
     * @return Element
     */
    Element findById(long id);
    
    /**
     * 新增Element
     * 
     * @param el Element
     * @return insert successful
     */
    long insert(Element el);
    
    /**
     * 更新Element
     * 
     * @param el Element
     * @return update successful
     */
    long update(Element el);
    
    /**
     * 删除Element
     * 
     * @param id The Element ID
     * @return delete successful
     */
    long deleteById(long id);
}
```

###### 创建实现类 org.nanoframework.examples.quickstart.dao.impl.ElementsDaoImpl
```java
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.examples.quickstart.dao.ElementsDao;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.orm.jdbc.binding.GlobalJdbcManager;
import org.nanoframework.orm.jdbc.binding.JdbcManager;
import com.google.common.collect.Lists;

public class ElementsDaoImpl implements ElementsDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElementsDaoImpl.class);
    private static final String FIND_ALL = "select id, text from elements";
    private static final String FIND_BY_ID = FIND_ALL + " where id = ?";
    private static final String INSERT = "insert into elements (text) values (?)";
    private static final String UPDATE = "update elements set text = ? where id = ?";
    private static final String DELETE_BY_ID = "delete from elements where id = ?";
    
    private final JdbcManager manager = GlobalJdbcManager.get("quickstart");
    
    @Override
    public List<Element> findAll() {
        try {
            return Element._getMapToBeans(Arrays.<Map<String, Object>>asList(manager.executeQuery(FIND_ALL).getRows()), Element.class);
        } catch(SQLException e) {
            LOGGER.error("Find ALL Element error: {}", e.getMessage());
        }
        
        return Collections.emptyList();
    }

    @Override
    public Element findById(long id) {
        try {
            SortedMap<String, Object>[] result = manager.executeQuery(FIND_BY_ID, Lists.newArrayList(id)).getRows();
            if(!ArrayUtils.isEmpty(result)) {
                return Element._getMapToBean(result[0], Element.class);
            }
            
        } catch(SQLException e) {
            LOGGER.error("Find Element error: {}", e.getMessage());
        }
        
        return null;
    }

    @Override
    public long insert(Element el) {
        try {
            return manager.executeUpdate(INSERT, Lists.newArrayList(el.getText()));
        } catch(SQLException e) {
            LOGGER.error("Insert Element error: {}", e.getMessage());
        }
        
        return 0;
    }

    @Override
    public long update(Element el) {
        try {
            return manager.executeUpdate(UPDATE, Lists.newArrayList(el.getText(), el.getId()));
        } catch(SQLException e) {
            LOGGER.error("Update Element error: {}", e.getMessage());
        }
        
        return 0;
    }

    @Override
    public long deleteById(long id) {
        try {
            return manager.executeUpdate(DELETE_BY_ID, Lists.newArrayList(id));
        } catch(SQLException e) {
            LOGGER.error("Delete Element error: {}", e.getMessage());
        }
        
        return 0;
    }
}
```

#### 4. 修改组件服务
###### 为了简化开发，我们在此不编写Service类来做逻辑处理，在正式开发过程中我们需要使用Service来做逻辑处理
###### 修改RestComponentImpl的代码实现，如下
```java
    private static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    private static final Map<String, Object> OK_MAP = OK._getBeanToMap();
    private static final ResultMap FAIL = ResultMap.create("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
    
    @Inject
    private ElementsDao elementsDao;
    
    @Override
    public Map<String, Object> getElements() {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        result.put("value", elementsDao.findAll());
        return result;
    }

    @Override
    public Map<String, Object> getElement(Long id) {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        Element element = elementsDao.findById(id);
        if(element != null) {
            result.put("value", element);
        }
        
        return result;
    }

    @Override
    public ResultMap postElement(Element el) {
        if(el.getId() != null) {
            return ResultMap.create("POST请求 Element Id不可存在.", HttpStatus.BAD_REQUEST);
        }
        
        if(elementsDao.insert(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap putElement(Element el) {
        if(el.getId() == null) {
            return ResultMap.create("PUT请求 Element Id必须存在", HttpStatus.BAD_REQUEST);
        }
        
        if(elementsDao.update(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap deleteElement(Long id) {
        if(elementsDao.deleteById(id) > 0) {
            return OK;
        }
        
        return FAIL;
    }
```

#### 5. 启动服务
##### 5.1. Run Bootstrap
##### 5.2. 模拟请求
###### [下载](https://curl.haxx.se/download.html)CURL工具, 选择使用操作系统的版本
###### CURL使用[参考文档](http://blog.csdn.net/lipei1220/article/details/8536520)(Windows版)

###### GET请求模拟
```shell
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[],"info":"OK","status":200}
```

###### POST请求模拟
```shell
curl -i http://localhost:8080/quickstart/rest/elements -XPOST -d 'el={"text":"new hello"}'
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[{"id":1,"text":"new hello"}],"info":"OK","status":200}
```

###### PUT请求模拟
```shell
curl -i http://localhost:8080/quickstart/rest/elements -XPUT -d 'el={"id":1,"text":"update hello"}'
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[{"id":1,"text":"update hello"}],"info":"OK","status":200}
```

###### DELETE请求模拟
```shell
curl -i http://localhost:8080/quickstart/rest/elements/1 -XDELETE
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[],"info":"OK","status":200}
```

源码
----
- [nano-examples-jdbc](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jdbc)
