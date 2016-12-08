package com.dnake.smart.core.session;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.dict.Device;
import com.dnake.smart.core.dict.SessionAttributeKey;
import com.dnake.smart.core.kit.ConvertKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.dnake.smart.core.config.Config.*;
import static com.dnake.smart.core.kit.ValidateKit.time;

/**
 * TCP会话(连接)管理
 */
public class TCPSessionManager {

	private static final Logger logger = LoggerFactory.getLogger(TCPSessionManager.class);
	//请求连接(登录后移除)
	private static final Map<String, TcpSessionInfo> ACCEPT_MAP = new ConcurrentHashMap<>();
	//登录的app连接
	private static final Map<String, TcpSessionInfo> APP_MAP = new ConcurrentHashMap<>(APP_PREDICT);
	//登录的网关连接,key为网关sn号
	private static final Map<String, TcpSessionInfo> GATEWAY_MAP = new ConcurrentHashMap<>(GATEWAY_PREDICT);

	/**
	 * 分配默认id:accept||app
	 */
	private static String id(Channel channel) {
		return channel.id().asLongText();
	}

	/**
	 * 初始连接时保存数据
	 */
	public static void init(Channel channel) {
		ACCEPT_MAP.put(id(channel), TcpSessionInfo.init(channel));
	}

	/**
	 * TODO:创建缓冲set优化
	 *
	 * @param sn    网关sn号
	 * @param apply 请求端口
	 * @return 为网关分配UDP端口
	 */
	public static int allocate(String sn, int apply) {
		synchronized (GATEWAY_MAP) {
			GATEWAY_MAP.forEach((k, v) -> {
			});
		}
		return 9999;
	}

	/**
	 * 登录成功操作
	 */
	public static boolean pass(Channel channel) {
		//1.通过登录验证,从ACCEPT中移除
		String id = id(channel);
		TcpSessionInfo sessionInfo = ACCEPT_MAP.remove(id);
		if (sessionInfo == null) {
			Log.logger(Category.EVENT, "登录超时(未及时登录,会话已关闭).");
			return false;
		}

		Device device = type(channel);
		if (device == null) {
			Log.logger(Category.EVENT, "验证失败(错误的登录信息).");
			return false;
		}

		switch (device) {
			case APP:
				APP_MAP.put(id, sessionInfo);
				Log.logger(Category.EVENT, "app登录成功.");
				return true;
			case GATEWAY:
				String sn = channel.attr(SessionAttributeKey.SN).get();
				synchronized (GATEWAY_MAP) {
					//关闭已存在的连接,防止重复登录
					TcpSessionInfo original = GATEWAY_MAP.remove(sn);
					if (original != null) {
						Log.logger(Category.EVENT, ">>>>>>>>>>>>>关闭已有的连接[" + sn + "],上次登录时间:" + ConvertKit.from(original.getCreate()));
						original.getChannel().close();
					}
				}
				GATEWAY_MAP.put(sn, sessionInfo);
				Log.logger(Category.EVENT, "网关登录成功.");
				return true;
			default:
				Log.logger(Category.EVENT, "未知设备.");
				return false;
		}
	}

	/**
	 * 查询连接类型
	 */
	private static Device type(Channel channel) {
		return channel.attr(SessionAttributeKey.TYPE).get();
	}

	/**
	 * 查询(已登录)的连接信息
	 */
	public static TcpSessionInfo info(Channel channel) {
		Device device = type(channel);
		if (device == null) {
			return null;
		}
		switch (device) {
			case APP:
				return APP_MAP.get(id(channel));
			case GATEWAY:
				String sn = channel.attr(SessionAttributeKey.SN).get();
				return GATEWAY_MAP.get(sn);
			default:
				return null;
		}
	}

	/**
	 * 关闭连接并删除连接记录
	 */
	public static boolean close(Channel channel) {
		//默认id,ACCEPT与APP
		String id = id(channel);
		if (channel != null && channel.isOpen()) {
			channel.close();
		}
		TcpSessionInfo info = ACCEPT_MAP.remove(id);
		if (info != null) {
			logger.debug("accept channel close.");
			return true;
		}
		info = APP_MAP.remove(id);
		if (info != null) {
			logger.debug("app channel close.");
			return true;
		}

		for (Map.Entry<String, TcpSessionInfo> entry : GATEWAY_MAP.entrySet()) {
			String sn = entry.getKey();
			TcpSessionInfo sessionInfo = entry.getValue();
			if (sessionInfo != null && sessionInfo.getChannel() == channel) {
				GATEWAY_MAP.remove(sn);
				logger.debug("gateway channel close.");
				return true;
			}
		}
		return false;

//		if (APP_MAP.containsKey(id)) {
//			synchronized (APP_MAP) {
//				channel.close();
//				APP_MAP.remove(id);
//			}
//			logger.debug("APP_MAP channel close.");
//			return true;
//		}
//
//		for (Map.Entry<String, TcpSessionInfo> entry : GATEWAY_MAP.entrySet()) {
//			String sn = entry.getKey();
//			TcpSessionInfo sessionInfo = entry.getValue();
//			if (sessionInfo != null && sessionInfo.getChannel() == channel) {
//				channel.close();
//				GATEWAY_MAP.remove(sn);
//				logger.debug("GATEWAY_MAP channel close.");
//				return true;
//			}
//		}
//
//		channel.close();
//		ACCEPT_MAP.remove(id);
//		logger.debug("ACCEPT_MAP channel close.");
//		return true;
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
				logger.debug("当前未登录连接:[" + ACCEPT_MAP.size() + "]");
				ACCEPT_MAP.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && time(sessionInfo.getCreate(), Config.LOGIN_TIME_OUT)) {
						logger.debug("超时未登录");
						remove(ACCEPT_MAP, id);
					}
				});

				//APP在线超时
				logger.debug("当前APP连接:[" + APP_MAP.size() + "]");
				APP_MAP.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && time(sessionInfo.getCreate(), APP_TIME_OUT)) {
						logger.debug("APP在线时长已到,移除!");
						remove(APP_MAP, id);
					}
				});

				//GATEWAY在线超时
				logger.debug("当前网关连接:[" + GATEWAY_MAP.size() + "]");
				GATEWAY_MAP.forEach((id, sessionInfo) -> {
					if (sessionInfo != null && time(sessionInfo.getCreate(), GATEWAY_TIME_OUT)) {
						logger.debug("网关在线时长已到,移除!");
						remove(GATEWAY_MAP, id);
					}
				});
			}
		};

		service.submit(() -> {
			while (true) {
				Log.logger(Category.EVENT, "当前网关数:" + GATEWAY_MAP.size());
				GATEWAY_MAP.forEach((k, v) -> Log.logger(Category.EVENT, "sn:" + k));
				Thread.sleep(1500);
			}
		});
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
