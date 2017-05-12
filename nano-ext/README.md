# Nano Extension简介

	此模块为NanoFramework的扩展插件模块，此模块主要针对一些需要使用的外部扩展进行封装，集成至NanoFramework中，
	使其在使用过程中达到最大的简化。
	
### HttpClient扩展模块

```java
import org.nanoframework.extension.httpclient.HttpClient;
import org.nanoframework.extension.httpclient.HttpResponse;

import com.google.inject.Inject;

@Inject
private HttpClient client;

// Invoke in method
HttpResponse response = client.get("www.baidu.com");
```

HttpClient中封装了大部分的http请求操作，因操作上非常简单，所以不再一一进行示例说明。

### Mail扩展模块

```java
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.DEBUG_ENABLED;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.FROM;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.HOST;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.PASSWORD;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.PORT;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.USERNAME;
import static org.nanoframework.extension.mail.AbstractMailSenderFactory.VALIDATE;

import org.nanoframework.extension.mail.defaults.DefaultMailSenderFactory;

import com.google.inject.Inject;

// Invoke once
System.setProperty(HOST, <SMTP server address>);
System.setProperty(PORT, <SMTP port>);
System.setProperty(VALIDATE, <Mail server validate>);
System.setProperty(USERNAME, <Sender username>);
System.setProperty(PASSWORD, <Sender password>);
System.setProperty(FROM, <From display name>);
System.setProperty(DEBUG_ENABLED, <If true, enabled debug mode>);

@Inject
private DefaultMailSenderFactory senderFactory;

// Invoke in method
boolean success = senderFactory.sendMail("Title", "Content", "To1,To2,...,ToAny");
if (success) {
  // Sender mail success
} else {
  // Sender mail failure
}
```