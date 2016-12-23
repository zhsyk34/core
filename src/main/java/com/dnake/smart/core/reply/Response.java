package com.dnake.smart.core.reply;

import lombok.Getter;

@Getter
class Response {
	private final String dest;//sn
	private final String command;

	private Response(String dest, String command) {
		this.dest = dest;
		this.command = command;
	}

	static Response of(String dest, String command) {
		return new Response(dest, command);
	}
}
