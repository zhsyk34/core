package com.dnake.smart.core.server.tcp;

import com.dnake.smart.core.config.Config;
import com.dnake.smart.core.kit.ByteKit;
import com.dnake.smart.core.kit.DESKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

import static com.dnake.smart.core.dict.Packet.*;
import static com.dnake.smart.core.kit.CodecKit.validateVerify;

/**
 * 解码TCP服务器接收到的数据
 * 业务处理场景基于一问一答方式:每次解码默认只有一个包,具体解码时简单处理了粘包的问题
 * <p>
 * 当包头数据非法时直接丢弃数据否则等待正确的包尾数据直至缓冲区大于指定值
 * 当包头包尾均正确时开始解析:根据长度定位到帧尾,如果数据正确且帧尾缓冲buffer则可能存在粘包==>截取解析后的数据后递归调用该方法继续解码剩余部分
 * <p>
 * 该解析方法不能正确处理半包情况(基于业务情境忽略此种情况)
 */
final class TCPDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Log.logger(Category.RECEIVE, "-----------------开始解析-----------------");

		final int size = in.readableBytes();

		//length
		if (size < MSG_MIN_LENGTH) {
			Log.logger(Category.RECEIVE, "等待数据中...数据至少应有[" + MSG_MIN_LENGTH + "]位");
			return;
		}
		if (size > Config.TCP_MAX_BUFFER_SIZE) {
			Log.logger(Category.RECEIVE, "缓冲数据已达[" + size + "]位,超过最大限制[" + Config.TCP_MAX_BUFFER_SIZE + "],丢弃本次数据");
			in.clear();
			return;
		}

		in.markReaderIndex();

		//header
		if (in.readByte() != HEADERS.get(0) || in.readByte() != HEADERS.get(1)) {
			in.clear();
			Log.logger(Category.RECEIVE, "包头非法,丢弃本次数据");
			return;
		}

		//direct to check footer
		if (in.getByte(size - 2) != FOOTERS.get(0) || in.getByte(size - 1) != FOOTERS.get(1)) {
			in.resetReaderIndex();
			Log.logger(Category.RECEIVE, "包尾不正确,尝试继续等待");
			return;
		}

		//length
		int length = ByteKit.byteArrayToInt(new byte[]{in.readByte(), in.readByte()});
		int actual = length - LENGTH - VERIFY;
		//Log.logger(Category.RECEIVE, "校验长度:[" + length + "], 指令长度应为:[" + actual + "]");
		if (actual < MIN_DATA || actual > size - REDUNDANT) {
			in.clear();
			Log.logger(Category.RECEIVE, "长度校验数据校验错误,丢弃本次数据");
			return;
		}

		//skip data and verify to check footer again
		in.markReaderIndex();
		in.skipBytes(actual + VERIFY);

		if (in.readByte() != FOOTERS.get(0) || in.readByte() != FOOTERS.get(1)) {
			in.clear();
			Log.logger(Category.RECEIVE, "包尾错误,丢弃本次数据");
			return;
		}

		//TODO:此处可改进为先校验verify
		//read data
		in.resetReaderIndex();

		byte[] data = new byte[actual];
		ByteBuf dataBuf = in.readBytes(actual);
		dataBuf.getBytes(0, data).release();

		//verify
		if (!validateVerify(data, new byte[]{in.readByte(), in.readByte()})) {
			in.clear();
			Log.logger(Category.RECEIVE, "校验值错误,丢弃本次数据");
			return;
		}

		//skip footer
		in.skipBytes(FOOTERS.size());

		String command = new String(DESKit.decrypt(data), CharsetUtil.UTF_8);
		out.add(command);

		//recursion
		if (in.readableBytes() > 0) {
			Log.logger(Category.RECEIVE, "解析剩余部分");
			decode(ctx, in, out);
		}
	}

}
