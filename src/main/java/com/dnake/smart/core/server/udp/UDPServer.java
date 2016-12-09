package com.dnake.smart.core.server.udp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * UDP服务器,主要用于接收、保存心跳信息以唤醒下线网关登录
 */
public class UDPServer {

	public static void start() {
		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap.group(group).channel(NioDatagramChannel.class);
			bootstrap.option(ChannelOption.SO_BROADCAST, false);
			bootstrap.handler(new UDPServerHandler());

			bootstrap.bind(Config.UDP_SERVER_PORT).syncUninterruptibly().channel().closeFuture().await();
			Log.logger(Category.UDP, "udp server start at port : " + Config.UDP_SERVER_PORT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

}
