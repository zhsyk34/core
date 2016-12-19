package com.dnake.smart.core.server.tcp;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.Action;
import com.dnake.smart.core.dict.Device;
import com.dnake.smart.core.dict.Key;
import com.dnake.smart.core.dict.Result;
import com.dnake.smart.core.kit.JsonKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.reply.Message;
import com.dnake.smart.core.reply.MessageManager;
import com.dnake.smart.core.session.tcp.TCPSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 处理除登录以外的相关指令
 * 1.推送指令:保存相关数据至数据库
 * 2.控制指令交由
 *
 * @see MessageManager 统一管理
 * 3.登录验证已在
 * @see TCPLoginHandler 中处理
 */
class TCPServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof String)) {
			return;
		}
		String command = (String) msg;

		JSONObject json = JsonKit.map(command);
		Action action = Action.get(json.getString(Key.ACTION.getName()));
		String result = json.getString(Key.RESULT.getName());

		if (action == null && result == null) {
			Log.logger(Category.EXCEPTION, "无效的指令:\n" + command);
			return;
		}

		Channel channel = ctx.channel();
		String sn = TCPSessionManager.sn(channel);
		Device device = TCPSessionManager.type(channel);

		switch (device) {
			case APP:
				Log.logger(Category.EVENT, "客户端请求,将其添加到消息处理队列...");
				MessageManager.request(sn, Message.of(TCPSessionManager.id(channel), command));
				break;
			case GATEWAY:
				System.err.println("网关接收到数据:" + command);

				//心跳
				if (action == Action.HEART_BEAT) {
					Log.logger(Category.EVENT, "网关[" + channel.remoteAddress() + "] 发送心跳");
					JSONObject heartResp = new JSONObject();
					heartResp.put(Key.RESULT.getName(), Result.OK.getName());
					channel.writeAndFlush(heartResp);
					return;
				}

				//推送
				if (action != null && action.getType() == 4) {
					Log.logger(Category.EVENT, "推送数据,直接保存到数据库");
					MessageManager.save(sn, command);
					return;
				}

				//响应请求
				if (result != null) {
					Log.logger(Category.EVENT, "处理指令应答,转发至APP");
					MessageManager.response(sn, command);
				}
				break;
			default:
				break;
		}
	}
}
