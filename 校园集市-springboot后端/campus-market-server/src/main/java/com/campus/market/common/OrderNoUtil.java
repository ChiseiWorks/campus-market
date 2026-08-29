package com.campus.market.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单号生成工具：日期(14) + 用户id后4位 + 随机4位，共 22 位
 * 对应数据库设计文档：order_no 规则 = 日期+用户id+随机数
 */
public class OrderNoUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtil() {
    }

    public static String generate(Long userId) {
        String time = LocalDateTime.now().format(FMT);
        long uidPart = userId == null ? 0 : userId % 10000;
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return time + String.format("%04d", uidPart) + rand;
    }
}
