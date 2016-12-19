package com.dnake.smart.core.dict;

/**
 * TODO:mutable array fix
 * TCP包数据格式
 */
public class Packet {
	//header
	public static final byte[] HEADER = new byte[]{0x5A, (byte) 0xA5};
	//footer
	public static final byte[] FOOTER = new byte[]{(byte) 0xA5, 0x5A};
	//length
	public static final int LENGTH_BYTES = 2;
	//data
	public static final int DATA_MIN_BYTES = 1;
	//verifyKey
	public static final int VERIFY_BYTES = 2;
	//数据部分以外(冗余数据)的长度
	public static final int REDUNDANT = HEADER.length + LENGTH_BYTES + VERIFY_BYTES + FOOTER.length;
	//total
	public static final int MSG_MIN_LENGTH = REDUNDANT + DATA_MIN_BYTES;
}
