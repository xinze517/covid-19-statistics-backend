package com.dgut.covid19statistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dgut.covid19statistics.mapper")
@EnableAsync
@EnableScheduling
@EnableCaching
public class Covid19StatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Covid19StatisticsApplication.class, args);
    }

}
