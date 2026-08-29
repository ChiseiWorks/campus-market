package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.MessageSendDTO;
import com.campus.market.entity.ChatMessage;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.enums.ErrandStatusEnum;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息服务（P1 轮询方案，后续可升级 WebSocket）
 */
public interface MessageService extends IService<ChatMessage> {

    /** 会话列表：最近联系人 + 最后一条消息 + 未读数 */
    List<Map<String, Object>> sessions(Long userId);

    /** 与某人的聊天记录（分页，时间倒序） */
    PageResult<ChatMessage> history(Long userId, Long peerId, int page, int size);

    /** 发送消息 */
    ChatMessage send(Long fromUserId, MessageSendDTO dto);

    /** 将与某人的会话标记已读 */
    void markRead(Long userId, Long peerId);

    /** 全局未读数（Tab 角标） */
    long unreadCount(Long userId);

    /** 推送跑腿单状态变更通知（骨架阶段为日志实现，TODO 接入消息表/推送通道） */
    void pushStatusChange(ErrandOrder order, ErrandStatusEnum target);
}
