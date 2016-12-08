package com.dnake.smart.core.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Result {
	OK("ok", "正确响应"),
	NO("no", "错误响应");

	private String name;
	private String description;
}
