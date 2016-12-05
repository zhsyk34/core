package com.dnake.smart.core.server.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UDP服务器,主要用于接收、保存心跳信息以唤醒下线网关登录
 */
public class UDPServer {

	private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);

	public static void start(int port) {
		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap.group(group).channel(NioDatagramChannel.class);
			bootstrap.option(ChannelOption.SO_BROADCAST, false);
			bootstrap.handler(new UDPServerHandler());

			bootstrap.bind(port).syncUninterruptibly().channel().closeFuture().await();
			logger.info("udp server start at port : {}", port);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) {
		UDPServer.start(15998);
	}

}
