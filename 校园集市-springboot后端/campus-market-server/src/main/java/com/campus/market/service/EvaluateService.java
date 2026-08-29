package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.EvaluateSubmitDTO;
import com.campus.market.entity.Evaluation;

/**
 * 评价服务
 */
public interface EvaluateService extends IService<Evaluation> {

    /** 提交评价；每单每人限一次（uk_order_from 唯一约束，重复提交按幂等返回成功） */
    void submit(Long userId, EvaluateSubmitDTO dto);

    /** 某用户收到的评价（卖家主页/跑男主页展示用） */
    PageResult<Evaluation> ofUser(Long userId, int page, int size);

    /** 我发出的评价 */
    PageResult<Evaluation> my(Long userId, int page, int size);
}
