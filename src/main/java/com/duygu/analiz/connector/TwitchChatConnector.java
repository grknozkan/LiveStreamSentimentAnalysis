package com.duygu.analiz.connector;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.StreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class TwitchChatConnector implements StreamConnector, WebSocket.Listener {

    private static final Logger log = LoggerFactory.getLogger(TwitchChatConnector.class);
    private static final String TWITCH_WS_URI = "wss://irc-ws.chat.twitch.tv:443";

    private final AtomicBoolean running = new AtomicBoolean(false);
    private WebSocket webSocket;
    private Consumer<ChatMessage> messageConsumer;
    private String currentChannel;
    private final StringBuilder messageBuffer = new StringBuilder();

    @Override
    public void start(StreamConfig config, Consumer<ChatMessage> consumer) {
        stop();
        this.messageConsumer = consumer;
        String rawChannel = config.getChannelOrVideoId();
        if (rawChannel == null || rawChannel.trim().isEmpty()) {
            rawChannel = "elraenn";
        }
        // Normalize channel name (lowercase, remove # if provided)
        this.currentChannel = rawChannel.toLowerCase().replace("#", "").trim();

        log.info("Connecting to Twitch IRC WebSocket for channel: #{}", currentChannel);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            client.newWebSocketBuilder()
                    .buildAsync(URI.create(TWITCH_WS_URI), this)
                    .thenAccept(ws -> {
                        this.webSocket = ws;
                        running.set(true);

                        // Anonymous authentication to Twitch IRC
                        int randomId = 10000 + new Random().nextInt(90000);
                        ws.sendText("PASS SCHMOOPIE\r\n", true);
                        ws.sendText("NICK justinfan" + randomId + "\r\n", true);
                        ws.sendText("JOIN #" + currentChannel + "\r\n", true);
                        log.info("Sent anonymous login and joined #{}", currentChannel);
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to connect to Twitch IRC: {}", ex.getMessage());
                        running.set(false);
                        return null;
                    });

        } catch (Exception e) {
            log.error("Twitch connection exception: {}", e.getMessage(), e);
            running.set(false);
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Twitch WebSocket connection opened.");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        messageBuffer.append(data);

        if (last) {
            String fullPayload = messageBuffer.toString();
            messageBuffer.setLength(0);

            String[] lines = fullPayload.split("\r\n");
            for (String line : lines) {
                processIrcLine(line);
            }
        }

        webSocket.request(1);
        return null;
    }

    private void processIrcLine(String line) {
        if (line == null || line.trim().isEmpty()) return;

        // Auto-reply to PING to keep connection alive
        if (line.startsWith("PING")) {
            if (webSocket != null) {
                webSocket.sendText("PONG :tmi.twitch.tv\r\n", true);
                log.debug("Replied PONG to Twitch server");
            }
            return;
        }

        // Parse PRIVMSG: :username!username@username.tmi.twitch.tv PRIVMSG #channel :message text
        if (line.contains("PRIVMSG")) {
            try {
                int exclamationIdx = line.indexOf('!');
                int colonSecondIdx = line.indexOf(" :", 1);

                if (exclamationIdx > 1 && colonSecondIdx > exclamationIdx) {
                    String username = line.substring(1, exclamationIdx);
                    String messageText = line.substring(colonSecondIdx + 2);

                    ChatMessage chatMsg = new ChatMessage();
                    chatMsg.setId(UUID.randomUUID().toString());
                    chatMsg.setPlatform("TWITCH");
                    chatMsg.setChannel(currentChannel);
                    chatMsg.setUsername(username);
                    chatMsg.setMessage(messageText);
                    chatMsg.setTimestamp(System.currentTimeMillis());

                    if (messageConsumer != null) {
                        messageConsumer.accept(chatMsg);
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing Twitch IRC line: {}", line, e);
            }
        }
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.info("Twitch WebSocket closed. Code: {}, Reason: {}", statusCode, reason);
        running.set(false);
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Twitch WebSocket error: {}", error.getMessage());
        running.set(false);
    }

    @Override
    public void stop() {
        running.set(false);
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Stopping");
            } catch (Exception ignored) {}
            webSocket = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public String getPlatform() {
        return "TWITCH";
    }
}