package com.campus.market.admin.service;

import com.campus.market.admin.dto.LocationSaveDTO;
import com.campus.market.admin.dto.NoticeSaveDTO;
import com.campus.market.common.PageResult;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;

import java.util.List;

/**
 * 管理端：基础数据维护（地点 / 公告，全量含停用下线）
 */
public interface AdminCommonService {

    /** 地点全量列表（含停用） */
    List<SchoolLocation> locationListAll();

    /** 保存地点：有 id 改、无 id 增（新增默认启用） */
    SchoolLocation saveLocation(LocationSaveDTO dto);

    /** 启用/停用切换 */
    void toggleLocation(Long id);

    /** 公告列表（全量含下线，分页） */
    PageResult<Notice> noticeList(int page, int size);

    /** 发布公告（status=1） */
    Notice publishNotice(NoticeSaveDTO dto);

    /** 下线公告 */
    void offlineNotice(Long id);
}
