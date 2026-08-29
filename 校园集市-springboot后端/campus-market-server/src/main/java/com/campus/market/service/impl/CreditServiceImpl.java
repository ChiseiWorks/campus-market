package com.campus.market.service.impl;

import com.campus.market.common.BizException;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;
import com.campus.market.mapper.CreditLogMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 信用分服务（对应设计文档 3.6）
 *
 * 设计红线：信用分只允许通过本类走流水变更——
 * 先改分（GREATEST/LEAST 上下限保护），再写流水，两步同一事务，保证可追溯。
 * 业务代码禁止直接 UPDATE user.credit_score。
 */
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditLogMapper creditLogMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void change(Long userId, int delta, String reason, Integer orderType, Long orderId) {
        // 1. 先扣/加分（SQL 层 GREATEST(0, LEAST(100, ...)) 上下限保护）
        int rows = userMapper.changeCredit(userId, delta);
        if (rows == 0) {
            throw new BizException("信用分更新失败");
        }
        // 2. 再写流水（变动后余额从库里回读，保证流水准确）
        User user = userMapper.selectById(userId);
        CreditLog creditLog = new CreditLog();
        creditLog.setUserId(userId);
        creditLog.setChangeValue(delta);
        creditLog.setBalance(user.getCreditScore());
        creditLog.setReason(reason);
        creditLog.setOrderType(orderType);
        creditLog.setOrderId(orderId);
        creditLog.setOperatorId(null); // NULL = 系统自动
        creditLogMapper.insert(creditLog);
        // 低于 60 分：发布/接单限制在 Service 校验层体现（发布/接单前检查 creditScore）
    }
}
