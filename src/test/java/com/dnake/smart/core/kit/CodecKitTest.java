package com.dnake.smart.core.kit;

import org.junit.Test;

public class CodecKitTest {
	@Test
	public void encode() throws Exception {

		byte[] bs = CodecKit.encode("a");
		//5A A5 00 05 61 00 62 A5 5A
		System.out.println(ByteKit.bytesToHex(bs));
		//5A A5 00 05 61 00 62 A5 5A
	}

}