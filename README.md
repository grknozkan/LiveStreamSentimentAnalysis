# 🎙️ SentinelPulse — Canlı Yayın Duygu Dalgalanması Analiz Sistemi

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-Real--time-blue?style=for-the-badge&logo=websocket)
![Twitch](https://img.shields.io/badge/Twitch-IRC-9146FF?style=for-the-badge&logo=twitch&logoColor=white)
![YouTube](https://img.shields.io/badge/YouTube-Data_API_v3-FF0000?style=for-the-badge&logo=youtube&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Canlı yayın sohbetlerindeki duygusal dalgalanmaları gerçek zamanlı olarak tespit eden, analiz eden ve görselleştiren ileri düzey bir analitik platformu.**

[🚀 Özellikler](#-özellikler) · [🏗️ Mimari](#️-mimari) · [⚡ Kurulum](#-kurulum) · [🎮 Kullanım](#-kullanım) · [📡 API](#-api-referansı)

</div>

---

## 📸 Ekran Görüntüleri

> Dashboard — Gerçek zamanlı duygu grafiği, KPI metrikleri, canlı chat akışı ve olay zaman çizelgesi

```
┌─────────────────────────────────────────────────────────────────┐
│  🔴 SentinelPulse  │  Canlı Bağlantı Aktif  │  🎮 Simülatör    │
├──────────────────┬──────────────────────────────────────────────┤
│ Yayın Kontrolü   │   Anlık Skor │ Volatilite │ Duygu │ Hacim   │
│ ──────────────── │  ──────────────────────────────────────────  │
│ 🧪 Simülatör     │         📈 Canlı Duygu Dalgalanması Grafiği  │
│ 🟣 Twitch        │   (EMA, Anlık Skor, ±σ Dalgalanma Koridoru) │
│ 🔴 YouTube       │  ──────────────────────────────────────────  │
│ ──────────────── │       ⚡ Tespit Edilen Duygusal Kırılmalar   │
│ Anlık Chat Akışı │   🚀 Hype Spike  💥 Rage  ⚡ Polarizasyon   │
└──────────────────┴──────────────────────────────────────────────┘
```

---

## 🚀 Özellikler

### 🧠 Duygu Analizi Motoru
- **Türkçe Lexicon Tabanlı NLP** — 150+ Türkçe kelime, gaming slang ve emoji desteği
- **Negasyon & Yoğunlaştırıcı İşleme** — "değil", "çok", "aşırı" gibi kelimelerin bağlamsal yorumu
- **Büyük Harf / Bağırma Tespiti** — Sohbetteki duygusal yoğunluğun ölçülmesi
- **Tekrarlayan Karakter Normalizasyonu** — "efsaneeee" → "efsane" eşleştirmesi

### 📊 İstatistiksel Dalgalanma Motoru
| Metrik | Açıklama |
|--------|----------|
| **Anlık Ortalama** | Son mesaj penceresinin ham duygu ortalaması |
| **EMA (α=0.25)** | Exponential Moving Average — gürültüyü filtreleyen yumuşatılmış eğri |
| **Volatilite İndeksi (σ)** | Duygu dağılımının standart sapması — tartışma/polarizasyon göstergesi |
| **Sentiment Velocity (dS/dt)** | Duygu değişim hızı — ani kırılma öncü sinyali |
| **Mesaj/Saniye** | Chat akış yoğunluğu |
| **Dağılım Oranları** | %Pozitif / %Nötr / %Negatif dağılımı |

### 🔔 Anomali & Olay Tespiti
- 🚀 **HYPE_SPIKE** — EMA ≥ 0.35 ve velocity > 0.08 → Ani coşku dalgası
- 💥 **RAGE_SPIKE** — EMA ≤ -0.30 ve velocity < -0.08 → Öfke/kriz patlaması
- ⚡ **CONTROVERSY** — Volatilite ≥ 0.40 → Sohbet ikiye bölünmesi (polarizasyon)

### 🔌 Platform Connectorları
| Platform | Bağlantı Yöntemi | API Gereksinimi |
|----------|-----------------|-----------------|
| **Twitch** | Anonim IRC WebSocket (`wss://irc-ws.chat.twitch.tv`) | ❌ Yok |
| **YouTube** | YouTube Data API v3 REST Polling | ✅ Google API Key |
| **Simülatör** | Dahili senaryo tabanlı mesaj üretici | ❌ Yok |

### 🖥️ Gerçek Zamanlı Dashboard
- Canvas tabanlı canlı duygu dalgalanması grafiği (EMA + volatilite koridoru)
- Anlık sohbet mesajı akışı (renk kodlu duygu etiketiyle)
- KPI metrik kartları (animasyonlu güncelleme)
- Olay/anomali zaman çizelgesi (timestamp, tür, tetikleyen mesaj)

---

## 🏗️ Mimari

```
┌─────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Browser)                          │
│   index.html + style.css + app.js  ←──── WebSocket (/ws/live-sentiment) │
└─────────────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │  Spring Boot Server   │
                    │     Port: 8080        │
                    └───────────┬───────────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
          ▼                     ▼                     ▼
  StreamAnalysisService   ApiController        WebSocketConfig
          │               (REST /api/*)      (/ws/live-sentiment)
          │
    ┌─────┼───────────────────────────────┐
    │     │                               │
    ▼     ▼                               ▼
Twitch  YouTube                      Simulator
Connector Connector                  Connector
(IRC WS) (REST Poll/4s)              (ScheduledTask)
    │     │                               │
    └─────┴───────────────────────────────┘
                      │
                      ▼ ChatMessage
          ┌───────────────────────────┐
          │     SentimentAnalyzer     │  ← Türkçe Lexicon NLP
          │  score ∈ [-1.0, +1.0]    │
          └───────────┬───────────────┘
                      │
          ┌───────────▼───────────────┐
          │   FluctuationDetector     │  ← EMA, Volatilite, Velocity
          │   Anomali Tespit Motoru   │
          └───────────┬───────────────┘
                      │
          ┌───────────▼───────────────┐
          │  SentimentWebSocketHandler│  → Browser'a Broadcast
          │  broadcast(METRICS|CHAT|EVENT) │
          └───────────────────────────┘
```

### 📁 Proje Yapısı

```
src/
├── main/
│   ├── java/com/duygu/analiz/
│   │   ├── LiveStreamSentimentAnalysisApplication.java   # Uygulama başlangıç noktası
│   │   ├── connector/
│   │   │   ├── StreamConnector.java          # Platform connector arayüzü
│   │   │   ├── TwitchChatConnector.java      # Twitch IRC WebSocket connector
│   │   │   ├── YouTubeChatConnector.java     # YouTube Data API connector
│   │   │   └── SimulatorChatConnector.java   # Test simülatörü
│   │   ├── sentiment/
│   │   │   └── SentimentAnalyzer.java        # NLP duygu analiz motoru
│   │   ├── fluctuation/
│   │   │   └── FluctuationDetector.java      # EMA/Volatilite/Anomali motoru
│   │   ├── service/
│   │   │   └── StreamAnalysisService.java    # Ana iş akışı orchestrator
│   │   ├── websocket/
│   │   │   ├── SentimentWebSocketHandler.java # WS mesaj yayıncısı
│   │   │   └── WebSocketConfig.java          # WS endpoint yapılandırması
│   │   ├── controller/
│   │   │   └── ApiController.java            # REST API endpoint'leri
│   │   └── model/
│   │       ├── ChatMessage.java              # Sohbet mesajı veri modeli
│   │       ├── SentimentMetrics.java         # Metrik anlık görüntü modeli
│   │       ├── EmotionalEvent.java           # Anomali olay modeli
│   │       └── StreamConfig.java             # Yayın yapılandırma modeli
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html                    # Dashboard ana sayfa
│           ├── style.css                     # Premium dark-mode UI
│           └── app.js                        # WebSocket + Canvas grafik
```

---

## ⚡ Kurulum

### Gereksinimler
- **Java 21+** (`java -version` ile kontrol edin)
- **Maven** (proje içinde `mvnw` wrapper mevcut)

### 1. Repoyu Klonlayın

```bash
git clone https://github.com/grknozkan/LiveStreamSentimentAnalysis.git
cd LiveStreamSentimentAnalysis
```

### 2. Uygulamayı Başlatın

```bash
# Windows
mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### 3. Dashboard'ı Açın

Tarayıcınızda şu adresi ziyaret edin:

```
http://localhost:8080
```

Uygulama başlar başlamaz **Simülatör** otomatik olarak devreye girer ve anlık veriler akmaya başlar.

---

## 🎮 Kullanım

### Simülatör Modları
Dashboard'un sol panelindeki butonlarla senaryo tetikleyebilirsiniz:

| Buton | Mod | Açıklama |
|-------|-----|----------|
| 🔥 Coşku Patlaması | `HYPE` | Sürekli pozitif gaming mesajları |
| 💥 Öfke / Kriz | `RAGE` | Sürekli negatif/öfkeli mesajlar |
| ⚡ Çatışma / Bölünme | `CONTROVERSY` | Karışık görüş mesajları |
| 🧘 Sakin Sohbet | `NORMAL` | Nötr günlük sohbet mesajları |
| 🔄 Otomatik Dalgalanma | `AUTO_CYCLE` | Her 35 mesajda senaryo otomatik değişir |

### Twitch Kanalına Bağlanma
1. **Twitch** sekmesine tıklayın
2. Kanal adını girin (örn: `elraenn`, `wtcn`, `shroud`)
3. **Akışı Başlat** butonuna tıklayın
4. API anahtarı gerekmez — anonim IRC bağlantısı kullanılır

### YouTube Canlı Yayınına Bağlanma
1. **YouTube** sekmesine tıklayın
2. `liveChatId` değerini girin (YouTube Data API'den alınır)
3. Google API anahtarınızı girin (`AIzaSy...`)
4. **Akışı Başlat** butonuna tıklayın

---

## 📡 API Referansı

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/status` | Mevcut config ve metrikler |
| `POST` | `/api/stream/start` | Yeni akış başlat (body: `StreamConfig`) |
| `POST` | `/api/stream/stop` | Aktif akışı durdur |
| `POST` | `/api/simulator/mode?mode=HYPE` | Simülatör modunu değiştir |
| `GET` | `/api/history` | Son 60 saniyelik metrik geçmişi |
| `GET` | `/api/events` | Son 30 duygusal olay |
| `GET` | `/api/messages` | Son 50 sohbet mesajı |

### WebSocket Mesaj Formatı

```
ws://localhost:8080/ws/live-sentiment
```

```json
// Metrik güncellemesi (her 1 saniyede)
{ "type": "METRICS", "data": { "movingAverage": 0.42, "volatilityIndex": 0.18, ... } }

// Yeni sohbet mesajı
{ "type": "CHAT", "data": { "username": "AlpGamer", "message": "KRAL ADAMSIN", "sentimentScore": 0.85, "sentimentLabel": "POSITIVE" } }

// Anomali olayı
{ "type": "EVENT", "data": { "type": "HYPE_SPIKE", "title": "🚀 Ani Coşku Dalgası!", "peakScore": 0.61 } }
```

### StreamConfig Formatı

```json
{
  "platform": "TWITCH",       // "TWITCH" | "YOUTUBE" | "SIMULATOR"
  "channelOrVideoId": "elraenn",
  "apiKey": ""                // Yalnızca YouTube için gerekli
}
```

---

## 🔧 Teknoloji Yığını

| Katman | Teknoloji |
|--------|-----------|
| **Backend Framework** | Spring Boot 4.1.1 |
| **Dil** | Java 21 |
| **Gerçek Zamanlı İletişim** | Spring WebSocket (Bare WebSocket — STOMP yok) |
| **REST API** | Spring MVC (`@RestController`) |
| **NLP Motoru** | Özel lexicon tabanlı (harici kütüphane yok) |
| **Frontend** | Vanilla HTML5 + CSS3 + JavaScript (ES2022) |
| **Grafik** | HTML5 Canvas API (framework yok) |
| **Build Aracı** | Apache Maven (wrapper dahil) |
| **Twitch Bağlantısı** | Java 11+ `HttpClient` WebSocket API |
| **YouTube Bağlantısı** | Java 11+ `HttpClient` + Jackson Databind |

---

## 📈 Algoritma Detayları

### Exponential Moving Average (EMA)

```
EMA_t = α × Score_t + (1 - α) × EMA_{t-1}
α = 0.25
```

### Volatilite İndeksi (Standart Sapma)

```
σ = √( Σ(score_i - mean)² / N )
```

### Sentiment Velocity (Türev)

```
velocity = (EMA_t - EMA_{t-1}) / Δt   [per second]
```

---

## 🤝 Katkıda Bulunma

1. Bu repoyu fork edin
2. Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -m 'feat: yeni özellik eklendi'`)
4. Branch'inizi push edin (`git push origin feature/yeni-ozellik`)
5. Pull Request açın

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakınız.

---

<div align="center">

**SentinelPulse** — Canlı yayın kalabalığının nabzını gerçek zamanlı hissediyoruz.

Made with ❤️ by [@grknozkan](https://github.com/grknozkan)

</div>
