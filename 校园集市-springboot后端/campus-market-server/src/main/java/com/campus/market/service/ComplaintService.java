package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.ComplaintSubmitDTO;
import com.campus.market.entity.Complaint;
import com.campus.market.entity.ErrandOrder;

/**
 * 投诉服务
 */
public interface ComplaintService extends IService<Complaint> {

    /** 提交投诉 */
    void submit(Long userId, ComplaintSubmitDTO dto);

    /** 我的投诉记录（我发起的 + 我被投诉的） */
    PageResult<Complaint> my(Long userId, int page, int size);

    /** 投诉详情（仅投诉双方可看处理进度与结果） */
    Complaint detail(Long id, Long userId);

    /** 跑腿申诉自动生成投诉单（状态机 DISPUTED 副作用调用） */
    void createFromDispute(ErrandOrder order, Long operatorId, String reason);
}
