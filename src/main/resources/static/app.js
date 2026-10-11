// SentinelPulse - Canlı Yayın Duygu Dalgalanması Analizi
document.addEventListener("DOMContentLoaded", () => {
    // State
    let selectedPlatform = "SIMULATOR";
    let isRunning = true;
    let ws = null;
    let chartHistory = [];
    let eventCount = 0;

    // DOM Elements
    const wsStatusBadge = document.getElementById("wsStatusBadge");
    const wsStatusText = document.getElementById("wsStatusText");
    const activePlatformBadge = document.getElementById("activePlatformBadge");
    const activePlatformIcon = document.getElementById("activePlatformIcon");
    const activeChannelText = document.getElementById("activeChannelText");
    const streamStateBadge = document.getElementById("streamStateBadge");

    const tabBtns = document.querySelectorAll(".tab-btn");
    const inputGroupTwitch = document.getElementById("inputGroupTwitch");
    const inputGroupYoutube = document.getElementById("inputGroupYoutube");
    const inputGroupSimulator = document.getElementById("inputGroupSimulator");

    const twitchChannelInput = document.getElementById("twitchChannelInput");
    const ytLiveChatId = document.getElementById("ytLiveChatId");
    const ytApiKey = document.getElementById("ytApiKey");

    const startStreamBtn = document.getElementById("startStreamBtn");
    const stopStreamBtn = document.getElementById("stopStreamBtn");
    const simActionBtns = document.querySelectorAll(".action-btn");

    const chatStreamList = document.getElementById("chatStreamList");
    const chatSpeedCounter = document.getElementById("chatSpeedCounter");

    // Metrics DOM
    const valSentimentScore = document.getElementById("valSentimentScore");
    const sentimentBadge = document.getElementById("sentimentBadge");
    const valMovingAvg = document.getElementById("valMovingAvg");
    const scoreSliderFill = document.getElementById("scoreSliderFill");

    const valVolatility = document.getElementById("valVolatility");
    const volatilityLevelBadge = document.getElementById("volatilityLevelBadge");
    const volatilityMeterFill = document.getElementById("volatilityMeterFill");
    const valVelocity = document.getElementById("valVelocity");

    const valDominantEmotion = document.getElementById("valDominantEmotion");
    const emotionStateIcon = document.getElementById("emotionStateIcon");
    const valAlertBanner = document.getElementById("valAlertBanner");
    const cardEmotionState = document.getElementById("cardEmotionState");

    const valTotalMessages = document.getElementById("valTotalMessages");
    const distPos = document.getElementById("distPos");
    const distNeu = document.getElementById("distNeu");
    const distNeg = document.getElementById("distNeg");
    const pctPos = document.getElementById("pctPos");
    const pctNeu = document.getElementById("pctNeu");
    const pctNeg = document.getElementById("pctNeg");

    const eventsTimelineList = document.getElementById("eventsTimelineList");
    const eventCountBadge = document.getElementById("eventCountBadge");

    // Canvas Setup
    const canvas = document.getElementById("sentimentCanvas");
    const ctx = canvas.getContext("2d");

    // Handle high DPI displays
    function resizeCanvas() {
        const rect = canvas.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;
        canvas.width = rect.width * dpr;
        canvas.height = rect.height * dpr;
        ctx.scale(dpr, dpr);
    }
    window.addEventListener("resize", () => {
        resizeCanvas();
        drawChart();
    });
    resizeCanvas();

    // Theme Switcher Logic
    const themeButtons = document.querySelectorAll(".theme-btn");
    const savedTheme = localStorage.getItem("sentinel_theme") || "solar-flare";
    applyTheme(savedTheme);

    themeButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const theme = btn.dataset.theme;
            applyTheme(theme);
            localStorage.setItem("sentinel_theme", theme);
            drawChart();
        });
    });

    function applyTheme(themeName) {
        document.body.dataset.theme = themeName;
        themeButtons.forEach(b => {
            if (b.dataset.theme === themeName) {
                b.classList.add("active");
            } else {
                b.classList.remove("active");
            }
        });
    }

    // Setup Tabs
    tabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            tabBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");
            selectedPlatform = btn.dataset.platform;

            inputGroupTwitch.style.display = selectedPlatform === "TWITCH" ? "flex" : "none";
            inputGroupYoutube.style.display = selectedPlatform === "YOUTUBE" ? "flex" : "none";
            inputGroupSimulator.style.display = selectedPlatform === "SIMULATOR" ? "flex" : "none";
        });
    });

    // Simulator Action Buttons
    simActionBtns.forEach(btn => {
        btn.addEventListener("click", async () => {
            const mode = btn.dataset.mode;
            simActionBtns.forEach(b => b.classList.remove("active"));
            btn.classList.add("active");

            try {
                await fetch(`/api/simulator/mode?mode=${mode}`, { method: "POST" });
            } catch (err) {
                console.error("Failed to set simulator mode:", err);
            }
        });
    });

    // Start / Stop Stream
    startStreamBtn.addEventListener("click", async () => {
        let channel = "";
        let key = "";

        if (selectedPlatform === "TWITCH") {
            channel = twitchChannelInput.value.trim() || "elraenn";
        } else if (selectedPlatform === "YOUTUBE") {
            channel = ytLiveChatId.value.trim();
            key = ytApiKey.value.trim();
        } else {
            channel = "demo_stream";
        }

        const payload = {
            platform: selectedPlatform,
            channelOrVideoId: channel,
            apiKey: key,
            running: true
        };

        try {
            startStreamBtn.disabled = true;
            const res = await fetch("/api/stream/start", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });
            const data = await res.json();
            if (data.success) {
                isRunning = true;
                updatePlatformDisplay(selectedPlatform, channel);
            }
        } catch (err) {
            console.error("Start stream failed:", err);
        } finally {
            startStreamBtn.disabled = false;
        }
    });

    stopStreamBtn.addEventListener("click", async () => {
        try {
            await fetch("/api/stream/stop", { method: "POST" });
            isRunning = false;
            streamStateBadge.textContent = "Durduruldu";
            streamStateBadge.style.color = "#ef4444";
            streamStateBadge.style.borderColor = "rgba(239, 68, 68, 0.3)";
        } catch (err) {
            console.error("Stop stream failed:", err);
        }
    });

    function updatePlatformDisplay(platform, channel) {
        streamStateBadge.textContent = "Çalışıyor";
        streamStateBadge.style.color = "var(--color-pos)";
        streamStateBadge.style.borderColor = "rgba(16, 185, 129, 0.3)";

        if (platform === "TWITCH") {
            activePlatformIcon.textContent = "🟣";
            activeChannelText.textContent = `Twitch: #${channel}`;
        } else if (platform === "YOUTUBE") {
            activePlatformIcon.textContent = "🔴";
            activeChannelText.textContent = `YouTube: ${channel || "Canlı"}`;
        } else {
            activePlatformIcon.textContent = "🎮";
            activeChannelText.textContent = "Simülatör: Test Akışı";
        }
    }

    // Connect WebSocket
    function connectWebSocket() {
        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const wsUrl = `${protocol}//${window.location.host}/ws/live-sentiment`;

        ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            wsStatusBadge.querySelector(".status-indicator").className = "status-indicator live";
            wsStatusText.textContent = "Canlı Bağlantı Aktif";
        };

        ws.onmessage = (event) => {
            try {
                const message = JSON.parse(event.data);
                handleWsMessage(message);
            } catch (err) {
                console.error("Error parsing WS message:", err);
            }
        };

        ws.onclose = () => {
            wsStatusBadge.querySelector(".status-indicator").className = "status-indicator offline";
            wsStatusText.textContent = "Bağlantı Kesildi (Yeniden deneniyor...)";
            setTimeout(connectWebSocket, 3000);
        };

        ws.onerror = (err) => {
            console.warn("WebSocket error:", err);
            ws.close();
        };
    }

    function handleWsMessage(msg) {
        if (msg.type === "METRICS") {
            updateMetrics(msg.data);
        } else if (msg.type === "CHAT") {
            appendChatMessage(msg.data);
        } else if (msg.type === "EVENT") {
            appendEmotionalEvent(msg.data);
        }
    }

    function updateMetrics(m) {
        if (!m) return;

        // Anlık Skor
        const score = m.averageScore || 0;
        valSentimentScore.textContent = (score > 0 ? "+" : "") + score.toFixed(2);
        valMovingAvg.textContent = (m.movingAverage > 0 ? "+" : "") + (m.movingAverage || 0).toFixed(2);

        // Renk & Rozet
        if (score >= 0.2) {
            sentimentBadge.textContent = "POZİTİF";
            sentimentBadge.className = "metric-badge pos";
            valSentimentScore.style.color = "var(--color-pos)";
        } else if (score <= -0.2) {
            sentimentBadge.textContent = "NEGATİF";
            sentimentBadge.className = "metric-badge neg";
            valSentimentScore.style.color = "var(--color-neg)";
        } else {
            sentimentBadge.textContent = "NÖTR";
            sentimentBadge.className = "metric-badge";
            valSentimentScore.style.color = "var(--text-primary)";
        }

        // Skor Slider Track
        const scorePct = Math.min(100, Math.max(0, (score + 1) * 50));
        if (score >= 0) {
            scoreSliderFill.style.left = "50%";
            scoreSliderFill.style.width = `${(scorePct - 50)}%`;
            scoreSliderFill.style.background = "var(--color-pos)";
        } else {
            scoreSliderFill.style.left = `${scorePct}%`;
            scoreSliderFill.style.width = `${(50 - scorePct)}%`;
            scoreSliderFill.style.background = "var(--color-neg)";
        }

        // Volatilite
        const vol = m.volatilityIndex || 0;
        valVolatility.textContent = vol.toFixed(2);
        valVelocity.textContent = (m.sentimentVelocity > 0 ? "+" : "") + (m.sentimentVelocity || 0).toFixed(2) + "/s";

        const volMeterPct = Math.min(100, vol * 130);
        volatilityMeterFill.style.width = `${volMeterPct}%`;

        if (vol >= 0.40) {
            volatilityLevelBadge.textContent = "AŞIRI YÜKSEK";
            volatilityLevelBadge.className = "metric-badge warn";
        } else if (vol >= 0.25) {
            volatilityLevelBadge.textContent = "ORTA";
            volatilityLevelBadge.className = "metric-badge info";
        } else {
            volatilityLevelBadge.textContent = "DÜŞÜK";
            volatilityLevelBadge.className = "metric-badge pos";
        }

        // Baskın Duygu Durumu
        const emotion = m.dominantEmotion || "SAKIN";
        valDominantEmotion.textContent = emotion.replace("_", " ");

        if (emotion === "COSKU_HYPE") {
            emotionStateIcon.textContent = "🔥";
            valDominantEmotion.style.color = "var(--color-pos)";
            valAlertBanner.textContent = "Sohbet genelinde yoğun coşku ve tezahürat dalgası!";
        } else if (emotion === "KRIZ_TEPKI") {
            emotionStateIcon.textContent = "💥";
            valDominantEmotion.style.color = "var(--color-neg)";
            valAlertBanner.textContent = "Sohbette ani reaksiyon, öfke ve hayal kırıklığı yaşanıyor!";
        } else if (emotion === "TARTISMA") {
            emotionStateIcon.textContent = "⚡";
            valDominantEmotion.style.color = "var(--color-warn)";
            valAlertBanner.textContent = "Sohbet kutuplaştı; zıt duygular sert şekilde çatışıyor!";
        } else {
            emotionStateIcon.textContent = "🧘";
            valDominantEmotion.style.color = "var(--color-cyan)";
            valAlertBanner.textContent = "Normal duygusal denge korunuyor.";
        }

        // Hacim & Dağılım
        valTotalMessages.textContent = (m.totalMessages || 0).toLocaleString();
        chatSpeedCounter.textContent = `${(m.messagesPerSecond || 0).toFixed(1)} msg/s`;

        pctPos.textContent = Math.round(m.positiveRatio || 0);
        pctNeu.textContent = Math.round(m.neutralRatio || 0);
        pctNeg.textContent = Math.round(m.negativeRatio || 0);

        distPos.style.width = `${m.positiveRatio || 0}%`;
        distNeu.style.width = `${m.neutralRatio || 0}%`;
        distNeg.style.width = `${m.negativeRatio || 0}%`;

        // Chart Verisine Ekle (Zaman damgalı akış)
        liveTargetScore = score;
        liveTargetEma = m.movingAverage || 0;
        liveTargetVol = vol;

        const now = Date.now();
        chartHistory.push({
            time: now,
            score: score,
            ema: m.movingAverage || 0,
            volatility: vol
        });

        // 65 saniyeden eski noktaları temizle
        const cutoff = now - TIME_WINDOW_MS - 5000;
        while (chartHistory.length > 0 && chartHistory[0].time < cutoff) {
            chartHistory.shift();
        }
    }

    // Chat Mesajlarını DOM'a Kasmadan Toplu Ekleme (DocumentFragment & RAF)
    const chatBatchQueue = [];
    let chatBatchScheduled = false;

    function appendChatMessage(msg) {
        if (!msg) return;
        chatBatchQueue.push(msg);

        if (!chatBatchScheduled) {
            chatBatchScheduled = true;
            requestAnimationFrame(flushChatQueue);
        }
    }

    function flushChatQueue() {
        chatBatchScheduled = false;
        if (chatBatchQueue.length === 0) return;

        const empty = chatStreamList.querySelector(".empty-chat-state");
        if (empty) empty.remove();

        const fragment = document.createDocumentFragment();
        const batch = chatBatchQueue.splice(0, chatBatchQueue.length);

        for (const msg of batch) {
            const item = document.createElement("div");
            const sentimentClass = msg.sentimentLabel === "POSITIVE" ? "pos" : (msg.sentimentLabel === "NEGATIVE" ? "neg" : "neu");
            item.className = `chat-msg-item ${sentimentClass}`;

            const scoreFormatted = (msg.sentimentScore > 0 ? "+" : "") + msg.sentimentScore.toFixed(2);

            let keywordsHtml = "";
            if (msg.detectedKeywords && msg.detectedKeywords.length > 0) {
                keywordsHtml = `<div class="msg-keywords">` +
                    msg.detectedKeywords.slice(0, 3).map(kw => `<span class="kw-tag">${kw}</span>`).join("") +
                    `</div>`;
            }

            item.innerHTML = `
                <div class="msg-header">
                    <span class="msg-user">${escapeHtml(msg.username)}</span>
                    <span class="msg-score-badge ${sentimentClass}">${scoreFormatted}</span>
                </div>
                <div class="msg-text">${escapeHtml(msg.message)}</div>
                ${keywordsHtml}
            `;
            fragment.appendChild(item);
        }

        chatStreamList.insertBefore(fragment, chatStreamList.firstChild);

        // Keep maximum 40 messages in DOM
        while (chatStreamList.children.length > 40) {
            chatStreamList.removeChild(chatStreamList.lastChild);
        }
    }

    function appendEmotionalEvent(ev) {
        if (!ev) return;

        const empty = eventsTimelineList.querySelector(".empty-events-state");
        if (empty) empty.remove();

        eventCount++;
        eventCountBadge.textContent = `${eventCount} Olay`;

        const item = document.createElement("div");
        item.className = `event-item ${ev.type}`;

        item.innerHTML = `
            <div class="event-time">${ev.timeFormatted}</div>
            <div class="event-content">
                <div class="event-title">${escapeHtml(ev.title)}</div>
                <div class="event-desc">${escapeHtml(ev.description)}</div>
                ${ev.sampleMessage ? `<div class="event-sample">"${escapeHtml(ev.sampleMessage)}"</div>` : ""}
            </div>
        `;

        eventsTimelineList.insertBefore(item, eventsTimelineList.firstChild);

        while (eventsTimelineList.children.length > 25) {
            eventsTimelineList.removeChild(eventsTimelineList.lastChild);
        }
    }

    // 60 FPS Sıfır Kasmayla Sürekli Kayan Ultra-Akıcı Grafik Motoru
    let liveTargetScore = 0;
    let liveTargetEma = 0;
    let liveTargetVol = 0;
    let displayedScore = 0;
    let displayedEma = 0;
    let displayedVol = 0;
    const TIME_WINDOW_MS = 40000; // 40 saniyelik pürüzsüz akıcı zaman penceresi

    function drawChart() {
        const rect = canvas.getBoundingClientRect();
        const width = rect.width;
        const height = rect.height;

        ctx.clearRect(0, 0, width, height);

        const padLeft = 45;
        const padRight = 20;
        const padTop = 25;
        const padBottom = 30;

        const graphW = width - padLeft - padRight;
        const graphH = height - padTop - padBottom;
        const zeroY = padTop + graphH / 2;

        function toY(val) {
            const clamped = Math.max(-1.0, Math.min(1.0, val));
            return zeroY - (clamped * (graphH / 2));
        }

        // Yatay Izgara Çizgileri
        ctx.strokeStyle = "rgba(255, 255, 255, 0.06)";
        ctx.lineWidth = 1;

        const gridLevels = [1.0, 0.5, 0.35, 0.0, -0.30, -0.5, -1.0];
        gridLevels.forEach(lvl => {
            const y = toY(lvl);
            ctx.beginPath();
            ctx.moveTo(padLeft, y);
            ctx.lineTo(width - padRight, y);
            ctx.stroke();

            ctx.fillStyle = lvl === 0 ? "#94a3b8" : (lvl > 0 ? "rgba(16, 185, 129, 0.6)" : "rgba(239, 68, 68, 0.6)");
            ctx.font = "10px 'JetBrains Mono'";
            ctx.textAlign = "right";
            ctx.textBaseline = "middle";
            ctx.fillText((lvl > 0 ? "+" : "") + lvl.toFixed(2), padLeft - 6, y);
        });

        // Kesikli Nötr Çizgisi (0.00)
        ctx.strokeStyle = "rgba(255, 255, 255, 0.2)";
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(padLeft, zeroY);
        ctx.lineTo(width - padRight, zeroY);
        ctx.stroke();
        ctx.setLineDash([]);

        if (chartHistory.length === 0) return;

        // Yumuşak LERP Geçişi (Yaylanma & Dalgalanma giderici)
        displayedScore += (liveTargetScore - displayedScore) * 0.12;
        displayedEma += (liveTargetEma - displayedEma) * 0.12;
        displayedVol += (liveTargetVol - displayedVol) * 0.12;

        const now = Date.now();

        function toX(time) {
            const elapsed = now - time;
            return (width - padRight) - (elapsed / TIME_WINDOW_MS) * graphW;
        }

        // Aktif Tema Renkleri
        const activeTheme = document.body.dataset.theme || "solar-flare";
        let cPos, cMid, cNeg, cGlow, cCorridor, cEma;

        if (activeTheme === "emerald-aurora") {
            cPos = "#05df72";
            cMid = "#10b981";
            cNeg = "#f43f5e";
            cGlow = "rgba(16, 185, 129, 0.35)";
            cCorridor = "rgba(16, 185, 129, 0.08)";
            cEma = "#38bdf8";
        } else if (activeTheme === "cyber-neon") {
            cPos = "#00f2fe";
            cMid = "#a855f7";
            cNeg = "#ff0055";
            cGlow = "rgba(244, 63, 94, 0.4)";
            cCorridor = "rgba(168, 85, 247, 0.1)";
            cEma = "#fbbf24";
        } else if (activeTheme === "titanium-ice") {
            cPos = "#2dd4bf";
            cMid = "#38bdf8";
            cNeg = "#fb7185";
            cGlow = "rgba(56, 189, 248, 0.35)";
            cCorridor = "rgba(56, 189, 248, 0.08)";
            cEma = "#818cf8";
        } else { // solar-flare
            cPos = "#00f5a0";
            cMid = "#ffaa00";
            cNeg = "#ff2a5f";
            cGlow = "rgba(255, 87, 34, 0.4)";
            cCorridor = "rgba(255, 87, 34, 0.09)";
            cEma = "#ff7a45";
        }

        // Ekranda görünen noktaları topla ve canlı ucu ekle
        const visiblePoints = chartHistory.filter(p => toX(p.time) >= padLeft - 30);
        const allPoints = [...visiblePoints];
        allPoints.push({
            time: now,
            score: displayedScore,
            ema: displayedEma,
            volatility: displayedVol
        });

        if (allPoints.length < 2) return;

        // 1. Dalgalanma Koridoru (EMA ± Volatilite Bandı)
        ctx.beginPath();
        for (let i = 0; i < allPoints.length; i++) {
            const x = Math.max(padLeft, Math.min(width - padRight, toX(allPoints[i].time)));
            const upper = allPoints[i].ema + (allPoints[i].volatility || 0);
            const y = toY(upper);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        for (let i = allPoints.length - 1; i >= 0; i--) {
            const x = Math.max(padLeft, Math.min(width - padRight, toX(allPoints[i].time)));
            const lower = allPoints[i].ema - (allPoints[i].volatility || 0);
            const y = toY(lower);
            ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.fillStyle = cCorridor;
        ctx.fill();

        // 2. EMA Eğrisi (Filtrelenmiş Trend Eğrisi)
        ctx.beginPath();
        ctx.strokeStyle = cEma;
        ctx.lineWidth = 2.2;
        for (let i = 0; i < allPoints.length; i++) {
            const x = Math.max(padLeft, Math.min(width - padRight, toX(allPoints[i].time)));
            const y = toY(allPoints[i].ema);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.stroke();

        // 3. Anlık Skor Eğrisi (Yüksek Performanslı GPU Çift-Katman Neon Işıma)
        ctx.beginPath();
        for (let i = 0; i < allPoints.length; i++) {
            const x = Math.max(padLeft, Math.min(width - padRight, toX(allPoints[i].time)));
            const y = toY(allPoints[i].score);
            if (i === 0) ctx.moveTo(x, y);
            else {
                const prevX = Math.max(padLeft, Math.min(width - padRight, toX(allPoints[i - 1].time)));
                const prevY = toY(allPoints[i - 1].score);
                const cpX = (prevX + x) / 2;
                ctx.bezierCurveTo(cpX, prevY, cpX, y, x, y);
            }
        }

        // 1. Katman: Sıfır gecikmeli geniş neon aura (Gecikmeli shadowBlur kullanılmaz)
        ctx.strokeStyle = cGlow;
        ctx.lineWidth = 6.5;
        ctx.stroke();

        // 2. Katman: Keskin degrade çekirdek çizgi
        const gradient = ctx.createLinearGradient(0, padTop, 0, height - padBottom);
        gradient.addColorStop(0, cPos);
        gradient.addColorStop(0.5, cMid);
        gradient.addColorStop(1, cNeg);

        ctx.strokeStyle = gradient;
        ctx.lineWidth = 2.8;
        ctx.stroke();

        // 4. En uçtaki canlı veri noktasında yumuşak nabız efekti
        const tipX = width - padRight;
        const tipY = toY(displayedScore);
        const pulse = 1 + 0.18 * Math.sin(now * 0.007);

        ctx.beginPath();
        ctx.arc(tipX, tipY, 7 * pulse, 0, Math.PI * 2);
        ctx.fillStyle = cGlow;
        ctx.fill();

        ctx.beginPath();
        ctx.arc(tipX, tipY, 4, 0, Math.PI * 2);
        ctx.fillStyle = "#ffffff";
        ctx.fill();
        ctx.strokeStyle = displayedScore >= 0 ? cPos : cNeg;
        ctx.lineWidth = 2.5;
        ctx.stroke();
    }

    // 60 FPS Donma Önleyici Animasyon Döngüsünü Başlat
    function startContinuousChartLoop() {
        function tick() {
            drawChart();
            requestAnimationFrame(tick);
        }
        requestAnimationFrame(tick);
    }
    startContinuousChartLoop();

    function escapeHtml(str) {
        if (!str) return "";
        return str.replace(/&/g, "&amp;")
                  .replace(/</g, "&lt;")
                  .replace(/>/g, "&gt;")
                  .replace(/"/g, "&quot;")
                  .replace(/'/g, "&#039;");
    }

    // Initial Data Fetch
    async function loadInitialData() {
        try {
            const [statusRes, histRes, eventsRes, msgsRes] = await Promise.all([
                fetch("/api/status").then(r => r.json()).catch(() => null),
                fetch("/api/history").then(r => r.json()).catch(() => []),
                fetch("/api/events").then(r => r.json()).catch(() => []),
                fetch("/api/messages").then(r => r.json()).catch(() => [])
            ]);

            if (statusRes && statusRes.config) {
                updatePlatformDisplay(statusRes.config.platform, statusRes.config.channelOrVideoId);
            }

            if (Array.isArray(histRes) && histRes.length > 0) {
                histRes.forEach(item => updateMetrics(item));
            }

            if (Array.isArray(eventsRes)) {
                eventsRes.forEach(ev => appendEmotionalEvent(ev));
            }

            if (Array.isArray(msgsRes)) {
                msgsRes.forEach(msg => appendChatMessage(msg));
            }

        } catch (err) {
            console.error("Initial load error:", err);
        }
    }

    // Initialize
    loadInitialData();
    connectWebSocket();
});