package com.dnake.smart.core.reply;

import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 网关待处理的消息队列
 */
@Getter
@Setter
class MessageQueue {
	private final BlockingQueue<Message> list = new LinkedBlockingQueue<>();
	private volatile boolean send = false;
	private volatile long time;

	boolean append(Message message) {
		if (message == null) {
			return false;
		}
		Log.logger(Category.EVENT, "将请求:\n" + message + "\n添加到待处理队列中");

		try {
			list.put(message);
		} catch (InterruptedException e) {
			Log.logger(Category.EVENT, "添加消息到队列出现异常:" + e.getMessage());
			return false;
		}

		return true;
	}
}
