package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.admin.dto.ComplaintHandleDTO;
import com.campus.market.admin.service.AdminComplaintService;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.entity.Complaint;
import com.campus.market.entity.User;
import com.campus.market.mapper.ComplaintMapper;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.GoodsOrderMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端：投诉仲裁
 */
@Service
@RequiredArgsConstructor
public class AdminComplaintServiceImpl implements AdminComplaintService {

    private final ComplaintMapper complaintMapper;
    private final UserMapper userMapper;
    private final GoodsOrderMapper goodsOrderMapper;
    private final ErrandOrderMapper errandOrderMapper;
    private final CreditService creditService;

    @Override
    public PageResult<Complaint> list(Integer status, int page, int size) {
        QueryWrapper<Complaint> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        Page<Complaint> p = complaintMapper.selectPage(new Page<>(page, size), qw);
        fillNicknames(p.getRecords());
        return PageResult.of(p);
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new BizException("投诉不存在");
        }
        List<Complaint> one = new ArrayList<>();
        one.add(complaint);
        fillNicknames(one);
        // 关联订单快照：1闲置 → goods_order，2跑腿 → errand_order
        Object order = null;
        if (complaint.getOrderType() != null && complaint.getOrderId() != null) {
            if (complaint.getOrderType() == 1) {
                order = goodsOrderMapper.selectById(complaint.getOrderId());
            } else if (complaint.getOrderType() == 2) {
                order = errandOrderMapper.selectById(complaint.getOrderId());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("complaint", complaint);
        data.put("order", order);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, ComplaintHandleDTO dto) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new BizException("投诉不存在");
        }
        if (complaint.getStatus() != null && complaint.getStatus() == 2) {
            throw new BizException("该投诉已办结，请勿重复处理");
        }
        complaint.setStatus(2); // 已办结
        complaint.setResult(dto.getResult());
        complaint.setHandleTime(LocalDateTime.now());
        // handlerId 为 user 表主键，管理员走配置账号不在 user 表，留 null
        complaintMapper.updateById(complaint);
        // 可选：调整被投诉人信用分（红线：必须走 CreditService 流水）
        if (dto.getDefendantCreditDelta() != null && dto.getDefendantCreditDelta() != 0
                && complaint.getDefendantId() != null) {
            creditService.change(complaint.getDefendantId(), dto.getDefendantCreditDelta(),
                    "投诉处理信用调整（投诉单#" + id + "）", complaint.getOrderType(), complaint.getOrderId());
        }
    }

    /** 批量填充投诉人/被投诉人昵称 */
    private void fillNicknames(List<Complaint> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = new ArrayList<>();
        for (Complaint c : list) {
            if (c.getPlaintiffId() != null) {
                userIds.add(c.getPlaintiffId());
            }
            if (c.getDefendantId() != null) {
                userIds.add(c.getDefendantId());
            }
        }
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(
                        userIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        for (Complaint c : list) {
            User plaintiff = userMap.get(c.getPlaintiffId());
            if (plaintiff != null) {
                c.setPlaintiffNickname(plaintiff.getNickname());
            }
            User defendant = userMap.get(c.getDefendantId());
            if (defendant != null) {
                c.setDefendantNickname(defendant.getNickname());
            }
        }
    }
}
