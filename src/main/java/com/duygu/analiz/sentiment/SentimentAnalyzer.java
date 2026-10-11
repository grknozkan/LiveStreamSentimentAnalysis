package com.duygu.analiz.sentiment;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

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
    private final Set<String> negations = new HashSet<>();
    private final Set<String> intensifiers = new HashSet<>();
    private final Pattern punctuationPattern = Pattern.compile("[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ\\s]");

    public SentimentAnalyzer() {
        initLexicon();
        initNegations();
        initIntensifiers();
    }

    private void initLexicon() {
        // --- TÜRKÇE POZİTİF KELİMELER ---
        lexicon.put("harika", 0.9);
        lexicon.put("mükemmel", 1.0);
        lexicon.put("mukemmel", 1.0);
        lexicon.put("süper", 0.85);
        lexicon.put("super", 0.85);
        lexicon.put("muazzam", 0.95);
        lexicon.put("efsane", 0.9);
        lexicon.put("tebrikler", 0.8);
        lexicon.put("tebrik", 0.75);
        lexicon.put("helal", 0.85);
        lexicon.put("kral", 0.8);
        lexicon.put("kralsın", 0.9);
        lexicon.put("kralsin", 0.9);
        lexicon.put("adamsın", 0.85);
        lexicon.put("adamsin", 0.85);
        lexicon.put("güzel", 0.6);
        lexicon.put("guzel", 0.6);
        lexicon.put("iyi", 0.5);
        lexicon.put("başarılı", 0.7);
        lexicon.put("basarili", 0.7);
        lexicon.put("sevdim", 0.7);
        lexicon.put("seviyorum", 0.8);
        lexicon.put("aferin", 0.75);
        lexicon.put("bravo", 0.85);
        lexicon.put("şaheser", 0.95);
        lexicon.put("kusursuz", 0.9);
        lexicon.put("aşk", 0.8);
        lexicon.put("mutlu", 0.7);
        lexicon.put("sevindim", 0.75);
        lexicon.put("kazandık", 0.9);
        lexicon.put("kazandik", 0.9);
        lexicon.put("şampiyon", 0.95);
        lexicon.put("sampiyon", 0.95);
        lexicon.put("zafer", 0.9);
        lexicon.put("gurur", 0.8);

        // --- YAYIN & OYUN POZİTİF (GAMING / SLANG) ---
        lexicon.put("gg", 0.7);
        lexicon.put("wp", 0.75);
        lexicon.put("goat", 0.95);
        lexicon.put("pog", 0.85);
        lexicon.put("poggers", 0.9);
        lexicon.put("poggies", 0.85);
        lexicon.put("w", 0.6);
        lexicon.put("clutch", 0.85);
        lexicon.put("carry", 0.8);
        lexicon.put("ace", 0.9);
        lexicon.put("ez", 0.5);
        lexicon.put("hype", 0.85);
        lexicon.put("based", 0.7);
        lexicon.put("sheesh", 0.75);
        lexicon.put("lol", 0.4);
        lexicon.put("lul", 0.4);
        lexicon.put("kekw", 0.5);
        lexicon.put("lmao", 0.5);
        lexicon.put("hahaha", 0.5);
        lexicon.put("ahaha", 0.5);

        // --- TÜRKÇE NEGATİF KELİMELER ---
        lexicon.put("berbat", -0.9);
        lexicon.put("rezalet", -0.95);
        lexicon.put("rezil", -0.9);
        lexicon.put("çöp", -0.8);
        lexicon.put("cop", -0.8);
        lexicon.put("iğrenç", -0.85);
        lexicon.put("igrenc", -0.85);
        lexicon.put("kötü", -0.6);
        lexicon.put("kotu", -0.6);
        lexicon.put("nefret", -0.9);
        lexicon.put("sinir", -0.7);
        lexicon.put("öfke", -0.75);
        lexicon.put("ofke", -0.75);
        lexicon.put("batırdı", -0.8);
        lexicon.put("batirdi", -0.8);
        lexicon.put("sattı", -0.75);
        lexicon.put("satti", -0.75);
        lexicon.put("kaybettik", -0.8);
        lexicon.put("yazık", -0.6);
        lexicon.put("yazik", -0.6);
        lexicon.put("saçmalık", -0.7);
        lexicon.put("sacmalik", -0.7);
        lexicon.put("yeteneksiz", -0.8);
        lexicon.put("ezik", -0.75);
        lexicon.put("aptal", -0.85);
        lexicon.put("salak", -0.8);
        lexicon.put("tilt", -0.7);
        lexicon.put("kanser", -0.75);
        lexicon.put("ağla", -0.6);
        lexicon.put("agla", -0.6);
        lexicon.put("yuh", -0.65);
        lexicon.put("ayıp", -0.6);
        lexicon.put("ayip", -0.6);
        lexicon.put("lanet", -0.7);
        lexicon.put("utan", -0.7);

        // --- YAYIN & OYUN NEGATİF (GAMING / SLANG) ---
        lexicon.put("l", -0.5);
        lexicon.put("rip", -0.7);
        lexicon.put("fail", -0.75);
        lexicon.put("throw", -0.75);
        lexicon.put("choke", -0.7);
        lexicon.put("chokeledi", -0.75);
        lexicon.put("grief", -0.8);
        lexicon.put("toxic", -0.85);
        lexicon.put("feeder", -0.7);
        lexicon.put("noob", -0.65);
        lexicon.put("bot", -0.5);
        lexicon.put("scam", -0.8);
        lexicon.put("f", -0.4);
        lexicon.put("cringe", -0.6);
        lexicon.put("cring", -0.6);
        lexicon.put("wtf", -0.5);
        lexicon.put("bruh", -0.3);

        // --- EMOJİLER ---
        lexicon.put("🔥", 0.95);
        lexicon.put("❤️", 0.9);
        lexicon.put("😍", 0.85);
        lexicon.put("🥰", 0.85);
        lexicon.put("👏", 0.8);
        lexicon.put("🎉", 0.85);
        lexicon.put("🥳", 0.85);
        lexicon.put("🏆", 0.9);
        lexicon.put("🚀", 0.85);
        lexicon.put("👑", 0.9);
        lexicon.put("💯", 0.85);
        lexicon.put("😎", 0.7);
        lexicon.put("🤩", 0.85);
        lexicon.put("😊", 0.6);
        lexicon.put("😂", 0.5);
        lexicon.put("🤣", 0.5);

        lexicon.put("💀", -0.7);
        lexicon.put("😡", -0.85);
        lexicon.put("🤬", -0.95);
        lexicon.put("💩", -0.8);
        lexicon.put("🤡", -0.75);
        lexicon.put("🤮", -0.9);
        lexicon.put("😭", -0.65);
        lexicon.put("👎", -0.8);
        lexicon.put("💔", -0.8);
        lexicon.put("📉", -0.6);
    }

    private void initNegations() {
        negations.add("değil");
        negations.add("degil");
        negations.add("yok");
        negations.add("hiç");
        negations.add("hic");
        negations.add("asla");
        negations.add("olmaz");
        negations.add("not");
        negations.add("no");
    }

    private void initIntensifiers() {
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
        intensifiers.add("super");
        intensifiers.add("ultra");
        intensifiers.add("very");
        intensifiers.add("so");
        intensifiers.add("extremely");
    }

    public AnalysisResult analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new AnalysisResult(0.0, "NEUTRAL", Collections.emptyList(), 0.0);
        }

        double totalScore = 0.0;
        int matchedWords = 0;
        List<String> detectedKeywords = new ArrayList<>();

        // Check for uppercase shouting / hype bonus
        int upperCaseCount = 0;
        int letterCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
                if (Character.isUpperCase(c)) upperCaseCount++;
            }
        }
        boolean isShouting = letterCount > 4 && ((double) upperCaseCount / letterCount > 0.65);

        // Check for multiple exclamation marks (Hype / Rage marker)
        int exclamationCount = 0;
        for (char c : text.toCharArray()) {
            if (c == '!') exclamationCount++;
        }

        // Direct emoji search first
        for (Map.Entry<String, Double> entry : lexicon.entrySet()) {
            String token = entry.getKey();
            if (token.codePointCount(0, token.length()) == 1 && Character.isSurrogate(token.charAt(0))) {
                if (text.contains(token)) {
                    totalScore += entry.getValue();
                    matchedWords++;
                    detectedKeywords.add(token);
                }
            }
        }

        // Tokenize text into words
        String lowerText = text.toLowerCase(new Locale("tr", "TR"));
        String[] tokens = lowerText.split("\\s+");

        boolean nextIsNegated = false;
        double currentMultiplier = 1.0;
        double lastWordScoreApplied = 0.0;

        for (int i = 0; i < tokens.length; i++) {
            String rawToken = tokens[i];
            // Remove punctuation for clean matching, but preserve root
            String cleanToken = punctuationPattern.matcher(rawToken).replaceAll("");

            if (cleanToken.isEmpty()) continue;

            // Handle intensifiers
            if (intensifiers.contains(cleanToken)) {
                currentMultiplier = 1.4;
                continue;
            }

            // Handle negation words
            if (negations.contains(cleanToken)) {
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
                    nextIsNegated = true;
                }
                continue;
            }

            // Look up in lexicon
            Double wordScore = lexicon.get(cleanToken);
            if (wordScore == null) {
                // Check if word ends with repeating letters (e.g., efsaneeee, harikaaaa)
                String normalized = normalizeRepeatingChars(cleanToken);
                wordScore = lexicon.get(normalized);
                if (wordScore != null) {
                    currentMultiplier *= 1.2; // Extra enthusiasm
                }
            }

            if (wordScore != null) {
                double effectiveScore = wordScore * currentMultiplier;
                if (nextIsNegated) {
                    effectiveScore = -effectiveScore * 0.85; // Negation inverts score
                    detectedKeywords.add("!" + cleanToken);
                } else {
                    detectedKeywords.add(cleanToken);
                }

                totalScore += effectiveScore;
                lastWordScoreApplied = effectiveScore;
                matchedWords++;

                // Reset negation & multiplier after applying to target word
                nextIsNegated = false;
                currentMultiplier = 1.0;
            } else {
                lastWordScoreApplied = 0.0;
                // Check if previous word was negated and current word is unknown
                if (nextIsNegated && i > 0) {
                    nextIsNegated = false;
                }
            }
        }

        // Calculate normalized final score between -1.0 and +1.0
        double finalScore = 0.0;
        if (matchedWords > 0) {
            double avg = totalScore / Math.sqrt(matchedWords);
            // Apply shouting multiplier
            if (isShouting) {
                avg *= 1.25;
            }
            if (exclamationCount >= 2) {
                avg *= 1.15;
            }
            // Clamp to [-1.0, 1.0]
            finalScore = Math.max(-1.0, Math.min(1.0, avg));
        }

        // Hype level represents emotional intensity regardless of polarity [0.0, 1.0]
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