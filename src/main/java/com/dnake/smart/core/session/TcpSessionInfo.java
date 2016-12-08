package com.dnake.smart.core.session;

import com.dnake.smart.core.dict.Device;
import com.dnake.smart.core.kit.ValidateKit;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TCP连接信息
 */
@Getter
@Setter
@ToString
public class TcpSessionInfo {
	private static final long MIN_MILL = LocalDateTime.of(2016, 12, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	private Channel channel;
	private String sn;//设备号(目前只记录网关)
	private long create;//本次连接的创建时间

	/**
	 * 以下字段作废
	 */
	@Deprecated
	private Device device;
	@Deprecated
	private String verify;//本次登录的验证码
	@Deprecated
	private volatile long update;//更新时间(暂时无用)
	@Deprecated
	private volatile boolean login;//是否通过验证(通过队列管理)

	private TcpSessionInfo() {
	}

	/**
	 * device,sn在接收登录请求后获取,pass在登录后更新
	 */
	@Deprecated
	private static TcpSessionInfo build(Channel channel, Device device, String sn, long create, boolean login) {
		if (channel == null || create < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		TcpSessionInfo sessionInfo = new TcpSessionInfo();

		sessionInfo.setChannel(channel);
		sessionInfo.setDevice(device);
		sessionInfo.setSn(sn);
		sessionInfo.setCreate(create);
		sessionInfo.setLogin(login);

		return sessionInfo;
	}

	/**
	 * @param channel 连接
	 * @param sn      网关sn
	 * @param create  创建连接的时间
	 * @return 会话信息
	 */
	private static TcpSessionInfo build(Channel channel, String sn, long create) {
		if (channel == null || create < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		TcpSessionInfo sessionInfo = new TcpSessionInfo();

		sessionInfo.setChannel(channel);
		sessionInfo.setSn(sn);
		sessionInfo.setCreate(create);

		return sessionInfo;
	}

	/**
	 * 连接时初始化
	 */
	public static TcpSessionInfo init(Channel channel) {
		return build(channel, null, System.currentTimeMillis());
	}

	/**
	 * 由队列管理
	 */
	@Deprecated
	public TcpSessionInfo pass(Device device, String sn, boolean login) {
		if (device == null) {
			throw new RuntimeException("error type with device");
		}
		this.device = device;

		if (device == Device.GATEWAY) {
			if (ValidateKit.isEmpty(sn)) {
				throw new RuntimeException("gateway sn can't be empty.");
			}
			this.sn = sn;
		}
		this.login = login;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o || o == null) {
			return this == o;
		}
		if (this.getClass() != o.getClass()) {
			return false;
		}

		TcpSessionInfo that = (TcpSessionInfo) o;

		return sn.equals(that.sn);
	}

	@Override
	public int hashCode() {
		return sn.hashCode();
	}

}
