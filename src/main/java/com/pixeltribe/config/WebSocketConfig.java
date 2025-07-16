package com.pixeltribe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 註冊一個 STOMP 的 endpoint，並指定使用 SockJS 協議
        // "/ws" 就是前端要連線的 WebSocket 入口
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 設定訊息代理
        // "/topic" 是廣播式，所有訂閱的客戶端都會收到
        registry.enableSimpleBroker("/topic");

        // 設定應用程式目的地前綴
        // 前端發送訊息的目的地前綴，例如：/api/chat/123
        registry.setApplicationDestinationPrefixes("/api");
    }

}
