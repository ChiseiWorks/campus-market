package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户表 Mapper
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 信用分变更（对应设计文档 3.6）：
     * GREATEST(0, LEAST(100, ...)) 上下限保护，只允许通过 CreditService 走流水调用，
     * 禁止业务代码直接 UPDATE credit_score，保证信用分可追溯
     */
    @Update("UPDATE user SET credit_score = GREATEST(0, LEAST(100, credit_score + #{delta})), " +
            "update_time = NOW() WHERE id = #{userId}")
    int changeCredit(@Param("userId") Long userId, @Param("delta") int delta);

    /** 管理端看板：某时刻之后新增用户数 */
    @Select("SELECT COUNT(*) FROM user WHERE create_time >= #{start}")
    long countNewSince(@Param("start") LocalDateTime start);

    /** 管理端看板：按天分组统计新增用户（返回 [{d, c}]，缺天由上层补 0） */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS c FROM user " +
            "WHERE create_time >= #{start} GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDaySince(@Param("start") LocalDateTime start);
}
