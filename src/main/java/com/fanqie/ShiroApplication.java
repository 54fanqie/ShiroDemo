package com.fanqie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


/**
 * @description: ShiroApplication
 * @date: 2022/5/9 15:46
 * @author: fanqie
 */
@SpringBootApplication
@ServletComponentScan
@MapperScan(basePackages = {"com.fanqie.core.dao"}) //扫描DAO
public class ShiroApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShiroApplication.class, args);
    }
}
