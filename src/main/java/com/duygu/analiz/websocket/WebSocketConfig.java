package com.duygu.analiz.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SentimentWebSocketHandler sentimentWebSocketHandler;

    public WebSocketConfig(SentimentWebSocketHandler sentimentWebSocketHandler) {
        this.sentimentWebSocketHandler = sentimentWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sentimentWebSocketHandler, "/ws/live-sentiment")
                .setAllowedOrigins("*");
    }
}