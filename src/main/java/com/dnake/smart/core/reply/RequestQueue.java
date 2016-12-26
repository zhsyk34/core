package com.dnake.smart.core.reply;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ValidateKit;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 网关待处理的消息队列
 */
@Getter
@Setter
final class RequestQueue {
	private final BlockingQueue<Request> queue;
	private volatile boolean send;
	private volatile long time;

	private RequestQueue() {
		queue = new LinkedBlockingQueue<>();
		this.reset();
	}

	static RequestQueue instance() {
		return new RequestQueue();
	}

	/**
	 * 重置队列状态
	 */
	private synchronized RequestQueue reset() {
		this.send = false;
		this.time = -1;
		return this;
	}

	/**
	 * 当队列数据被处理时开启警戒状态以进行监测
	 */
	private synchronized RequestQueue guard() {
		this.send = true;
		this.time = System.currentTimeMillis();
		return this;
	}

	/**
	 * 添加数据
	 */
	boolean offer(Request request) {
		return request != null && queue.offer(request);
	}

	/**
	 * 查看队列首元素是否正被处理,如是则不进行任何操作,否则取出并进入警戒状态
	 */
	synchronized Request peek() {
		if (send) {
			return null;
		}
		Request request = queue.peek();
		if (request != null) {
			guard();
		}
		return request;
	}

	/**
	 * 移除已处理完的数据
	 */
	synchronized Request poll() {
		Request request = queue.poll();
		reset();
		return request;
	}

	/**
	 * 响应超时时清空队列
	 *
	 * @return 返回当前队列中所有元素的副本
	 */
	synchronized Queue<Request> clear() {
		if (send && !ValidateKit.time(time, Config.TCP_MESSAGE_SEND_AWAIT)) {
			BlockingQueue<Request> copy = new LinkedBlockingQueue<>(queue);
			queue.clear();
			return copy;
		}
		return null;
	}
}
