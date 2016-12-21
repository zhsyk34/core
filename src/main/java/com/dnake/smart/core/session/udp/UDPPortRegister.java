package com.dnake.smart.core.session.udp;

/**
 * UDP端口登记信息
 */
public final class UDPPortRegister {

	private int port;

	/**
	 * 记录登记时间,用以在定时任务中删除过期数据
	 * 防止因网关ip的频繁变动导致的端口占用
	 */
	private long happen;

	private UDPPortRegister(int port, long happen) {
		this.port = port;
		this.happen = happen;
	}

	public static UDPPortRegister from(int port, long happen) {
		return new UDPPortRegister(port, happen);
	}

	public static UDPPortRegister from(int port) {
		return new UDPPortRegister(port, System.currentTimeMillis());
	}

	public int port() {
		return port;
	}

	public UDPPortRegister port(int port) {
		this.port = port;
		return this;
	}

	public long happen() {
		return happen;
	}

	public UDPPortRegister happen(long happen) {
		this.happen = happen;
		return this;
	}
}
