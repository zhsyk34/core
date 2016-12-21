package com.dnake.smart.core.entry;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ThreadKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.reply.MessageManager;
import com.dnake.smart.core.server.tcp.TCPServer;
import com.dnake.smart.core.server.udp.UDPServer;
import com.dnake.smart.core.session.tcp.PortManager;
import com.dnake.smart.core.session.udp.UDPSessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.dnake.smart.core.config.Config.UDP_ONLINE_SCAN_FREQUENCY;

public class Entry {

	public static void start() {
		//TODO:从数据加载网关端口使用数据
		PortManager.load();

		//TCP服务器
		ExecutorService tcpService = Executors.newSingleThreadExecutor();
		tcpService.submit(TCPServer::start);
		while (!TCPServer.isStarted()) {
			Log.logger(Category.EVENT, TCPServer.class.getSimpleName() + " 正在启动...");
			ThreadKit.await(Config.SERVER_START_MONITOR_TIME);
		}

		//UDP服务器
		ExecutorService udpService = Executors.newSingleThreadExecutor();
		udpService.submit(UDPServer::start);
		while (!UDPServer.isStarted()) {
			Log.logger(Category.EVENT, UDPServer.class.getSimpleName() + " 正在启动...");
			ThreadKit.await(Config.SERVER_START_MONITOR_TIME);
		}

		//TODO
		ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
		//UDP端口回收
//		service.scheduleWithFixedDelay(PortManager::reduce, Config.SCHEDULE_TASK_DELAY_TIME, UDP_PORT_COLLECTION_SCAN_FREQUENCY, TimeUnit.SECONDS);
		//UDP端口信息定期保存至数据库
		service.scheduleAtFixedRate(PortManager::persistent, Config.UDP_PORT_SAVE_FREQUENCY, Config.UDP_PORT_SAVE_FREQUENCY, TimeUnit.SECONDS);

		//UDP连接监控
		service.scheduleWithFixedDelay(UDPSessionManager::monitor, Config.SCHEDULE_TASK_DELAY_TIME, UDP_ONLINE_SCAN_FREQUENCY, TimeUnit.SECONDS);

		//TCP连接监控
//		service.scheduleWithFixedDelay(TCPSessionManager::acceptMonitor, Config.SCHEDULE_TASK_DELAY_TIME, TCP_TIME_OUT_SCAN_FREQUENCY, TimeUnit.SECONDS);
//		service.scheduleWithFixedDelay(TCPSessionManager::appMonitor, Config.SCHEDULE_TASK_DELAY_TIME, TCP_TIME_OUT_SCAN_FREQUENCY, TimeUnit.SECONDS);
//		service.scheduleWithFixedDelay(TCPSessionManager::gatewayMonitor, Config.SCHEDULE_TASK_DELAY_TIME, TCP_TIME_OUT_SCAN_FREQUENCY, TimeUnit.SECONDS);

		//TCP消息处理和监控
		service.scheduleWithFixedDelay(MessageManager::monitor, Config.SCHEDULE_TASK_DELAY_TIME, 3, TimeUnit.SECONDS);
		service.scheduleWithFixedDelay(MessageManager::process, Config.SCHEDULE_TASK_DELAY_TIME, 3, TimeUnit.SECONDS);
//		service.scheduleWithFixedDelay(MessageManager::persistent, Config.SCHEDULE_TASK_DELAY_TIME, 5, TimeUnit.SECONDS);
		//TODO:日志处理
	}

}
