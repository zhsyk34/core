package com.dnake.smart.core.task;

import com.dnake.smart.core.reply.MessageManager;
import com.dnake.smart.core.session.tcp.PortManager;
import com.dnake.smart.core.session.tcp.TCPSessionManager;
import com.dnake.smart.core.session.udp.UDPSessionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.dnake.smart.core.config.Config.*;

public class TaskHandler {

	public static void execute() {
		//TODO:读取端口信息
		PortManager.init();
		//TODO
		ScheduledExecutorService service = Executors.newScheduledThreadPool(8);
		//端口回收
		service.scheduleWithFixedDelay(PortManager::reduce, 1, UDP_PORT_COLLECTION_SCAN_FREQUENCY, TimeUnit.SECONDS);
		//TCP连接监控
		service.scheduleWithFixedDelay(TCPSessionManager::monitor, 1, TCP_TIME_OUT_SCAN, TimeUnit.SECONDS);
		//UDP连接监控
		service.scheduleWithFixedDelay(UDPSessionManager::monitor, 1, UDP_ONLINE_SCAN_FREQUENCY, TimeUnit.SECONDS);
		//消息处理和监控
		service.scheduleWithFixedDelay(MessageManager::monitor, 1, 10, TimeUnit.SECONDS);
		service.scheduleWithFixedDelay(MessageManager::process, 1, 10, TimeUnit.SECONDS);
		//service.scheduleWithFixedDelay(MessageManager::persistent, 1, 5, TimeUnit.SECONDS);
		//TODO:日志处理
	}

}
