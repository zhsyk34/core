package com.dnake.smart.core.config;

/**
 * 参数配置
 */
public class Config {

	public static final String LOCAL_HOST = "127.0.0.1";

	//public static final String BROADCAST_HOST = "255.255.255.255";

	/**
	 *
	 */
	//TCP服务器默认端口
	public static final int TCP_SERVER_PORT = 15999;

	//UDP服务器默认端口
	public static final int UDP_SERVER_PORT = 15998;
	//网关发送UDP心跳包端口
	public static final int UDP_CLIENT_PORT = 50000;
	//网关发送UDP心跳包频率
	public static final int UDP_CLIENT_INTERVAL = 5 * 60;

	public static final int SERVER_BACKLOG = 1024;
	//预计并发连接数
	public static final int APP_PREDICT = 1 << 10;
	public static final int GATEWAY_PREDICT = 1 << 9;

	/**
	 * -----------------以下为超时设置,单位秒------------------
	 */
	public static final int APP_TIME_OUT = 15;

	//连接超时
	public static final int CONNECT_TIME_OUT = 5;

	//登录用时
	public static final int LOGIN_TIME_OUT = 5;
	//网关单次与服务器建立连接的最长在线时间
	public static final int GATEWAY_TIME_OUT = 30 * 60;

	//信息发送后最长等待时间
	public static final int MESSAGE_SEND_AWAIT = 15;

	/**
	 * logger
	 */

	public static final int LOGGER_CAPACITY = 5000;
}
