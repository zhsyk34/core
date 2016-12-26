package com.dnake.smart.core.push;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.Action;
import com.dnake.smart.core.dict.Key;
import com.dnake.smart.core.session.udp.UDPSession;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Push {

	public static void main(String[] args) {
		tcpLogin();
		tcpLogout();
		udp();
	}

	public static void tcpLogin() {
		JSONObject json = new JSONObject();
		json.put(Key.ACTION.getName(), Action.LOGIN.getName());
		json.put(Key.SN.getName(), "2-1-1-1");
		json.put(Key.IP.getName(), "192.168.1.111");
		json.put(Key.PORT.getName(), 3421);
		json.put(Key.HAPPEN.getName(), System.currentTimeMillis());
		System.out.println(json);
	}

	public static void tcpLogout() {
		JSONObject json = new JSONObject();
		json.put(Key.ACTION.getName(), Action.LOGOUT.getName());
		json.put(Key.SN.getName(), "2-1-1-1");
		json.put(Key.HAPPEN.getName(), System.currentTimeMillis());
		System.out.println(json);
	}

	public static void udp() {
		List<UDPSession> list = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			UDPSession session = UDPSession.from(new InetSocketAddress("192.168.14.222", 50005));
			session.setSn("2-1-1-1").setHappen(System.currentTimeMillis()).setVersion("v2.4");

			list.add(session);
		}

		JSONObject json = new JSONObject();
		json.put(Key.ACTION.getName(), Action.UDP_PUSH.getName());
		json.put(Key.DATA.getName(), list);
		System.out.println(json);
	}
}
