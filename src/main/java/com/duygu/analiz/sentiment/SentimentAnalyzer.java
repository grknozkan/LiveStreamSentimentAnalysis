package com.duygu.analiz.sentiment;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Çok Dilli (Multilingual) ve Evrensel Canlı Yayın Duygu Analiz Motoru.
 * 
 * Desteklenen Diller & İfadeler:
 * - Türkçe (TR)
 * - İngilizce (EN)
 * - İspanyolca (ES)
 * - Almanca (DE)
 * - Fransızca (FR)
 * - Portekizce (PT)
 * - Rusça (RU / Kiril)
 * - Global Gaming & Twitch/YouTube Slang
 * - Emojiler & ASCII İfadeleri (:), :D, :( vb.)
 */
@Component
public class SentimentAnalyzer {

    public static class AnalysisResult {
        private final double score;
        private final String label;
        private final List<String> detectedKeywords;
        private final double hypeLevel;

        public AnalysisResult(double score, String label, List<String> detectedKeywords, double hypeLevel) {
            this.score = score;
            this.label = label;
            this.detectedKeywords = detectedKeywords;
            this.hypeLevel = hypeLevel;
        }

        public double getScore() { return score; }
        public String getLabel() { return label; }
        public List<String> getDetectedKeywords() { return detectedKeywords; }
        public double getHypeLevel() { return hypeLevel; }
    }

    private final Map<String, Double> lexicon = new HashMap<>();
    private final Map<String, Double> emojiLexicon = new HashMap<>();
    private final Set<String> negations = new HashSet<>();
    private final Set<String> intensifiers = new HashSet<>();

    // Unicode uyumlu noktalama temizleyici (Tüm dillerin harflerini ve rakamlarını korur)
    private final Pattern unicodePunctuationPattern = Pattern.compile("[^\\p{L}\\p{Nd}\\s]");

    public SentimentAnalyzer() {
        initLexicon();
        initNegations();
        initIntensifiers();
    }

    private void initLexicon() {
        // =====================================================================
        // 1. TÜRKÇE (TR)
        // =====================================================================
        // Pozitif
        addWord("harika", 0.9);
        addWord("mükemmel", 1.0);
        addWord("mukemmel", 1.0);
        addWord("süper", 0.85);
        addWord("super", 0.85);
        addWord("muazzam", 0.95);
        addWord("efsane", 0.9);
        addWord("tebrikler", 0.8);
        addWord("tebrik", 0.75);
        addWord("helal", 0.85);
        addWord("kral", 0.8);
        addWord("kralsın", 0.9);
        addWord("kralsin", 0.9);
        addWord("adamsın", 0.85);
        addWord("adamsin", 0.85);
        addWord("güzel", 0.6);
        addWord("guzel", 0.6);
        addWord("iyi", 0.5);
        addWord("başarılı", 0.7);
        addWord("basarili", 0.7);
        addWord("sevdim", 0.7);
        addWord("seviyorum", 0.8);
        addWord("aferin", 0.75);
        addWord("bravo", 0.85);
        addWord("şaheser", 0.95);
        addWord("kusursuz", 0.9);
        addWord("aşk", 0.8);
        addWord("mutlu", 0.7);
        addWord("sevindim", 0.75);
        addWord("kazandık", 0.9);
        addWord("kazandik", 0.9);
        addWord("şampiyon", 0.95);
        addWord("sampiyon", 0.95);
        addWord("zafer", 0.9);
        addWord("gurur", 0.8);
        addWord("tatlı", 0.6);
        addWord("tatli", 0.6);
        addWord("temiz", 0.65);

        // Negatif
        addWord("berbat", -0.9);
        addWord("rezalet", -0.95);
        addWord("rezil", -0.9);
        addWord("çöp", -0.8);
        addWord("cop", -0.8);
        addWord("iğrenç", -0.85);
        addWord("igrenc", -0.85);
        addWord("kötü", -0.6);
        addWord("kotu", -0.6);
        addWord("nefret", -0.9);
        addWord("sinir", -0.7);
        addWord("öfke", -0.75);
        addWord("ofke", -0.75);
        addWord("batırdı", -0.8);
        addWord("batirdi", -0.8);
        addWord("sattı", -0.75);
        addWord("satti", -0.75);
        addWord("kaybettik", -0.8);
        addWord("yazık", -0.6);
        addWord("yazik", -0.6);
        addWord("saçmalık", -0.7);
        addWord("sacmalik", -0.7);
        addWord("yeteneksiz", -0.8);
        addWord("ezik", -0.75);
        addWord("aptal", -0.85);
        addWord("salak", -0.8);
        addWord("tilt", -0.7);
        addWord("kanser", -0.75);
        addWord("ağla", -0.6);
        addWord("agla", -0.6);
        addWord("yuh", -0.65);
        addWord("ayıp", -0.6);
        addWord("ayip", -0.6);
        addWord("lanet", -0.7);
        addWord("utan", -0.7);

        // =====================================================================
        // 2. İNGİLİZCE (EN - English)
        // =====================================================================
        // Pozitif
        addWord("amazing", 0.9);
        addWord("awesome", 0.9);
        addWord("great", 0.75);
        addWord("good", 0.55);
        addWord("best", 0.85);
        addWord("love", 0.8);
        addWord("insane", 0.85);
        addWord("brilliant", 0.85);
        addWord("fantastic", 0.9);
        addWord("incredible", 0.9);
        addWord("wow", 0.7);
        addWord("nice", 0.6);
        addWord("crazy", 0.75);
        addWord("cool", 0.6);
        addWord("fire", 0.85);
        addWord("sick", 0.8);
        addWord("peak", 0.8);
        addWord("perfect", 0.95);
        addWord("win", 0.75);
        addWord("winner", 0.8);
        addWord("victory", 0.85);
        addWord("masterclass", 0.95);
        addWord("clean", 0.7);
        addWord("beautiful", 0.8);
        addWord("godlike", 0.95);
        addWord("outstanding", 0.9);
        addWord("superb", 0.85);
        addWord("slay", 0.8);
        addWord("lit", 0.75);
        addWord("holy", 0.7);
        addWord("masterpiece", 0.95);
        addWord("legendary", 0.9);
        addWord("cracked", 0.85);
        addWord("congrats", 0.75);
        addWord("congratulations", 0.8);
        addWord("proud", 0.75);
        addWord("wonderful", 0.85);
        addWord("gorgeous", 0.8);
        addWord("respect", 0.75);
        addWord("happy", 0.7);
        addWord("fun", 0.65);
        addWord("genius", 0.85);
        addWord("legend", 0.85);

        // Negatif
        addWord("trash", -0.85);
        addWord("garbage", -0.85);
        addWord("bad", -0.6);
        addWord("terrible", -0.9);
        addWord("awful", -0.9);
        addWord("horrible", -0.95);
        addWord("worst", -0.9);
        addWord("hate", -0.85);
        addWord("lose", -0.7);
        addWord("loser", -0.8);
        addWord("lost", -0.7);
        addWord("suck", -0.8);
        addWord("sucks", -0.8);
        addWord("boring", -0.7);
        addWord("sleep", -0.4);
        addWord("disaster", -0.9);
        addWord("fraud", -0.8);
        addWord("washed", -0.75);
        addWord("unwatchable", -0.9);
        addWord("useless", -0.8);
        addWord("stupid", -0.85);
        addWord("idiot", -0.85);
        addWord("dumb", -0.8);
        addWord("clown", -0.7);
        addWord("disgusting", -0.9);
        addWord("dead", -0.6);
        addWord("troll", -0.7);
        addWord("shame", -0.75);
        addWord("embarrassing", -0.85);
        addWord("pathetic", -0.9);
        addWord("cheat", -0.85);
        addWord("cheater", -0.9);
        addWord("sad", -0.6);
        addWord("angry", -0.75);
        addWord("mad", -0.65);
        addWord("ugly", -0.7);

        // =====================================================================
        // 3. GLOBAL GAMING & INTERNET SLANG
        // =====================================================================
        addWord("gg", 0.7);
        addWord("wp", 0.75);
        addWord("goat", 0.95);
        addWord("pog", 0.85);
        addWord("poggers", 0.9);
        addWord("poggies", 0.85);
        addWord("w", 0.6);
        addWord("clutch", 0.85);
        addWord("carry", 0.8);
        addWord("ace", 0.9);
        addWord("ez", 0.5);
        addWord("hype", 0.85);
        addWord("based", 0.7);
        addWord("sheesh", 0.75);
        addWord("lol", 0.4);
        addWord("lul", 0.4);
        addWord("kekw", 0.5);
        addWord("lmao", 0.5);
        addWord("hahaha", 0.5);
        addWord("ahaha", 0.5);

        addWord("l", -0.5);
        addWord("rip", -0.7);
        addWord("fail", -0.75);
        addWord("throw", -0.75);
        addWord("threw", -0.75);
        addWord("choke", -0.7);
        addWord("chokeledi", -0.75);
        addWord("grief", -0.8);
        addWord("toxic", -0.85);
        addWord("feeder", -0.7);
        addWord("noob", -0.65);
        addWord("bot", -0.5);
        addWord("scam", -0.8);
        addWord("f", -0.4);
        addWord("cringe", -0.65);
        addWord("cring", -0.65);
        addWord("wtf", -0.5);
        addWord("bruh", -0.3);

        // =====================================================================
        // 4. İSPANYOLCA (ES - Español)
        // =====================================================================
        addWord("bueno", 0.6);
        addWord("genial", 0.85);
        addWord("increible", 0.9);
        addWord("increíble", 0.9);
        addWord("crack", 0.85);
        addWord("grande", 0.75);
        addWord("dios", 0.85);
        addWord("maquina", 0.85);
        addWord("máquina", 0.85);
        addWord("golazo", 0.9);
        addWord("vamos", 0.75);
        addWord("hermoso", 0.8);
        addWord("epico", 0.85);
        addWord("épico", 0.85);
        addWord("victoria", 0.85);
        addWord("campeon", 0.9);
        addWord("campeón", 0.9);
        addWord("maravilla", 0.9);
        addWord("fenomenal", 0.9);

        addWord("malo", -0.6);
        addWord("basura", -0.85);
        addWord("mierda", -0.9);
        addWord("asco", -0.85);
        addWord("horrible", -0.9);
        addWord("fatal", -0.8);
        addWord("paquete", -0.7);
        addWord("manco", -0.75);
        addWord("perder", -0.7);
        addWord("muerto", -0.65);
        addWord("robo", -0.8);
        addWord("desastre", -0.85);
        addWord("verguenza", -0.8);
        addWord("vergüenza", -0.8);
        addWord("tonto", -0.7);
        addWord("llorar", -0.6);

        // =====================================================================
        // 5. ALMANCA (DE - Deutsch)
        // =====================================================================
        addWord("gut", 0.55);
        addWord("toll", 0.8);
        addWord("geil", 0.85);
        addWord("stark", 0.8);
        addWord("wunderbar", 0.85);
        addWord("klasse", 0.8);
        addWord("beste", 0.85);
        addWord("bester", 0.85);
        addWord("ehre", 0.8);
        addWord("perfekt", 0.9);
        addWord("liebe", 0.8);
        addWord("sieg", 0.85);
        addWord("krass", 0.8);

        addWord("schlecht", -0.7);
        addWord("müll", -0.85);
        addWord("muell", -0.85);
        addWord("scheiße", -0.9);
        addWord("scheisse", -0.9);
        addWord("furchtbar", -0.9);
        addWord("schrecklich", -0.9);
        addWord("verloren", -0.75);
        addWord("dumm", -0.8);
        addWord("hass", -0.85);
        addWord("katastrophe", -0.9);
        addWord("peinlich", -0.8);
        addWord("ekelhaft", -0.85);

        // =====================================================================
        // 6. FRANSIZCA (FR - Français)
        // =====================================================================
        addWord("bien", 0.55);
        addWord("beau", 0.75);
        addWord("parfait", 0.9);
        addWord("lourd", 0.8);
        addWord("roi", 0.8);
        addWord("pepite", 0.85);
        addWord("pépite", 0.85);
        addWord("magnifique", 0.9);

        addWord("nul", -0.8);
        addWord("poubelle", -0.85);
        addWord("merde", -0.9);
        addWord("mauvais", -0.7);
        addWord("decevant", -0.75);
        addWord("décevant", -0.75);
        addWord("horreur", -0.9);
        addWord("honte", -0.8);

        // =====================================================================
        // 7. PORTEKİZCE (PT - Português)
        // =====================================================================
        addWord("bom", 0.55);
        addWord("mito", 0.85);
        addWord("monstro", 0.85);
        addWord("brabo", 0.85);
        addWord("craque", 0.85);
        addWord("parabens", 0.8);
        addWord("parabéns", 0.8);
        addWord("lindo", 0.75);

        addWord("ruim", -0.65);
        addWord("lixo", -0.85);
        addWord("horrivel", -0.9);
        addWord("horrível", -0.9);
        addWord("podre", -0.8);
        addWord("bagre", -0.7);
        addWord("vergonha", -0.8);
        addWord("chora", -0.6);

        // =====================================================================
        // 8. RUSÇA (RU - Русский / Kiril Alfabesi)
        // =====================================================================
        addWord("красава", 0.9);
        addWord("топ", 0.85);
        addWord("лучший", 0.9);
        addWord("кайф", 0.8);
        addWord("круто", 0.75);
        addWord("имба", 0.85);
        addWord("тащи", 0.8);
        addWord("мощно", 0.8);
        addWord("гений", 0.85);
        addWord("победа", 0.9);
        addWord("харош", 0.85);
        addWord("молодец", 0.8);
        addWord("огонь", 0.85);
        addWord("пушка", 0.85);
        addWord("класс", 0.75);

        addWord("дно", -0.8);
        addWord("мусор", -0.85);
        addWord("рак", -0.75);
        addWord("кринж", -0.65);
        addWord("руина", -0.85);
        addWord("слив", -0.75);
        addWord("фу", -0.6);
        addWord("отстой", -0.8);
        addWord("ужас", -0.85);
        addWord("позор", -0.85);
        addWord("дурак", -0.8);
        addWord("кошмар", -0.85);
        addWord("фид", -0.75);
        addWord("фидер", -0.75);

        // =====================================================================
        // 9. EMOJİLER & ASCII EMOTICONLAR
        // =====================================================================
        addEmoji("🔥", 0.95);
        addEmoji("❤️", 0.9);
        addEmoji("😍", 0.85);
        addEmoji("🥰", 0.85);
        addEmoji("👏", 0.8);
        addEmoji("🎉", 0.85);
        addEmoji("🥳", 0.85);
        addEmoji("🏆", 0.9);
        addEmoji("🚀", 0.85);
        addEmoji("👑", 0.9);
        addEmoji("💯", 0.85);
        addEmoji("😎", 0.7);
        addEmoji("🤩", 0.85);
        addEmoji("😊", 0.6);
        addEmoji("😂", 0.5);
        addEmoji("🤣", 0.5);
        addEmoji("<3", 0.8);
        addEmoji(":)", 0.6);
        addEmoji("(:", 0.6);
        addEmoji(":-)", 0.6);
        addEmoji(":d", 0.65);
        addEmoji("xd", 0.6);
        addEmoji(";)", 0.55);

        addEmoji("💀", -0.7);
        addEmoji("😡", -0.85);
        addEmoji("🤬", -0.95);
        addEmoji("💩", -0.8);
        addEmoji("🤡", -0.75);
        addEmoji("🤮", -0.9);
        addEmoji("😭", -0.65);
        addEmoji("👎", -0.8);
        addEmoji("💔", -0.8);
        addEmoji("📉", -0.6);
        addEmoji(":(", -0.6);
        addEmoji("):", -0.6);
        addEmoji(":-(", -0.6);
        addEmoji(":'(", -0.7);
        addEmoji("d:", -0.6);
        addEmoji(":/", -0.5);
        addEmoji(":|", -0.4);
    }

    private void addWord(String word, double score) {
        lexicon.put(word.toLowerCase(Locale.ROOT), score);
    }

    private void addEmoji(String emoji, double score) {
        emojiLexicon.put(emoji.toLowerCase(Locale.ROOT), score);
    }

    private void initNegations() {
        // Türkçe
        negations.add("değil");
        negations.add("degil");
        negations.add("yok");
        negations.add("hiç");
        negations.add("hic");
        negations.add("asla");
        negations.add("olmaz");

        // İngilizce
        negations.add("not");
        negations.add("no");
        negations.add("never");
        negations.add("dont");
        negations.add("don't");
        negations.add("cant");
        negations.add("can't");
        negations.add("wont");
        negations.add("won't");
        negations.add("isnt");
        negations.add("isn't");
        negations.add("hardly");
        negations.add("barely");
        negations.add("without");
        negations.add("aint");
        negations.add("ain't");

        // İspanyolca
        negations.add("nunca");
        negations.add("jamas");
        negations.add("jamás");
        negations.add("tampoco");

        // Almanca
        negations.add("nicht");
        negations.add("kein");
        negations.add("keine");
        negations.add("nie");
        negations.add("niemals");

        // Fransızca
        negations.add("pas");
        negations.add("non");
        negations.add("ne");
        negations.add("rien");

        // Portekizce
        negations.add("não");
        negations.add("nao");

        // Rusça
        negations.add("не");
        negations.add("нет");
        negations.add("никогда");
    }

    private void initIntensifiers() {
        // Türkçe
        intensifiers.add("çok");
        intensifiers.add("cok");
        intensifiers.add("aşırı");
        intensifiers.add("asiri");
        intensifiers.add("baya");
        intensifiers.add("bayağı");
        intensifiers.add("bayagi");
        intensifiers.add("fena");
        intensifiers.add("inanılmaz");
        intensifiers.add("inanilmaz");
        intensifiers.add("feci");
        intensifiers.add("dehşet");

        // İngilizce
        intensifiers.add("so");
        intensifiers.add("very");
        intensifiers.add("extremely");
        intensifiers.add("insanely");
        intensifiers.add("super");
        intensifiers.add("ultra");
        intensifiers.add("mega");
        intensifiers.add("really");
        intensifiers.add("damn");
        intensifiers.add("deeply");
        intensifiers.add("truly");
        intensifiers.add("highly");
        intensifiers.add("pretty");
        intensifiers.add("incredibly");
        intensifiers.add("totally");
        intensifiers.add("absolutely");

        // İspanyolca
        intensifiers.add("muy");
        intensifiers.add("tan");
        intensifiers.add("demasiado");

        // Almanca
        intensifiers.add("sehr");
        intensifiers.add("voll");
        intensifiers.add("total");

        // Fransızca
        intensifiers.add("tres");
        intensifiers.add("très");
        intensifiers.add("trop");
        intensifiers.add("tellement");

        // Portekizce
        intensifiers.add("muito");
        intensifiers.add("demais");

        // Rusça
        intensifiers.add("очень");
        intensifiers.add("капец");
    }

    public AnalysisResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new AnalysisResult(0.0, "NEUTRAL", Collections.emptyList(), 0.0);
        }

        double totalScore = 0.0;
        int matchedWords = 0;
        List<String> detectedKeywords = new ArrayList<>();

        // Bağırma / Caps-lock tespiti
        int upperCaseCount = 0;
        int letterCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
                if (Character.isUpperCase(c)) upperCaseCount++;
            }
        }
        boolean isShouting = letterCount > 4 && ((double) upperCaseCount / letterCount > 0.65);

        // Ünlem işareti yoğunluğu
        int exclamationCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '!') exclamationCount++;
        }

        // 1. Doğrudan Emoji ve Sembol Eşleşmeleri
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Double> entry : emojiLexicon.entrySet()) {
            String token = entry.getKey();
            if (lowerText.contains(token)) {
                totalScore += entry.getValue();
                matchedWords++;
                detectedKeywords.add(token);
            }
        }

        // 2. Tokenize et
        String[] rawTokens = text.split("\\s+");

        boolean nextIsNegated = false;
        double currentMultiplier = 1.0;
        double lastWordScoreApplied = 0.0;

        for (int i = 0; i < rawTokens.length; i++) {
            String rawToken = rawTokens[i];
            if (rawToken.isEmpty()) continue;

            // Sembol/Emoji ise zaten yukarıda işlendi
            String lowerRaw = rawToken.toLowerCase(Locale.ROOT);
            if (emojiLexicon.containsKey(lowerRaw)) {
                continue;
            }

            // Temiz kelime kökü (Noktalamalardan arındırılmış)
            String cleanToken = unicodePunctuationPattern.matcher(rawToken).replaceAll("");
            if (cleanToken.isEmpty()) continue;

            String lowerTokenRoot = cleanToken.toLowerCase(Locale.ROOT);
            String lowerTokenTr = cleanToken.toLowerCase(new Locale("tr", "TR"));

            // Yoğunlaştırıcı kontrolü ("very", "çok", "muy", "sehr" vb.)
            if (intensifiers.contains(lowerTokenRoot) || intensifiers.contains(lowerTokenTr)) {
                currentMultiplier = 1.4;
                continue;
            }

            // Olumsuzluk kontrolü ("not", "değil", "no", "nicht", "pas" vb.)
            if (negations.contains(lowerTokenRoot) || negations.contains(lowerTokenTr)) {
                if (lastWordScoreApplied != 0.0) {
                    // Türkçe ardıl olumsuzluk ("güzel değil", "iyi değil")
                    totalScore -= lastWordScoreApplied;
                    double invertedScore = -lastWordScoreApplied * 0.85;
                    totalScore += invertedScore;
                    lastWordScoreApplied = 0.0;
                    if (!detectedKeywords.isEmpty()) {
                        int lastIdx = detectedKeywords.size() - 1;
                        detectedKeywords.set(lastIdx, "!" + detectedKeywords.get(lastIdx));
                    }
                } else {
                    // Öncül olumsuzluk ("not good", "no bueno", "nicht gut")
                    nextIsNegated = true;
                }
                continue;
            }

            // Sözlük araması (Hem Root hem TR küçük harf duyarlılığı)
            Double wordScore = lexicon.get(lowerTokenRoot);
            String matchedKey = lowerTokenRoot;
            if (wordScore == null) {
                wordScore = lexicon.get(lowerTokenTr);
                matchedKey = lowerTokenTr;
            }

            if (wordScore == null) {
                // Tekrarlayan harfleri sadeleştir (Örn: "insaaaneee" -> "insane", "efsaneeee" -> "efsane")
                String normalized = normalizeRepeatingChars(lowerTokenRoot);
                wordScore = lexicon.get(normalized);
                matchedKey = normalized;
                if (wordScore == null) {
                    String normalizedTr = normalizeRepeatingChars(lowerTokenTr);
                    wordScore = lexicon.get(normalizedTr);
                    matchedKey = normalizedTr;
                }
                if (wordScore != null) {
                    currentMultiplier *= 1.2; // Ekstra coşku çarpanı
                }
            }

            if (wordScore != null) {
                double effectiveScore = wordScore * currentMultiplier;
                if (nextIsNegated) {
                    effectiveScore = -effectiveScore * 0.85; // Olumsuzluk skoru tersine çevirir
                    detectedKeywords.add("!" + matchedKey);
                } else {
                    detectedKeywords.add(matchedKey);
                }

                totalScore += effectiveScore;
                lastWordScoreApplied = effectiveScore;
                matchedWords++;

                // İşlendikten sonra bayrakları sıfırla
                nextIsNegated = false;
                currentMultiplier = 1.0;
            } else {
                lastWordScoreApplied = 0.0;
                if (nextIsNegated && i > 0) {
                    nextIsNegated = false;
                }
            }
        }

        // Nihai Skor [-1.0, +1.0] aralığında normalize edilir
        double finalScore = 0.0;
        if (matchedWords > 0) {
            double avg = totalScore / Math.sqrt(matchedWords);
            if (isShouting) {
                avg *= 1.25;
            }
            if (exclamationCount >= 2) {
                avg *= 1.15;
            }
            finalScore = Math.max(-1.0, Math.min(1.0, avg));
        }

        double hype = Math.abs(finalScore);
        if (isShouting) hype = Math.min(1.0, hype + 0.25);
        if (exclamationCount >= 3) hype = Math.min(1.0, hype + 0.2);

        String label;
        if (finalScore >= 0.20) {
            label = "POSITIVE";
        } else if (finalScore <= -0.20) {
            label = "NEGATIVE";
        } else {
            label = "NEUTRAL";
        }

        return new AnalysisResult(
            Math.round(finalScore * 100.0) / 100.0,
            label,
            detectedKeywords,
            Math.round(hype * 100.0) / 100.0
        );
    }

    private String normalizeRepeatingChars(String word) {
        if (word == null || word.length() < 3) return word;
        StringBuilder sb = new StringBuilder();
        char prev = word.charAt(0);
        int count = 1;
        sb.append(prev);

        for (int i = 1; i < word.length(); i++) {
            char curr = word.charAt(i);
            if (curr == prev) {
                count++;
                if (count <= 2) {
                    sb.append(curr);
                }
            } else {
                prev = curr;
                count = 1;
                sb.append(curr);
            }
        }
        return sb.toString();
    }
}