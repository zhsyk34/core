package com.dnake.smart.core.session;

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
	private long happen;
}
