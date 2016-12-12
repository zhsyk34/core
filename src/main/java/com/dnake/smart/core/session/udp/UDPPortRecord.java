package com.dnake.smart.core.session.udp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * UDP端口分配信息
 */
@Getter
@Setter
@AllArgsConstructor
public class UDPPortRecord {
	private int port;
	//用以删除过期数据(网关ip可能发生变动,导致占用端口)
	private long happen;
}
