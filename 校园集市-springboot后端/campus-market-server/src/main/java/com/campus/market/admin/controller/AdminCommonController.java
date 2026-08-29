package com.campus.market.admin.controller;

import com.campus.market.admin.dto.IdDTO;
import com.campus.market.admin.dto.LocationSaveDTO;
import com.campus.market.admin.dto.NoticeSaveDTO;
import com.campus.market.admin.service.AdminCommonService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.Notice;
import com.campus.market.entity.SchoolLocation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端：基础数据维护（校内地点 / 系统公告，全量含停用下线）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCommonController {

    private final AdminCommonService adminCommonService;

    // ==================== 地点管理 ====================

    /** 地点全量列表（含停用） */
    @GetMapping("/location/list")
    public Result<List<SchoolLocation>> locationList() {
        return Result.ok(adminCommonService.locationListAll());
    }

    /** 保存地点：{id?, name, type, sort}，有 id 改、无 id 增（新增默认启用） */
    @PostMapping("/location/save")
    public Result<SchoolLocation> saveLocation(@Valid @RequestBody LocationSaveDTO dto) {
        return Result.ok(adminCommonService.saveLocation(dto));
    }

    /** 启用/停用切换：{id} */
    @PostMapping("/location/toggle")
    public Result<Void> toggleLocation(@Valid @RequestBody IdDTO dto) {
        adminCommonService.toggleLocation(dto.getId());
        return Result.ok();
    }

    // ==================== 公告管理 ====================

    /** 公告列表（全量含下线，分页） */
    @GetMapping("/notice/list")
    public Result<PageResult<Notice>> noticeList(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminCommonService.noticeList(page, size));
    }

    /** 发布公告：{title, content}（status=1） */
    @PostMapping("/notice/publish")
    public Result<Notice> publishNotice(@Valid @RequestBody NoticeSaveDTO dto) {
        return Result.ok(adminCommonService.publishNotice(dto));
    }

    /** 下线公告：{id} */
    @PostMapping("/notice/offline")
    public Result<Void> offlineNotice(@Valid @RequestBody IdDTO dto) {
        adminCommonService.offlineNotice(dto.getId());
        return Result.ok();
    }
}
