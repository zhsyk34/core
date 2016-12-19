package com.dnake.smart.core.session.tcp;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TODO:合并session?
 */
@Deprecated
public class TCPSession {
	private static final long MIN_MILL = LocalDateTime.of(2016, 12, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	//连接通道
	private final Channel channel;
	//连接的创建时间
	private final long createTime;
	private final String ip;
	//TCP端口
	private final int port;
	private String sn;

	TCPSession(Channel channel, long createTime) {
		if (channel == null || createTime < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		this.channel = channel;
		this.createTime = createTime;
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		this.ip = address.getHostString();
		this.port = address.getPort();
	}
}
