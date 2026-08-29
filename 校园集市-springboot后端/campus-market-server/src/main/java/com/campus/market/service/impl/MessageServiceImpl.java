package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.MessageSendDTO;
import com.campus.market.entity.ChatMessage;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.User;
import com.campus.market.enums.ErrandStatusEnum;
import com.campus.market.mapper.ChatMessageMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现（P1 轮询方案，后续可升级 WebSocket）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements MessageService {

    private final UserMapper userMapper;

    /**
     * 会话列表：按最后消息时间倒序，含未读数
     * 基础实现：拉取最近消息在内存中聚合（TODO: 数据量大后改为 SQL 分组聚合）
     */
    @Override
    public List<Map<String, Object>> sessions(Long userId) {
        List<ChatMessage> recent = list(new QueryWrapper<ChatMessage>()
                .eq("from_user_id", userId).or().eq("to_user_id", userId)
                .orderByDesc("create_time")
                .orderByDesc("id")
                .last("LIMIT 500"));
        // 按对方用户分组：第一条即最后一条消息（已按时间倒序）
        Map<Long, Map<String, Object>> sessionMap = new LinkedHashMap<>();
        for (ChatMessage msg : recent) {
            Long peerId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            Map<String, Object> session = sessionMap.computeIfAbsent(peerId, k -> {
                Map<String, Object> s = new HashMap<>();
                s.put("peerId", k);
                s.put("lastMessage", msg.getContent());
                s.put("lastMessageType", msg.getType());
                s.put("lastTime", msg.getCreateTime());
                s.put("goodsId", msg.getGoodsId());
                s.put("unreadCount", 0);
                return s;
            });
            // 统计对方发给我且未读的消息数
            if (msg.getToUserId().equals(userId) && msg.getIsRead() != null && msg.getIsRead() == 0) {
                session.put("unreadCount", (Integer) session.get("unreadCount") + 1);
            }
        }
        // 补充对方昵称/头像
        if (!sessionMap.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(sessionMap.keySet());
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
            sessionMap.forEach((peerId, session) -> {
                User u = userMap.get(peerId);
                if (u != null) {
                    session.put("nickname", u.getNickname());
                    session.put("avatar", u.getAvatar());
                }
            });
        }
        return new ArrayList<>(sessionMap.values());
    }

    @Override
    public PageResult<ChatMessage> history(Long userId, Long peerId, int page, int size) {
        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        // (我发给对方) OR (对方发给我)
        qw.and(w -> w.eq("from_user_id", userId).eq("to_user_id", peerId))
                .or(w -> w.eq("from_user_id", peerId).eq("to_user_id", userId));
        qw.orderByDesc("create_time").orderByDesc("id");
        return PageResult.of(page(new Page<>(page, size), qw));
    }

    @Override
    public ChatMessage send(Long fromUserId, MessageSendDTO dto) {
        if (dto.getToUserId().equals(fromUserId)) {
            throw new BizException("不能给自己发消息");
        }
        User to = userMapper.selectById(dto.getToUserId());
        if (to == null) {
            throw new BizException("对方用户不存在");
        }
        ChatMessage msg = new ChatMessage();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(dto.getToUserId());
        msg.setGoodsId(dto.getGoodsId());
        msg.setType(dto.getType() != null ? dto.getType() : 1);
        msg.setContent(dto.getContent());
        msg.setIsRead(0);
        save(msg);
        return msg;
    }

    @Override
    public void markRead(Long userId, Long peerId) {
        ChatMessage update = new ChatMessage();
        update.setIsRead(1);
        update(update, new UpdateWrapper<ChatMessage>()
                .eq("to_user_id", userId)
                .eq("from_user_id", peerId)
                .eq("is_read", 0));
    }

    @Override
    public long unreadCount(Long userId) {
        return count(new QueryWrapper<ChatMessage>()
                .eq("to_user_id", userId)
                .eq("is_read", 0));
    }

    /**
     * 推送跑腿单状态变更通知
     * TODO: 骨架阶段仅打日志，后续接入系统消息表 / 订阅消息推送
     */
    @Override
    public void pushStatusChange(ErrandOrder order, ErrandStatusEnum target) {
        log.info("【状态变更通知】跑腿单 {} 状态推进为 {}（发单人={}, 接单人={}）",
                order.getOrderNo(), target.getDesc(), order.getPublisherId(), order.getRunnerId());
    }
}
