package com.campus.market.service;

import com.campus.market.entity.Category;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;

import java.util.List;

/**
 * 基础数据服务：分类 / 地点 / 公告（平台预置，只读）
 */
public interface CommonService {

    /** 分类列表：type=1 闲置 / 2 跑腿，不传返回全部启用分类 */
    List<Category> categoryList(Integer type);

    /** 校内地点库（预置数据，只读） */
    List<SchoolLocation> locationList();

    /** 系统公告（status=1） */
    List<Notice> noticeList();
}
