package com.sst.utopia.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:database-config.properties")
@SpringBootApplication
public class UtopiaBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtopiaBookingApplication.class, args);
	}

}
