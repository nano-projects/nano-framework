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
package org.nanoframework.extension.websocket;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.nanoframework.commons.support.logging.Logger;
import org.nanoframework.commons.support.logging.LoggerFactory;
import org.nanoframework.commons.util.Assert;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 
 * @author yanghe
 * @since 1.1
 */
public final class WebSocketServer {
	private Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);
	
	private static List<WebSocketServer> servers = new ArrayList<>();
    private Throwable throwable = null;
    private boolean isOK = false;
    private Channel ch;
    private String host;
    private String location;
    private int port;
    private int proxyPort;
    private boolean ssl;
    
    static {
    	Runtime.getRuntime().addShutdownHook(new Thread(() -> closeAll() ));
    }
    
	private WebSocketServer(String host, int port, boolean ssl, String location) {
		this.host = host;
		this.port = port;
		this.ssl = ssl;
		this.location = location;
	}
	
	private WebSocketServer(String host, int port, int proxyPort, boolean ssl, String location) {
		this.host = host;
		this.port = port;
		this.proxyPort = proxyPort;
		this.ssl = ssl;
		this.location = location;
	}
    
    public static WebSocketServer create(String host, int port, boolean ssl, String websocketPath, AbstractWebSocketHandler handler) throws CertificateException, SSLException, InterruptedException {
    	Assert.notNull(handler);
    	WebSocketServer server = new WebSocketServer(host, port, ssl, websocketPath);
    	server.create(handler);
    	servers.add(server);
    	return server;
    }
    
    public static WebSocketServer create(String host, int port, int proxyPort, boolean ssl, String websocketPath, AbstractWebSocketHandler handler) throws CertificateException, SSLException, InterruptedException {
    	Assert.notNull(handler);
    	WebSocketServer server = new WebSocketServer(host, port, proxyPort, ssl, websocketPath);
    	server.create(handler);
    	servers.add(server);
    	return server;
    }
    
    public void create(final AbstractWebSocketHandler handler) throws InterruptedException, CertificateException, SSLException {
    	Thread websocketThread = new Thread(() -> {
    		try {
	            final SslContext sslCtx;
	            if (ssl) {
	                SelfSignedCertificate ssc = new SelfSignedCertificate();
	                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
	            } else {
	                sslCtx = null;
	            }
	
	            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	            EventLoopGroup workerGroup = new NioEventLoopGroup();
	            try {
	                ServerBootstrap b = new ServerBootstrap();
	                b.group(bossGroup, workerGroup)
	                 .channel(NioServerSocketChannel.class)
	                 .handler(new LoggingHandler(LogLevel.INFO))
	                 .childHandler(new WebSocketServerInitializer(sslCtx, handler));
	
	                ch = b.bind(port).sync().channel();
	
	                isOK = true;
	                
	                LOG.info("Open your web browser and navigate to " + (ssl? "https" : "http") + "://127.0.0.1:" + port + '/');
	                ch.closeFuture().sync();
	                
	            } finally {
	                bossGroup.shutdownGracefully();
	                workerGroup.shutdownGracefully();
	            }
	            
    		} catch(CertificateException | SSLException | InterruptedException e) {
    			throwable = e;
    			isOK = true;
    		}
    	});
    	
    	websocketThread.setDaemon(true);
    	websocketThread.start();
    	
    	while(!isOK) {
    		Thread.sleep(10L);
    	}
    	
        if(throwable != null) {
        	if(throwable instanceof CertificateException)
        		throw (CertificateException) throwable;
        	else if(throwable instanceof SSLException)
        		throw (SSLException) throwable;
        	else if(throwable instanceof InterruptedException) 
        		throw (InterruptedException) throwable;
        	
        }
    }
    
    public void close() {
    	if(ch != null) {
    		ch.close();
    	}
    }
    
    public String getWebSocketLocation(FullHttpRequest req) {
        String _location =  req.headers().get(HttpHeaderNames.HOST) + location;
        if (ssl) {
            return "wss://" + _location;
        } else {
            return "ws://" + _location;
        }
    }
    
    public static final void closeAll() {
    	servers.forEach(server -> server.close());
    	servers.clear();
    }
    
	public String getHost() {
		return host;
	}
    
	public int getPort() {
		return port;
	}
	
	public String getLocation() {
		return location;
	}
	
	public boolean isSsl() {
		return ssl;
	}

	public int getProxyPort() {
		return proxyPort;
	}
	
}
