package com.dnake.smart.core.session;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
final class Message {
	private String src;//来源(channelId)
	private String dest;//目标(gatewayUdid)
	private String action;//action
	private byte[] data;//发送数据
	private boolean send;//是否发送
	private long time;//开始发送时间
}