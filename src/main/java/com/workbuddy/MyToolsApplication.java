package com.workbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * my tools 个人工作台 启动类
 * 约束：JDK 1.8 / Spring Boot 2.7.18 / javax.* (非 jakarta)
 */
@SpringBootApplication
public class MyToolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyToolsApplication.class, args);
    }
}
