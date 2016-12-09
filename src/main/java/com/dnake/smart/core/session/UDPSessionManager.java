package com.dnake.smart.core.session;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * UDP心跳管理
 */
public class UDPSessionManager {

	private static final Map<String, UDPGatewaySession> UDP_GATEWAY = new ConcurrentHashMap<>();

	public static void append(UDPGatewaySession session) {
		UDP_GATEWAY.put(session.getSn(), session);
	}

	/**
	 * 更新TCP为其分配端口的UDP端口
	 *
	 * @param sn         网关SN
	 * @param allocation 分配出的端口
	 */
	public static void update(String sn, int allocation) {
		UDPGatewaySession session = UDP_GATEWAY.get(sn);
		if (session == null) {
			Log.logger(Category.UDP, "网关:[" + sn + "]未登录(UDP心跳)");
			return;
		}
		session.setAllocation(allocation);
	}

	public static void monitor() {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(() -> {
			Iterator<Map.Entry<String, UDPGatewaySession>> iterator = UDP_GATEWAY.entrySet().iterator();
			while (iterator.hasNext()) {
				UDPGatewaySession session = iterator.next().getValue();
				long createTime = session.getCreateTime();
				if (ValidateKit.time(createTime, Config.UDP_MAX_IDLE)) {
					iterator.remove();
				}
			}
		}, 10, Config.UDP_SCAN_TIME, TimeUnit.SECONDS);
	}
}
