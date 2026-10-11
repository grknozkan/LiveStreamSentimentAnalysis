package com.duygu.analiz.connector;

import com.duygu.analiz.model.ChatMessage;
import com.duygu.analiz.model.StreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Component
public class SimulatorChatConnector implements StreamConnector {

    private static final Logger log = LoggerFactory.getLogger(SimulatorChatConnector.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private ScheduledExecutorService scheduler;
    private Consumer<ChatMessage> messageConsumer;
    private String currentMode = "AUTO_CYCLE"; // AUTO_CYCLE, HYPE, RAGE, CONTROVERSY, NORMAL
    private final Random random = new Random();

    private final List<String> usernames = List.of(
            "AlpGamer", "CyberTurk", "NeonFalcon", "ProClutch", "Zeynep_X",
            "MertKing", "Berkant06", "SelinPlayz", "KralOyuncu", "Vortex35",
            "ShadowHunter", "EceNur", "PixelWolf", "NightHawk", "LegendaryTR"
    );

    private final List<String> normalMessages = List.of(
            "Selam aleykum herkese iyi yayınlar",
            "Bugün hangi oyunu oynuyoruz kral?",
            "Yayın sesi ve görüntü gayet net bende",
            "Çayımı aldım geldim keyifli yayınlar",
            "Aleykum selam hoş geldin",
            "Oyunda fps kaç alıyorsun şu an?",
            "Afk mı kaldın az önce?",
            "Yayın kaç gibi biter tahminen?",
            "Selamlar iyi akşamlar chat",
            "Müzik listesini paylaşır mısın?"
    );

    private final List<String> hypeMessages = List.of(
            "HAYDİİİİİ BE İŞTE BUUUU 🔥🔥🔥",
            "MÜKEMMELSİN ABİ GOAT YEMİNLE 👑",
            "İNANILMAZ BİR CLUTCH ATTI HELAL OLSUN 👏",
            "POGGERS KRAL ADAMSIN BE 🎉🎉",
            "GG WP ÇOK İYİ OYNADIN BE HARİKASIN!",
            "EFSANE VURUŞTU AĞZIM AÇIK KALDI 🚀",
            "W W W W W W W W W 🔥",
            "AŞIRI İYİ HAREKET TÜYLERİM DİKEN DİKEN OLDU",
            "TEBRİKLER ŞAMPİYON MAŞALLAH SANA ❤️❤️",
            "BU MAÇ BİZİMDİR MUAZZAM OYUN 💯"
    );

    private final List<String> rageMessages = List.of(
            "HAYIRRR YA BÖYLE HATA YAPILIR MI BERBATTI 💀",
            "Rezalet oynadın maçı sattın resmen 😡",
            "Gözlerim kanıyor ne yaptın sen ya çöp hareket 🤬",
            "Aşırı kötü karar verdi tilt oldum şu an 👎",
            "Kaybettik işte saçmalık böyle oynanmaz ki!",
            "Yeteneksiz gibi öldü ya inanılmaz berbat 😭",
            "L L L L L L L L L 📉",
            "Sattı maçı abi çıldıracağım ya rezil olduk 🤮",
            "Kanser ettin bütün chati kapat yayını ya",
            "Hiç iyi oynamadı rezalet bir turdu"
    );

    private final List<String> controversyMessages = List.of(
            "Bence gayet iyi oynadı suçu adama atmayın",
            "Neresi iyi oynadı rezaletti maçı verdi işte!",
            "Hakem/oyun haksızlık yaptı takım arkadaşı suçlu",
            "Boş yapmayın adam elinden geleni yaptı mükemmeldi 🔥",
            "Kusura bakmayın ama berbattı savunanlar kör",
            "Chat sakin olun herkes hata yapar gayet güzel gidiyor"
    );

    private int cycleCounter = 0;
    private String simulatedScenario = "NORMAL";

    @Override
    public void start(StreamConfig config, Consumer<ChatMessage> consumer) {
        stop();
        this.messageConsumer = consumer;
        this.running.set(true);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Her 600 - 1000 ms aralığında bir mesaj gönder
        scheduler.scheduleWithFixedDelay(this::emitSimulatedMessage, 300, 800, TimeUnit.MILLISECONDS);
        log.info("Simulator chat connector started in mode: {}", currentMode);
    }

    private void emitSimulatedMessage() {
        if (!running.get() || messageConsumer == null) return;

        try {
            String activeMessagePool;

            if ("AUTO_CYCLE".equals(currentMode)) {
                cycleCounter++;
                // Her 25 mesajda bir senaryo değişimi (Duygu dalgalanması döngüsü)
                if (cycleCounter % 35 == 0) {
                    int r = random.nextInt(4);
                    if (r == 0) simulatedScenario = "HYPE";
                    else if (r == 1) simulatedScenario = "RAGE";
                    else if (r == 2) simulatedScenario = "CONTROVERSY";
                    else simulatedScenario = "NORMAL";
                    log.info("Simulator AUTO_CYCLE shifted scenario to: {}", simulatedScenario);
                }
                activeMessagePool = simulatedScenario;
            } else {
                activeMessagePool = currentMode;
            }

            String text;
            switch (activeMessagePool) {
                case "HYPE":
                    text = hypeMessages.get(random.nextInt(hypeMessages.size()));
                    break;
                case "RAGE":
                    text = rageMessages.get(random.nextInt(rageMessages.size()));
                    break;
                case "CONTROVERSY":
                    text = controversyMessages.get(random.nextInt(controversyMessages.size()));
                    break;
                default:
                    text = normalMessages.get(random.nextInt(normalMessages.size()));
                    break;
            }

            String user = usernames.get(random.nextInt(usernames.size()));

            ChatMessage msg = new ChatMessage();
            msg.setId(UUID.randomUUID().toString());
            msg.setPlatform("SIMULATOR");
            msg.setChannel("simulated_stream");
            msg.setUsername(user);
            msg.setMessage(text);
            msg.setTimestamp(System.currentTimeMillis());

            messageConsumer.accept(msg);

        } catch (Exception e) {
            log.error("Simulator emit error: {}", e.getMessage());
        }
    }

    public void setMode(String mode) {
        this.currentMode = mode;
        this.simulatedScenario = mode;
        log.info("Simulator mode manually updated to: {}", mode);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    @Override
    public void stop() {
        running.set(false);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public String getPlatform() {
        return "SIMULATOR";
    }
}