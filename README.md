NanoFramework
====
[![Release](https://img.shields.io/badge/release-1.3.11-blue.svg)](https://github.com/nano-projects/nano-framework/releases)
[![Build Status](https://travis-ci.org/nano-projects/nano-framework.svg?branch=master)](https://travis-ci.org/nano-projects/nano-framework)
[![Coverage Status](https://coveralls.io/repos/github/nano-projects/nano-framework/badge.svg)](https://coveralls.io/github/nano-projects/nano-framework)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

	NanoFramework是一个用于快速开发Web及任务调度项目的框架，以极少量的配置即可搭建Web项目或任务调度项目。
	
	
环境要求
----
	NanoFramework基于JDK8进行开发，内部使用了JDK8的一部分新特性，开发时必须使用JDK8进行开发和编译

安装
----
```shell
wget https://github.com/nano-projects/nano-framework/archive/nano-1.3.9.tar.gz
tar -zxvf nano-1.3.9.tar.gz
cd nano-framework-nano-1.3.9
mvn clean install -Dmaven.test.skip=true
```

Quickstart
----
- [Examples](nano-examples/examples/examples.md)
