package com.dnake.smart.core.session;

import lombok.Getter;
import lombok.Setter;

/**
 * 网关信息
 */
@Getter
@Setter
public class GatewayInfo {

	private String sn;
	private String udid;//sn加密(冗余)

	/**
	 * TCP信息
	 */
	private String tcpIp;
	private int tcpPort;
	//TCPServer为其分配的UDP端口号(需保证唯一,50000+,尽量小)
	private int allocation;
	private long tcpLogin;

	/**
	 * UDP信息
	 */
	private String udpIp;//理论上应与 tcpIp 一致
	private int udpPort;
	private String version;
	private long udpLogin;
}
