package com.duygu.analiz.connector;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.StreamConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class YouTubeChatConnector implements StreamConnector {

    private static final Logger log = LoggerFactory.getLogger(YouTubeChatConnector.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ScheduledExecutorService scheduler;
    private Consumer<ChatMessage> messageConsumer;
    private String liveChatId;
    private String nextPageToken = null;
    private String apiKey;

    @Override
    public void start(StreamConfig config, Consumer<ChatMessage> consumer) {
        stop();
        this.messageConsumer = consumer;
        this.apiKey = config.getApiKey();
        String videoOrChatId = config.getChannelOrVideoId();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("YouTube API Key not provided. YouTube connector requires a Google API key.");
            // If no API key provided, fall back to safe error or note
            running.set(false);
            return;
        }

        running.set(true);
        this.liveChatId = videoOrChatId;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Start polling every 4 seconds
        scheduler.scheduleWithFixedDelay(this::pollYouTubeChat, 1, 4, TimeUnit.SECONDS);
        log.info("YouTube live chat polling started for liveChatId: {}", liveChatId);
    }

    private void pollYouTubeChat() {
        if (!running.get()) return;

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            StringBuilder url = new StringBuilder("https://www.googleapis.com/youtube/v3/liveChat/messages")
                    .append("?liveChatId=").append(liveChatId)
                    .append("&part=snippet,authorDetails")
                    .append("&key=").append(apiKey);

            if (nextPageToken != null) {
                url.append("&pageToken=").append(nextPageToken);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("nextPageToken")) {
                    this.nextPageToken = root.get("nextPageToken").asText();
                }

                JsonNode items = root.get("items");
                if (items != null && items.isArray()) {
                    for (JsonNode item : items) {
                        String author = item.path("authorDetails").path("displayName").asText("Anonim");
                        String text = item.path("snippet").path("displayMessage").asText();

                        ChatMessage msg = new ChatMessage();
                        msg.setId(UUID.randomUUID().toString());
                        msg.setPlatform("YOUTUBE");
                        msg.setChannel(liveChatId);
                        msg.setUsername(author);
                        msg.setMessage(text);
                        msg.setTimestamp(System.currentTimeMillis());

                        if (messageConsumer != null) {
                            messageConsumer.accept(msg);
                        }
                    }
                }
            } else {
                log.warn("YouTube API error response: {} - {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("YouTube poll error: {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        running.set(false);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public String getPlatform() {
        return "YOUTUBE";
    }
}