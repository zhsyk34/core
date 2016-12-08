package com.dnake.smart.core.dict;

import io.netty.util.AttributeKey;

public class SessionAttributeKey {

	/**
	 * 登录相关信息
	 */
	//设备类型
	public static final AttributeKey<Device> TYPE = AttributeKey.newInstance(CommandKey.TYPE.getName());
	//网关SN号
	public static final AttributeKey<String> SN = AttributeKey.newInstance(CommandKey.SN.getName());
	//网关请求的UDP端口
	public static final AttributeKey<Integer> UDP_PORT = AttributeKey.newInstance(CommandKey.UDP_PORT.getName());
	//当前连接登录验证码
	public static final AttributeKey<String> KEYCODE = AttributeKey.newInstance(CommandKey.KEYCODE.getName());
}
