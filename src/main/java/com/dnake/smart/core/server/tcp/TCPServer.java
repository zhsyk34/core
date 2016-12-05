package com.dnake.smart.core.server.tcp;

import com.dnake.smart.core.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServer {

	private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

	private static volatile boolean isStart = false;

	public static void start() {
		if (isStart) {
			return;
		}
		isStart = true;

		ServerBootstrap bootstrap = new ServerBootstrap();

		EventLoopGroup mainGroup = new NioEventLoopGroup();
		EventLoopGroup handleGroup = new NioEventLoopGroup();

		bootstrap.group(mainGroup, handleGroup).channel(NioServerSocketChannel.class);

		//setting options
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.SO_BACKLOG, Config.SERVER_BACKLOG);

		//pool
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Config.CONNECT_TIME_OUT * 1000);

		//logging
		bootstrap.childHandler(new LoggingHandler());

		bootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new TCPDecodeHandler());
				pipeline.addLast(new TCPServerHandler());
			}
		});

		try {
			ChannelFuture future = bootstrap.bind(Config.TCP_SERVER_PORT).sync();
			logger.info("TCPServer start at port : " + Config.TCP_SERVER_PORT);
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mainGroup.shutdownGracefully();
			handleGroup.shutdownGracefully();
		}
	}

}
