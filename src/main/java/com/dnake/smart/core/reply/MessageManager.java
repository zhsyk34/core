package com.dnake.smart.core.reply;

import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.session.tcp.TCPSessionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MessageManager {
	private static final Map<String, MessageQueue> MAP = new ConcurrentHashMap<>();
	private static final List<Record> DB_LIST = new CopyOnWriteArrayList<>();

	/**
	 * APP 请求
	 */
	public static boolean request(String sn, Message message) {
		final MessageQueue queue;
		synchronized (MAP) {
			if (MAP.containsKey(sn)) {
				queue = MAP.get(sn);
			} else {
				queue = new MessageQueue();
				MAP.put(sn, queue);
			}
		}
		return queue.append(message);
	}

	/**
	 * 网关响应
	 */
	public static void response(String sn, String command) {
		MessageQueue queue = MAP.get(sn);
		if (queue == null || ValidateKit.isEmpty(queue.getList())) {
			Log.logger(Category.EVENT, "消息队列已被清空");
			return;
		}
		try {
			Message message = queue.getList().take();
			String app = message.getSrc();
			queue.setSend(false);
			TCPSessionManager.respond(app, command);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 循环扫描处理消息队列
	 */
	public static void process() {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		Runnable task = () -> {
			Log.logger(Category.EVENT, "开始处理消息队列,共[" + MAP.size() + "]条");
			MAP.forEach((sn, queue) -> {
				BlockingQueue<Message> list = queue.getList();
				Message message = list.peek();
				if (message != null && !queue.isSend()) {
					TCPSessionManager.forward(sn, message.getCommand());
					queue.setSend(true);
				}
			});
		};
		service.scheduleWithFixedDelay(task, 1, 5, TimeUnit.SECONDS);
	}

	/**
	 * 保存网关推送的数据
	 */
	public static void save(String sn, String command) {
		DB_LIST.add(new Record(sn, command));
	}

//	/**
//	 * 移除响应时间超时(自发送起**秒内未及时回复)的消息
//	 */
//	public static void monitor() {
////		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
////		Runnable task = () -> {
////			while (true) {
////				MAP.forEach((sn, list) -> {
////					Message data = list.peek();
////					if (data != null && data.isSend() && !ValidateKit.time(data.getTime(), MESSAGE_SEND_AWAIT)) {
////						list.poll();
////					}
////				});
////			}
////		};
////		service.schedule(task, 0, TimeUnit.SECONDS);
//	}

}
