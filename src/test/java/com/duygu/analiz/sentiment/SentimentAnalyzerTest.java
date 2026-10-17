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
    @DisplayName("İngilizce pozitif ve negatif ifadeler doğru analiz edilmeli")
    void testEnglishSentiment() {
        SentimentAnalyzer.AnalysisResult posResult = analyzer.analyze("This play was totally awesome and incredible win");
        assertTrue(posResult.getScore() > 0.4, "İngilizce pozitif skor yüksek olmalı: " + posResult.getScore());
        assertEquals("POSITIVE", posResult.getLabel());

        SentimentAnalyzer.AnalysisResult negResult = analyzer.analyze("What an awful terrible trash game");
        assertTrue(negResult.getScore() < -0.4, "İngilizce negatif skor düşük olmalı: " + negResult.getScore());
        assertEquals("NEGATIVE", negResult.getLabel());

        SentimentAnalyzer.AnalysisResult notGood = analyzer.analyze("This stream is not good");
        assertTrue(notGood.getScore() <= 0.0, "İngilizce negasyon ('not good') negatife çevirmeli: " + notGood.getScore());
    }

    @Test
    @DisplayName("Farklı diller (İspanyolca, Almanca, Fransızca) doğru analiz edilmeli")
    void testMultilingualSentiment() {
        // İspanyolca
        SentimentAnalyzer.AnalysisResult esResult = analyzer.analyze("Muy bueno genial victoria vamos crack");
        assertTrue(esResult.getScore() > 0.3, "İspanyolca pozitif tespit edilmeli");
        assertEquals("POSITIVE", esResult.getLabel());

        // Almanca
        SentimentAnalyzer.AnalysisResult deResult = analyzer.analyze("Das Spiel ist furchtbar schlecht und schrecklich");
        assertTrue(deResult.getScore() < -0.3, "Almanca negatif tespit edilmeli");
        assertEquals("NEGATIVE", deResult.getLabel());

        // Fransızca
        SentimentAnalyzer.AnalysisResult frResult = analyzer.analyze("C'est magnifique très bien bravo");
        assertTrue(frResult.getScore() > 0.3, "Fransızca pozitif tespit edilmeli");
        assertEquals("POSITIVE", frResult.getLabel());
    }

    @Test
    @DisplayName("Kiril / Rusça ifadeler doğru analiz edilmeli")
    void testRussianSentiment() {
        SentimentAnalyzer.AnalysisResult ruPos = analyzer.analyze("Красава лучший игрок топ огонь");
        assertTrue(ruPos.getScore() > 0.3, "Rusça pozitif tespit edilmeli");
        assertEquals("POSITIVE", ruPos.getLabel());

        SentimentAnalyzer.AnalysisResult ruNeg = analyzer.analyze("Полный мусор руина и дно");
        assertTrue(ruNeg.getScore() < -0.3, "Rusça negatif tespit edilmeli");
        assertEquals("NEGATIVE", ruNeg.getLabel());
    }

    @Test
    @DisplayName("Emojiler ve ASCII emoticonlar skoru etkilemeli")
    void testEmojisAndEmoticons() {
        SentimentAnalyzer.AnalysisResult happy = analyzer.analyze("hello :) <3 🔥");
        assertTrue(happy.getScore() > 0.3);

        SentimentAnalyzer.AnalysisResult sad = analyzer.analyze("sad :(");
        assertTrue(sad.getScore() < 0.0);
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
