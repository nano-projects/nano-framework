开启Restful之旅
----

#### 1. 复制 nano-examples-jetty-support 并重名为 nano-examples-restful

#### 2. 编辑 pom.xml
##### 2.1. 修改 artifactId
```xml
<artifactId>nano-examples-jetty-support</artifactId>
```
##### 修改为
```xml
<artifactId>nano-examples-restful</artifactId>
```

#### 3. 创建Restful(GET, POST, PUT, DELETE)服务
##### 3.1. 创建 Element对象
###### org.nanoframework.examples.quickstart.domain.Element
```java
package org.nanoframework.examples.quickstart.domain;

import org.nanoframework.commons.entity.BaseEntity;

public class Element extends BaseEntity {
    private static final long serialVersionUID = 1370524425856113002L;

    private Long id;
    private String text;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
```

##### 3.2. 创建Rest组件服务
###### 接口: org.nanoframework.examples.quickstart.component.RestComponent
```java
package org.nanoframework.examples.quickstart.component;

import org.nanoframework.core.component.stereotype.Component;
import org.nanoframework.examples.quickstart.component.impl.RestComponentImpl;
import com.google.inject.ImplementedBy;

@Component
@ImplementedBy(RestComponentImpl.class)
@RequestMapping("/rest")
public interface RestComponent {

}

```
###### 类: org.nanoframework.examples.quickstart.component.impl.RestComponentImpl
```java
package org.nanoframework.examples.quickstart.component.impl;

import org.nanoframework.examples.quickstart.component.RestComponent;

public class RestComponentImpl implements RestComponent {
    private static final AtomicLong INDEX = new AtomicLong();
    private static final ConcurrentMap<Long, Element> ELEMENTS = Maps.newConcurrentMap();
    private static final ResultMap OK = ResultMap.create("OK", HttpStatus.OK);
    private static final Map<String, Object> OK_MAP = OK._getBeanToMap();
}

```

##### 3.3. 添加GET请求处理
###### RestComponent接口中增加 getElements 和 getElement 方法
```java
    /**
     * 获取所有Element对象
     * 
     * @return All element
     */
    @RequestMapping(value = "/elements", method = RequestMethod.GET)
    Map<String, Object> getElements();
    
    /**
     * 根据元素Id获取Element对象
     * 
     * @param id the element id
     * @return id of elements
     */
    @RequestMapping(value = "/elements/{id}", method = RequestMethod.GET)
    Map<String, Object> getElement(@PathVariable("id") Long id);
```

###### RestComponentImpl对接口方法进行实现
```java
    @Override
    public Map<String, Object> getElements() {
        Map<String, Object> result = Maps.newHashMap(OK_MAP);
        result.put("value", ELEMENTS.values());
        return result;
    }

    @Override
    public Map<String, Object> getElement(Long id) {
        Map<String, Object> result = new ConcurrentHashMap<>(OK_MAP);
        if(ELEMENTS.containsKey(id)) {
            result.put("value", ELEMENTS.get(id));
        }
        
        return result;
    }
```

##### 3.4. 添加POST请求
###### RestComponent接口中增加 postElement 方法
```java
    /**
     * 新增Element
     * 
     * @param el Element对象(JSON格式)
     * @return ResultMap
     */
    @RequestMapping(value = "/elements", method = RequestMethod.POST)
    ResultMap postElement(@RequestParam("el") Element el);
```

###### RestComponentImpl对接口方法进行实现
```java
    @Override
    public ResultMap postElement(Element el) {
        if(el.getId() != null) {
            return ResultMap.create("POST请求 Element Id不可存在.", HttpStatus.BAD_REQUEST);
        }
        
        el.setId(INDEX.incrementAndGet());
        ELEMENTS.put(el.getId(), el);
        
        return OK;
    }

```

##### 3.5. 添加PUT请求
###### RestComponent接口中增加 putElement 方法
```java
    /**
     * 更新Element
     * 
     * @param el Element对象(JSON格式)
     * @return ResultMap
     */
    @RequestMapping(value = "/elements", method = RequestMethod.PUT)
    ResultMap putElement(@RequestParam("el") Element el);
```

###### RestComponentImpl对接口方法进行实现
```java
    @Override
    public ResultMap putElement(Element el) {
        if(el.getId() == null) {
            return ResultMap.create("PUT请求 Element Id必须存在", HttpStatus.BAD_REQUEST);
        }
        
        ELEMENTS.put(el.getId(), el);
        
        return OK;
    }

```

##### 3.6. 添加DELETE请求
###### RestComponent接口中增加 deleteElement 方法
```java
    /**
     * 删除Element
     * 
     * @param id Element ID
     * @return ResultMap
     */
    @RequestMapping(value = "/elements/{id}", method = RequestMethod.DELETE)
    ResultMap deleteElement(@PathVariable("id") Long id);
```

###### RestComponentImpl对接口方法进行实现
```java
    @Override
    public ResultMap deleteElement(Long id) {
        Element el = ELEMENTS.remove(id);
        if(el == null) {
            return ResultMap.create("Element对象不存在", HttpStatus.BAD_REQUEST);
        }
        
        return OK;
    }

```

#### 4. 启动服务
##### 4.1. Run Bootstrap

#### 5. 编写测试类模拟请求
###### 为了能够快速的进行测试，我们需要在pom.xml中添加httpclient依赖
```xml
    <dependency>
		<groupId>org.nanoframework</groupId>
		<artifactId>nano-ext-httpclient</artifactId>
		<version>1.3.4</version>
	</dependency>
```
###### 为了能查看日志输出，我们需要添加Log4j相关的依赖
```xml
    <dependency>
		<groupId>org.slf4j</groupId>
		<artifactId>slf4j-api</artifactId>
		<version>1.7.7</version>
	</dependency>
	<dependency>
		<groupId>org.apache.logging.log4j</groupId>
		<artifactId>log4j-api</artifactId>
		<version>2.5</version>
	</dependency>
	<dependency>
		<groupId>org.apache.logging.log4j</groupId>
		<artifactId>log4j-core</artifactId>
		<version>2.5</version>
	</dependency>
	<dependency>
		<groupId>org.apache.logging.log4j</groupId>
		<artifactId>log4j-slf4j-impl</artifactId>
		<version>2.5</version>
	</dependency>
```
##### 5.1. 编写单元测试
###### 在 src/test/java 下建立测试类 org.nanoframework.examples.quickstart.component.RestTest
```java
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.examples.quickstart.domain.Element;
import org.nanoframework.extension.httpclient.HttpClient;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.inject.Guice;

public class RestTest {
    private final Logger logger = LoggerFactory.getLogger(RestTest.class);
    private HttpClient httpClient;
    
    @Before
    public void before() {
        httpClient = Guice.createInjector().getInstance(HttpClient.class);
    }
    
    private void getAllTest() throws IOException {
        logger.debug("Get ALL: {}", httpClient.httpGetRequest("http://localhost:8080/quickstart/rest/elements").entity);
    }
    
    private void getByIdTest(long id) throws IOException {
        logger.debug("GET by ID [{}]: {}", id, httpClient.httpGetRequest("http://localhost:8080/quickstart/rest/elements/" + id).entity);
    }
    
    private void postTest() throws IOException {
        Element el = new Element();
        el.setText("new hello");
        Map<String, String> params = Maps.newHashMap();
        params.put("el", JSON.toJSONString(el));
        logger.debug(httpClient.httpPostRequest("http://localhost:8080/quickstart/rest/elements", params).entity);
        
        getAllTest();
        getByIdTest(1);
    }
    
    private void putTest() throws IOException {
        Element el = new Element();
        el.setId(1L);
        el.setText("Update Hello");
        Map<String, String> params = Maps.newHashMap();
        params.put("el", JSON.toJSONString(el));
        logger.debug(httpClient.httpPutRequest("http://localhost:8080/quickstart/rest/elements", params).entity);
        
        getAllTest();
        getByIdTest(1L);
    }
    
    private void deleteTest(long id) throws IOException {
        logger.debug(httpClient.httpDeleteRequest("http://localhost:8080/quickstart/rest/elements/" + id).entity);
        getAllTest();
        getByIdTest(id);

    }
    
    @Test
    public void httpTest() throws IOException {
        logger.debug("POST Request");
        postTest();
        logger.debug("");
        logger.debug("PUT Request");
        putTest();
        logger.debug("DELETE Request");
        logger.debug("");
        deleteTest(1);
    }
}
```
###### 执行单元测试，查看输出日志信息
```logger
POST Request 
{"info":"OK","message":"OK","status":200} 
Get ALL: {"message":"OK","value":[{"id":1,"text":"new hello"}],"info":"OK","status":200} 
GET by ID [1]: {"message":"OK","value":{"id":1,"text":"new hello"},"info":"OK","status":200} 
```
```logger
PUT Request 
{"info":"OK","message":"OK","status":200} 
Get ALL: {"message":"OK","value":[{"id":1,"text":"Update Hello"}],"info":"OK","status":200} 
GET by ID [1]: {"message":"OK","value":{"id":1,"text":"Update Hello"},"info":"OK","status":200} 
```
```logger
DELETE Request 
{"info":"OK","message":"OK","status":200} 
Get ALL: {"message":"OK","value":[],"info":"OK","status":200} 
GET by ID [1]: {"message":"OK","info":"OK","status":200} 
```

源码
----
- [nano-examples-restful](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-restful)
