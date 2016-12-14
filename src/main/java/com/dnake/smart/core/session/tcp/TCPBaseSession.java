package com.dnake.smart.core.session.tcp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ConvertKit;
import io.netty.channel.Channel;
import lombok.Getter;

import java.net.InetSocketAddress;

/**
 * 基础的会话信息
 */
@Getter
class TCPBaseSession {
	private static final long MIN_MILL = ConvertKit.from(ConvertKit.from(Config.START_TIME));

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
