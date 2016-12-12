package com.dnake.smart.core.session.tcp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.AllocateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.session.udp.UDPPortRecord;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PortManager {

	//UDP端口分配(ip,(sn,obj))
	private static final Map<String, Map<String, UDPPortRecord>> PORT_MAP = new ConcurrentHashMap<>();

	/**
	 * TODO
	 * 从数据库加载初始信息
	 */
	public static void init() {
	}

	/**
	 * 定时清理垃圾数据(ip地址改变导致的端口占用)
	 */
	public static void reduce() {
		//转为(sn,(ip,obj))
		final Map<String, Map<String, UDPPortRecord>> snMap = new HashMap<>();
		PORT_MAP.forEach((ip, map) -> map.forEach((sn, record) -> {
			Map<String, UDPPortRecord> ipMap;
			if (!snMap.containsKey(sn)) {
				ipMap = new HashMap<>();
				snMap.put(sn, ipMap);
			} else {
				ipMap = snMap.get(sn);
			}

			ipMap.put(ip, record);
		}));
		//
		snMap.forEach((sn, map) -> {
			//
			if (map.size() > 1) {
				LinkedHashMap<String, UDPPortRecord> linkMap = map.entrySet().stream()
						.sorted((o1, o2) -> (int) (o1.getValue().getHappen() - o2.getValue().getHappen()))//map
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			}
		});

//		snMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
//				.collect(Collectors.toMap(
//						Map.Entry::getKey,
//						Map.Entry::getValue,
//						(e1, e2) -> e1,
//						LinkedHashMap::new
//				));
	}

	/**
	 * @param sn    网关sn号
	 * @param ip    网关ip
	 * @param apply 申请的端口
	 * @return 为网关分配UDP端口
	 */
	public static int allocate(String sn, String ip, int apply) {
		synchronized (PORT_MAP) {
			Log.logger(Category.EVENT, "网关请求登录登录信息为[" + ip + " : " + apply + "]");

			Map<String, UDPPortRecord> map;

			//1.
			if (!PORT_MAP.containsKey(ip)) {
				Log.logger(Category.EVENT, "该IP下无相应网关");
				map = new ConcurrentHashMap<>();
				map.put(sn, new UDPPortRecord(apply, System.currentTimeMillis()));

				PORT_MAP.put(ip, map);
				Log.logger(Category.EVENT, "启用端口");
				return apply;
			}

			//2.used
			map = PORT_MAP.get(ip);
			Set<Integer> set = new HashSet<>();
			map.forEach((k, v) -> set.add(v.getPort()));
			Log.logger(Category.EVENT, "该IP下已被使用的端口:\n" + Arrays.toString(set.toArray(new Integer[set.size()])));

			//3.init
			UDPPortRecord record = map.get(sn);
			if (record == null) {
				record = new UDPPortRecord(-1, System.currentTimeMillis());
				map.put(sn, record);
			}

			//4.该IP下的端口未被使用
			if (!set.contains(apply)) {
				Log.logger(Category.EVENT, "该端口未被使用");
				record.setPort(apply);
				Log.logger(Category.EVENT, "启用端口");
				return apply;
			}

			//5.申请的端口 apply 恰为原网关使用
			if (record.getPort() == apply) {
				Log.logger(Category.EVENT, "申请的端口" + apply + "恰为原网关使用");
				return apply;
			}

			//6.分配新端口
			int allocate = AllocateKit.allocate(Config.UDP_CLIENT_MIN_PORT, set);
			record.setPort(allocate);
			Log.logger(Category.EVENT, "网关" + sn + "申请的端口[" + apply + "]已被使用,为其分配新端口[" + allocate + "]");
			return allocate;
		}
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<>();
		map.put("c", "ccccc");
		map.put("a", "aaaaa");
		map.put("b", "bbbbb");
		map.put("d", "ddddd");

		Map<String, String> r = map.entrySet().stream().sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		r.forEach((k, v) -> {
			System.out.println(v);
		});
	}
}
