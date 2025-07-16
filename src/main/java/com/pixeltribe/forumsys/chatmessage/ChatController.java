package com.pixeltribe.forumsys.chatmessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{forumId}/sendMessage")
    // 【修改】 新增 SimpMessageHeaderAccessor 參數，用來讀取 session 資訊
    public void sendMessage(@DestinationVariable String forumId, @Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // 【新增】 從 WebSocket session 中獲取 addUser 時存入的 memberId
        // 這樣可以防止使用者在發送訊息時偽造 ID，更安全
        Integer memberId = (Integer) headerAccessor.getSessionAttributes().get("memberId");

        // 【新增】 將從 session 來的、可信的 memberId 設定到要廣播的訊息中
        chatMessage.setMemberId(memberId);

        // 將附加上了可信 memberId 的訊息廣播出去
        messagingTemplate.convertAndSend("/topic/chat/" + forumId, chatMessage);
    }

    @MessageMapping("/chat/{forumId}/addUser")
    public void addUser(@DestinationVariable String forumId, @Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {
        // 【修改】除了存 username，也要把使用者 ID 存起來
        // 這個 chatMessage 是從客戶端 connect 時傳來的，包含了 memberId
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("forumId", forumId);
        // 【新增】 將使用者 ID 存入 WebSocket session
        headerAccessor.getSessionAttributes().put("memberId", chatMessage.getMemberId());

        // 廣播使用者加入的訊息
        messagingTemplate.convertAndSend("/topic/chat/" + forumId, chatMessage);
    }

}