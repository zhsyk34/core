package com.dnake.smart.core.test;

import com.alibaba.fastjson.JSONObject;
import com.dnake.smart.core.kit.CodecKit;

public class Command {

	/**
	 * {
	 * "action":"loginReq",  #登录请求
	 * "clientType":0,  #取值范围：0-智能网关，1-手机应用程序
	 * "devSn":"xxxxxx", #网关 SN 码，仅适用于智能网关登录云服务器
	 * "UDPPort":1234 #网关 UDP 端口，仅适用于智能网关登录云服务器
	 * }
	 */
	public static String login() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("action", "loginReq");
		jsonObject.put("clientType", 0);
		jsonObject.put("devSn", "2-1-1-100");
		jsonObject.put("UDPPort", 50000);

		return jsonObject.toString();
	}

	/**
	 * 90-91084-67-87-9914-9873-116-35-7254291226412064-9-105-1193329-38-5293-4421358343-83-124-34255114-4645-54-20-115-68-51103-4395-122118-13-1191012105-12428239711959-3116-1137072-78-721058-1979-5112613-24-7843-5080-28-111-105-3239121-91905AA50054BDA99D0E9E498CDDB8361D7A407840F79789211DDACC5DD41523532BAD84DE190572D22DCAEC8DBCCD67D55F8676F3890A0C69841C1761773BFD748F4648B2B80A3AED4FCD7E0DE8B22BCE50E49197E02779A55A
	 * 88
	 *
	 * @param args
	 */

	public static void main(String[] args) {
		String cmd = "{\"action\":\"loginReq\",\"clientType\":0,\"devSN\":\"2-1-1-100\",\"UDPPort\":50000}";
//		byte[] bs = DESKit.encrypt(cmd.getBytes());
		byte[] bs = CodecKit.encode(cmd);
		for (byte b : bs) {
			System.out.print(b + " ");
		}

//		System.out.println(ByteKit.bytesToHex(encode));
	}
}
