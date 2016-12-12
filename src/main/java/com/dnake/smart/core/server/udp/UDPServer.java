package com.dnake.smart.core.server.udp;

import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.dnake.smart.core.config.Config.UDP_SERVER_PORT;

/**
 * UDP服务器,主要用于接收、保存心跳信息以唤醒下线网关登录
 */
public class UDPServer {

	private static final AtomicBoolean started = new AtomicBoolean(false);
	private static Channel channel;

	public static void start() {
		new Thread(() -> {
			init();
		}).start();
	}

	public static void init() {
//		if (started.get()) {
//			return;
//		}
		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap.group(group).channel(NioDatagramChannel.class);
			bootstrap.option(ChannelOption.SO_BROADCAST, true);
			bootstrap.handler(new UDPServerHandler());

			channel = bootstrap.bind(UDP_SERVER_PORT).syncUninterruptibly().channel();
//			channel = bootstrap.bind(UDP_SERVER_PORT).sync().channel();
			started.set(true);
			Log.logger(Category.UDP, UDPServer.class.getSimpleName() + " start at port : " + UDP_SERVER_PORT);
			System.err.println(">>>>>>>>>>>>>>>>started!!!");
			channel.closeFuture().await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void send(String host, int port, Object msg) {
		Log.logger(Category.EXCEPTION, UDPServer.class.getSimpleName() + "启动完毕");
		ByteBuf buf = Unpooled.copiedBuffer((String) msg, CharsetUtil.UTF_8);
		DatagramPacket packet = new DatagramPacket(buf, new InetSocketAddress(host, port));
		channel.writeAndFlush(packet);
		Log.logger(Category.SEND, "数据已发出");
	}
}
