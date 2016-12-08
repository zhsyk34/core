package com.dnake.smart.core.test;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.CodecKit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TCPClient {

	public static void start() {

		Bootstrap bootstrap = new Bootstrap();

		EventLoopGroup group = new NioEventLoopGroup();
		//EventLoopGroup handleGroup = new NioEventLoopGroup();

		bootstrap.group(group).channel(NioSocketChannel.class);

		//setting options
		bootstrap.option(ChannelOption.TCP_NODELAY, true);

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						ByteBuf buffer = Unpooled.buffer();
						for (int i = 0; i < 100; i++) {
							buffer.writeBytes(login());
						}
						ctx.writeAndFlush(buffer);
					}

					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						System.out.println(msg);
					}
				});
			}
		});

		try {
			Channel channel = bootstrap.connect(Config.LOCAL_HOST, Config.TCP_SERVER_PORT).sync().channel();

			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	private static byte[] login() {
		String cmd = "{\"action\":\"loginReq\",\"clientType\":0,\"devSN\":\"2-1-1-100\",\"UDPPort\":50000}";
//		return cmd;
		return CodecKit.encode(cmd);
	}

	public static void main(String[] args) {
		start();
	}
}
