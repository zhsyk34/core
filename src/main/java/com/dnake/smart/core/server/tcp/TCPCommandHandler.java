package com.dnake.smart.core.server.tcp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.kit.DESKit;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPCommandHandler {

	private static final Logger logger = LoggerFactory.getLogger(TCPCommandHandler.class);

	public static void execute(ChannelHandlerContext ctx, Object msg) {
		ByteBuf buf = (ByteBuf) msg;
		byte[] bs = new byte[buf.readableBytes()];
		buf.readBytes(bs);
		String cmd = new String(DESKit.decrypt(bs), CharsetUtil.UTF_8);
		logger.info("receive data:{}", cmd);
		JSONObject jsonObject = JSON.parseObject(cmd);
		System.out.println(jsonObject.get("action"));
	}
}
