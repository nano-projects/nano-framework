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
package org.nanoframework.server.cfg;

import org.apache.catalina.connector.Connector;

import com.alibaba.fastjson.annotation.JSONField;

/**
 *
 * @author yanghe
 * @since 1.4.2
 */
public class ConnectorConf extends AbstractConf {
    private static final long serialVersionUID = -5280315541834101307L;

    static {
        final ConnectorConf conf = new ConnectorConf();
        conf.port = 7000;
        conf.protocal = "org.apache.coyote.http11.Http11Nio2Protocol";
        conf.connectionTimeout = 20_000L;
        conf.redirectPort = 7443;
        conf.executor = "tomcatThreadPool";
        conf.enableLookups = Boolean.FALSE;
        conf.acceptCount = 100;
        conf.maxPostSize = 10 * 1024 * 1024;
        conf.compression = "on";
        conf.disableUploadTimeout = Boolean.TRUE;
        conf.compressionMinSize = 2 * 1024;
        conf.noCompressionUserAgents = "gozilla, traviata";
        conf.acceptorThreadCount = 2;
        conf.compressableMimeType = "text/html,text/xml,text/plain,text/css,text/javascript,application/javascript";
        conf.uriEncoding = "UTF-8";
        DEF = conf;
    }

    private Integer port;
    private String protocal;
    private Long connectionTimeout;
    private Integer redirectPort;
    private String executor;
    private Boolean enableLookups;
    private Integer acceptCount;
    private Integer maxPostSize;
    private String compression;
    private Boolean disableUploadTimeout;
    private Integer compressionMinSize;
    private String noCompressionUserAgents;
    private Integer acceptorThreadCount;
    private String compressableMimeType;
    @JSONField(name = "URIEncoding")
    private String uriEncoding;
    
    private ConnectorConf() {
        
    }
    
    public ConnectorConf(final ConnectorConf conf) {
        this.merge(conf);
    }
    
    public Connector init() {
        final Connector connector = new Connector(protocal);
        connector.setPort(port);
        connector.setAsyncTimeout(connectionTimeout);
        connector.setRedirectPort(redirectPort);
        connector.setAttribute("executor", executor);
        connector.setEnableLookups(enableLookups);
        connector.setAttribute("acceptCount", acceptCount);
        connector.setMaxPostSize(maxPostSize);
        connector.setAttribute("compression", compression);
        connector.setAttribute("disableUploadTimeout", disableUploadTimeout);
        connector.setAttribute("noCompressionUserAgents", noCompressionUserAgents);
        connector.setAttribute("acceptorThreadCount", acceptorThreadCount);
        connector.setAttribute("compressableMimeType", compressableMimeType);
        connector.setURIEncoding(uriEncoding);
        return connector;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public String getProtocal() {
        return protocal;
    }

    public void setProtocal(final String protocal) {
        this.protocal = protocal;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(final Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public void setRedirectPort(final Integer redirectPort) {
        this.redirectPort = redirectPort;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(final String executor) {
        this.executor = executor;
    }

    public Boolean getEnableLookups() {
        return enableLookups;
    }

    public void setEnableLookups(final Boolean enableLookups) {
        this.enableLookups = enableLookups;
    }

    public Integer getAcceptCount() {
        return acceptCount;
    }

    public void setAcceptCount(final Integer acceptCount) {
        this.acceptCount = acceptCount;
    }

    public Integer getMaxPostSize() {
        return maxPostSize;
    }

    public void setMaxPostSize(final Integer maxPostSize) {
        this.maxPostSize = maxPostSize;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(final String compression) {
        this.compression = compression;
    }

    public Boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }

    public void setDisableUploadTimeout(final Boolean disableUploadTimeout) {
        this.disableUploadTimeout = disableUploadTimeout;
    }

    public Integer getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(final Integer compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }

    public void setNoCompressionUserAgents(final String noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }

    public Integer getAcceptorThreadCount() {
        return acceptorThreadCount;
    }

    public void setAcceptorThreadCount(final Integer acceptorThreadCount) {
        this.acceptorThreadCount = acceptorThreadCount;
    }

    public String getCompressableMimeType() {
        return compressableMimeType;
    }

    public void setCompressableMimeType(final String compressableMimeType) {
        this.compressableMimeType = compressableMimeType;
    }

    public String getUriEncoding() {
        return uriEncoding;
    }

    public void setUriEncoding(final String uriEncoding) {
        this.uriEncoding = uriEncoding;
    }

}
