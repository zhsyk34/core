package com.dnake.smart.core.server.udp;

import com.alibaba.fastjson.JSON;
import com.dnake.smart.core.kit.CommandKit;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.session.GatewaySessionManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * UDP服务器处理器
 * 心跳数据格式:{"action":"cmtHeartbeat","devSN":"2-1-1-5","appVersionNo":"V2.5"}
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private static final Logger logger = LoggerFactory.getLogger(UDPServerHandler.class);
	private static final String CMD = "cmtHeartbeat";

	private HeartInfo parse(DatagramPacket msg) {
		String data = msg.content().toString(CharsetUtil.UTF_8);
		if (ValidateKit.isEmpty(data)) {
			return null;
		}
		return JSON.parseObject(data, HeartInfo.class);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		HeartInfo info = parse(msg);
		InetSocketAddress address = msg.sender();
		String response;
		logger.info("receive : {} from {}", info, address);
		if (info != null && CMD.equals(info.getAction()) && ValidateKit.notEmpty(info.getDevSN())) {
			GatewaySessionManager.update(info, address);
			response = CommandKit.correct();
		} else {
			response = CommandKit.wrong(101);
		}
		ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(response, CharsetUtil.UTF_8), address));
	}

	@Getter
	@Setter
	@ToString
	public static class HeartInfo {
		private String action;
		private String devSN;
		private String appVersionNo;
	}
}
