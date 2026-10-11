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
        String input = config.getChannelOrVideoId();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("YouTube API Key not provided. YouTube connector requires a Google API key.");
            running.set(false);
            return;
        }

        running.set(true);
        this.liveChatId = resolveLiveChatId(input);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Start polling every 4 seconds
        scheduler.scheduleWithFixedDelay(this::pollYouTubeChat, 1, 4, TimeUnit.SECONDS);
        log.info("YouTube live chat polling started for liveChatId: {}", liveChatId);
    }

    private String resolveLiveChatId(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String trimmed = input.trim();

        // Extract video ID if user passed a YouTube URL
        String videoId = trimmed;
        if (trimmed.contains("youtube.com/watch?v=")) {
            int idx = trimmed.indexOf("v=") + 2;
            int amp = trimmed.indexOf('&', idx);
            videoId = amp != -1 ? trimmed.substring(idx, amp) : trimmed.substring(idx);
        } else if (trimmed.contains("youtu.be/")) {
            int idx = trimmed.indexOf("youtu.be/") + 9;
            int q = trimmed.indexOf('?', idx);
            videoId = q != -1 ? trimmed.substring(idx, q) : trimmed.substring(idx);
        } else if (trimmed.contains("youtube.com/live/")) {
            int idx = trimmed.indexOf("live/") + 5;
            int q = trimmed.indexOf('?', idx);
            videoId = q != -1 ? trimmed.substring(idx, q) : trimmed.substring(idx);
        }

        // If it looks like a video ID (typical YouTube video IDs are ~11 alphanumeric characters)
        if (videoId.length() <= 15 && !videoId.startsWith("Ei")) {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();
                String url = "https://www.googleapis.com/youtube/v3/videos?part=liveStreamingDetails&id=" 
                        + videoId + "&key=" + apiKey;
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(resp.body());
                    JsonNode items = root.path("items");
                    if (items.isArray() && items.size() > 0) {
                        String chatId = items.get(0).path("liveStreamingDetails").path("activeLiveChatId").asText(null);
                        if (chatId != null && !chatId.isEmpty()) {
                            log.info("Successfully resolved activeLiveChatId {} from videoId {}", chatId, videoId);
                            return chatId;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to resolve liveChatId from videoId {}: {}", videoId, e.getMessage());
            }
        }

        return videoId;
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