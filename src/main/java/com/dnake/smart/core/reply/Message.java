package com.dnake.smart.core.reply;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public final class Message {
	private String src;//来源(channelId)
	private String dest;//目标(gateway sn)

	private String context;//内容
	//private String action;//action
	//private byte[] data;//发送数据
	private boolean send;//是否发送
	private long time;//开始发送时间

	private Message(String src, String dest, String context) {
		this.src = src;
		this.dest = dest;
		this.context = context;
	}

	public static Message init(String src, String dest, String context) {
		return new Message(src, dest, context);//.setSend(false).setTime(0);
	}
}