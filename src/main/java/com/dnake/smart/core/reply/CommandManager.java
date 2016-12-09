package com.dnake.smart.core.reply;

import com.dnake.smart.core.config.Config;

import java.util.Map;
import java.util.concurrent.*;

import static com.dnake.smart.core.kit.ValidateKit.time;

public class CommandManager {

	private static final Map<String, BlockingQueue<Message>> MAP = new ConcurrentHashMap<>();

	public static boolean add(Message message) {
		if (message == null) {
			return false;
		}
		BlockingQueue<Message> queue;
		String dest = message.getDest();

		synchronized (MAP) {
			if (MAP.containsKey(dest)) {
				queue = MAP.get(dest);
			} else {
				queue = new LinkedBlockingQueue<>();
			}
		}

		try {
			queue.put(message);
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	/**
	 * 移除响应时间超时(自发送起**秒内未及时回复)的消息
	 */
	public static void monitor() {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		Runnable task = () -> {
			while (true) {
				MAP.forEach((dest, list) -> {
					Message data = list.peek();
					if (data != null && data.isSend() && time(data.getTime(), Config.MESSAGE_SEND_AWAIT)) {
						list.poll();
					}
				});
			}
		};
		service.schedule(task, 0, TimeUnit.SECONDS);
	}

}
