package com.dnake.smart.core.config;

/**
 * 包数据格式
 */

public class PacketRule {
	//header
	public static final byte[] HEADER = new byte[]{0x5A, (byte) 0xA5};
	//footer
	public static final byte[] FOOTER = new byte[]{(byte) 0xA5, 0x5A};
	//length
	public static int LENGTH_BYTES = 2;
	//data
	public static int DATA_MIN_BYTES = 1;
	//verify
	public static int VERIFY_BYTES = 2;
	//total
	public static int MSG_MIN_LENGTH = HEADER.length + LENGTH_BYTES + DATA_MIN_BYTES + VERIFY_BYTES + FOOTER.length;
}
