package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.ErrandPublishDTO;
import com.campus.market.dto.ReasonDTO;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.service.ErrandService;
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
 * 跑腿单接口（接口文档第 2 节）
 * 状态推进全部走 ErrandStateMachine 统一入口；抢单为原子 CAS
 */
@RestController
@RequestMapping("/api/errand")
@RequiredArgsConstructor
public class ErrandController {

    private final ErrandService errandService;

    /** 发布跑腿单（已认证用户，信用分≥60） */
    @PostMapping("/publish")
    public Result<ErrandOrder> publish(@RequestAttribute Long userId, @Valid @RequestBody ErrandPublishDTO dto) {
        return Result.ok(errandService.publish(userId, dto));
    }

    /** 接单大厅：type / sort(latest|reward) / page */
    @GetMapping("/hall")
    public Result<PageResult<ErrandOrder>> hall(@RequestParam(required = false) Integer type,
                                                @RequestParam(required = false) String sort,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(errandService.hall(type, sort, page, size));
    }

    /** 订单详情（按角色 + 状态脱敏：取件码 / 联系方式） */
    @GetMapping("/{id}")
    public Result<ErrandOrder> detail(@PathVariable Long id, @RequestAttribute Long userId) {
        return Result.ok(errandService.detail(id, userId));
    }

    /** 抢单（原子 CAS，失败提示"手慢了"） */
    @PostMapping("/{id}/accept")
    public Result<Void> accept(@PathVariable Long id, @RequestAttribute Long userId) {
        errandService.accept(id, userId);
        return Result.ok();
    }

    /** 开始配送（本单跑男） */
    @PostMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id, @RequestAttribute Long userId) {
        errandService.deliver(id, userId);
        return Result.ok();
    }

    /** 确认送达（本单跑男） */
    @PostMapping("/{id}/arrive")
    public Result<Void> arrive(@PathVariable Long id, @RequestAttribute Long userId) {
        errandService.arrive(id, userId);
        return Result.ok();
    }

    /** 确认完成（本单发单人，双方信用分 +2） */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, @RequestAttribute Long userId) {
        errandService.confirm(id, userId);
        return Result.ok();
    }

    /** 取消订单（body 带原因；跑男已接单后取消扣 10 信用分） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestAttribute Long userId,
                               @RequestBody(required = false) ReasonDTO dto) {
        errandService.cancel(id, userId, dto != null ? dto.getReason() : null);
        return Result.ok();
    }

    /** 发起申诉（本单发单人，生成投诉单进入仲裁队列） */
    @PostMapping("/{id}/dispute")
    public Result<Void> dispute(@PathVariable Long id, @RequestAttribute Long userId,
                                @RequestBody(required = false) ReasonDTO dto) {
        errandService.dispute(id, userId, dto != null ? dto.getReason() : null);
        return Result.ok();
    }

    /** 我发布的单 */
    @GetMapping("/my/publish")
    public Result<PageResult<ErrandOrder>> myPublish(@RequestAttribute Long userId,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(errandService.myPublish(userId, status, page, size));
    }

    /** 我接的单 */
    @GetMapping("/my/accept")
    public Result<PageResult<ErrandOrder>> myAccept(@RequestAttribute Long userId,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(errandService.myAccept(userId, status, page, size));
    }
}
