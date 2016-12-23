package com.dnake.smart.core.session.udp;

/**
 * 网关UDP端口信息
 * 初始化数据从数据库中加载,网关登录后重新进行验证/分配
 */
public final class UDPPortRegister {

	private int port;

	/**
	 * 记录登记时间用以在定时任务中删除过期数据
	 * 防止因网关ip的频繁变动导致的端口占用
	 */
	private long happen;

	private UDPPortRegister(int port, long happen) {
		this.port = port;
		this.happen = happen;
	}

	public static UDPPortRegister of(int port, long happen) {
		return new UDPPortRegister(port, happen);
	}

	static UDPPortRegister of(int port) {
		return of(port, System.currentTimeMillis());
	}

	public int port() {
		return port;
	}

	public UDPPortRegister port(int port) {
		this.port = port;
		return this;
	}

	long happen() {
		return happen;
	}

	UDPPortRegister happen(long happen) {
		this.happen = happen;
		return this;
	}

	/**
	 * for map.remove(key,value)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		UDPPortRegister that = (UDPPortRegister) o;

		return port == that.port && happen == that.happen;

	}

	@Override
	public int hashCode() {
		return 31 * port + (int) (happen ^ (happen >>> 32));
	}
}
