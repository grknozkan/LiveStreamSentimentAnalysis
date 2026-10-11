package com.duygu.analiz.fluctuation;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.EmotionalEvent;
import com.duygu.analiz.model.SentimentMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FluctuationDetectorTest {

    private FluctuationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new FluctuationDetector();
    }

    private ChatMessage createMessage(double score, String text) {
        ChatMessage msg = new ChatMessage();
        msg.setId(java.util.UUID.randomUUID().toString());
        msg.setMessage(text);
        msg.setSentimentScore(score);
        msg.setPlatform("SIMULATOR");
        msg.setUsername("test_user");
        msg.setTimestamp(System.currentTimeMillis());
        return msg;
    }

    @Test
    @DisplayName("Boş mesaj penceresinde varsayılan nötr metrikler dönmeli")
    void testEmptyWindowMetrics() {
        SentimentMetrics metrics = detector.calculateMetrics();
        assertNotNull(metrics);
        assertEquals(0.0, metrics.getAverageScore());
        assertEquals("SAKIN", metrics.getDominantEmotion());
        assertEquals(100.0, metrics.getNeutralRatio());
    }

    @Test
    @DisplayName("Pozitif mesajlar eklendiğinde ortalama ve oranlar doğru hesaplanmalı")
    void testPositiveStreamMetrics() {
        for (int i = 0; i < 15; i++) {
            detector.addMessageAndCheckEvent(createMessage(0.8, "Harika oyun"));
        }

        SentimentMetrics metrics = detector.calculateMetrics();
        assertTrue(metrics.getAverageScore() > 0.5);
        assertTrue(metrics.getPositiveRatio() > 80.0);
        assertEquals(0.0, metrics.getNegativeRatio());
    }

    @Test
    @DisplayName("Zıt duyguların karışımı yüksek volatilite üretmeli")
    void testVolatilityCalculation() {
        for (int i = 0; i < 20; i++) {
            if (i % 2 == 0) {
                detector.addMessageAndCheckEvent(createMessage(0.9, "Mükemmel"));
            } else {
                detector.addMessageAndCheckEvent(createMessage(-0.9, "Berbat"));
            }
        }

        SentimentMetrics metrics = detector.calculateMetrics();
        assertTrue(metrics.getVolatilityIndex() > 0.4, "Zıt kutuplar yüksek volatilite üretmeli: " + metrics.getVolatilityIndex());
    }
}
