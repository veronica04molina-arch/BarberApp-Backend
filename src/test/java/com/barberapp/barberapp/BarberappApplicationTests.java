package com.barberapp.barberapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.scheduling.enabled=false")
class BarberappApplicationTests {

	@Test
	void contextLoads() {
	}

}
