package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.ErrandPublishDTO;
import com.campus.market.entity.ErrandOrder;

/**
 * 跑腿订单服务
 * 状态推进统一走 ErrandStateMachine；抢单走原子 CAS（见 accept）
 */
public interface ErrandService extends IService<ErrandOrder> {

    /** 发布跑腿单（需已认证 + 信用分≥60） */
    ErrandOrder publish(Long userId, ErrandPublishDTO dto);

    /** 接单大厅：status=0 待接单分页列表，sort=latest|reward */
    PageResult<ErrandOrder> hall(Integer type, String sort, int page, int size);

    /** 订单详情：按查看者角色 + 订单状态脱敏（取件码/联系方式） */
    ErrandOrder detail(Long id, Long viewerId);

    /** 抢单：原子 CAS，数据库层面保证只有一个赢家 */
    void accept(Long id, Long userId);

    /** 开始配送（本单跑男） */
    void deliver(Long id, Long userId);

    /** 确认送达（本单跑男） */
    void arrive(Long id, Long userId);

    /** 确认完成（本单发单人，双方信用分 +2） */
    void confirm(Long id, Long userId);

    /** 取消订单（双方按状态校验；跑男已接单后取消扣 10 信用分） */
    void cancel(Long id, Long userId, String reason);

    /** 发起申诉（本单发单人，生成投诉单进入仲裁队列） */
    void dispute(Long id, Long userId, String reason);

    /** 我发布的单 */
    PageResult<ErrandOrder> myPublish(Long userId, Integer status, int page, int size);

    /** 我接的单 */
    PageResult<ErrandOrder> myAccept(Long userId, Integer status, int page, int size);
}
