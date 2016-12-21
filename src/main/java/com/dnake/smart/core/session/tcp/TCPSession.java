package com.dnake.smart.core.session.tcp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ConvertKit;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * TCP连接信息
 */
final class TCPSession {
	private static final long MIN_MILL = ConvertKit.from(ConvertKit.from(Config.SERVER_START_TIME));

	//连接通道
	private final Channel channel;
	//连接的创建时间
	private final long happen;

	//private final String ip;
	//TCP端口
	//private final int port;
	//app请求网关 或 登录网关的sn号
	//private String sn;

	private TCPSession(Channel channel, long happen) {
		if (channel == null || happen < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		this.channel = channel;
		this.happen = happen;
	}

	static TCPSession from(Channel channel) {
		return new TCPSession(channel, System.currentTimeMillis());
	}

	public Channel channel() {
		return channel;
	}

	long happen() {
		return happen;
	}

	String ip() {
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		return address.getAddress().getHostAddress();
	}

}
