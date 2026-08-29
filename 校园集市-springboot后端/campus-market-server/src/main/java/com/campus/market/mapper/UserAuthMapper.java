package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.entity.UserAuth;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 校园认证表 Mapper
 */
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    /**
     * 管理端审核列表：联查 user 表附 nickname/phone，按提交时间倒序分页。
     * status 为 null 查全部（0待审核 1通过 2驳回）
     */
    @Select("<script>" +
            "SELECT a.*, u.nickname, u.phone FROM user_auth a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "<where>" +
            "<if test='status != null'>a.audit_status = #{status}</if>" +
            "</where>" +
            "ORDER BY a.create_time DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<UserAuth> selectPageWithUser(@Param("status") Integer status,
                                      @Param("offset") long offset,
                                      @Param("size") int size);

    /** 管理端审核列表总数（status 为 null 查全部） */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_auth " +
            "<where>" +
            "<if test='status != null'>audit_status = #{status}</if>" +
            "</where>" +
            "</script>")
    long countWithStatus(@Param("status") Integer status);
}
