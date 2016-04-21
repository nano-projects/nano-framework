NanoFramework
====

	NanoFramework是一个用于快速开发Web及任务调度项目的框架，以极少量的配置即可搭建Web项目或任务调度项目。
	
	
环境要求
----
	NanoFramework基于JDK8进行开发，内部使用了JDK8的一部分新特性，开发时必须使用JDK8进行开发和编译

安装
----
```shell
wget https://github.com/nano-projects/nano-framework/archive/nano-1.3.4.tar.gz
tar -zxvf nano-1.3.4.tar.gz
cd nano-framework-nano-1.3.4
mvn clean install -Dmaven.test.skip=true
```

Quickstart
----
- [Examples](nano-examples/examples/examples.md)