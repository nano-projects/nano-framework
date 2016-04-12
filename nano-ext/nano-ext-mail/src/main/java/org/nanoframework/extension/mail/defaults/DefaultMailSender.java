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

import java.security.GeneralSecurityException;
import java.util.Properties;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.extension.mail.AbstractMailSender;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 *
 * @author yanghe
 * @since 1.3.3
 */
public class DefaultMailSender extends AbstractMailSender {
    private static final long serialVersionUID = -2962247276809993543L;
    private final Logger logger = LoggerFactory.getLogger(AbstractMailSender.class);
    
    @Override
    public Properties getProperties() {
        Properties prop = new Properties();
        prop.setProperty("mail.smtp.host", getMailServerHost());
        prop.setProperty("mail.smtp.port", getMailServerPort());
        prop.setProperty("mail.smtp.auth", isValidate() ? "true" : "false");
        prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.setProperty("mail.smtp.socketFactory.fallback", "false");
        prop.setProperty("mail.smtp.socketFactory.port", getMailServerPort());
        prop.setProperty("mail.smtp.starttls.enable", "true");
        
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true); 
            prop.put("mail.smtp.ssl.socketFactory", sf);  
        } catch (final GeneralSecurityException e) {
            logger.error("General Security Exception: {}", e.getMessage());
        }
        
        return prop;
    }

}
