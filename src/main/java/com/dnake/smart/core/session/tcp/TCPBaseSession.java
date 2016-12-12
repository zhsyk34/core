package com.dnake.smart.core.session.tcp;

import io.netty.channel.Channel;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 基础的会话信息
 */
@Getter
public class TCPBaseSession {
	private static final long MIN_MILL = LocalDateTime.of(2016, 12, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	//连接通道
	protected final Channel channel;
	//连接的创建时间
	protected final long createTime;
	protected final String ip;
	//TCP端口
	protected final int port;

	TCPBaseSession(Channel channel, long createTime) {
		if (channel == null || createTime < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		this.channel = channel;
		this.createTime = createTime;
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		this.ip = address.getHostString();
		this.port = address.getPort();
	}

	public static TCPBaseSession init(Channel channel) {
		return new TCPBaseSession(channel, System.currentTimeMillis());
	}

}
