package com.dnake.smart.core.session.tcp;

import com.dnake.smart.core.session.udp.UDPPortRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PortManagerTest {
	private static Map<String, Map<String, UDPPortRegister>> ipMap = new HashMap<>();

	private static void init() {
		int gwCnt = 4, ipCnt = 5;
		Random random = new Random();
		for (int i = 1; i < ipCnt; i++) {
			Map<String, UDPPortRegister> snMap = new HashMap<>();
			ipMap.put("192.168.1." + (100 + i), snMap);
			//>gwCnt random duplicate
			for (int j = 0; j < 8; j++) {
				int k = random.nextInt(gwCnt);
				long happen = random.nextInt(100);
				int port = 50000 + 7 * (j + 1);
				snMap.put("sn" + k, UDPPortRegister.of(port, happen));
			}
		}
	}

	public static void main(String[] args) {
		init();
		ipMap.forEach((ip, map) -> map.forEach((sn, record) -> {
//			System.out.println(ip + " " + sn + " " + record);
		}));
		System.out.println("-------------------------");

//		PortManager.reduce(ipMap);
	}

}