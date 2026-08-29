package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 闲置商品表 Mapper
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /** 浏览数 +1（详情接口调用，服务端累计 view_count） */
    @Update("UPDATE goods SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementView(@Param("id") Long id);

    /** 收藏数增减（GREATEST 保护不出现负数） */
    @Update("UPDATE goods SET fav_count = GREATEST(0, fav_count + #{delta}) WHERE id = #{id}")
    int changeFavCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 下单锁定商品：CAS 把状态从 1在售 改为 2已售出。
     * 同一商品同一时刻只允许一条进行中订单（文档 3.5 约束），
     * rows=0 说明已被别人抢先下单，与"先查后改"相比无并发窗口
     */
    @Update("UPDATE goods SET status = 2, update_time = NOW() WHERE id = #{id} AND status = 1 AND is_deleted = 0")
    int lockForOrder(@Param("id") Long id);

    /** 订单取消后恢复在售（仅当商品仍处于锁定状态） */
    @Update("UPDATE goods SET status = 1, update_time = NOW() WHERE id = #{id} AND status = 2")
    int restoreOnSale(@Param("id") Long id);

    /** 管理端看板：某时刻之后新增商品数 */
    @Select("SELECT COUNT(*) FROM goods WHERE create_time >= #{start} AND is_deleted = 0")
    long countNewSince(@Param("start") LocalDateTime start);

    /** 管理端看板：按天分组统计新增商品（返回 [{d, c}]，缺天由上层补 0） */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS c FROM goods " +
            "WHERE create_time >= #{start} AND is_deleted = 0 GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDaySince(@Param("start") LocalDateTime start);

    /** 管理端看板：在售商品按分类计数（返回 [{name, value}]） */
    @Select("SELECT c.name AS name, COUNT(*) AS value FROM goods g " +
            "JOIN category c ON g.category_id = c.id " +
            "WHERE g.status = 1 AND g.is_deleted = 0 GROUP BY c.id, c.name ORDER BY value DESC")
    List<Map<String, Object>> countOnSaleByCategory();
}
