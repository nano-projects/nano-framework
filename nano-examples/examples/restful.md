开启Restful之旅
----

####1. 复制 nano-examples-jetty-support 并重名为 nano-examples-restful

####2. 编辑 pom.xml
#####2.1. 修改 artifactId
```xml
<artifactId>nano-examples-jetty-support</artifactId>
```
##### 修改为
```xml
<artifactId>nano-examples-restful</artifactId>
```

####3. 创建Restful(GET, POST, PUT, DELETE)服务
#####3.1. 创建 Element对象
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

#####3.2. 创建Rest组件服务
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

#####3.3. 添加GET请求处理
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

#####3.4. 添加POST请求
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

#####3.5. 添加PUT请求
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

#####3.6. 添加DELETE请求
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

####4. 启动服务
##### 4.1. Run Bootstrap
##### 4.2. 模拟请求
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
{"message":"OK","value":[{"id":1,"text":"hello1"}],"info":"OK","status":200}
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
- [nano-examples-restful](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-restful)
