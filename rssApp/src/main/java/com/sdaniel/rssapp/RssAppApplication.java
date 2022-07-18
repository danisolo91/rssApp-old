package com.sdaniel.rssapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class RssAppApplication {
		
	public static void main(String[] args) {
		SpringApplication.run(RssAppApplication.class, args);
	}
}
