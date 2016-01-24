package org.nanoframework.extension.etcd.etcd4j.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.nanoframework.extension.etcd.etcd4j.EtcdClient;
import org.nanoframework.extension.etcd.etcd4j.responses.EtcdAuthenticationException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EtcdNettyClientTest {

  @Test
  public void testConfig() throws Exception {
    NioEventLoopGroup evl = new NioEventLoopGroup();

    URI uri = URI.create("http://192.168.180.204:2379");

    EtcdNettyConfig config = new EtcdNettyConfig()
        .setConnectTimeout(100)
        .setSocketChannelClass(NioSocketChannel.class)
        .setMaxFrameSize(1024 * 1024)
        .setEventLoopGroup(evl)
        .setHostName("etcd-infra2");

    EtcdNettyClient client = new EtcdNettyClient(config, uri);
    Bootstrap bootstrap = client.getBootstrap();

    assertEquals(evl, bootstrap.group());

    Channel channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();

    assertEquals(100, channel.config().getOption(ChannelOption.CONNECT_TIMEOUT_MILLIS).intValue());
  }

  @Ignore
  @Test
  public void testAuth() throws Exception {
    EtcdClient client = new EtcdClient(
      "test",
      "test",
      URI.create("http://192.168.180.204:2379"));

    assertNotNull(client.get("/test/messages").send().get());
  }

  @Ignore
  @Test(expected = EtcdAuthenticationException.class)
  public void testAuthFailure() throws Exception {
    EtcdClient client = new EtcdClient(
      "test",
      "test_",
      URI.create("http://192.168.180.204:2379"));

    client.get("/test/messages").send().get();
  }
}