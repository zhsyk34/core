package com.dnake.smart.core.server;

import com.dnake.smart.core.server.tcp.TCPServer;
import com.dnake.smart.core.session.TCPSessionManager;
import org.junit.Test;

public class TCPServerTest {
	@Test
	public void start() throws Exception {
		TCPServer.start();
		TCPSessionManager.monitor();
	}

}