package com.dnake.smart.core.server.tcp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.session.tcp.TCPSessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * TCP服务器
 */
public class TCPServer {

	private static volatile boolean started = false;

	public static void start() {
		if (started) {
			return;
		}
		started = true;

		ServerBootstrap bootstrap = new ServerBootstrap();

		EventLoopGroup mainGroup = new NioEventLoopGroup();
		EventLoopGroup handleGroup = new NioEventLoopGroup();

		bootstrap.group(mainGroup, handleGroup).channel(NioServerSocketChannel.class);

		//setting options
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_BACKLOG, Config.SERVER_BACKLOG);

		//pool
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Config.CONNECT_TIME_OUT * 1000);

		//logging
		bootstrap.childHandler(new LoggingHandler(LogLevel.WARN));

		//handler
		bootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(new TCPInitHandler());
				pipeline.addLast(new TCPDecoder());
				pipeline.addLast(new TCPEncoder());
				pipeline.addLast(new TCPLoginHandler());
				pipeline.addLast(new TCPServerHandler());
			}
		});

		try {
			ChannelFuture future = bootstrap.bind(Config.TCP_SERVER_PORT).sync();
			Log.logger(Category.EVENT, TCPServer.class.getSimpleName() + " start at port : " + Config.TCP_SERVER_PORT);

			//task
			TCPSessionManager.monitor();

			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mainGroup.shutdownGracefully();
			handleGroup.shutdownGracefully();
		}
	}

}
