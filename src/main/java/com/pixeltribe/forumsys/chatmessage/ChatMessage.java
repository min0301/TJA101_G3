package com.pixeltribe.forumsys.chatmessage;

import lombok.Data;

@Data
public class ChatMessage {

    private String sender; // 發送者
    private String content; // 訊息內容
    private MessageType type; // 訊息類型 (例如：加入、聊天)
    private Integer memberId;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

}
