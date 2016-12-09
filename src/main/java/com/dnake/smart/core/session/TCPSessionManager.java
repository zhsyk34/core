package com.dnake.smart.core.session;

import com.dnake.smart.core.dict.Device;
import com.dnake.smart.core.dict.SessionAttributeKey;
import com.dnake.smart.core.kit.ConvertKit;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.dnake.smart.core.config.Config.APP_PREDICT;
import static com.dnake.smart.core.config.Config.GATEWAY_PREDICT;
import static com.dnake.smart.core.session.PortManager.PORT_MAP;
import static com.dnake.smart.core.session.PortManager.allocate;

/**
 * TCP会话(连接)管理
 */
public class TCPSessionManager {

	//请求连接(登录后移除)
	private static final Map<String, TCPBaseSession> ACCEPT_MAP = new ConcurrentHashMap<>();
	//登录的app连接
	private static final Map<String, TCPAppSession> APP_MAP = new ConcurrentHashMap<>(APP_PREDICT);
	//登录的网关连接,key为网关sn号
	private static final Map<String, TCPGatewaySession> GATEWAY_MAP = new ConcurrentHashMap<>(GATEWAY_PREDICT);

	private static void remove(Channel channel) {
		if (channel != null && channel.isOpen()) {
			channel.close();
		}
	}

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
		ACCEPT_MAP.put(id(channel), TCPBaseSession.init(channel));
	}

	/**
	 * 通过验证后处理(调用前已验证channel缓存数据)
	 *
	 * @return 分配端口{-1:失败,0:APP,50000+:网关}
	 */
	public static int pass(Channel channel) {
		//1.通过登录验证,从 ACCEPT_MAP 中移除(可能存在超时)
		String id = id(channel);
		TCPBaseSession baseInfo = ACCEPT_MAP.remove(id);
		if (baseInfo == null) {
			Log.logger(Category.EVENT, "登录超时(未及时登录,会话已关闭).");
			return -1;
		}

		//2.登录类型
		Device device = type(channel);
		if (device == null) {
			Log.logger(Category.EVENT, "验证失败(错误的登录信息).");
			return -1;
		}

		switch (device) {
			case APP:
				APP_MAP.put(id, TCPAppSession.init(baseInfo));
				Log.logger(Category.EVENT, "app登录成功.");
				return 0;
			case GATEWAY:
				String sn = channel.attr(SessionAttributeKey.SN).get();
				String ip = baseInfo.getIp();
				int apply = channel.attr(SessionAttributeKey.UDP_PORT).get();

				synchronized (GATEWAY_MAP) {
					//关闭已存在的连接,防止重复登录
					TCPGatewaySession gatewayInfo = GATEWAY_MAP.remove(sn);
					if (gatewayInfo != null) {
						Log.logger(Category.EVENT, ">>>>>>>>>>>>>关闭已有的连接[" + sn + "],上次登录时间:" + ConvertKit.from(gatewayInfo.getCreateTime()));
						remove(gatewayInfo.getChannel());
					}

					//设置sn,allocation
					TCPGatewaySession session = TCPGatewaySession.init(baseInfo);
					int allocation = allocate(sn, ip, apply);
					GATEWAY_MAP.put(sn, session);
					Log.logger(Category.EVENT, "网关登录成功.");
					return allocation;
				}
			default:
				Log.logger(Category.EVENT, "未知设备.");
				return -1;
		}
	}

	/**
	 * 查询连接类型
	 */
	private static Device type(Channel channel) {
		return channel.attr(SessionAttributeKey.TYPE).get();
	}

	/**
	 * TODO:通过type关闭需加锁
	 * 关闭连接并删除连接记录
	 */
	public static boolean close(Channel channel) {
		if (channel == null) {
			return true;
		}
		//先直接关闭channel
		remove(channel);

		//默认id,ACCEPT与APP
		String id = id(channel);
		TCPBaseSession info = ACCEPT_MAP.remove(id);
		if (info != null) {
			return true;
		}
		info = APP_MAP.remove(id);
		if (info != null) {
			return true;
		}

		String sn = channel.attr(SessionAttributeKey.SN).get();
		if (ValidateKit.notEmpty(sn)) {
			return GATEWAY_MAP.remove(sn) != null;
		}

		Log.logger(Category.EXCEPTION, "关闭异常(缓存队列中未找到指定的连接):" + channel.remoteAddress());
		return false;
	}

	/**
	 * 启动线程扫描并移除
	 * 1.登录超时的连接
	 * 2.在线超时的连接
	 */
	public static void monitor() {
		//迭代
//		Iterator 获取最新数据
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		Runnable task = () -> PORT_MAP.forEach((ip, map) -> {
			System.out.println(ip);
			map.forEach((sn, port) -> System.out.println(sn + ":" + port));
		});

		//TODO:分别扫描
//		Runnable task = () -> {
//			while (true) {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				//登录超时
//				logger.debug("当前未登录连接:[" + ACCEPT_MAP.size() + "]");
//				ACCEPT_MAP.forEach((id, sessionInfo) -> {
//					if (sessionInfo != null && time(sessionInfo.getCreate(), Config.LOGIN_TIME_OUT)) {
//						logger.debug("超时未登录");
//						remove(ACCEPT_MAP, id);
//					}
//				});
//
//				//APP在线超时
//				logger.debug("当前APP连接:[" + APP_MAP.size() + "]");
//				APP_MAP.forEach((id, sessionInfo) -> {
//					if (sessionInfo != null && time(sessionInfo.getCreate(), APP_TIME_OUT)) {
//						logger.debug("APP在线时长已到,移除!");
//						remove(APP_MAP, id);
//					}
//				});
//
//				//GATEWAY在线超时
//				logger.debug("当前网关连接:[" + GATEWAY_MAP.size() + "]");
//				GATEWAY_MAP.forEach((id, sessionInfo) -> {
//					if (sessionInfo != null && time(sessionInfo.getCreate(), GATEWAY_TIME_OUT)) {
//						logger.debug("网关在线时长已到,移除!");
//						remove(GATEWAY_MAP, id);
//					}
//				});
//			}
//		};

//		service.submit(() -> {
//			while (true) {
//				Log.logger(Category.EXCEPTION, "当前网关数:" + GATEWAY_MAP.size());
//				GATEWAY_MAP.forEach((k, v) -> Log.logger(Category.EXCEPTION, "sn:" + k + ",port:" + v.getAllocation()));
//				Thread.sleep(1500);
//			}
//		});
		service.scheduleAtFixedRate(task, 10, 3, TimeUnit.SECONDS);
	}

}
