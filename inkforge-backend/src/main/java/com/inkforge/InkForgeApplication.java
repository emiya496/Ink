package com.inkforge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.inkforge.mapper")
public class InkForgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(InkForgeApplication.class, args);
    }
}
