package com.dnake.smart.core.session;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.AllocateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PortManager {

	//UDP端口分配(ip,(sn,obj))
	public static final Map<String, Map<String, UDPPortRecord>> PORT_MAP = new ConcurrentHashMap<>();

	private static boolean idle(String sn, String ip, int apply) {
		Log.logger(Category.EVENT, "网关请求登录登录信息为[" + ip + " : " + apply + "]");

		Map<String, UDPPortRecord> map;
		synchronized (PORT_MAP) {
			if (!PORT_MAP.containsKey(ip)) {
				Log.logger(Category.EVENT, "该IP下无相应网关");
				map = new ConcurrentHashMap<>();
				map.put(sn, new UDPPortRecord(apply, System.currentTimeMillis()));

				PORT_MAP.put(ip, map);
				Log.logger(Category.EVENT, "启用端口");
				return true;
			}
		}

		map = PORT_MAP.get(ip);
		synchronized (map) {
			Set<Integer> set = new HashSet<>();
			map.forEach((k, v) -> set.add(v.getPort()));
			Log.logger(Category.EVENT, "该IP下使用的端口:\n" + Arrays.toString(set.toArray(new Integer[set.size()])));

			//2.该IP下的端口未被使用
			if (!set.contains(apply)) {
				Log.logger(Category.EVENT, "该端口未被使用");
				if (!map.containsKey(sn)) {
					Log.logger(Category.EVENT, "启用端口");
					map.put(sn, new UDPPortRecord(apply, System.currentTimeMillis()));
				}
				return true;
			}

			//3.申请的端口apply恰为原网关使用
			UDPPortRecord record = map.get(sn);
			if (record != null && record.getPort() == apply) {
				Log.logger(Category.EVENT, "申请的端口" + apply + "恰为原网关使用");
				return apply;
			}

			//4.分配新端口
			int allocate = AllocateKit.allocate(Config.UDP_CLIENT_MIN_PORT, set);
			record.setPort(allocate);
			Log.logger(Category.EVENT, ">>>>>为网关" + sn + "分配新端口[" + allocate + "]");
			return allocate;
		}
	}

	/**
	 * @param sn 网关sn号
	 * @return 为网关分配UDP端口
	 */
	public static int allocate(String sn, String ip, int apply) {
		idle();
	}
}
