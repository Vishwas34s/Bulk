package com.Bulk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class BulkApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulkApplication.class, args);
	}

}
