示例7: 切面编程(AOP)
====

####1、编写AOP
######1.1、AOP注解说明
* @Before
* @After
* @BeforeAndAfter

######1.2、在src/main/java/org.nanoframework.examples.first.webapp.aop下添加
```java
public class ExamplesAOP {
	private Logger LOG = LoggerFactory.getLogger(ExamplesAOP.class);
	
	public void before(MethodInvocation invocation) {
		if(LOG.isDebugEnabled()) {
			String params = StringUtils.join(invocation.getMethod().getParameters(), ", ");
			String args = StringUtils.join(invocation.getArguments(), ", ");
			LOG.debug("Before invoke method: " + invocation.getThis().getClass().getName() + "." + invocation.getMethod().getName() + "("+ (params == null ? "" : params) +"):: [" + (args == null ? "" : args) + "]");
		}
	}
}
```
######1.3、修改MybatisExampleComponentImpl
```java
@Before(classType = ExamplesAOP.class, methodName = "before")
public Object findAll() {
...
```

######1.4、注意事项
* 需要进行AOP的方法必须是public的
* AOP的实现必须是public

####2、至此，AOP的服务开发完成

- [首页](https://github.com/nano-projects/nano-framework/blob/master/README.md)
- [上一节](examples-05.md)
- [下一节](examples-07.md)