package com.dnake.smart.core.kit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import static com.dnake.smart.core.config.PacketRule.*;
import static com.dnake.smart.core.kit.ByteKit.compare;
import static com.dnake.smart.core.kit.ByteKit.smallIntToByteArray;

public class CodecKit {

	private static final int MARK = 0xff;
	//数据部分以外(冗余数据)的长度
	private static final int REDUNDANT = HEADER.length + LENGTH_BYTES + VERIFY_BYTES + FOOTER.length;

	/*---------------------以下是编码部分---------------------*/
	/*---------------------编码时先加密command-data---------------------*/

	/**
	 * 长度编码
	 * 编码内容:数据部分长度+长度(2byte)+校验(2byte)
	 *
	 * @param data 数据部分
	 * @return 编码为small int(2byte)
	 */
	private static byte[] encodeLength(byte[] data) {
		return smallIntToByteArray(data.length + LENGTH_BYTES + VERIFY_BYTES);
	}

	/**
	 * 校验码编码原始结果:长度+数据部分逐位求和
	 *
	 * @param data 数据部分
	 * @return 编码后原始内容
	 */
	private static int verify(byte[] data) {
		if (data == null || data.length == 0) {
			throw new RuntimeException("data is isEmpty.");
		}
		int value = data.length + 4;//TODO
		for (byte b : data) {
			value += b & MARK;
		}
		return value;
	}

	/**
	 * 校验码编码:返回结算结果的低位(2byte)
	 */
	private static byte[] encodeVerify(byte[] data) {
		int value = verify(data);
		return smallIntToByteArray(value);
	}

	/**
	 * @param cmd 数据部分(原始的指令json)
	 * @return 编码结果
	 */
	public static byte[] encode(String cmd) {
		if (ValidateKit.isEmpty(cmd)) {
			throw new RuntimeException("command is null.");
		}
		byte[] data = DESKit.encrypt(cmd.getBytes(CharsetUtil.UTF_8));

		ByteBuf buffer = Unpooled.buffer(data.length + REDUNDANT);

		//header:2
		buffer.writeBytes(HEADER);
		//length:2
		buffer.writeBytes(encodeLength(data));
		//data
		buffer.writeBytes(data);
		//verify:2
		buffer.writeBytes(encodeVerify(data));
		//footer:2
		buffer.writeBytes(FOOTER);

		return buffer.array();
	}

	/*---------------------以下是解码部分---------------------*/

	/**
	 * @param data      数据部分
	 * @param verifyArr 检验码(2byte)
	 * @return 校验码是否合法
	 */
	public static boolean validateVerify(byte[] data, byte[] verifyArr) {
		return compare(encodeVerify(data), verifyArr);
	}

	public static String decode(byte[] bytes) {
		if (bytes == null || bytes.length < MSG_MIN_LENGTH) {
			return null;
		}
		return new String(bytes, HEADER.length + LENGTH_BYTES, bytes.length - REDUNDANT, CharsetUtil.UTF_8);
	}

	public static String decode(ByteBuf buf) {
		if (buf == null || buf.readableBytes() < MSG_MIN_LENGTH) {
			return null;
		}
		return buf.toString(HEADER.length + LENGTH_BYTES, buf.readableBytes() - REDUNDANT, CharsetUtil.UTF_8);
	}

}
