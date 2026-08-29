package com.campus.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 跑腿单状态枚举（对应《跑腿状态机与接口设计》3.1，严格一致）
 *
 * 设计要点：用一张显式的流转表 TRANSITIONS 而不是散落在各处的 if-else，
 * 新增状态时只改这一个地方，杜绝"野流转"。
 *
 * 0待接单 1已接单 2配送中 3送达待确认 4已完成(终止) 5已取消(终止) 6申诉中
 */
@Getter
@AllArgsConstructor
public enum ErrandStatusEnum {

    PENDING(0, "待接单"),
    ACCEPTED(1, "已接单"),
    DELIVERING(2, "配送中"),
    ARRIVED(3, "送达待确认"),
    FINISHED(4, "已完成"),
    CANCELLED(5, "已取消"),
    DISPUTED(6, "申诉中");

    private final int code;
    private final String desc;

    /** 合法流转表：key=当前状态，value=允许到达的状态集合 */
    private static final Map<ErrandStatusEnum, Set<ErrandStatusEnum>> TRANSITIONS = Map.of(
            PENDING, EnumSet.of(ACCEPTED, CANCELLED),
            ACCEPTED, EnumSet.of(DELIVERING, CANCELLED),
            DELIVERING, EnumSet.of(ARRIVED),
            ARRIVED, EnumSet.of(FINISHED, DISPUTED),
            DISPUTED, EnumSet.of(FINISHED, CANCELLED),
            FINISHED, EnumSet.noneOf(ErrandStatusEnum.class),
            CANCELLED, EnumSet.noneOf(ErrandStatusEnum.class)
    );

    /** 校验当前状态能否流转到目标状态（配送中之后不允许单方面取消，由本表保证） */
    public boolean canTransitTo(ErrandStatusEnum target) {
        return TRANSITIONS.get(this).contains(target);
    }

    /** 由数据库状态码取枚举 */
    public static ErrandStatusEnum of(int code) {
        for (ErrandStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知的跑腿单状态码：" + code);
    }
}
