package com.dnake.smart.core.reply;

import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;

import java.util.Map;
import java.util.concurrent.*;

import static com.dnake.smart.core.config.Config.MESSAGE_SEND_AWAIT;

public class CommandManager {

	private static final Map<String, BlockingQueue<Message>> MAP = new ConcurrentHashMap<>();

	public static boolean add(Message message) {
		if (message == null) {
			return false;
		}
		Log.logger(Category.EVENT, "将请求:\n" + message + "\n添加到待处理队列中");

		BlockingQueue<Message> queue;
		String dest = message.getDest();

		synchronized (MAP) {
			if (MAP.containsKey(dest)) {
				queue = MAP.get(dest);
			} else {
				queue = new LinkedBlockingQueue<>();
				MAP.put(dest, queue);
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
					if (data != null && data.isSend() && !ValidateKit.time(data.getTime(), MESSAGE_SEND_AWAIT)) {
						list.poll();
					}
				});
			}
		};
		service.schedule(task, 0, TimeUnit.SECONDS);
	}

}
