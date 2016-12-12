package com.dnake.smart.core.server;

import com.dnake.smart.core.server.udp.UDPServer;

public class UDPServerTest {
//	@Test
//	public void start() throws Exception {
////		new Thread(() -> UDPServer.start()).start();
//
////		for (int i = 0; i < 10; i++) {
////			Thread.sleep(50);
////			UDPServer.send(Config.LOCAL_HOST, 15678, "hello");
////		}
////		Thread.sleep(2000);
////		System.out.println("<<<<<<<<<<<<<<<<<<<<");
////		UDPServer.send(Config.LOCAL_HOST, 15678, "hello");
//	}

	public static void main(String[] args) {
		UDPServer.start();
	}

}