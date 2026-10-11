package com.duygu.analiz.model;

public class StreamConfig {
    private String platform = "SIMULATOR"; // TWITCH, YOUTUBE, SIMULATOR
    private String channelOrVideoId = "demo_stream";
    private String apiKey = "";
    private boolean running = false;

    public StreamConfig() {}

    public StreamConfig(String platform, String channelOrVideoId, String apiKey, boolean running) {
        this.platform = platform;
        this.channelOrVideoId = channelOrVideoId;
        this.apiKey = apiKey;
        this.running = running;
    }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getChannelOrVideoId() { return channelOrVideoId; }
    public void setChannelOrVideoId(String channelOrVideoId) { this.channelOrVideoId = channelOrVideoId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }
}