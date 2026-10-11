package com.duygu.analiz.service;

import com.duygu.analiz.connector.SimulatorChatConnector;
import com.duygu.analiz.connector.StreamConnector;
import com.duygu.analiz.connector.TwitchChatConnector;
import com.duygu.analiz.connector.YouTubeChatConnector;
import com.duygu.analiz.fluctuation.FluctuationDetector;
import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.EmotionalEvent;
import com.duygu.analiz.model.SentimentMetrics;
import com.duygu.analiz.model.StreamConfig;
import com.duygu.analiz.sentiment.SentimentAnalyzer;
import com.duygu.analiz.websocket.SentimentWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class StreamAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(StreamAnalysisService.class);

    private final SentimentAnalyzer sentimentAnalyzer;
    private final FluctuationDetector fluctuationDetector;
    private final TwitchChatConnector twitchChatConnector;
    private final YouTubeChatConnector youTubeChatConnector;
    private final SimulatorChatConnector simulatorChatConnector;
    private final SentimentWebSocketHandler webSocketHandler;

    private StreamConfig currentConfig = new StreamConfig("SIMULATOR", "demo_stream", "", true);
    private StreamConnector activeConnector;

    private final List<SentimentMetrics> metricHistory = new CopyOnWriteArrayList<>();
    private final List<EmotionalEvent> eventHistory = new CopyOnWriteArrayList<>();
    private final Deque<ChatMessage> recentMessages = new ConcurrentLinkedDeque<>();
    private ScheduledExecutorService metricsBroadcastScheduler;

    public StreamAnalysisService(SentimentAnalyzer sentimentAnalyzer,
                                 FluctuationDetector fluctuationDetector,
                                 TwitchChatConnector twitchChatConnector,
                                 YouTubeChatConnector youTubeChatConnector,
                                 SimulatorChatConnector simulatorChatConnector,
                                 SentimentWebSocketHandler webSocketHandler) {
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.fluctuationDetector = fluctuationDetector;
        this.twitchChatConnector = twitchChatConnector;
        this.youTubeChatConnector = youTubeChatConnector;
        this.simulatorChatConnector = simulatorChatConnector;
        this.webSocketHandler = webSocketHandler;
    }

    @PostConstruct
    public void init() {
        metricsBroadcastScheduler = Executors.newSingleThreadScheduledExecutor();
        metricsBroadcastScheduler.scheduleAtFixedRate(this::broadcastMetricsPeriodically, 1, 1, TimeUnit.SECONDS);

        // Varsayılan olarak test simülatörünü başlat (hemen canlı veri akışı başlar)
        startStream(currentConfig);
    }

    public synchronized void startStream(StreamConfig config) {
        stopCurrentStream();

        this.currentConfig = config;
        this.currentConfig.setRunning(true);
        this.fluctuationDetector.reset();

        String platform = config.getPlatform().toUpperCase();
        switch (platform) {
            case "TWITCH":
                activeConnector = twitchChatConnector;
                break;
            case "YOUTUBE":
                activeConnector = youTubeChatConnector;
                break;
            case "SIMULATOR":
            default:
                activeConnector = simulatorChatConnector;
                break;
        }

        log.info("Starting stream analysis for platform: {} (Channel/Target: {})", platform, config.getChannelOrVideoId());
        activeConnector.start(config, this::processIncomingMessage);
    }

    public synchronized void stopCurrentStream() {
        if (activeConnector != null && activeConnector.isRunning()) {
            activeConnector.stop();
            log.info("Stopped connector for platform: {}", activeConnector.getPlatform());
        }
        currentConfig.setRunning(false);
    }

    public void processIncomingMessage(ChatMessage message) {
        // 1. Duygu Analizi yap
        SentimentAnalyzer.AnalysisResult result = sentimentAnalyzer.analyze(message.getMessage());
        message.setSentimentScore(result.getScore());
        message.setSentimentLabel(result.getLabel());
        message.setDetectedKeywords(result.getDetectedKeywords());
        message.setHypeLevel(result.getHypeLevel());

        // 2. Mesaj hafızasına ekle (Son 50 mesaj)
        recentMessages.addLast(message);
        while (recentMessages.size() > 50) {
            recentMessages.removeFirst();
        }

        // 3. Mesajı WebSocket üzerinden canlı chate gönder
        webSocketHandler.broadcast("CHAT", message);

        // 4. Dalgalanma / Anomali kontrolü yap
        Optional<EmotionalEvent> eventOpt = fluctuationDetector.addMessageAndCheckEvent(message);
        eventOpt.ifPresent(event -> {
            log.info("Emotional event detected: [{}] {}", event.getType(), event.getTitle());
            eventHistory.add(event);
            if (eventHistory.size() > 30) {
                eventHistory.remove(0);
            }
            webSocketHandler.broadcast("EVENT", event);
        });
    }

    private void broadcastMetricsPeriodically() {
        try {
            SentimentMetrics metrics = fluctuationDetector.calculateMetrics();

            // Ring buffer saklama (Grafik için son 60 saniye)
            metricHistory.add(metrics);
            while (metricHistory.size() > 60) {
                metricHistory.remove(0);
            }

            // WebSocket ile ön yüze fırlat
            webSocketHandler.broadcast("METRICS", metrics);
        } catch (Exception e) {
            log.error("Error broadcasting periodic metrics: {}", e.getMessage());
        }
    }

    public void setSimulatorMode(String mode) {
        simulatorChatConnector.setMode(mode);
    }

    public StreamConfig getCurrentConfig() {
        return currentConfig;
    }

    public List<SentimentMetrics> getMetricHistory() {
        return new ArrayList<>(metricHistory);
    }

    public List<EmotionalEvent> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }

    public List<ChatMessage> getRecentMessages() {
        return new ArrayList<>(recentMessages);
    }

    public SentimentMetrics getCurrentMetrics() {
        return fluctuationDetector.calculateMetrics();
    }

    @PreDestroy
    public void cleanup() {
        stopCurrentStream();
        if (metricsBroadcastScheduler != null) {
            metricsBroadcastScheduler.shutdownNow();
        }
    }
}