package com.dnake.smart.core.session.udp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.database.PortDao;
import com.dnake.smart.core.database.UDPRecord;
import com.dnake.smart.core.kit.AllocateKit;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class UDPPortManager {

	/**
	 * UDP端口分配(ip,(sn,obj))
	 */
	private static final Map<String, Map<String, UDPPortRegister>> PORT_MAP = new ConcurrentHashMap<>();

	/**
	 * 从数据库批量加载初始信息
	 */
	public static void load() {
		Log.logger(Category.EVENT, "正从数据库加载网关UDP端口信息...");

		List<UDPRecord> list;
		int cursor = 0;
		while (true) {
			list = PortDao.find(cursor, Config.BATCH_FETCH_SIZE);
			if (ValidateKit.isEmpty(list)) {
				break;
			}
			list.forEach(UDPPortManager::append);
			cursor += Config.BATCH_FETCH_SIZE;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("加载完毕:\n");
		builder.append("----------------------------------\n");
		PORT_MAP.forEach((ip, map) -> builder.append("ip[").append(ip).append("]下有[").append(map.size()).append("]个端口正被使用\n"));
		builder.append("----------------------------------\n");
		Log.logger(Category.EVENT, builder.toString());
	}

	private static void append(UDPRecord record) {
		final Map<String, UDPPortRegister> map;
		final String ip = record.getIp();
		synchronized (PORT_MAP) {
			if (PORT_MAP.containsKey(ip)) {
				map = PORT_MAP.get(ip);
			} else {
				map = new ConcurrentHashMap<>();
				PORT_MAP.put(ip, map);
			}
		}
		map.put(record.getSn(), UDPPortRegister.of(record.getPort(), record.getHappen()));
	}

	/**
	 * 获取数据后网关可能重新登录改变信息
	 *
	 * @return 网关最近被分配的端口
	 */
	public static int port(String ip, String sn) {
		Map<String, UDPPortRegister> map = PORT_MAP.get(ip);
		if (map == null) {
			return -1;
		}
		UDPPortRegister record = map.get(sn);
		return record == null ? -1 : record.port();
	}

	/**
	 * 定时清理垃圾数据以回收端口(ip地址改变导致的端口占用)
	 */
	public static void recycle() {
		//重新分组:转为(sn,(ip,record))
		final Map<String, Map<String, UDPPortRegister>> snMap = new HashMap<>();

		PORT_MAP.forEach((ip, map) -> map.forEach((sn, record) -> {
			final Map<String, UDPPortRegister> ipMap;
			if (snMap.containsKey(sn)) {
				ipMap = snMap.get(sn);
			} else {
				ipMap = new HashMap<>();
				snMap.put(sn, ipMap);
			}

			ipMap.put(ip, record);
		}));

		snMap.forEach((sn, map) -> {
			Log.logger(Category.EVENT, "网关[" + sn + "]占用的端口号数为" + map.size());
			if (map.size() < 2) {
				Log.logger(Category.EVENT, "占用端口号数 < 2,无需清理");
				return;
			}
			//对需要移除的数据按照端口分配时间进行排序
			LinkedHashMap<String, UDPPortRegister> linkMap = map.entrySet().stream().sorted((o1, o2) -> o2.getValue().happen() - o1.getValue().happen() > 0 ? 1 : -1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			UDPPortRegister last = linkMap.entrySet().iterator().next().getValue();
			Log.logger(Category.EVENT, "网关[" + sn + "]最后使用的端口信息:[" + last + "]");
//			linkMap.forEach((ip, record) -> System.out.println(ip + ":" + record));

			//开始移除(移除时与首元素再次进行比较,防止误删,避免加锁)
			linkMap.forEach((ip, record) -> {
				Map<String, UDPPortRegister> presentSnMap = PORT_MAP.get(ip);
				if (presentSnMap != null) {
					UDPPortRegister udpPortRecord = presentSnMap.get(sn);
					if (udpPortRecord != null && udpPortRecord.happen() < last.happen()) {
						presentSnMap.remove(sn, udpPortRecord);
					}
				}
			});
		});
	}

	/**
	 * @param sn    网关sn号
	 * @param ip    网关ip
	 * @param apply 网关申请的UDP连接端口
	 * @return 为网关分配UDP端口
	 */
	public static int allocate(String sn, String ip, int apply) {
		synchronized (PORT_MAP) {
			Log.logger(Category.EVENT, "网关[" + sn + "]请求登录登录信息:[" + ip + " : " + apply + "]");

			final Map<String, UDPPortRegister> map;

			//1.idle
			if (!PORT_MAP.containsKey(ip)) {
				map = new ConcurrentHashMap<>();
				map.put(sn, UDPPortRegister.of(apply));

				PORT_MAP.put(ip, map);
				Log.logger(Category.EVENT, "该ip下无相应网关,直接启用端口");
				return apply;
			}

			//2.used
			map = PORT_MAP.get(ip);
			Set<Integer> set = new HashSet<>();
			map.forEach((k, v) -> set.add(v.port()));
			Log.logger(Category.EVENT, "该IP下已被使用的端口:\n" + Arrays.toString(set.toArray(new Integer[set.size()])));

			//3.load
			UDPPortRegister record = map.get(sn);
			if (record == null) {
				record = UDPPortRegister.of(-1);//temp,must update
				map.put(sn, record);
			} else {
				record.happen(System.currentTimeMillis());
			}

			//4.该IP下的端口未被使用
			if (!set.contains(apply)) {
				record.port(apply);
				Log.logger(Category.EVENT, "该端口未被使用,直接启用");
				return apply;
			}

			//5.申请的端口 apply 恰为原网关使用
			if (record.port() == apply) {
				Log.logger(Category.EVENT, "申请的端口" + apply + "恰为原网关使用");
				return apply;
			}

			//6.分配新端口
			int allocate = AllocateKit.allocate(Config.UDP_CLIENT_MIN_PORT, set);
			record.port(allocate);
			Log.logger(Category.EVENT, "网关[" + sn + "]申请的端口[" + apply + "]已被使用,为其分配新端口[" + allocate + "]");
			return allocate;
		}
	}

	/**
	 * 定期保存到数据库
	 */
	public static void persistent() {
		List<UDPRecord> list = new ArrayList<>();

		PORT_MAP.forEach((ip, map) -> map.forEach((sn, record) -> {
			list.add(new UDPRecord(ip, sn, record.port(), record.happen()));
		}));

		PortDao.save(list, Config.BATCH_FETCH_SIZE);
		Log.logger(Category.EVENT, "共更新了[" + list.size() + "]条数据");
	}

}
