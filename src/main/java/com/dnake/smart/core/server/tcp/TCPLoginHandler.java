package com.dnake.smart.core.server.tcp;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.*;
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
 * 处理登录或心跳(等不需要转包的数据)
 */
public class TCPLoginHandler extends ChannelInboundHandlerAdapter {

	//测试统计用
	//private static final AtomicInteger count = new AtomicInteger();

	//TODO
	private void heart(ChannelHandlerContext ctx, JSONObject json) {
	}

	private boolean login(ChannelHandlerContext ctx, JSONObject json) {
		JSONObject response = new JSONObject();
		Channel channel = ctx.channel();

		//1.处理登录请求
		Device device = Device.get(json.getIntValue(CommandKey.TYPE.getName()));
		String sn = json.getString(CommandKey.SN.getName());
		int port = json.getIntValue(CommandKey.UDP_PORT.getName());
		//1-1.错误的登录请求
		if (device == null || (device == Device.GATEWAY && (ValidateKit.isEmpty(sn) || port < 1))) {
			Log.logger(Category.EVENT, "错误的登录请求");
			response.put(CommandKey.RESULT.getName(), Result.NO.getName());
			response.put(CommandKey.ERRNO.getName(), ErrorCode.UNKNOWN.getNo());
			ctx.writeAndFlush(response);
			return false;
		}

		//1-2.处理登录请求
		//将登录信息缓存在channel上,待通过验证后再统一管理;同时可实现sn与channel的双向绑定
		Log.logger(Category.EVENT, "正在处理登录请求");

		channel.attr(SessionAttributeKey.TYPE).set(device);//{1}
		if (device == Device.GATEWAY) {
			channel.attr(SessionAttributeKey.SN).set(sn);//{2}
			channel.attr(SessionAttributeKey.UDP_PORT).set(port);//{3}
		}

		//2.发送本次登录的验证信息,缓存登录验证码
		int group = RandomKit.randomInteger(0, 49);
		int offset = RandomKit.randomInteger(0, 9);

		response.put(CommandKey.ACTION.getName(), Action.LOGIN_VERIFY.getName());
		//2-1.验证信息
		response.put(CommandKey.KEY.getName(), CodecKit.loginKey(group, offset));

		//2-2.验证码
		channel.attr(SessionAttributeKey.KEYCODE).set(CodecKit.loginVerify(group, offset));//{4}
		ctx.writeAndFlush(response);
		return true;
	}

	//登录验证
	private void verify(ChannelHandlerContext ctx, String keyCode) {
		JSONObject response = new JSONObject();
		Channel channel = ctx.channel();

		Device device = channel.attr(SessionAttributeKey.TYPE).get();
		String verify = channel.attr(SessionAttributeKey.KEYCODE).get();
		String sn = channel.attr(SessionAttributeKey.SN).get();
		Integer apply = channel.attr(SessionAttributeKey.UDP_PORT).get();//请求端口

		Log.logger(Category.EVENT, "客户端[" + channel.remoteAddress() + "] 进行登录验证[" + keyCode + "],正确的验证码为[" + verify + "]");

		//非法登录
		if (ValidateKit.isEmpty(verify) || device == null || (device == Device.GATEWAY && (ValidateKit.isEmpty(sn) || apply == null || apply < 0))) {
			Log.logger(Category.EVENT, "非法登录");
			return;
		}

		//验证码错误
		if (!verify.equals(keyCode)) {
			Log.logger(Category.EVENT, "登录验证码错误");
			response.put(CommandKey.RESULT.getName(), Result.NO.getName());
			response.put(CommandKey.ERRNO.getName(), ErrorCode.UNKNOWN.getNo());
			ctx.writeAndFlush(response);
			TCPSessionManager.close(channel);
			return;
		}

		Log.logger(Category.EVENT, "验证码通过");
		switch (device) {
			case APP:
				Log.logger(Category.EVENT, "app尝试登录");
				break;
			case GATEWAY:
				Log.logger(Category.EVENT, "网关尝试登录");
				int port = TCPSessionManager.allocate(sn, apply);
				response.put(CommandKey.UDP_PORT.getName(), port);
				break;
			default:
				Log.logger(Category.EVENT, "未知设备类型");
				break;
		}

		//可能存在登录超时
		boolean pass = TCPSessionManager.pass(channel);
		if (pass) {
			response.put(CommandKey.RESULT.getName(), Result.OK.getName());
		} else {
			response.put(CommandKey.RESULT.getName(), Result.NO.getName());
			response.put(CommandKey.ERRNO.getName(), ErrorCode.TIMEOUT.getNo());
		}
		ctx.writeAndFlush(response);

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof String)) {
			return;
		}
		String command = (String) msg;
		Log.logger(Category.EVENT, "command:\n" + command);

		Channel channel = ctx.channel();
		SocketAddress address = channel.remoteAddress();

		//TODO:统计接收到的数据(有效即可,测试用,不验证登录)
		//Log.logger(Category.EVENT,  address + "---valid count:" + count.incrementAndGet());

		JSONObject json = JsonKit.map(command);
		Action action = Action.get(json.getString(CommandKey.ACTION.getName()));

		//心跳
		if (action == Action.HEART_BEAT) {
			Log.logger(Category.EVENT, "网关[" + address + "] 发送心跳.");
			heart(ctx, json);
			return;
		}

		//登录请求
		if (action == Action.LOGIN_REQ) {
			Log.logger(Category.EVENT, "客户端[" + address + "] 请求登录.");
			login(ctx, json);
			return;
		}

		//登录验证
		String result = JsonKit.getString(command, CommandKey.RESULT.getName());
		String keyCode = JsonKit.getString(command, CommandKey.KEYCODE.getName());
		if (Result.OK.getName().equals(result) && keyCode != null) {
			verify(ctx, keyCode);
		}

	}
}
