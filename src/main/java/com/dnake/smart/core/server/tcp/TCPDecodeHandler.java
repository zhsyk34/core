package com.dnake.smart.core.server.tcp;

import com.dnake.smart.core.kit.ByteKit;
import com.dnake.smart.core.kit.DESKit;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.dnake.smart.core.config.PacketRule.*;
import static com.dnake.smart.core.kit.CodecKit.validateVerify;

/**
 * 解码接收到的数据
 */
class TCPDecodeHandler extends ByteToMessageDecoder {

	private static final Logger logger = LoggerFactory.getLogger(TCPDecodeHandler.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		//logger
		logger.info("--------------------------------------");
		logger.info("search data from index[" + in.readerIndex() + "] - [" + (in.readerIndex() + in.readableBytes()) + "]");
		logger.info("read data \n {}", ByteBufUtil.hexDump(in));

		//invalid
		if (in.readableBytes() < MSG_MIN_LENGTH) {
			logger.info("等待数据中...数据至少应有" + MSG_MIN_LENGTH + "位");
			return;
		}

		//header:1
		//TODO:起始位置并非readIndex
//		int index = in.bytesBefore(HEADER[0]);
		int index = in.indexOf(in.readerIndex(), in.readerIndex() + in.readableBytes() - 1, HEADER[0]);
		if (index == -1) {
			in.clear();
			logger.info("没有匹配到合法数据,清除已接收到的数据");
			return;
		}
		logger.info("匹配到第一个帧头位置:" + index);

		//length-fuzzy
		if (in.readableBytes() < MSG_MIN_LENGTH) {
			logger.info("数据不完整(依据数据长度粗略估计),继续等待中...");
			return;
		}

		//header:2
		in.readerIndex(index + 1);
		if (in.readByte() != HEADER[1]) {
			logger.info("第二个帧头数据不匹配,丢弃此前数据:");
			in.readerIndex(index + 1);
			return;
		}
		logger.info("匹配到第二个帧头位置:" + (index + 1));

		//length-exact
		int length = ByteKit.byteArrayToInt(new byte[]{in.readByte(), in.readByte()});
		//actual-length
		int actual = length - LENGTH_BYTES - VERIFY_BYTES;
		logger.info("校验长度:[" + length + "], 解析数据显示长度应为:[" + actual + "]");
		if (length < LENGTH_BYTES + DATA_MIN_BYTES + VERIFY_BYTES) {
			logger.info("长度校验数据校验错误,继续从下个位置:[" + (index + 1) + "]开始查找");
			in.readerIndex(index + 1);
			return;
		}

		if (in.readableBytes() < length) {
			logger.info("数据不完整(校验长度),继续等待中...");
			in.readerIndex(index);
			return;
		}

		//data
		byte[] data = new byte[actual];
		in.readBytes(actual).getBytes(0, data);

		//verify
		if (!validateVerify(data, new byte[]{in.readByte(), in.readByte()})) {
			logger.info("校验值错误,继续从下个位置:[" + (index + 1) + "]开始查找");
			in.readerIndex(index + 1);
			return;
		}

		//footer
		if (in.readByte() == FOOTER[0] && in.readByte() == FOOTER[1]) {
			logger.info("帧尾校验通过,获取数据:" + new String(DESKit.decrypt(data), StandardCharsets.UTF_8));
			out.add(in.slice(index, HEADER.length + length + FOOTER.length).retain());
			return;
		}

		logger.info("帧尾数据错误,继续从下个位置:[" + (index + 1) + "]开始查找");
		in.readerIndex(index + 1);
	}

}
