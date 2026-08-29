package com.campus.market.service;

/**
 * 信用分服务
 * 设计红线：信用分只能通过本服务走流水变更（先改分、再写流水，同一事务），
 * 业务代码禁止直接 UPDATE user.credit_score
 */
public interface CreditService {

    /**
     * 变更信用分（GREATEST(0, LEAST(100, ...)) 上下限保护）
     *
     * @param userId    变动用户
     * @param delta     变动值（正加负减）
     * @param reason    变动原因（写入流水）
     * @param orderType 关联订单类型：1闲置 2跑腿，可为 null
     * @param orderId   关联订单，可为 null
     */
    void change(Long userId, int delta, String reason, Integer orderType, Long orderId);
}
