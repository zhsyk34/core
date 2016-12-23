package com.dnake.smart.core.session.udp;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;

/**
 * 网关UDP心跳信息
 * 相较 {@link UDPPortRegister} 多出version信息,ip+sn由 {@link UDPPortManager} 中的map维护
 * 相较 {@link com.dnake.smart.core.database.UDPRecord} 多出version信息
 */
@Getter
@Setter
@Accessors(chain = true)
public final class UDPSession {
	private final String ip;
	private final int port;
	private String sn;
	private String version;
	private long happen;

	private UDPSession(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public static UDPSession from(InetSocketAddress address) {
		UDPSession session = new UDPSession(address.getAddress().getHostAddress(), address.getPort());
		session.happen = System.currentTimeMillis();
		return session;
	}
}
