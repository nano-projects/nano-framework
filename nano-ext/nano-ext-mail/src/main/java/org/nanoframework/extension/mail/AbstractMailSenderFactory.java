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

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nanoframework.commons.crypt.CryptUtil;
import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 
 *
 * @author yanghe
 * @since 1.3.3
 */
public abstract class AbstractMailSenderFactory {
    /** HOST property. */
    public static final String HOST = "javax.mail.server.host";
    /** PORT property. */
    public static final String PORT = "javax.mail.server.port";
    /** VALIDATE property. */
    public static final String VALIDATE = "javax.mail.server.validate";
    /** USERNAME property. */
    public static final String USERNAME = "javax.mail.server.username";
    /** PASSWORD property. */
    public static final String PASSWORD = "javax.mail.server.password";
    /** MAIL FROM property. */
    public static final String FROM = "javax.mail.server.from";
    /** Singleton mail session instance. */
    public static final String SINGLETON_SESSION_INSTANCE = "javax.mail.server.session.singleton";
    /** Mail Session Debug enabled. */
    public static final String DEBUG_ENABLED = "javax.mail.server.session.debug.enabled";
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractMailSenderFactory.class);
    
    protected final String host = System.getProperty(HOST);
    protected final String port = System.getProperty(PORT);
    protected final boolean validate = Boolean.parseBoolean(System.getProperty(VALIDATE, "false"));
    protected final String username = System.getProperty(USERNAME);
    protected final String password = StringUtils.isBlank(System.getProperty(PASSWORD)) ? StringUtils.EMPTY : CryptUtil.decrypt(System.getProperty(PASSWORD), username);
    protected final String from = System.getProperty(FROM);
    protected final boolean singletonSessionInstance = Boolean.parseBoolean(System.getProperty(SINGLETON_SESSION_INSTANCE, "true"));
    /** Mail session debug enabled, default is false */
    protected final boolean debugEnabled = Boolean.parseBoolean(System.getProperty(DEBUG_ENABLED, "false"));
    
    /**
     * 以文本格式发送邮件.
     *
     * @param mailSender 待发送的邮件的信息
     * @return Boolean
     */
    protected boolean sendTextMail(final AbstractMailSender mailSender) {
        final Properties pro = mailSender.getProperties();
        MailAuthenticator authenticator = null;
        if (mailSender.isValidate()) {
            authenticator = new MailAuthenticator(mailSender.getUserName(), mailSender.getPassword());
        }
        
        final Session sendMailSession;
        if(singletonSessionInstance) {
            sendMailSession = Session.getDefaultInstance(pro, authenticator);
        } else {
            sendMailSession = Session.getInstance(pro, authenticator);
        }
        
        sendMailSession.setDebug(debugEnabled);
        try {
            final Message mailMessage = new MimeMessage(sendMailSession);
            final Address from = new InternetAddress(mailSender.getFromAddress());
            mailMessage.setFrom(from);
            mailMessage.setRecipients(Message.RecipientType.TO, toAddresses(mailSender.getToAddress()));
            mailMessage.setSubject(mailSender.getSubject());
            mailMessage.setSentDate(new Date());
            mailMessage.setText(mailSender.getContent());
            Transport.send(mailMessage);
            return true;
        } catch (final MessagingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
        return false;
    }

    /**
     * 以HTML格式发送邮件.
     *
     * @param mailSender 待发送的邮件信息
     * @return Boolean
     */
    protected boolean sendHtmlMail(final AbstractMailSender mailSender) {
        final Properties pro = mailSender.getProperties();
        MailAuthenticator authenticator = null;
        if (mailSender.isValidate()) {
            authenticator = new MailAuthenticator(mailSender.getUserName(), mailSender.getPassword());
        }

        final Session sendMailSession;
        if(singletonSessionInstance) {
            sendMailSession = Session.getDefaultInstance(pro, authenticator);
        } else {
            sendMailSession = Session.getInstance(pro, authenticator);
        }
        
        sendMailSession.setDebug(debugEnabled);
        try {
            final Message mailMessage = new MimeMessage(sendMailSession);
            final Address from = new InternetAddress(mailSender.getFromAddress());
            mailMessage.setFrom(from);
            mailMessage.setRecipients(Message.RecipientType.TO, toAddresses(mailSender.getToAddress()));
            mailMessage.setSubject(mailSender.getSubject());
            mailMessage.setSentDate(new Date());
            final Multipart mainPart = new MimeMultipart();
            final BodyPart html = new MimeBodyPart();
            html.setContent(mailSender.getContent(), "text/html; charset=utf-8");
            mainPart.addBodyPart(html);
            mailMessage.setContent(mainPart);
            Transport.send(mailMessage);
            return true;
        } catch (final MessagingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        
        return false;
    }
    
    /**
     * 
     * @param tos address String array
     * @return Address Array
     * @throws AddressException address convert exception
     */
    protected Address[] toAddresses(final String tos) throws AddressException {
        if(tos != null && !"".equals(tos)) {
            final List<Address> to = Lists.newArrayList();
            final String[] toArray = tos.split(";");
            if(ArrayUtils.isNotEmpty(toArray)) {
                for(final String address : toArray) {
                    if(StringUtils.isNotBlank(address)) {
                        to.add(new InternetAddress(address.trim()));
                    }
                }
            }
            
            return to.toArray(new InternetAddress[0]);
        }
        
        return null;
    }

    /**
     * 
     * @param subject the mail subject
     * @param content the mail content
     * @param to the mail receiver address
     * @return if send mail success then true else false
     */
    public abstract boolean sendMail(final String subject, final String content, final String to);
    
    /**
     * 
     * @param content the mail content
     * @return return convert mail content
     */
    public abstract String buildContent(final String content);
}
