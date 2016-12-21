package com.dnake.smart.core.session.udp;

import java.net.InetSocketAddress;

/**
 * UDP心跳信息
 */
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

	public String ip() {
		return ip;
	}

	public int port() {
		return port;
	}

	public String sn() {
		return sn;
	}

	public String version() {
		return version;
	}

	long happen() {
		return happen;
	}

	public UDPSession sn(String sn) {
		this.sn = sn;
		return this;
	}

	public UDPSession version(String version) {
		this.version = version;
		return this;
	}

}
