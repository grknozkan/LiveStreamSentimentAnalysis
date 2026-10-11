package com.duygu.analiz.controller;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.EmotionalEvent;
import com.duygu.analiz.model.SentimentMetrics;
import com.duygu.analiz.model.StreamConfig;
import com.duygu.analiz.service.StreamAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final StreamAnalysisService analysisService;

    public ApiController(StreamAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "config", analysisService.getCurrentConfig(),
                "metrics", analysisService.getCurrentMetrics()
        ));
    }

    @PostMapping("/stream/start")
    public ResponseEntity<Map<String, Object>> startStream(@RequestBody StreamConfig config) {
        analysisService.startStream(config);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Akış başarıyla başlatıldı: " + config.getPlatform(),
                "config", analysisService.getCurrentConfig()
        ));
    }

    @PostMapping("/stream/stop")
    public ResponseEntity<Map<String, Object>> stopStream() {
        analysisService.stopCurrentStream();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Canlı akış analizi durduruldu."
        ));
    }

    @PostMapping("/simulator/mode")
    public ResponseEntity<Map<String, Object>> setSimulatorMode(@RequestParam String mode) {
        analysisService.setSimulatorMode(mode);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "mode", mode,
                "message", "Simülatör modu güncellendi: " + mode
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<List<SentimentMetrics>> getMetricHistory() {
        return ResponseEntity.ok(analysisService.getMetricHistory());
    }

    @GetMapping("/events")
    public ResponseEntity<List<EmotionalEvent>> getEvents() {
        return ResponseEntity.ok(analysisService.getEventHistory());
    }

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessage>> getRecentMessages() {
        return ResponseEntity.ok(analysisService.getRecentMessages());
    }
}