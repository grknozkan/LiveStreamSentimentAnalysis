package com.duygu.analiz.sentiment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SentimentAnalyzerTest {

    private SentimentAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SentimentAnalyzer();
    }

    @Test
    @DisplayName("Pozitif Türkçe ifadeler pozitif skor üretmeli")
    void testPositiveSentiment() {
        SentimentAnalyzer.AnalysisResult result = analyzer.analyze("Harika bir yayın tebrikler süper oyun");
        assertTrue(result.getScore() > 0.3, "Skor pozitif olmalı, güncel: " + result.getScore());
        assertEquals("POSITIVE", result.getLabel());
        assertTrue(result.getDetectedKeywords().contains("harika") || result.getDetectedKeywords().contains("super"));
    }

    @Test
    @DisplayName("Negatif ifadeler negatif skor üretmeli")
    void testNegativeSentiment() {
        SentimentAnalyzer.AnalysisResult result = analyzer.analyze("Berbat bir oyun rezalet çok kötü");
        assertTrue(result.getScore() < -0.3, "Skor negatif olmalı, güncel: " + result.getScore());
        assertEquals("NEGATIVE", result.getLabel());
        assertTrue(result.getDetectedKeywords().contains("berbat") || result.getDetectedKeywords().contains("rezalet"));
    }

    @Test
    @DisplayName("Gaming slang (hype, ez, tilt, bot) doğru tespit edilmeli")
    void testGamingSlang() {
        SentimentAnalyzer.AnalysisResult hypeResult = analyzer.analyze("Efsane oynadın gg ez");
        assertTrue(hypeResult.getScore() > 0.2);

        SentimentAnalyzer.AnalysisResult tiltResult = analyzer.analyze("Çok tilt oldum satıcı bot");
        assertTrue(tiltResult.getScore() < -0.2);
    }

    @Test
    @DisplayName("Negasyon (değil, yok) tersine çevirmeli")
    void testNegationHandling() {
        SentimentAnalyzer.AnalysisResult result = analyzer.analyze("Bu oyun güzel değil");
        // "güzel değil" negatife çekilmeli
        assertTrue(result.getScore() <= 0.0, "Negasyon sonrası skor pozitif olmamalı: " + result.getScore());
    }

    @Test
    @DisplayName("Büyük harfle yazılan (bağırma) mesajlarda yoğunluk artmalı")
    void testShoutingMultiplier() {
        SentimentAnalyzer.AnalysisResult normal = analyzer.analyze("harika efsane");
        SentimentAnalyzer.AnalysisResult shouted = analyzer.analyze("HARİKA EFSANE!!!");

        assertTrue(shouted.getHypeLevel() >= normal.getHypeLevel());
    }

    @Test
    @DisplayName("Boş veya anlamsız mesajlar nötr dönmeli")
    void testNeutralEmptyMessage() {
        SentimentAnalyzer.AnalysisResult emptyResult = analyzer.analyze("");
        assertEquals(0.0, emptyResult.getScore());
        assertEquals("NEUTRAL", emptyResult.getLabel());

        SentimentAnalyzer.AnalysisResult randomResult = analyzer.analyze("masanın üstünde bir bardak var");
        assertEquals("NEUTRAL", randomResult.getLabel());
    }
}
