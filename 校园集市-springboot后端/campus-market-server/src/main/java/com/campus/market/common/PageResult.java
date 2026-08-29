package com.campus.market.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 统一分页返回结构：{ list: [...], total: n }
 * 前端所有列表页按 res.list / res.total 取值
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> list;

    private Long total;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }

    /** 由 MyBatis-Plus 分页对象直接转换 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }
}
