package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.ErrandOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 跑腿订单表 Mapper（核心表）
 * 并发设计核心考点：所有抢单/状态推进都是 UPDATE ... WHERE status=旧状态 的原子 CAS，
 * 禁止"先 SELECT 判断再 UPDATE"——两条语句之间的时间窗口会导致一单两人接。
 */
public interface ErrandOrderMapper extends BaseMapper<ErrandOrder> {

    /**
     * 状态推进乐观锁更新（设计文档 3.2）：
     * WHERE status=旧状态 AND version=旧版本，防并发脏写；
     * rows=0 说明已被并发请求改过，上层抛"手慢了"
     */
    @Update("UPDATE errand_order SET status = #{target}, version = version + 1, " +
            "update_time = NOW() WHERE id = #{id} AND status = #{current} AND version = #{version}")
    int updateStatusWithLock(@Param("id") Long id, @Param("current") int current,
                             @Param("target") int target, @Param("version") int version);

    /**
     * 原子抢单（设计文档 3.5）：只有 status 仍是 0(待接单) 才能更新成功，
     * MySQL 行锁保证并发下只有一个赢家
     */
    @Update("UPDATE errand_order SET runner_id = #{runnerId}, status = 1, accept_time = NOW(), " +
            "version = version + 1 WHERE id = #{id} AND status = 0")
    int acceptOrder(@Param("id") Long id, @Param("runnerId") Long runnerId);

    /** 统计跑男进行中的单数（已接单/配送中/送达待确认），用于防囤单（最多 3 单） */
    @Select("SELECT COUNT(*) FROM errand_order WHERE runner_id = #{runnerId} " +
            "AND status IN (1, 2, 3) AND is_deleted = 0")
    int countDoingByRunner(@Param("runnerId") Long runnerId);

    /** 管理端看板：某时刻之后新增跑腿单数 */
    @Select("SELECT COUNT(*) FROM errand_order WHERE create_time >= #{start} AND is_deleted = 0")
    long countNewSince(@Param("start") LocalDateTime start);

    /** 管理端看板：按天分组统计新增跑腿单（返回 [{d, c}]，缺天由上层补 0） */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS c FROM errand_order " +
            "WHERE create_time >= #{start} AND is_deleted = 0 GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDaySince(@Param("start") LocalDateTime start);

    /** 管理端看板：时间段内完成单的悬赏总额（无数据返回 0） */
    @Select("SELECT IFNULL(SUM(reward), 0) FROM errand_order " +
            "WHERE status = 4 AND finish_time >= #{start} AND finish_time < #{end} AND is_deleted = 0")
    BigDecimal sumFinishedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 管理端看板：按小时分布统计跑腿单（返回 [{hour, count}]，缺时由上层补 0） */
    @Select("SELECT HOUR(create_time) AS hour, COUNT(*) AS `count` FROM errand_order " +
            "WHERE is_deleted = 0 GROUP BY HOUR(create_time)")
    List<Map<String, Object>> countByHour();
}
