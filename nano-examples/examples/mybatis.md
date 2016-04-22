体验Mybatis带来的快感
====

  基于前篇JDBC Transactional的示例，我们将JDBC的方式替换为Mybatis的方式，来体验更快、更少
  
#### 1. 前期准备

###### 复制 nano-examples-jdbc 并重命名为 nano-examples-mybatis

##### 1.1. 修改artifactId
```xml
<artifactId>nano-examples-jdbc</artifactId>
```
###### 修改为
```xml
<artifactId>nano-examples-mybaits</artifactId>
```

#### 2. 开始体验Mybatis

##### 2.1. 修改pom.xml 添加mybatis依赖
```xml
    <dependency>
		<groupId>org.nanoframework</groupId>
		<artifactId>nano-orm-mybatis</artifactId>
		<version>1.3.4</version>
	</dependency>
```

##### 2.2. 修改属性文件 quickstart.jdbc.properties 内容
```properties
JDBC.environment.id=quickstart
```
###### 替换为
```properties
mybatis.environment.id=quickstart
```
###### 增加新属性
```properties
mapper.package.name=org.nanoframework.examples.quickstart.mapper
```
###### 这个包路径代表了我们的ORM层代码所存放的路径，允许在这个包下建立子包以做代码分类

##### 2.3. 新增Mapper定义
###### 接口 org.nanoframework.examples.quickstart.mapper.ElementsMapper
```java
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.nanoframework.examples.quickstart.domain.Element;

public interface ElementsMapper {
    /**
     * 获取所有Element对象.
     * @return All Element
     */
    List<Element> findAll();
    
    /**
     * 根据元素Id获取Element对象.
     * 
     * @param id The Element ID
     * @return Element
     */
    Element findById(@Param("id") long id);
    
    /**
     * 新增Element.
     * 
     * @param el Element
     * @return insert successful
     */
    long insert(Element el);
    
    /**
     * 更新Element.
     * 
     * @param el Element
     * @return update successful
     */
    long update(Element el);
    
    /**
     * 删除Element.
     * 
     * @param id The Element ID
     * @return delete successful
     */
    long deleteById(@Param("id") long id);
    
}
```

###### 接口定义的内容基本与Jdbc方式中的Dao的定义一致，但是需要注意参数列表中的@Param注解，关于这个注解的说明可以自行查找，这里不再赘述

##### 2.4. 定义Mybatis SQL相关的xml文件
###### 在 src/main/resources 下建立文件 org.nanoframework.examples.quickstart.mapper.ElementsMapper.xml
###### 请务必注意文件的包路径，必须要与mapper接口所在路径一直，否则将无法生效
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="org.nanoframework.examples.quickstart.mapper.ElementsMapper">
	<select id="findAll" resultType="org.nanoframework.examples.quickstart.domain.Element">
		select id, text from elements
	</select>	
	
	<select id="findById" resultType="org.nanoframework.examples.quickstart.domain.Element" parameterType="java.lang.Long">
		select id, text from elements where id = #{id}
	</select>
	
	<insert id="insert" parameterType="org.nanoframework.examples.quickstart.domain.Element">
		insert into elements (text) values (#{text})
	</insert>
	
	<update id="update" parameterType="org.nanoframework.examples.quickstart.domain.Element">
		update elements set text = #{text} where id = #{id}
	</update>
	
	<delete id="deleteById" parameterType="java.lang.Long">
		delete from elements where id = #{id}
	</delete>
	
</mapper>
```

##### 2.5. 修改RestComponentImpl
###### 将原来的ElementsDao 修改为 ElementsMapper，并修改Insert Batch的实现
```java
import java.util.Map;

import org.nanoframework.examples.quickstart.component.RestComponent;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.examples.quickstart.mapper.ElementsMapper;
import org.nanoframework.orm.mybatis.MultiTransactional;
import org.nanoframework.web.server.http.status.HttpStatus;
import org.nanoframework.web.server.http.status.ResultMap;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class RestComponentImpl implements RestComponent {
    private static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    private static final Map<String, Object> OK_MAP = OK._getBeanToMap();
    private static final ResultMap FAIL = ResultMap.create("FAIL", HttpStatus.INTERNAL_SERVER_ERROR);
    
    @Inject
    private ElementsMapper elementsMapper;
    
    @Override
    public Map<String, Object> getElements() {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        result.put("value", elementsMapper.findAll());
        return result;
    }

    @Override
    public Map<String, Object> getElement(Long id) {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        Element element = elementsMapper.findById(id);
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
        
        if(elementsMapper.insert(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap putElement(Element el) {
        if(el.getId() == null) {
            return ResultMap.create("PUT请求 Element Id必须存在", HttpStatus.BAD_REQUEST);
        }
        
        if(elementsMapper.update(el) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @Override
    public ResultMap deleteElement(Long id) {
        if(elementsMapper.deleteById(id) > 0) {
            return OK;
        }
        
        return FAIL;
    }

    @MultiTransactional(envId = "quickstart")
    @Override
    public ResultMap postElements(Element[] els) {
        try {
            for(Element el : els) {
                elementsMapper.insert(el);
            }
            
            return OK;
        } catch(Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        
    }
}
```

###### 与JDBC实务处理相对应的，在Mybatis中我们使用@MultiTransactional注解来完成对Mybatis的单数据源或多数据源的实务处理

##### 2.6. 运行测试，Mybatis就这么简单

#### 3. 让编写Mybatis变的更快
##### 3.1. 新增 Elements2Mapper 接口定义
###### 直接复制 ElementsMapper 并重命名为 Elements2Mapper 即可
###### 接下来让我们编写更简单的Mybatis实现
```java
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.nanoframework.examples.quickstart.domain.Element;

public interface Elements2Mapper {
    /**
     * 获取所有Element对象.
     * @return All Element
     */
    @Select("select id, text from elements")
    List<Element> findAll();
    
    /**
     * 根据元素Id获取Element对象.
     * 
     * @param id The Element ID
     * @return Element
     */
    @Select("select id, text from elements where id = #{id}")
    Element findById(@Param("id") long id);
    
    /**
     * 新增Element.
     * 
     * @param el Element
     * @return insert successful
     */
    @Insert("insert into elements (text) values (#{text})")
    long insert(Element el);
    
    /**
     * 更新Element.
     * 
     * @param el Element
     * @return update successful
     */
    @Update("update elements set text = #{text} where id = #{id}")
    long update(Element el);
    
    /**
     * 删除Element.
     * 
     * @param id The Element ID
     * @return delete successful
     */
    @Delete("delete from elements where id = #{id}")
    long deleteById(@Param("id") long id);
    
}
```

##### 3.2. 修改RestComponentImpl
```java
    @Inject
    private ElementsMapper elementsMapper;
```
###### 修改为
```java
    @Inject
    private Elements2Mapper elementsMapper;
```

##### 3.3. 运行测试代码，更快的Mybatis就是这样的快

源码
----
- [nano-examples-mybatis](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-mybatis)
