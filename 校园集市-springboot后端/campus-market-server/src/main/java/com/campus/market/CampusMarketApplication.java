package com.campus.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园集市后端启动类
 * 校内闲置交易 + 跑腿互助平台
 */
@MapperScan("com.campus.market.mapper")
@SpringBootApplication
public class CampusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
    }
}
