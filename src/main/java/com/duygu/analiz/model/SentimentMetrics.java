package com.duygu.analiz.model;

public class SentimentMetrics {
    private long timestamp;
    private double averageScore; // Anlık duygu ortalaması (-1.0 ile 1.0)
    private double movingAverage; // Ağırlıklı hareketli ortalama (-1.0 ile 1.0)
    private double volatilityIndex; // Duygu dalgalanma şiddeti (0.0 ile 1.0)
    private double sentimentVelocity; // Duygu değişim hızı (türev dS/dt)
    private String dominantEmotion; // COSKU_HYPE, SAKIN, TARTISMA, KRIZ_TEPKI
    private int totalMessages; // Toplam işlenen mesaj sayısı
    private double messagesPerSecond; // Saniye başına mesaj akış hızı
    private double positiveRatio; // % pozitif
    private double neutralRatio; // % nötr
    private double negativeRatio; // % negatif
    private boolean anomalyDetected; // Ani kırılma / dalgalanma uyarısı
    private String alertMessage; // Dalgalanma açıklama mesajı

    public SentimentMetrics() {}

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    public double getMovingAverage() { return movingAverage; }
    public void setMovingAverage(double movingAverage) { this.movingAverage = movingAverage; }
    public double getVolatilityIndex() { return volatilityIndex; }
    public void setVolatilityIndex(double volatilityIndex) { this.volatilityIndex = volatilityIndex; }
    public double getSentimentVelocity() { return sentimentVelocity; }
    public void setSentimentVelocity(double sentimentVelocity) { this.sentimentVelocity = sentimentVelocity; }
    public String getDominantEmotion() { return dominantEmotion; }
    public void setDominantEmotion(String dominantEmotion) { this.dominantEmotion = dominantEmotion; }
    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
    public double getMessagesPerSecond() { return messagesPerSecond; }
    public void setMessagesPerSecond(double messagesPerSecond) { this.messagesPerSecond = messagesPerSecond; }
    public double getPositiveRatio() { return positiveRatio; }
    public void setPositiveRatio(double positiveRatio) { this.positiveRatio = positiveRatio; }
    public double getNeutralRatio() { return neutralRatio; }
    public void setNeutralRatio(double neutralRatio) { this.neutralRatio = neutralRatio; }
    public double getNegativeRatio() { return negativeRatio; }
    public void setNegativeRatio(double negativeRatio) { this.negativeRatio = negativeRatio; }
    public boolean isAnomalyDetected() { return anomalyDetected; }
    public void setAnomalyDetected(boolean anomalyDetected) { this.anomalyDetected = anomalyDetected; }
    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
}