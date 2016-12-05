package com.dnake.smart.core.session;

import com.dnake.smart.core.kit.ValidateKit;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * TCP连接信息
 *
 * @author zhsy
 */
@Getter
@Setter
@ToString
public class TcpSessionInfo {
	private static final long MIN_MILL = LocalDateTime.of(2016, 12, 1, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

	private Channel channel;
	private Device device;
	private String sn;//设备号(目前只记录网关)
	private long create;//本次连接的创建时间

	//private volatile long update;//更新时间(暂时无用)
	private volatile boolean pass;//是否通过验证(目前只用于客户端登录验证)

	private TcpSessionInfo() {
	}

	/**
	 * device,sn在接收登录请求后获取,pass在登录后更新
	 */
	private static TcpSessionInfo build(Channel channel, Device device, String sn, long create, boolean pass) {
		if (channel == null || create < MIN_MILL) {
			throw new RuntimeException("params is invalid.");
		}
		TcpSessionInfo sessionInfo = new TcpSessionInfo();

		sessionInfo.setChannel(channel);
		sessionInfo.setDevice(device);
		sessionInfo.setSn(sn);
		sessionInfo.setCreate(create);
		sessionInfo.setPass(pass);

		return sessionInfo;
	}

	/**
	 * 连接时初始化
	 */
	public static TcpSessionInfo init(Channel channel) {
		return build(channel, null, null, System.currentTimeMillis(), false);
	}

	/**
	 * 通过登录后
	 */
	public TcpSessionInfo pass(Device device, String sn) {
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

		this.pass = true;
		return this;
	}

}
