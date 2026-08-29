package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.ComplaintSubmitDTO;
import com.campus.market.entity.Complaint;
import com.campus.market.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 投诉接口（接口文档 5.5）
 */
@RestController
@RequestMapping("/api/complaint")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /** 提交投诉（orderType/orderId/defendantId/type/content/evidence） */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestAttribute Long userId, @Valid @RequestBody ComplaintSubmitDTO dto) {
        complaintService.submit(userId, dto);
        return Result.ok();
    }

    /** 我的投诉记录（我发起的 + 我被投诉的） */
    @GetMapping("/my")
    public Result<PageResult<Complaint>> my(@RequestAttribute Long userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(complaintService.my(userId, page, size));
    }

    /** 投诉详情（处理进度与结果，仅投诉双方可见） */
    @GetMapping("/{id}")
    public Result<Complaint> detail(@PathVariable Long id, @RequestAttribute Long userId) {
        return Result.ok(complaintService.detail(id, userId));
    }
}
