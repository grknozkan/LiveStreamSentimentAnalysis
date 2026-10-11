package com.duygu.analiz.connector;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.StreamConfig;
import java.util.function.Consumer;

public interface StreamConnector {
    void start(StreamConfig config, Consumer<ChatMessage> messageConsumer);
    void stop();
    boolean isRunning();
    String getPlatform();
}