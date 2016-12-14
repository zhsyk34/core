package com.dnake.smart.core.server.tcp;

import com.dnake.smart.core.kit.ByteKit;
import com.dnake.smart.core.kit.DESKit;
import com.dnake.smart.core.log.Category;
import com.dnake.smart.core.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

import static com.dnake.smart.core.dict.Packet.*;
import static com.dnake.smart.core.kit.CodecKit.validateVerify;

/**
 * TODO:数据校验长度如果过大将导致等待...
 * 解码TCP服务器接收到的数据
 */
class TCPDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//logger
		Log.logger(Category.RECEIVE, "--------------------------------------");
		Log.logger(Category.RECEIVE, "search data from index[" + in.readerIndex() + "] - [" + (in.readerIndex() + in.readableBytes()) + "]");
		Log.logger(Category.RECEIVE, "read data \n" + ByteBufUtil.hexDump(in));

		//invalid
		if (in.readableBytes() < MSG_MIN_LENGTH) {
			Log.logger(Category.RECEIVE, "等待数据中...数据至少应有[" + MSG_MIN_LENGTH + "]位");
			return;
		}

		//header:1
		//TODO:起始位置并非readIndex
		int index = in.indexOf(in.readerIndex(), in.readerIndex() + in.readableBytes() - 1, HEADER[0]);
		if (index == -1) {
			in.clear();
			Log.logger(Category.RECEIVE, "没有匹配到合法数据,清除已接收到的数据");
			return;
		}
		Log.logger(Category.RECEIVE, "匹配到第一个帧头位置[" + index + "]");

		//length-fuzzy
		if (in.readableBytes() < MSG_MIN_LENGTH) {
			Log.logger(Category.RECEIVE, "数据不完整(依据数据长度粗略估计),继续等待中...");
			return;
		}

		//header:2
		in.readerIndex(index + 1);
		if (in.readByte() != HEADER[1]) {
			Log.logger(Category.RECEIVE, "第二个帧头数据不匹配,丢弃此前数据.");
			in.readerIndex(index + 1);
			return;
		}
		Log.logger(Category.RECEIVE, "匹配到第二个帧头位置[" + (index + 1) + "]");

		//length-exact
		int length = ByteKit.byteArrayToInt(new byte[]{in.readByte(), in.readByte()});
		//actual-length
		int actual = length - LENGTH_BYTES - VERIFY_BYTES;
		Log.logger(Category.RECEIVE, "校验长度:[" + length + "], 解析数据显示长度应为:[" + actual + "]");
		if (length < LENGTH_BYTES + DATA_MIN_BYTES + VERIFY_BYTES) {
			Log.logger(Category.RECEIVE, "长度校验数据校验错误,继续从下个位置:[" + (index + 1) + "]开始查找");
			in.readerIndex(index + 1);
			return;
		}

		if (in.readableBytes() < length) {
			Log.logger(Category.RECEIVE, "数据不完整(校验长度),继续等待中...");
			in.readerIndex(index);
			return;
		}

		//data
		byte[] data = new byte[actual];
		ByteBuf dataBuf = in.readBytes(actual);
		dataBuf.getBytes(0, data).release();

		//verifyKey
		if (!validateVerify(data, new byte[]{in.readByte(), in.readByte()})) {
			Log.logger(Category.RECEIVE, "校验值错误,继续从下个位置:[" + (index + 1) + "]开始查找");
			in.readerIndex(index + 1);
			return;
		}

		//footer
		if (in.readByte() == FOOTER[0] && in.readByte() == FOOTER[1]) {
			String command = new String(DESKit.decrypt(data), CharsetUtil.UTF_8);
			Log.logger(Category.RECEIVE, "帧尾校验通过,获取数据:\n" + command);
			out.add(command);
			return;
		}

		Log.logger(Category.RECEIVE, "帧尾数据错误,继续从下个位置:[" + (index + 1) + "]开始查找");
		in.readerIndex(index + 1);
	}

}
