package com.dnake.smart.core.session.udp;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.dict.Action;
import com.dnake.smart.core.dict.Key;
import com.dnake.smart.core.dict.Result;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.server.udp.UDPClient;
import com.dnake.smart.core.server.udp.UDPServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UDP心跳管理
 */
public final class UDPSessionManager {
	/**
	 * 网关UDP心跳记录,key=sn
	 */
	private static final Map<String, UDPSession> GATEWAY_MAP = new ConcurrentHashMap<>();

	public static UDPSession find(String sn) {
		return GATEWAY_MAP.get(sn);
	}

	public static void append(UDPSession session) {
		GATEWAY_MAP.put(session.getSn(), session);
	}

	public static void respond(InetSocketAddress target) {
		JSONObject json = new JSONObject();
		json.put(Key.RESULT.getName(), Result.OK.getName());
		send(target, json);
	}

	public static void awake(String host, int port) {
		awake(new InetSocketAddress(host, port));
	}

	private static void awake(InetSocketAddress target) {
		JSONObject json = new JSONObject();
		json.put(Key.ACTION.getName(), Action.LOGIN_READY.getName());
		send(target, json);
	}

	/**
	 * 清理过期的数据
	 */
	public static void monitor() {
		Log.logger(Category.EVENT, "当前UDP在线网关数:[" + GATEWAY_MAP.size() + "]");
		Iterator<Map.Entry<String, UDPSession>> iterator = GATEWAY_MAP.entrySet().iterator();
		while (iterator.hasNext()) {
			UDPSession session = iterator.next().getValue();
			long createTime = session.getHappen();
			if (!ValidateKit.time(createTime, Config.UDP_MAX_IDLE)) {
				iterator.remove();
			}
		}
	}

	/**
	 * @param target 目标地址
	 * @param json   JSON数据
	 */
	private static void send(InetSocketAddress target, JSONObject json) {
		if (UDPServer.getChannel() == null) {
			Log.logger(Category.UDP, UDPServer.class.getSimpleName() + " 尚未启动");
			return;
		}
		ByteBuf buf = Unpooled.copiedBuffer(json.toString().getBytes(CharsetUtil.UTF_8));
		UDPServer.getChannel().writeAndFlush(new DatagramPacket(buf, target));
	}

	/**
	 * 推送至web服务器
	 */
	public static void push() {
		List<UDPSession> list = new ArrayList<>(GATEWAY_MAP.values());

		final int batch = 10;

		for (int i = 0; i < list.size(); i += batch) {
			JSONObject json = new JSONObject();
			json.put(Key.ACTION.getName(), Action.UDP_PUSH.getName());
			json.put(Key.DATA.getName(), list.subList(i, Math.min(i + batch, list.size())));
			UDPClient.send(json.toString());
		}
	}
}
