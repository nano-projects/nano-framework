自定义JDBC操作
====

  有些时候，我们的代码需要更加灵活的控制，我们需要自己来控制何时获取连接、何时开启事务、何时提交事务以及回滚事务。
  NanoFramework在设计初为了考虑到了这一点，留下了这个一种自定义JDBC操作的处理，下面我们来揭开它的面纱。
  
#### 1. 前期准备

###### 我们使用上一版本的示例代码作为基础， 复制 nano-examples-jdbc-transactional 并重命名为 nano-examples-jdbc-customize

##### 1.1. 修改 artifactId
```xml
<artifactId>nano-examples-jdbc-transactional</artifactId>
```
###### 修改为
```xml
<artifactId>nano-examples-jdbc-customize</artifactId>
```

#### 2. 开始我们的自定义JDBC操作开发
##### 2.1. 修改Dao实现类
```java
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang3.ArrayUtils;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;
import org.nanoframework.examples.quickstart.dao.ElementsDao;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.orm.jdbc.JdbcAdapter;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class ElementsDaoImpl implements ElementsDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElementsDaoImpl.class);
    private static final String FIND_ALL = "select id, text from elements";
    private static final String FIND_BY_ID = FIND_ALL + " where id = ?";
    private static final String INSERT = "insert into elements (text) values (?)";
    private static final String UPDATE = "update elements set text = ? where id = ?";
    private static final String DELETE_BY_ID = "delete from elements where id = ?";
    
    private final JdbcAdapter adapter = JdbcAdapter.ADAPTER;
    
    @Override
    public List<Element> findAll() {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return Element._getMapToBeans(Arrays.<Map<String, Object>>asList(adapter.executeQuery(FIND_ALL, conn).getRows()), Element.class);
        } catch(SQLException e) {
            LOGGER.error("Find ALL Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return Collections.emptyList();
    }

    @Override
    public Element findById(long id) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            SortedMap<String, Object>[] result = adapter.executeQuery(FIND_BY_ID, Lists.newArrayList(id), conn).getRows();
            if(!ArrayUtils.isEmpty(result)) {
                return Element._getMapToBean(result[0], Element.class);
            }
            
        } catch(SQLException e) {
            LOGGER.error("Find Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return null;
    }

    @Override
    public long insert(Element el) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(INSERT, Lists.newArrayList(el.getText()), conn);
        } catch(SQLException e) {
            LOGGER.error("Insert Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }
    
    @Override
    public long update(Element el) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(UPDATE, Lists.newArrayList(el.getText(), el.getId()), conn);
        } catch(SQLException e) {
            LOGGER.error("Update Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }

    @Override
    public long deleteById(long id) {
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            return adapter.executeUpdate(DELETE_BY_ID, Lists.newArrayList(id), conn);
        } catch(SQLException e) {
            LOGGER.error("Delete Element error: {}", e.getMessage());
        } finally {
            adapter.close(conn);
        }
        
        return 0;
    }

    @Override
    public long insertBatch(List<Element> els) {
        Assert.notEmpty(els, "els list must be not empty");
        long successful = 0;
        Connection conn = null;
        try {
            conn = adapter.getConnection("quickstart");
            conn.setAutoCommit(false);
            for(Element el : els) {
                successful += adapter.executeUpdate(INSERT, Lists.newArrayList(el.getText()), conn);
            }
            
            adapter.commit(conn);
        } catch(SQLException e) {
            LOGGER.error("Insert Element error: {}", e.getMessage());
            try {
                adapter.rollback(conn);
            } catch (SQLException e1) {
                LOGGER.error("Rollback connection error: {}", e1.getMessage());
            }
            
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            adapter.close(conn);
        }
        
        return successful;
    }
}
```

###### 在这里，我们使用JdbcAdapter.ADAPTER对象来替代JdbcManager来自行控制连接的相关操作。
###### 使用这种方式，我们必须要手动的回收连接，否则连接池中的连接将很快耗尽。
###### 对JDBC操作要求比较高的应用，在不失灵活性和代码复杂性的前提下我们可以使用这种方式来解决一些问题。

#### 3. 启动服务
##### 3.1. Run Bootstrap
##### 3.2 单元测试
###### 使用RestTest和JdbcTransactionalTest 2个单元测试类进行Adapter方式的测试
###### 测试结果因为与之前几篇中的内容一样，所以不再这里列出，需要参考的请查看前几篇文章中的描述

源码
----
- [nano-examples-jdbc-customize](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jdbc-customize)