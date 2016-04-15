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
        builder.append("<!DOCTYPE html>");
        builder.append("<html lang=\"en\">");
        builder.append("<head>");
        builder.append("  <meta charset=\"UTF-8\">");
        builder.append("  <style>");
        builder.append("    body { background-color: #eaeaea; padding: 20px 20% 20px 20%; }");
        builder.append("    .content { background-color: white; padding: 20px; border-radius: 5px; min-height: 200px; ");
        builder.append("    -webkit-box-shadow: #aaaaaa 2px 2px 2px; -moz-box-shadow: #aaaaaa 2px 2px 2px; box-shadow: #aaaaaa 2px 2px 2px; }");
        builder.append("    p { font-weight: 300; font-size: 14px; font-family: \"Chalkboard\"; }");
        builder.append("    a { text-decoration:none; color: #4b83ee; }");
        builder.append("    a:link { text-decoration:none; }");
        builder.append("    a:visited { text-decoration:none; }");
        builder.append("    a:hover { text-decoration:none; }");
        builder.append("    a:active { text-decoration:none; }");
        builder.append("  </style>");
        builder.append("</head>");
        builder.append("<body>");
        builder.append("  <div class=\"content\">");
        builder.append(content);
        builder.append("  </div>");
        builder.append("</body>");
        builder.append("</html>");
        
        return builder.toString();
    }

}
