package com.dnake.smart.core.server.udp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.Getter;

import java.net.InetSocketAddress;

import static com.dnake.smart.core.config.Config.UDP_SERVER_PORT;

/**
 * UDP客户端,用以向web服务器推送信息
 */
public final class UDPClient {
	@Getter
	private static volatile boolean started = false;

	@Getter
	private static Channel channel;

	public static synchronized void start() {
		if (started) {
			return;
		}

		Bootstrap bootstrap = new Bootstrap();
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap.group(group).channel(NioDatagramChannel.class);
			bootstrap.option(ChannelOption.SO_BROADCAST, false);
			bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
				@Override
				protected void initChannel(DatagramChannel ch) throws Exception {
				}
			});

			channel = bootstrap.bind(UDP_SERVER_PORT).syncUninterruptibly().channel();

			started = true;

			Log.logger(Category.EVENT, UDPClient.class.getSimpleName() + " 在端口[" + UDP_SERVER_PORT + "]启动完毕");
			channel.closeFuture().await();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	public static void send(String message) {
		if (channel == null) {
			Log.logger(Category.UDP, UDPClient.class.getSimpleName() + " 尚未启动");
			return;
		}
		InetSocketAddress target = new InetSocketAddress(Config.WEB_UDP_IP, Config.WEB_UDP_PORT);
		ByteBuf buf = Unpooled.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
		UDPServer.getChannel().writeAndFlush(new DatagramPacket(buf, target));
	}
}
