/**
 * Copyright 2015- the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nanoframework.extension.websocket;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.nanoframework.commons.util.Assert;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * 
 * @author yanghe
 * @date 2015年8月19日 上午9:22:52
 */
public final class WebSocketServer {

	private static List<WebSocketServer> servers = new ArrayList<>();
    private Throwable throwable = null;
    private boolean isOK = false;
    private Channel ch;
    private String location;
    private int port;
    private boolean ssl;
    
	private WebSocketServer(int port, boolean ssl, String location) {
		this.port = port;
		this.ssl = ssl;
		this.location = location;
	}
    
    public static WebSocketServer create(int port, boolean ssl, String websocketPath, AbstractWebSocketHandler handler) throws CertificateException, SSLException, InterruptedException {
    	Assert.notNull(handler);
    	WebSocketServer server = new WebSocketServer(port, ssl, websocketPath);
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
	                
	                System.out.println("Open your web browser and navigate to " + (ssl? "https" : "http") + "://127.0.0.1:" + port + '/');
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
        String _location =  req.headers().get(HOST) + location;
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
    
	public int getPort() {
		return port;
	}
	
	public String getLocation() {
		return location;
	}
	
	public boolean isSsl() {
		return ssl;
	}
}
