package com.unihack.unihack;

import org.springframework.boot.SpringApplication;

public class TestUnihackApplication {

	public static void main(String[] args) {
		SpringApplication.from(UnihackApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
