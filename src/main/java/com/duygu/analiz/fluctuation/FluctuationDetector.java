package com.duygu.analiz.fluctuation;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.EmotionalEvent;
import com.duygu.analiz.model.SentimentMetrics;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FluctuationDetector {

    private static final int WINDOW_SIZE = 60; // Son 60 mesaj veya zaman dilimi
    private static final double HYPE_THRESHOLD = 0.35;
    private static final double RAGE_THRESHOLD = -0.30;
    private static final double VOLATILITY_HIGH_THRESHOLD = 0.40;

    private final Deque<ChatMessage> messageWindow = new ConcurrentLinkedDeque<>();
    private final AtomicInteger totalProcessedCount = new AtomicInteger(0);

    private double lastMovingAverage = 0.0;
    private long lastCalculationTime = System.currentTimeMillis();
    private long lastEventTriggerTime = 0;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public synchronized Optional<EmotionalEvent> addMessageAndCheckEvent(ChatMessage message) {
        totalProcessedCount.incrementAndGet();
        messageWindow.addLast(message);

        while (messageWindow.size() > WINDOW_SIZE) {
            messageWindow.removeFirst();
        }

        // Anomaly / Spike event detection (en az 5 saniye arayla tetiklenir)
        long now = System.currentTimeMillis();
        if (now - lastEventTriggerTime > 5000 && messageWindow.size() >= 10) {
            SentimentMetrics metrics = calculateMetrics();

            // 1. Ani Coşku / Hype Patlaması
            if (metrics.getMovingAverage() >= HYPE_THRESHOLD && metrics.getSentimentVelocity() > 0.08) {
                lastEventTriggerTime = now;
                return Optional.of(new EmotionalEvent(
                        UUID.randomUUID().toString(),
                        now,
                        timeFormatter.format(Instant.ofEpochMilli(now)),
                        "HYPE_SPIKE",
                        "🚀 Canlı Yayında Ani Coşku Dalgası (Hype Spike)!",
                        String.format("Duygu ortalaması hızla yükseldi: %.2f (Değişim hızı: +%.2f)",
                                metrics.getMovingAverage(), metrics.getSentimentVelocity()),
                        metrics.getMovingAverage(),
                        metrics.getVolatilityIndex(),
                        message.getMessage()
                ));
            }

            // 2. Ani Öfke / Reaksiyon / Kriz
            if (metrics.getMovingAverage() <= RAGE_THRESHOLD && metrics.getSentimentVelocity() < -0.08) {
                lastEventTriggerTime = now;
                return Optional.of(new EmotionalEvent(
                        UUID.randomUUID().toString(),
                        now,
                        timeFormatter.format(Instant.ofEpochMilli(now)),
                        "RAGE_SPIKE",
                        "💥 Ani Tepki ve Öfke Dalgası (Rage Spike)!",
                        String.format("Duygu ortalaması sert düşüş yaşadı: %.2f (Değişim hızı: %.2f)",
                                metrics.getMovingAverage(), metrics.getSentimentVelocity()),
                        metrics.getMovingAverage(),
                        metrics.getVolatilityIndex(),
                        message.getMessage()
                ));
            }

            // 3. Yüksek Polarizasyon / Tartışma (Aşırı Dalgalanma)
            if (metrics.getVolatilityIndex() >= VOLATILITY_HIGH_THRESHOLD && messageWindow.size() >= 15) {
                lastEventTriggerTime = now;
                return Optional.of(new EmotionalEvent(
                        UUID.randomUUID().toString(),
                        now,
                        timeFormatter.format(Instant.ofEpochMilli(now)),
                        "CONTROVERSY",
                        "⚡ Sohbet İkiye Bölündü (Şiddetli Dalgalanma / Polarizasyon)",
                        String.format("Duygu dalgalanma katsayısı tavan yaptı: %.2f (Zıt duygular çatışıyor)",
                                metrics.getVolatilityIndex()),
                        metrics.getMovingAverage(),
                        metrics.getVolatilityIndex(),
                        message.getMessage()
                ));
            }
        }

        return Optional.empty();
    }

    public synchronized SentimentMetrics calculateMetrics() {
        long now = System.currentTimeMillis();
        SentimentMetrics metrics = new SentimentMetrics();
        metrics.setTimestamp(now);
        metrics.setTotalMessages(totalProcessedCount.get());

        if (messageWindow.isEmpty()) {
            metrics.setAverageScore(0.0);
            metrics.setMovingAverage(0.0);
            metrics.setVolatilityIndex(0.0);
            metrics.setSentimentVelocity(0.0);
            metrics.setDominantEmotion("SAKIN");
            metrics.setPositiveRatio(0.0);
            metrics.setNeutralRatio(100.0);
            metrics.setNegativeRatio(0.0);
            metrics.setMessagesPerSecond(0.0);
            metrics.setAnomalyDetected(false);
            return metrics;
        }

        int count = messageWindow.size();
        double sum = 0.0;
        int positiveCount = 0;
        int neutralCount = 0;
        int negativeCount = 0;

        for (ChatMessage m : messageWindow) {
            double s = m.getSentimentScore();
            sum += s;
            if (s >= 0.20) positiveCount++;
            else if (s <= -0.20) negativeCount++;
            else neutralCount++;
        }

        double instantAverage = sum / count;

        // Exponential Moving Average (EMA) with alpha = 0.25
        double alpha = 0.25;
        double movingAverage = (alpha * instantAverage) + ((1 - alpha) * lastMovingAverage);

        // Volatility: Sample standard deviation of scores in window
        double varianceSum = 0.0;
        for (ChatMessage m : messageWindow) {
            double diff = m.getSentimentScore() - instantAverage;
            varianceSum += (diff * diff);
        }
        double volatility = Math.sqrt(varianceSum / count);

        // Velocity: change in moving average over time (delta per second)
        double deltaTimeSec = Math.max(0.5, (now - lastCalculationTime) / 1000.0);
        double velocity = (movingAverage - lastMovingAverage) / deltaTimeSec;

        lastMovingAverage = movingAverage;
        lastCalculationTime = now;

        // Dominant Emotion determination
        String dominantEmotion;
        boolean anomaly = false;
        String alert = null;

        if (movingAverage >= HYPE_THRESHOLD) {
            dominantEmotion = "COSKU_HYPE";
            anomaly = true;
            alert = "🔥 Sohbet coşku dalgası yaşıyor!";
        } else if (movingAverage <= RAGE_THRESHOLD) {
            dominantEmotion = "KRIZ_TEPKI";
            anomaly = true;
            alert = "⚠️ Sohbette ani hoşnutsuzluk/öfke patlaması!";
        } else if (volatility >= VOLATILITY_HIGH_THRESHOLD) {
            dominantEmotion = "TARTISMA";
            anomaly = true;
            alert = "⚡ Sohbet keskin biçimde ikiye bölündü (Tartışma/Polarizasyon)";
        } else {
            dominantEmotion = "SAKIN";
        }

        // Messages per second (over the timestamp spread of current window)
        ChatMessage oldest = messageWindow.peekFirst();
        ChatMessage newest = messageWindow.peekLast();
        double msgPerSec = 0.0;
        if (oldest != null && newest != null && newest.getTimestamp() > oldest.getTimestamp()) {
            double windowDurationSec = (newest.getTimestamp() - oldest.getTimestamp()) / 1000.0;
            if (windowDurationSec > 0) {
                msgPerSec = count / windowDurationSec;
            }
        }

        metrics.setAverageScore(Math.round(instantAverage * 100.0) / 100.0);
        metrics.setMovingAverage(Math.round(movingAverage * 100.0) / 100.0);
        metrics.setVolatilityIndex(Math.round(volatility * 100.0) / 100.0);
        metrics.setSentimentVelocity(Math.round(velocity * 1000.0) / 1000.0);
        metrics.setDominantEmotion(dominantEmotion);
        metrics.setPositiveRatio(Math.round(((double) positiveCount / count) * 1000.0) / 10.0);
        metrics.setNeutralRatio(Math.round(((double) neutralCount / count) * 1000.0) / 10.0);
        metrics.setNegativeRatio(Math.round(((double) negativeCount / count) * 1000.0) / 10.0);
        metrics.setMessagesPerSecond(Math.round(msgPerSec * 10.0) / 10.0);
        metrics.setAnomalyDetected(anomaly);
        metrics.setAlertMessage(alert);

        return metrics;
    }

    public synchronized void reset() {
        messageWindow.clear();
        totalProcessedCount.set(0);
        lastMovingAverage = 0.0;
        lastCalculationTime = System.currentTimeMillis();
        lastEventTriggerTime = 0;
    }
}