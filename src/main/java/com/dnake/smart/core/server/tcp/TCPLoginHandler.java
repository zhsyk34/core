package com.dnake.smart.core.server.tcp;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.*;
import com.dnake.smart.core.dict.Error;
import com.dnake.smart.core.kit.CodecKit;
import com.dnake.smart.core.kit.JsonKit;
import com.dnake.smart.core.kit.RandomKit;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.session.TCPSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.SocketAddress;

/**
 * 处理登录
 */
public class TCPLoginHandler extends ChannelInboundHandlerAdapter {

	//测试统计用
	//private static final AtomicInteger count = new AtomicInteger();

	/**
	 * 验证登录信息
	 * 1.必需信息:clientType
	 * 2.网关额外信息:devSn+UDPPort
	 */
	private boolean validate(Channel channel, JSONObject json) {
		Device device = Device.get(json.getIntValue(Key.TYPE.getName()));
		if (device == null) {
			Log.logger(Category.EVENT, "无效的登录请求(未明确登录类型).");
			return false;
		}
		//缓存登录类型
		channel.attr(SessionAttributeKey.TYPE).set(device);

		switch (device) {
			case APP:
				Log.logger(Category.EVENT, "APP登录请求");
				return true;
			case GATEWAY:
				String sn = json.getString(Key.SN.getName());
				Integer apply = json.getIntValue(Key.UDP_PORT.getName());

				if ((ValidateKit.isEmpty(sn) || ValidateKit.invalid(apply))) {
					Log.logger(Category.EVENT, "网关登录出错(请求数据错误)");
					return false;
				}

				Log.logger(Category.EVENT, "网关登录请求");
				//将网关额外的登录信息缓存在channel上,待通过验证后再统一管理;同时可实现sn与channel的双向绑定
				channel.attr(SessionAttributeKey.SN).set(sn);//{gateway-1}
				channel.attr(SessionAttributeKey.UDP_PORT).set(apply);//{gateway-2}

				return true;
			default:
				Log.logger(Category.EVENT, "无效的登录请求(未知的登录类型).");
				return false;
		}

	}

	/**
	 * 处理登录请求
	 * 1.非法或错误的请求将被拒绝
	 * 2.否则发送验证码
	 */
	private void login(ChannelHandlerContext ctx, JSONObject json) {
		JSONObject response = new JSONObject();
		if (!validate(ctx.channel(), json)) {
			response.put(Key.RESULT.getName(), Result.NO.getName());
			response.put(Key.ERRNO.getName(), Error.UNKNOWN.getNo());
			ctx.channel().writeAndFlush(response);
			//关闭连接
			ctx.channel().close();
			Log.logger(Category.EVENT, "无效的登录请求,拒绝连接");
			return;
		}

		int group = RandomKit.randomInteger(0, 49);
		int offset = RandomKit.randomInteger(0, 9);
		response.put(Key.ACTION.getName(), Action.LOGIN_VERIFY.getName());
		//本次登录的验证信息
		response.put(Key.KEY.getName(), CodecKit.loginKey(group, offset));
		//缓存本次登录的验证码
		ctx.channel().attr(SessionAttributeKey.KEYCODE).set(CodecKit.loginVerify(group, offset));

		ctx.writeAndFlush(response);
	}

	/**
	 * 通过验证码进行登录验证
	 * 验证码在此前步骤已被缓存
	 */
	private boolean verify(Channel channel, String keyCode) {
		//必需信息
		Device device = channel.attr(SessionAttributeKey.TYPE).get();
		String verify = channel.attr(SessionAttributeKey.KEYCODE).get();
		//网关额外信息
		String sn = channel.attr(SessionAttributeKey.SN).get();
		Integer apply = channel.attr(SessionAttributeKey.UDP_PORT).get();

		//非法登录
		if (ValidateKit.isEmpty(verify) || device == null || (device == Device.GATEWAY && (ValidateKit.isEmpty(sn) || ValidateKit.invalid(apply)))) {
			Log.logger(Category.EVENT, "非法登录");
			return false;
		}

		Log.logger(Category.EVENT, "客户端[" + channel.remoteAddress() + "] 进行登录验证[" + keyCode + "],正确的验证码为[" + verify + "]");
		//验证码错误
		if (!verify.equals(keyCode)) {
			Log.logger(Category.EVENT, "登录验证码错误");
			return false;
		}

		Log.logger(Category.EVENT, "验证码通过");
		return true;
	}

	/**
	 * 登录处理
	 */
	private void pass(ChannelHandlerContext ctx, String keyCode) {
		JSONObject response = new JSONObject();
		Channel channel = ctx.channel();

		if (!verify(channel, keyCode)) {
			response.put(Key.RESULT.getName(), Result.NO.getName());
			response.put(Key.ERRNO.getName(), Error.UNKNOWN.getNo());
			ctx.writeAndFlush(response);
			TCPSessionManager.close(channel);
			return;
		}

		int allocation = TCPSessionManager.pass(channel);

		if (allocation == -1) {
			response.put(Key.RESULT.getName(), Result.NO.getName());
			response.put(Key.ERRNO.getName(), Error.TIMEOUT.getNo());
			ctx.writeAndFlush(response);
			TCPSessionManager.close(channel);
		}

		response.put(Key.RESULT.getName(), Result.OK.getName());
		if (allocation > 0) {
			response.put(Key.UDP_PORT.getName(), allocation);
		}
		ctx.writeAndFlush(response);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof String)) {
			return;
		}
		String command = (String) msg;
		Log.logger(Category.RECEIVE, "command:\n" + command);

		Channel channel = ctx.channel();
		SocketAddress address = channel.remoteAddress();

		//TODO:统计接收到的数据(有效即可,测试用,不验证登录)
		//Log.logger(Category.EVENT,  address + "---valid count:" + count.incrementAndGet());

		JSONObject json = JsonKit.map(command);
		Action action = Action.get(json.getString(Key.ACTION.getName()));

		//心跳
		if (action == Action.HEART_BEAT) {
			Log.logger(Category.EVENT, "网关[" + address + "] 发送心跳.");
			return;
		}

		//登录请求
		if (action == Action.LOGIN_REQ) {
			login(ctx, json);
			return;
		}

		//登录验证
		String result = JsonKit.getString(command, Key.RESULT.getName());
		String keyCode = JsonKit.getString(command, Key.KEYCODE.getName());
		if (Result.OK.getName().equals(result) && keyCode != null) {
			pass(ctx, keyCode);
		}
	}
}
