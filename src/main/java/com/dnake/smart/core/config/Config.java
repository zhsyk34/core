package com.dnake.smart.core.config;

/**
 * 参数配置
 * 如无特殊说明,时间的单位均为秒
 */
public final class Config {
	/**
	 * -----------------------------TCP配置-----------------------------
	 */
	//本地服务器地址
	public static final String LOCAL_HOST = "127.0.0.1";
	//TCP服务器默认端口
	public static final int TCP_SERVER_PORT = 15999;
	//TCP最大并发连接数
	public static final int TCP_SERVER_BACKLOG = 1 << 10;
	//TCP预计并发连接数,用于初始化连接队列
	public static final int TCP_APP_COUNT_PREDICT = 1 << 12;
	public static final int TCP_GATEWAY_COUNT_PREDICT = 1 << 14;

	//TCP连接超时时间
	public static final int CONNECT_TIME_OUT = 5;
	//TCP登录时间
	public static final int TCP_LOGIN_TIME_OUT = 5;
	//app单次与服务器建立连接的最大时长
	public static final int TCP_APP_TIME_OUT = 17;
	//网关单次与服务器建立连接的最大时长
	public static final int TCP_GATEWAY_TIME_OUT = 30 * 60;
	//信息发送后等待响应最长时间
	public static final int TCP_MESSAGE_SEND_AWAIT = 18;
	//TCP管理(扫描)线程执行频率
	public static final int TCP_TIME_OUT_SCAN_FREQUENCY = 10;
	//允许最大的无效缓冲数据
	public static final int TCP_MAX_BUFFER_SIZE = 1 << 10;

	/**
	 * -----------------------------UDP配置-----------------------------
	 */
	//UDP服务器默认端口
	public static final int UDP_SERVER_PORT = 15998;
	//网关UDP端口分配起始地址(唯一)
	public static final int UDP_CLIENT_MIN_PORT = 50000;
	//扫描网关在线状态扫描频率
	public static final int UDP_ONLINE_SCAN_FREQUENCY = 10;
	//端口回收扫描频率
	public static final int UDP_PORT_COLLECTION_SCAN_FREQUENCY = 24 * 60 * 60;
	//端口信息保存频率
	public static final int UDP_PORT_SAVE_FREQUENCY = 6 * 60 * 60;

	/**
	 * -----------------------------日志配置-----------------------------
	 */

	public static final int LOGGER_CAPACITY = 5000;

	/**
	 * -----------------------------系统时间配置-----------------------------
	 */
	//服务器启动完毕后执行扫描任务
	public static final int SCHEDULE_TASK_DELAY_TIME = 1;
	//服务器启动状态监视时间间隔
	public static final int SERVER_START_MONITOR_TIME = 1;
	//通过UDP唤醒网关时检测状态时间间隔
	public static final int GATEWAY_AWAKE_CHECK_TIME = 1;
	//服务器启动时间
	public static final String SERVER_START_TIME = "2016-12-01";

	/**
	 * -----------------------------DB配置-----------------------------
	 */
	public static final int BATCH_FETCH_SIZE = 10;
	/**
	 * -----------------------------web-udp信息-----------------------------
	 */
	//本地服务器地址
	public static final String WEB_UDP_IP = "127.0.0.1";
	//TCP服务器默认端口
	public static final int WEB_UDP_PORT = 12345;
	//网关发送UDP心跳包频率
	private static final int UDP_CLIENT_FREQUENCY = 10;
	//网关UDP心跳最长离线时间
	public static final int UDP_MAX_IDLE = UDP_CLIENT_FREQUENCY * 10;

}
