package com.dnake.smart.core.session.udp;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;

@Getter
@Setter
@Accessors(chain = true)
public class UDPGatewaySession {
	private String ip;
	private int port;
	private int allocation;//TCP服务器分配的端口
	private String sn;
	private String version;
	private long createTime;

	public static UDPGatewaySession init(InetSocketAddress address) {
		UDPGatewaySession session = new UDPGatewaySession();
		session.setIp(address.getHostName());
		session.setPort(address.getPort());
		session.createTime = System.currentTimeMillis();
		return session;
	}
}
