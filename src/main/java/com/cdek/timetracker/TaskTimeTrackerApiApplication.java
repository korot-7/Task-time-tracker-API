package com.cdek.timetracker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.cdek.timetracker.mapper")
public class TaskTimeTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTimeTrackerApiApplication.class, args);
	}

}
