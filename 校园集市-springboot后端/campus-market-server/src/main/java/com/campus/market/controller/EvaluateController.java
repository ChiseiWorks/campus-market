package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.EvaluateSubmitDTO;
import com.campus.market.entity.Evaluation;
import com.campus.market.service.EvaluateService;
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
 * 评价接口（接口文档 5.5）
 */
@RestController
@RequestMapping("/api/evaluate")
@RequiredArgsConstructor
public class EvaluateController {

    private final EvaluateService evaluateService;

    /** 提交评价（每单每人限一次，重复提交按幂等返回成功） */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestAttribute Long userId, @Valid @RequestBody EvaluateSubmitDTO dto) {
        evaluateService.submit(userId, dto);
        return Result.ok();
    }

    /** 某用户收到的评价（卖家主页/跑男主页展示用） */
    @GetMapping("/user/{userId}")
    public Result<PageResult<Evaluation>> ofUser(@PathVariable Long userId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(evaluateService.ofUser(userId, page, size));
    }

    /** 我发出的评价 */
    @GetMapping("/my")
    public Result<PageResult<Evaluation>> my(@RequestAttribute Long userId,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(evaluateService.my(userId, page, size));
    }
}
