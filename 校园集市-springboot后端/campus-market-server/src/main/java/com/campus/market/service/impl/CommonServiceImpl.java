package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.market.entity.Category;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.mapper.NoticeMapper;
import com.campus.market.mapper.SchoolLocationMapper;
import com.campus.market.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基础数据服务实现：分类 / 地点 / 公告（平台预置，只读）
 */
@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final CategoryMapper categoryMapper;
    private final SchoolLocationMapper schoolLocationMapper;
    private final NoticeMapper noticeMapper;

    @Override
    public List<Category> categoryList(Integer type) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        qw.eq("status", 1); // 只返回启用
        if (type != null) {
            qw.eq("type", type);
        }
        qw.orderByAsc("sort").orderByAsc("id");
        return categoryMapper.selectList(qw);
    }

    @Override
    public List<SchoolLocation> locationList() {
        return schoolLocationMapper.selectList(new QueryWrapper<SchoolLocation>()
                .eq("status", 1)
                .orderByAsc("sort")
                .orderByAsc("id"));
    }

    @Override
    public List<Notice> noticeList() {
        return noticeMapper.selectList(new QueryWrapper<Notice>()
                .eq("status", 1)
                .orderByDesc("create_time"));
    }
}
