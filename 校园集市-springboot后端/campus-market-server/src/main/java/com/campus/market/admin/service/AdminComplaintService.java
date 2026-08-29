package com.campus.market.admin.service;

import com.campus.market.admin.dto.ComplaintHandleDTO;
import com.campus.market.common.PageResult;
import com.campus.market.entity.Complaint;

import java.util.Map;

/**
 * 管理端：投诉仲裁
 */
public interface AdminComplaintService {

    /** 投诉列表：item 附 plaintiffNickname + defendantNickname */
    PageResult<Complaint> list(Integer status, int page, int size);

    /** 投诉详情 + 关联订单快照 */
    Map<String, Object> detail(Long id);

    /** 办结投诉：status=2 + result + handle_time；可选调整被投诉人信用分（走 CreditService 流水） */
    void handle(Long id, ComplaintHandleDTO dto);
}
