package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.MessageReadDTO;
import com.campus.market.dto.MessageSendDTO;
import com.campus.market.entity.ChatMessage;
import com.campus.market.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 聊天消息接口（接口文档 5.4，P1 轮询方案）
 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 会话列表：最近联系人 + 最后一条消息 + 未读数 */
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> sessions(@RequestAttribute Long userId) {
        return Result.ok(messageService.sessions(userId));
    }

    /** 与某人的聊天记录（分页，时间倒序）；userId 参数为对方用户ID */
    @GetMapping("/history")
    public Result<PageResult<ChatMessage>> history(@RequestAttribute("userId") Long userId,
                                                   @RequestParam("userId") Long peerId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(messageService.history(userId, peerId, page, size));
    }

    /** 发送消息 */
    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestAttribute Long userId, @Valid @RequestBody MessageSendDTO dto) {
        return Result.ok(messageService.send(userId, dto));
    }

    /** 将与某人的会话标记已读 */
    @PostMapping("/read")
    public Result<Void> read(@RequestAttribute Long userId, @Valid @RequestBody MessageReadDTO dto) {
        messageService.markRead(userId, dto.getUserId());
        return Result.ok();
    }

    /** 全局未读数（Tab 角标） */
    @GetMapping("/unread/count")
    public Result<Long> unreadCount(@RequestAttribute Long userId) {
        return Result.ok(messageService.unreadCount(userId));
    }
}
