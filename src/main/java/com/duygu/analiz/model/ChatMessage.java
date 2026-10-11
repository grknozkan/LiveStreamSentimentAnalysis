package com.duygu.analiz.model;

import java.util.ArrayList;
import java.util.List;

public class ChatMessage {
    private String id;
    private String platform; // TWITCH, YOUTUBE, SIMULATOR
    private String channel;
    private String username;
    private String message;
    private long timestamp;
    private double sentimentScore; // -1.0 to +1.0
    private String sentimentLabel; // POSITIVE, NEUTRAL, NEGATIVE
    private List<String> detectedKeywords = new ArrayList<>();
    private double hypeLevel; // 0.0 to 1.0

    public ChatMessage() {}

    public ChatMessage(String id, String platform, String channel, String username, String message, 
                       long timestamp, double sentimentScore, String sentimentLabel, 
                       List<String> detectedKeywords, double hypeLevel) {
        this.id = id;
        this.platform = platform;
        this.channel = channel;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.sentimentScore = sentimentScore;
        this.sentimentLabel = sentimentLabel;
        this.detectedKeywords = detectedKeywords != null ? detectedKeywords : new ArrayList<>();
        this.hypeLevel = hypeLevel;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }
    public String getSentimentLabel() { return sentimentLabel; }
    public void setSentimentLabel(String sentimentLabel) { this.sentimentLabel = sentimentLabel; }
    public List<String> getDetectedKeywords() { return detectedKeywords; }
    public void setDetectedKeywords(List<String> detectedKeywords) { this.detectedKeywords = detectedKeywords; }
    public double getHypeLevel() { return hypeLevel; }
    public void setHypeLevel(double hypeLevel) { this.hypeLevel = hypeLevel; }
}