package com.example.myspringbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class MySpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBatchApplication.class, args);
    }

}
