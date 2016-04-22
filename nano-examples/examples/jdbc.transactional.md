JDBC事务管理初识
====
  引用他人的话语: 
  事务，也是数据库事务，指的是作为单个逻辑工作单元执行的一系列操作。正常的情况下，操作应该顺利进行，与操作相关的所有数据库信息也成功地更新；
  但是，如果在这一系列过程中任何一个环节出了差错，导致操作失败了，数据库中所有信息都必须保持操作前的状态不变。否则，数据库的信息将会一片混乱而不可预测。
  一个逻辑工作单元要称为事务，必须满足[ACID](http://baike.baidu.com/view/600227.htm)(原子性，一致性，隔离性和持久性):
  事务的结束只能有两种形式：提交和回滚。操作完全成功则提交，产生永久性的修改；操作不完全成功则回滚，恢复到事务开始前的状态。它们将结束一个事务。
  
  回归主题，我们基于前篇JDBC为基础进行修改，使其支持事务操作。
  同样的，事务相关操作已经封装至 nano-orm-jdbc 模块中，我们可以快速的使用封装好的API进行快速且简单的事务开发。
  
#### 1. 前期准备

###### 按照惯例，我们将上一版本的示例代码作为基础， 复制 nano-examples-jdbc 并重名为 nano-examples-jdbc-transactional

##### 1.1 修改 artifactId
```xml
<artifactId>nano-examples-jdbc</artifactId>
```
###### 修改为
```xml
<artifactId>nano-examples-jdbc-transactional</artifactId>
```

#### 2. 开始我们的JDBC事务开发
##### 2.1 修改Dao，增加 insertBatch方法
###### 接口 ElementsDao
```java
    /**
     * 批量新增 Element.
     * @param els Element List
     * @return insert successful
     */
    long insertBatch(List<Element> els);
```
###### 实现类 ElementsDaoImpl
```java
    @JdbcTransactional(envId = "quickstart")
    @Override
    public long insertBatch(List<Element> els) {
        Assert.notEmpty(els, "els list must be not empty");
        long successful = 0;
        for(Element el : els) {
            try {
                successful += manager.executeUpdate(INSERT, Lists.newArrayList(el.getText()));
            } catch(SQLException e) {
                LOGGER.error("Insert Element error: {}", e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
        return successful;
    }
```
###### 我们需要为 insertBatch 绑定事务，为了演示事务操作，我们这里不使用JDBC的批量操作 executeBatchUpdate 而是使用 executeUpdate 进行循环处理
###### 注意注解"@JdbcTransactional"的写法，envId传入一个字符串或一组字符串，这个值代表了数据源的名称，一组名称代码了我们可以对多个数据库同时进行事务操作。
###### 我们在JDBC的属性文件配置里填写的数据源名称为 "quickstart"，所以这里的 envId也必须为 "quickstart"，否则会出现"无法获取到JdbcManager"的异常提示
##### 注意: 使用注解开启事务的方法必须是public的，否则事务将无法开启。

##### 2.2. 编写批量写入的组件服务
###### 组件接口 RestComponent
```java
    /**
     * 批量新增Element.
     * 
     * @param els
     * @return ResultMap
     */
    @RequestMapping(value = "/elements/batch", method = RequestMethod.POST)
    ResultMap postElements(@RequestParam("els[]") Element[] els);
```
###### 组件实现类 RestComponentImpl
```java
    @Override
    public ResultMap postElements(Element[] els) {
        if(elementsDao.insertBatch(Lists.newArrayList(els)) > 0) {
            return OK;
        }
        
        return FAIL;
    }
```

#### 3. 启动服务
##### 3.1. Run Bootstrap
##### 3.2. 模拟请求
###### [下载](https://curl.haxx.se/download.html)CURL工具, 选择使用操作系统的版本
###### CURL使用[参考文档](http://blog.csdn.net/lipei1220/article/details/8536520)(Windows版)

###### POST请求模拟
```shell
curl -i http://localhost:8080/quickstart/rest/elements/batch -XPOST -d 'els[]={"text":"new hello batch 0"}&els[]={"text":"new hello batch 1"}'
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[{"id":33,"text":"new hello batch 0"},{"id":34,"text":"new hello batch 1"}],"info":"OK","status":200}
```

###### POST请求模拟 失败请求
```shell
curl -i http://localhost:8080/quickstart/rest/elements/batch -XPOST -d 'els[]={"text":"new hello batch 2"}&els[]={}'
```
###### 返回报文
```json
{"info":"ComponentInvokeException","message":"组件调用异常: NULL not allowed for column \"TEXT\"; SQL statement:\ninsert into elements (text) values (?) [23502-191]","status":9099}
```
###### 失败后查询数据变化
```shell
curl -i http://localhost:8080/quickstart/rest/elements
```
###### 返回报文
```json
{"message":"OK","value":[{"id":33,"text":"new hello batch 0"},{"id":34,"text":"new hello batch 1"}],"info":"OK","status":200}
```
###### 第二条数据在写入的时候因为text字段为null所以无法写入表，抛出异常后，成功的回滚了第一条写入的数据。
###### 这样，我们的简单事务处理就开发完成了。

源码
----
- [nano-examples-jdbc-transactional](https://github.com/nano-projects/nano-framework/tree/master/nano-examples/nano-examples-jdbc-transactional)

