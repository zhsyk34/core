package com.dnake.smart.core.database;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PortDaoTest {
	@Test
	public void find() throws Exception {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl("123");

	}

	@Test
	public void save() throws Exception {

	}

	@Test
	public void batchSave() throws Exception {
		List<UDPRecord> list = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			int ip = new Random().nextInt(255);
			int sn = new Random().nextInt(10);
			int port = 50000 + new Random().nextInt(1000);
			UDPRecord record = new UDPRecord("192.168.14." + ip, "2-1-1-" + sn, port, System.currentTimeMillis());
			list.add(record);
		}
		PortDao.save(list, 20);
	}

}