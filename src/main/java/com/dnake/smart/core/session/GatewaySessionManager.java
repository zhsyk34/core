package com.dnake.smart.core.session;

import com.dnake.smart.core.kit.DESKit;
import com.dnake.smart.core.server.udp.UDPServerHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GatewaySessionManager {

	@Getter
	private static final Map<String, GatewayInfo> map = new HashMap<>();

	//TODO:TASK
	static {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(() -> {
			System.out.println("count:" + map.size());
			map.forEach((sn, info) -> System.out.println(sn));
		}, 1, 5, TimeUnit.SECONDS);
	}

	public static void init() {
		//TODO load from db
	}

	/**
	 * 更新网关信息:数据校验在调用前进行
	 *
	 * @param info    接收到的心跳包数据
	 * @param address 网关网络信息
	 */
	public static boolean update(UDPServerHandler.HeartInfo info, InetSocketAddress address) {
		String sn = info.getDevSN();
		String udid = DESKit.digestMd5(sn);
		String version = info.getAppVersionNo();

		String ip = address.getHostString();
		int port = address.getPort();

		if (map.containsKey(udid)) {
			GatewayInfo gatewayInfo = map.get(udid);
			gatewayInfo.setIp(ip).setCurUdpPort(port).setVersion(version).setUpdateTime(System.currentTimeMillis());
		} else {
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setSn(sn).setUdid(udid);
			gatewayInfo.setIp(ip).setCurUdpPort(port).setVersion(version).setUpdateTime(System.currentTimeMillis());
			map.put(udid, gatewayInfo);
		}
		return true;
	}

	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	@Accessors(chain = true)
	private static class GatewayInfo {
		private int id;
		private String sn;
		private String udid;
		private String ip;
		private int tcpPort;
		private int udpPort;//分配的端口号(unique)
		private int curUdpPort;//实际(外网)连接的端口号
		private long createTime;//首次连接时间
		private long updateTime;//心跳时更新时间
		private String version;
	}

}
