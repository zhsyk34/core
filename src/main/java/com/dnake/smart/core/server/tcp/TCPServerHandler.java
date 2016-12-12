package com.dnake.smart.core.server.tcp;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.dict.Action;
import com.dnake.smart.core.dict.Key;
import com.dnake.smart.core.kit.JsonKit;
import com.dnake.smart.core.kit.ValidateKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import com.dnake.smart.core.reply.CommandManager;
import com.dnake.smart.core.reply.Message;
import com.dnake.smart.core.session.tcp.TCPSessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

class TCPServerHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof String)) {
			return;
		}
		String command = (String) msg;
		Channel channel = ctx.channel();

		JSONObject json = JsonKit.map(command);
		Action action = Action.get(json.getString(Key.ACTION.getName()));

		if (action == null) {
			Log.logger(Category.EXCEPTION, "无效的指令:\n" + command);
			return;
		}

		if (action.getType() == 4) {
			//TODO
			Log.logger(Category.EVENT, "推送数据,直接保存到数据库");
			return;
		}

		String src = TCPSessionManager.id(channel);
		String sn = TCPSessionManager.sn(channel);
		if (ValidateKit.isEmpty(sn)) {
			Log.logger(Category.EXCEPTION, "非法的连接");
			return;
		}
		CommandManager.add(Message.init(src, sn, command));
	}
}
