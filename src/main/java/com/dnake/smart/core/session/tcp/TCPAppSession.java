package com.dnake.smart.core.session.tcp;

import io.netty.channel.Channel;
import lombok.ToString;

@ToString
public class TCPAppSession extends TCPBaseSession {

	private TCPAppSession(Channel channel, long createTime) {
		super(channel, createTime);
	}

	public static TCPAppSession init(Channel channel) {
		return new TCPAppSession(channel, System.currentTimeMillis());
	}

	public static TCPAppSession init(TCPBaseSession baseInfo) {
		return init(baseInfo.getChannel());
	}
}
