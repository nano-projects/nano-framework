/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.mail;

import java.util.Properties;

import org.nanoframework.commons.entity.BaseEntity;

/**
 * 
 *
 * @author yanghe
 * @since 1.3.3
 */
public abstract class AbstractMailSender extends BaseEntity {
    private static final long serialVersionUID = 7448156380947823302L;
    
    // 发送邮件的服务器的IP和端口   
    private String mailServerHost;
    private String mailServerPort = "25";
    // 邮件发送者的地址   
    private String fromAddress;
    // 邮件接收者的地址   
    private String toAddress;
    // 登陆邮件发送服务器的用户名和密码   
    private String userName;
    private String password;
    // 是否需要身份验证   
    private boolean validate;
    // 邮件主题   
    private String subject;
    // 邮件的文本内容   
    private String content;
    // 邮件附件的文件名   
    private String[] attachFileNames;

    /**
     * 
     * @return 获得邮件会话属性
     */
    public abstract Properties getProperties();

    public String getMailServerHost() {
        return mailServerHost;
    }

    public void setMailServerHost(final String mailServerHost) {
        this.mailServerHost = mailServerHost;
    }

    public String getMailServerPort() {
        return mailServerPort;
    }

    public void setMailServerPort(final String mailServerPort) {
        this.mailServerPort = mailServerPort;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(final boolean validate) {
        this.validate = validate;
    }

    public String[] getAttachFileNames() {
        return attachFileNames;
    }

    public void setAttachFileNames(final String[] fileNames) {
        this.attachFileNames = fileNames;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(final String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(final String toAddress) {
        this.toAddress = toAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String textContent) {
        this.content = textContent;
    }
}
