package com.dnake.smart.core.reply;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.ErrNo;
import com.dnake.smart.core.dict.Key;
import com.dnake.smart.core.dict.Result;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.session.tcp.TCPSessionManager;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dnake.smart.core.config.Config.TCP_MESSAGE_SEND_AWAIT;

public final class MessageManager {
	/**
	 * app请求消息处理队列
	 */
	private static final Map<String, MessageQueue> APP_REQUEST = new ConcurrentHashMap<>();

	/**
	 * 网关推送消息处理队列
	 */
	private static final List<Response> GATEWAY_PUSH = new CopyOnWriteArrayList<>();

	/**
	 * 将app请求添加到消息处理队列
	 *
	 * @param sn      app请求的网关
	 * @param request app请求的相关信息
	 * @return 是否受理
	 */
	public static boolean request(String sn, Request request) {
		final MessageQueue queue;
		synchronized (APP_REQUEST) {
			if (APP_REQUEST.containsKey(sn)) {
				queue = APP_REQUEST.get(sn);
			} else {
				queue = MessageQueue.instance();
				APP_REQUEST.put(sn, queue);
			}
		}
		return queue.offer(request);
	}

	/**
	 * 网关响应app请求后,将其请求从相应的队列中移除
	 *
	 * @param sn       受理的网关
	 * @param response 网关的回复
	 */
	public static void response(String sn, String response) {
		MessageQueue queue = APP_REQUEST.get(sn);

		Request request = queue.poll();
		if (request == null) {
			Log.logger(Category.EVENT, "消息队列已被清空(网关异常导致响应超时)");
		} else {
			TCPSessionManager.response(request.getSrc(), response);
		}
	}

	/**
	 * 保存网关推送的数据
	 */
	public static void save(String sn, String command) {
		GATEWAY_PUSH.add(Response.of(sn, command));
	}

	/**
	 * 处理app消息队列
	 */
	public static void process() {
		AtomicInteger count = new AtomicInteger();

		APP_REQUEST.forEach((sn, queue) -> {
			count.addAndGet(queue.getQueue().size());
			Request request = queue.peek();
			if (request != null) {
				TCPSessionManager.forward(sn, request.getCommand());
			}
		});

		Log.logger(Category.EVENT, "开始处理消息队列,共[" + count.get() + "]条");
	}

	/**
	 * TODO:推送至web服务器
	 * 持久化数据
	 */
	public static void push() {
		GATEWAY_PUSH.forEach(record -> {
			System.out.println(record.getDest());
			System.out.println(record.getCommand());
		});
	}

	/**
	 * 网关响应超时则清空当前的请求队列信息并关闭网关连接
	 * 同时提示app
	 */
	public static void monitor() {
		APP_REQUEST.forEach((sn, queue) -> {
			if (queue.isSend() && !ValidateKit.time(queue.getTime(), TCP_MESSAGE_SEND_AWAIT)) {
				TCPSessionManager.close(sn);
				Queue<Request> history = queue.clear();
				if (history != null) {
					Log.logger(Category.EXCEPTION, "网关[" + sn + "]响应超时,关闭连接并移除当前所有请求,共[" + history.size() + "]条");
					feedback(history);
				}
			}
		});
	}

	/**
	 * 回馈响应失败
	 *
	 * @param queue 需要回馈的消息队列
	 */
	private static void feedback(Queue<Request> queue) {
		ExecutorService service = Executors.newSingleThreadExecutor();
		JSONObject json = new JSONObject();
		json.put(Key.RESULT.getName(), Result.NO.getName());
		json.put(Key.ERRNO.getName(), ErrNo.TIMEOUT.getNo());
		json.put(Key.ERR_INFO.getName(), ErrNo.TIMEOUT.getDescription());
		service.submit(() -> queue.forEach(request -> TCPSessionManager.response(request.getSrc(), json.toString())));
		service.shutdown();
	}
}
