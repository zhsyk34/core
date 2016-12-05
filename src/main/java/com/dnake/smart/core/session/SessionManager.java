package com.dnake.smart.core.session;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ValidateKit;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.dnake.smart.core.config.Config.*;
import static com.dnake.smart.core.kit.ValidateKit.validateTime;
import static com.dnake.smart.core.session.Device.APP;
import static com.dnake.smart.core.session.Device.GATEWAY;

/**
 * 会话(连接)管理
 */
public class SessionManager {

	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
	//请求连接
	private static final Map<String, TcpSessionInfo> accept = new ConcurrentHashMap<>();
	//登录的app连接
	private static final Map<String, TcpSessionInfo> app = new ConcurrentHashMap<>(APP_PREDICT);
	//登录的网关连接
	private static final Map<String, TcpSessionInfo> gateway = new ConcurrentHashMap<>(GATEWAY_PREDICT);
	List<Channel> list;

	private static String id(Channel channel) {
		return channel.id().asLongText();
	}

	/**
	 * 初始连接时保存数据
	 */
	public static void add(Channel channel) {
		TcpSessionInfo sessionInfo = TcpSessionInfo.init(channel);
		accept.put(id(channel), sessionInfo);
	}

	/**
	 * 登录后更新数据
	 *
	 * @param type 设备类型
	 * @param sn   设备sn号(网关必要信息udid)
	 */
	public static boolean pass(Channel channel, int type, String sn) {
		Device device = Device.get(type);
		if (device == null) {
			logger.debug("error type with device.");
			return false;
		}
		if (device == GATEWAY && ValidateKit.isEmpty(sn)) {
			logger.debug("gateway sn can't be empty.");
			return false;
		}

		String id = id(channel);
		if (accept.containsKey(id)) {
			synchronized (accept) {
				if (accept.containsKey(id)) {
					TcpSessionInfo sessionInfo = accept.remove(id);

					sessionInfo.pass(device, sn);
					switch (device) {
						case APP:
							app.put(id, sessionInfo);
							break;
						case GATEWAY:
							gateway.put(sn, sessionInfo);
							break;
					}
					logger.debug("登录成功");
					return true;
				}
			}
		}
		logger.debug("the channel is not exists.");
		return false;
	}

	/**
	 * 查询(已登录)连接类型
	 */
	private static Device type(Channel channel) {
		//TODO
		/*AttributeKey<Integer> type = AttributeKey.newInstance("type");
		channel.attr(type).set(1);*/
		if (app.containsKey(id(channel))) {
			return APP;
		}

		for (TcpSessionInfo sessionInfo : gateway.values()) {
			if (sessionInfo != null && sessionInfo.getChannel() == channel) {
				return GATEWAY;
			}
		}

		return null;
	}

	/**
	 * 查询(已登录)的连接信息
	 */
	public static TcpSessionInfo info(Channel channel) {
		String id = id(channel);
		if (app.containsKey(id)) {
			synchronized (app) {
				if (app.containsKey(id)) {
					return app.get(id);
				}
			}
		}

		for (TcpSessionInfo sessionInfo : gateway.values()) {
			if (sessionInfo != null && sessionInfo.getChannel() == channel) {
				return sessionInfo;
			}
		}
		return null;
	}

	/**
	 * 关闭连接并删除连接记录
	 */
	public static boolean close(Channel channel) {
		String id = id(channel);
		if (app.containsKey(id)) {
			synchronized (app) {
				channel.close();
				app.remove(id);
			}
			logger.debug("app channel close.");
			return true;
		}

		for (Map.Entry<String, TcpSessionInfo> entry : gateway.entrySet()) {
			String sn = entry.getKey();
			TcpSessionInfo sessionInfo = entry.getValue();
			if (sessionInfo != null && sessionInfo.getChannel() == channel) {
				channel.close();
				gateway.remove(sn);
				logger.debug("gateway channel close.");
				return true;
			}
		}

		channel.close();
		accept.remove(id);
		logger.debug("accept channel close.");
		return true;
	}

	/**
	 * 启动线程扫描并移除
	 * 1.登录超时的连接
	 * 2.在线超时的连接
	 */
	public static void monitor() {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

		//TODO:分别扫描
		Runnable task = () -> {
			while (true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//登录超时
				logger.debug("当前未登录连接:[" + accept.size() + "]");
				accept.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && validateTime(sessionInfo.getCreate(), Config.LOGIN_TIME_OUT)) {
						logger.debug("超时未登录");
						remove(accept, id);
					}
				});

				//APP在线超时
				logger.debug("当前APP连接:[" + app.size() + "]");
				app.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && validateTime(sessionInfo.getCreate(), APP_TIME_OUT)) {
						logger.debug("APP在线时长已到,移除!");
						remove(app, id);
					}
				});

				//GATEWAY在线超时
				logger.debug("当前网关连接:[" + gateway.size() + "]");
				gateway.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && validateTime(sessionInfo.getCreate(), GATEWAY_TIME_OUT)) {
						logger.debug("网关在线时长已到,移除!");
						remove(gateway, id);
					}
				});
			}
		};

		service.submit(task);
		service.shutdown();
	}

	/**
	 * 删除指定监测数据并关闭对应的连接
	 */
	private static void remove(Map<String, TcpSessionInfo> map, String id) {
		TcpSessionInfo sessionInfo = map.remove(id);
		if (sessionInfo != null) {
			Channel channel = sessionInfo.getChannel();
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
		}
	}

}
