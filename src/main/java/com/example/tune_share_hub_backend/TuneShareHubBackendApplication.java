package com.example.tune_share_hub_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.example.tune_share_hub_backend.dao")
public class TuneShareHubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TuneShareHubBackendApplication.class, args);
	}

}
