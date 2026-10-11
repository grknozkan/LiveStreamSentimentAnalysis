package com.duygu.analiz.model;

public class EmotionalEvent {
    private String id;
    private long timestamp;
    private String timeFormatted;
    private String type; // HYPE_SPIKE, RAGE_SPIKE, CONTROVERSY, VOLATILITY_SURGE
    private String title;
    private String description;
    private double peakScore;
    private double volatility;
    private String sampleMessage;

    public EmotionalEvent() {}

    public EmotionalEvent(String id, long timestamp, String timeFormatted, String type, 
                          String title, String description, double peakScore, double volatility, String sampleMessage) {
        this.id = id;
        this.timestamp = timestamp;
        this.timeFormatted = timeFormatted;
        this.type = type;
        this.title = title;
        this.description = description;
        this.peakScore = peakScore;
        this.volatility = volatility;
        this.sampleMessage = sampleMessage;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getTimeFormatted() { return timeFormatted; }
    public void setTimeFormatted(String timeFormatted) { this.timeFormatted = timeFormatted; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPeakScore() { return peakScore; }
    public void setPeakScore(double peakScore) { this.peakScore = peakScore; }
    public double getVolatility() { return volatility; }
    public void setVolatility(double volatility) { this.volatility = volatility; }
    public String getSampleMessage() { return sampleMessage; }
    public void setSampleMessage(String sampleMessage) { this.sampleMessage = sampleMessage; }
}