/*
 * Copyright (c) 2015, Jurriaan Mous and contributors as indicated by the @author tags.
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
package org.nanoframework.extension.etcd.etcd4j.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Jurriaan Mous
 *
 * Settings for the etcd Netty client
 */
public class EtcdNettyConfig implements Cloneable {
  private static final Logger logger = LoggerFactory.getLogger(EtcdNettyConfig.class);

  private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

  private Class<? extends SocketChannel> socketChannelClass = NioSocketChannel.class;

  private int connectTimeout = 3000;

  private int maxFrameSize = 1024 * 1000;

  private String hostName;
  
  private final String CONNECT_TIMEOUT = "context.scheduler.etcd.connect.timeout";
  private final String FRAME_SIZE = "context.scheduler.etcd.max.frame.size";

  /**
   * Constructor
   */
  public EtcdNettyConfig() {
    String connectTimeout = System.getProperty(CONNECT_TIMEOUT);
    if(connectTimeout != null && !connectTimeout.trim().isEmpty()) {
      logger.warn("Setting context.scheduler.etcd.connect.timeout to " + connectTimeout.trim());
      this.connectTimeout = Integer.parseInt(connectTimeout.trim());
    }
    
    String frameSize = System.getProperty(FRAME_SIZE);
    if (frameSize != null && !frameSize.trim().isEmpty()) {
      logger.warn("Setting context.scheduler.etcd.max.frame.size to " + frameSize.trim());
      maxFrameSize = Integer.parseInt(frameSize.trim());
    }
    
  }

  /**
   * Get the connect timeout
   *
   * @return the connect timeout
   */
  public int getConnectTimeout() {
    return connectTimeout;
  }

  /**
   * Set the connect timeout
   *
   * @param connectTimeout to set
   * @return itself for chaining.
   */
  public EtcdNettyConfig setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  /**
   * Set a custom event loop group. For use within existing netty architectures
   *
   * @param eventLoopGroup to set.
   * @return itself for chaining.
   */
  public EtcdNettyConfig setEventLoopGroup(EventLoopGroup eventLoopGroup) {
    this.eventLoopGroup = eventLoopGroup;
    return this;
  }

  /**
   * Get the current event loop group. If it was never set it will use one loop group
   * for al etcd clients
   *
   * @return Event loop group.
   */
  public EventLoopGroup getEventLoopGroup() {
    return eventLoopGroup;
  }

  /**
   * Get the max frame size
   *
   * @return max frame size
   */
  public int getMaxFrameSize() {
    return maxFrameSize;
  }

  /**
   * Set the max frame size
   *
   * @param maxFrameSize to set
   * @return itself for chaining.
   */
  public EtcdNettyConfig setMaxFrameSize(int maxFrameSize) {
    this.maxFrameSize = maxFrameSize;
    return this;
  }

  /**
   * Get Socket channel class
   *
   * @return Socket channel class. Default is NioSocketChannel.class
   */
  public Class<? extends SocketChannel> getSocketChannelClass() {
    return socketChannelClass;
  }

  /**
   * Set Socket channel class. Default is NioSocketChannel.class
   *
   * @param socketChannelClass to set
   * @return itself for chaining
   */
  public EtcdNettyConfig setSocketChannelClass(Class<? extends SocketChannel> socketChannelClass) {
    this.socketChannelClass = socketChannelClass;
    return this;
  }

  public boolean hasHostName() {
    return hostName != null && !hostName.trim().isEmpty();
  }

  /**
   * Get the local host name
   *
   * @return local host name
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * Set the host name for the local machine.
   *
   * @param hostName name of local host
   * @return itself for chaining
   */
  public EtcdNettyConfig setHostName(String hostName) {
    this.hostName = hostName;
    return this;
  }

  @Override
  public EtcdNettyConfig clone() {
    try {
      return (EtcdNettyConfig) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }
}