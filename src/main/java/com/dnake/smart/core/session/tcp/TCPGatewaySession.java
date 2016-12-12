package com.dnake.smart.core.session.tcp;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class TCPGatewaySession extends TCPBaseSession {

	private String sn;

	private TCPGatewaySession(Channel channel, long createTime) {
		super(channel, createTime);
	}

	public static TCPGatewaySession init(Channel channel) {
		return new TCPGatewaySession(channel, System.currentTimeMillis());
	}

	public static TCPGatewaySession init(TCPBaseSession baseInfo) {
		return init(baseInfo.getChannel());
	}

}
