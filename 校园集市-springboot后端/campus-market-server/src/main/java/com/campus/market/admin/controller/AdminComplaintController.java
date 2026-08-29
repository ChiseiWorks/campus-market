package com.campus.market.admin.controller;

import com.campus.market.admin.dto.ComplaintHandleDTO;
import com.campus.market.admin.service.AdminComplaintService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.Complaint;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端：投诉仲裁
 */
@RestController
@RequestMapping("/api/admin/complaint")
@RequiredArgsConstructor
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    /** 投诉列表：status=0待处理 1处理中 2已办结（空查全部），item 附双方昵称 */
    @GetMapping("/list")
    public Result<PageResult<Complaint>> list(@RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminComplaintService.list(status, page, size));
    }

    /** 投诉详情 + 关联订单快照：{complaint, order} */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(adminComplaintService.detail(id));
    }

    /** 办结投诉：{id, result, defendantCreditDelta(可空)}，信用分走 CreditService 流水 */
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody ComplaintHandleDTO dto) {
        adminComplaintService.handle(dto.getId(), dto);
        return Result.ok();
    }
}
