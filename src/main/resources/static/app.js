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

        // Chart Verisine Ekle
        chartHistory.push({
            time: m.timestamp,
            score: score,
            ema: m.movingAverage || 0,
            volatility: vol
        });

        if (chartHistory.length > 60) {
            chartHistory.shift();
        }

        drawChart();
    }

    function appendChatMessage(msg) {
        if (!msg) return;

        const empty = chatStreamList.querySelector(".empty-chat-state");
        if (empty) empty.remove();

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

        chatStreamList.insertBefore(item, chatStreamList.firstChild);

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

    // Chart Renderer
    function drawChart() {
        const rect = canvas.getBoundingClientRect();
        const width = rect.width;
        const height = rect.height;

        ctx.clearRect(0, 0, width, height);

        if (chartHistory.length < 2) return;

        const padLeft = 45;
        const padRight = 20;
        const padTop = 25;
        const padBottom = 30;

        const graphW = width - padLeft - padRight;
        const graphH = height - padTop - padBottom;
        const zeroY = padTop + graphH / 2;

        // Function to map score [-1.0, 1.0] to Y coordinate
        function toY(val) {
            const clamped = Math.max(-1.0, Math.min(1.0, val));
            return zeroY - (clamped * (graphH / 2));
        }

        // Draw horizontal grid lines
        ctx.strokeStyle = "rgba(255, 255, 255, 0.06)";
        ctx.lineWidth = 1;

        // Threshold levels
        const gridLevels = [1.0, 0.5, 0.35, 0.0, -0.30, -0.5, -1.0];
        gridLevels.forEach(lvl => {
            const y = toY(lvl);
            ctx.beginPath();
            ctx.moveTo(padLeft, y);
            ctx.lineTo(width - padRight, y);
            ctx.stroke();

            // Label text
            ctx.fillStyle = lvl === 0 ? "#94a3b8" : (lvl > 0 ? "rgba(16, 185, 129, 0.6)" : "rgba(239, 68, 68, 0.6)");
            ctx.font = "10px 'JetBrains Mono'";
            ctx.textAlign = "right";
            ctx.textBaseline = "middle";
            ctx.fillText((lvl > 0 ? "+" : "") + lvl.toFixed(2), padLeft - 6, y);
        });

        // Prominent Neutral Line (0.00)
        ctx.strokeStyle = "rgba(255, 255, 255, 0.2)";
        ctx.setLineDash([4, 4]);
        ctx.beginPath();
        ctx.moveTo(padLeft, zeroY);
        ctx.lineTo(width - padRight, zeroY);
        ctx.stroke();
        ctx.setLineDash([]);

        const n = chartHistory.length;
        const stepX = graphW / (Math.max(30, n - 1));

        // 1. Draw Volatility Corridor Band (EMA ± Volatility)
        ctx.beginPath();
        for (let i = 0; i < n; i++) {
            const x = padLeft + (i * stepX);
            const upper = chartHistory[i].ema + (chartHistory[i].volatility || 0);
            const y = toY(upper);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        for (let i = n - 1; i >= 0; i--) {
            const x = padLeft + (i * stepX);
            const lower = chartHistory[i].ema - (chartHistory[i].volatility || 0);
            const y = toY(lower);
            ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.fillStyle = "rgba(0, 242, 254, 0.05)";
        ctx.fill();

        // 2. Draw Moving Average (EMA) Line (Amber)
        ctx.beginPath();
        ctx.strokeStyle = "#f59e0b";
        ctx.lineWidth = 2;
        for (let i = 0; i < n; i++) {
            const x = padLeft + (i * stepX);
            const y = toY(chartHistory[i].ema);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.stroke();

        // 3. Draw Instant Score Line with Glowing Gradient
        ctx.beginPath();
        for (let i = 0; i < n; i++) {
            const x = padLeft + (i * stepX);
            const y = toY(chartHistory[i].score);
            if (i === 0) ctx.moveTo(x, y);
            else {
                // Smooth line using bezier or lineTo
                const prevX = padLeft + ((i - 1) * stepX);
                const prevY = toY(chartHistory[i - 1].score);
                const cpX = (prevX + x) / 2;
                ctx.bezierCurveTo(cpX, prevY, cpX, y, x, y);
            }
        }

        const gradient = ctx.createLinearGradient(0, padTop, 0, height - padBottom);
        gradient.addColorStop(0, "#10b981");
        gradient.addColorStop(0.5, "#00f2fe");
        gradient.addColorStop(1, "#ef4444");

        ctx.strokeStyle = gradient;
        ctx.lineWidth = 3;
        ctx.shadowColor = "rgba(0, 242, 254, 0.4)";
        ctx.shadowBlur = 8;
        ctx.stroke();
        ctx.shadowBlur = 0;

        // Draw glowing point at the newest score
        const lastX = padLeft + ((n - 1) * stepX);
        const lastY = toY(chartHistory[n - 1].score);
        ctx.beginPath();
        ctx.arc(lastX, lastY, 5, 0, Math.PI * 2);
        ctx.fillStyle = "#ffffff";
        ctx.fill();
        ctx.strokeStyle = chartHistory[n - 1].score >= 0 ? "var(--color-pos)" : "var(--color-neg)";
        ctx.lineWidth = 2;
        ctx.stroke();
    }

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