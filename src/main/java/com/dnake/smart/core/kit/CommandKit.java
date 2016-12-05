package com.dnake.smart.core.kit;

import com.alibaba.fastjson.JSONObject;

public class CommandKit {

	public static String correct() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "ok");
		return jsonObject.toString();
	}

	public static String wrong() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "no");
		return jsonObject.toString();
	}

	public static void main(String[] args) {
		correct();
	}
}
