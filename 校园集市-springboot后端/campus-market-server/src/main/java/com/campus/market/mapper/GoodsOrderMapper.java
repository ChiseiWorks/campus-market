package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.GoodsOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 闲置交易订单表 Mapper
 */
public interface GoodsOrderMapper extends BaseMapper<GoodsOrder> {

    /** 管理端看板：某时刻之后新增订单数 */
    @Select("SELECT COUNT(*) FROM goods_order WHERE create_time >= #{start} AND is_deleted = 0")
    long countNewSince(@Param("start") LocalDateTime start);

    /** 管理端看板：按天分组统计新增订单（返回 [{d, c}]，缺天由上层补 0） */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS c FROM goods_order " +
            "WHERE create_time >= #{start} AND is_deleted = 0 GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDaySince(@Param("start") LocalDateTime start);

    /** 管理端看板：时间段内完成订单的成交额（金额快照 deal_price，无数据返回 0） */
    @Select("SELECT IFNULL(SUM(deal_price), 0) FROM goods_order " +
            "WHERE status = 2 AND finish_time >= #{start} AND finish_time < #{end} AND is_deleted = 0")
    BigDecimal sumFinishedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
