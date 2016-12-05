package com.dnake.smart.core.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPServerHandler extends ChannelInboundHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TCPServerHandler.class);

	@Override
	public boolean isSharable() {
		return true;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info(">>>>>>>>>>>>>>>client " + ctx.channel().remoteAddress() + " closed.");
//		SessionManager.close(ctx.channel());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info(">>>>>>>>>>>>>>>>>>>>client " + ctx.channel().remoteAddress() + " connected.");

		/*Channel channel = ctx.channel();
		if (!channel.hasAttr(create)) {
			Attribute<Long> attr = channel.attr(create);
			channel.attr(instance).set(TcpSessionInfo.init(channel));

			attr.set(System.currentTimeMillis());
		}*/
//		SessionManager.add(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//		cause.printStackTrace();
		logger.error(cause.getMessage());
		logger.info(">>>>>>>>>>>>>>>>>>>>client " + ctx.channel().remoteAddress() + " error");
//		SessionManager.close(ctx.channel());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		TCPCommandHandler.execute(ctx, msg);
	}
}
