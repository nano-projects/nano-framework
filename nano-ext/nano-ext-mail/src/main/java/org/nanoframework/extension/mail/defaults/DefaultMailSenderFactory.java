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
package org.nanoframework.extension.mail.defaults;

import org.nanoframework.commons.format.DateFormat;
import org.nanoframework.commons.format.Pattern;
import org.nanoframework.extension.mail.AbstractMailSenderFactory;

import com.google.inject.Singleton;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
@Singleton
public class DefaultMailSenderFactory extends AbstractMailSenderFactory {
    protected DefaultMailSender sender = new DefaultMailSender() {
        private static final long serialVersionUID = -506000164665588990L; {
        setMailServerHost(host);
        setMailServerPort(port);
        setValidate(validate);
        setUserName(username);
        setPassword(password);
        setFromAddress(from);
    }};
    
    @Override
    public boolean sendMail(String subject, String content, String to) {
        DefaultMailSender sender = (DefaultMailSender) this.sender.clone();
        sender.setSubject(subject);
        sender.setContent(buildContent(content));
        sender.setToAddress(to);
        
        try {
            if(sendHtmlMail(sender)) { 
                return true;
            } 
        } catch(final Exception e) {
            LOGGER.error("Send mail error: {}", e.getMessage());
        }
        
        return false;
    }
    
    @Override
    public String buildContent(String content) {
        StringBuilder builder = new StringBuilder();
        builder.append("<div style='font-size:12px;'>").append(content).append("</div>");
        builder.append("<div style='margin-top:100px;font-size:11px;'>");
        builder.append("    <div style='margin-top:5px;'>--------------------------------------------------------------------------------------</div>");
        builder.append("    <div style='margin-top:5px;'>此邮件由系统自动生成，请勿回复，如有问题请联系管理员。</div>");
        builder.append("    <div style='margin-top:5px;'>").append(DateFormat.format(System.currentTimeMillis(), Pattern.DATETIME)).append("</div>");
        builder.append("    <div style='margin-top:5px;'>--------------------------------------------------------------------------------------</div>");
        builder.append("</div>");
        return builder.toString();
    }

}
