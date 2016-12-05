package com.dnake.smart.core.kit;

import com.dnake.smart.core.session.TcpSessionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 日志管理
 */
public class LogKit {

	private final LinkedBlockingQueue<Content> queue;
	/**
	 * 日志
	 */
	private final Logger logger;
	private Logger receiveLogger;
	private Logger sendLogger;
	private Logger exceptionLogger;
	private Logger eventLogger;
	private ExecutorService service;

	public LogKit() {
		this(LogKit.class.getName());
	}

	public LogKit(String name) {
		this(name, 5000);
	}

	public LogKit(String name, int capacity) {
		queue = new LinkedBlockingQueue<>(capacity);
		logger = LoggerFactory.getLogger(name);
	}

	/**
	 * @param logger         日志管理
	 * @param category       日志分类
	 * @param tcpSessionInfo 连接信息
	 * @param message        日志内容
	 */
	private void append(Logger logger, Category category, TcpSessionInfo tcpSessionInfo, Object message) {
		try {
			queue.add(new Content(category, tcpSessionInfo, message));
		} catch (IllegalStateException e) {
			System.err.println("日志队列已满...");
			return;
		}

		if (service == null) {
			service = Executors.newSingleThreadExecutor();
			service.execute(() -> {
				try {
					Content content = queue.take();
					String msg = content.getMessage().toString();
					switch (category) {
						case RECEIVE:
							receiveLogger.info(msg);
							break;
						case SEND:
							sendLogger.info(msg);
							break;
						case EXCEPTION:
							exceptionLogger.info(msg);
							break;
						case EVENT:
							eventLogger.info(msg);
							break;
						default:
							break;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					exceptionLogger.error("error", e);
				}
			});
		}
	}

	/**
	 * 日志分类
	 */
	private enum Category {
		RECEIVE,
		SEND,
		EXCEPTION,
		EVENT,
	}

	/**
	 * 日志内容
	 */
	@Getter
	@AllArgsConstructor
	private static class Content {
		private Category category;
		private TcpSessionInfo tcpSessionInfo;
		private Object message;
	}

}
