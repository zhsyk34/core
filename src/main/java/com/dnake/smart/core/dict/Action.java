package com.dnake.smart.core.dict;

import com.dnake.smart.core.kit.ValidateKit;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 可能的指令action,,只枚举服务端需要处理的情况,转包情况未列入
 */
@Getter
public enum Action {

	/**
	 * 1.登录相关
	 */
	LOGIN_READY(1, 1, "loginReady", "登录通知"),
	LOGIN_REQ(1, 2, "loginReq", "登录请求"),
	LOGIN_VERIFY(1, 3, "loginVerify", "登录验证"),

	/**
	 * 4.心跳/推送
	 */
	HEART_BEAT(4, 1, "cmtHeartbeat", "登录验证"),
	UNLOCK(4, 2, "cmtUnlock", "开锁信息"),
	ALARM(4, 3, "cmtAlarm", "报警信息"),
	DEV_INFO(4, 4, "cmtDevInfo", "设备信息"),
	LOGIN(4, 5, "login", "tcp登录"),
	LOGOUT(4, 6, "logout", "tcp离线"),
	UDP_PUSH(4, 6, "udpPush", "udp推送信息"),

	//TODO:测试
	TEST(5, 1, "test", "测试");

	private static final Map<String, Action> MAP = new HashMap<>();

	static {
		for (Action action : values()) {
			MAP.put(action.getName(), action);
		}
	}

	private final int type;
	private final int index;
	private final String name;
	private final String description;

	/**
	 * @param type        指令类型:1.登录相关,2.控制命令,3.设置命令,4.推送命令
	 * @param index       指令编号(暂时无用)
	 * @param name        指令名称
	 * @param description 指令描述
	 */
	Action(int type, int index, String name, String description) {
		this.type = type;
		this.index = index;
		this.name = name;
		this.description = description;
	}

	public static Action get(String name) {
		return ValidateKit.isEmpty(name) ? null : MAP.get(name);
	}

}
