package com.stylometry;

import java.util.*;
import java.util.stream.Collectors;

public class MetricsCalculator {

    // Window size for MATTR (Moving-Average Type-Token Ratio).
    // 500 is a commonly used window size in stylometry literature.
    private static final int MATTR_WINDOW = 500;

    // Function words are the backbone of authorship comparison: unlike content
    // words, they are used subconsciously and don't vary with topic, so their
    // relative frequency is a strong fingerprint of an individual author's style.
    // Public so AuthorshipComparator can reuse the exact same word list when
    // computing Burrows' Delta.
    public static final List<String> FUNCTION_WORDS = List.of(
            "the", "and", "a", "to", "of", "in", "is", "it", "that", "was",
            "for", "on", "he", "she", "his", "her", "you", "with", "as", "but",
            "at", "by", "from", "not", "this", "have", "had", "were", "which",
            "or", "be", "are", "they", "an", "if", "so", "there", "when", "all"
    );

    // Chunk size (in words) used to split each text into samples for Burrows'
    // Delta. 1000 is a commonly used chunk size in stylometry literature -
    // large enough to give stable per-chunk word rates, small enough that a
    // normal novel yields dozens of chunks to build statistics from.
    private static final int DELTA_CHUNK_SIZE = 1000;

    public StylometricMetrics computeMetrics(List<String> words, List<String> sentences, String rawText) {
        if (words.isEmpty()) {
            return new StylometricMetrics(0.0, 0.0, 0.0, Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyList(), Collections.emptyList(), 0);
        }

        double mattr = calculateMATTR(words, MATTR_WINDOW);
        double avgSentenceLen = calculateAvgSentenceLength(words, sentences);
        double fkGrade = calculateFleschKincaid(words, sentences, rawText);
        Map<String, Integer> topFrequencies = getTopWordFrequencies(words, 5);
        Map<String, Integer> functionWordFrequencies = getFunctionWordFrequencies(words);

        List<Map<String, Integer>> chunkFunctionWordCounts = new ArrayList<>();
        List<Integer> chunkSizes = new ArrayList<>();
        chunkFunctionWordCounts(words, DELTA_CHUNK_SIZE, chunkFunctionWordCounts, chunkSizes);

        return new StylometricMetrics(mattr, avgSentenceLen, fkGrade, topFrequencies,
                functionWordFrequencies, chunkFunctionWordCounts, chunkSizes, words.size());
    }

    /**
     * Splits words into fixed-size chunks and computes function-word counts
     * for each chunk. Each chunk becomes one "sample" used later to compute
     * a mean/standard-deviation baseline for Burrows' Delta. A trailing
     * chunk smaller than half the target size is dropped rather than kept
     * as a noisy, under-sized sample.
     */
    private void chunkFunctionWordCounts(List<String> words, int chunkSize,
                                         List<Map<String, Integer>> outChunks,
                                         List<Integer> outChunkSizes) {
        int n = words.size();
        if (n <= chunkSize) {
            outChunks.add(getFunctionWordFrequencies(words));
            outChunkSizes.add(n);
            return;
        }
        for (int start = 0; start < n; start += chunkSize) {
            int end = Math.min(start + chunkSize, n);
            if (end - start < chunkSize / 2) break; // drop small tail chunk
            List<String> sub = words.subList(start, end);
            outChunks.add(getFunctionWordFrequencies(sub));
            outChunkSizes.add(sub.size());
        }
    }

    /**
     * MATTR (Moving-Average Type-Token Ratio).
     *
     * Raw TTR (unique words / total words) shrinks mechanically as a text gets
     * longer, since you keep re-using words you've already used. That makes
     * raw TTR meaningless when comparing texts of very different lengths
     * (e.g. a 140k-word novel vs a 46k-word novel).
     *
     * MATTR fixes this by sliding a fixed-size window across the text,
     * computing TTR for each window, and averaging the results. Because
     * every window is the same size, the comparison is length-independent.
     * If the text is shorter than the window, it falls back to plain TTR.
     */
    private double calculateMATTR(List<String> words, int windowSize) {
        int n = words.size();
        if (n <= windowSize) {
            Set<String> unique = new HashSet<>(words);
            return (double) unique.size() / n;
        }

        double ttrSum = 0.0;
        int windowCount = n - windowSize + 1;

        // Sliding window with an incrementally maintained frequency map,
        // so this stays O(n) instead of O(n * windowSize).
        Map<String, Integer> windowFreq = new HashMap<>();
        for (int i = 0; i < windowSize; i++) {
            windowFreq.merge(words.get(i), 1, Integer::sum);
        }
        ttrSum += (double) windowFreq.size() / windowSize;

        for (int i = windowSize; i < n; i++) {
            String leaving = words.get(i - windowSize);
            String entering = words.get(i);

            int leavingCount = windowFreq.get(leaving);
            if (leavingCount == 1) {
                windowFreq.remove(leaving);
            } else {
                windowFreq.put(leaving, leavingCount - 1);
            }
            windowFreq.merge(entering, 1, Integer::sum);

            ttrSum += (double) windowFreq.size() / windowSize;
        }

        return ttrSum / windowCount;
    }

    private double calculateAvgSentenceLength(List<String> words, List<String> sentences) {
        int sentenceCount = Math.max(sentences.size(), 1);
        return (double) words.size() / sentenceCount;
    }

    private double calculateFleschKincaid(List<String> words, List<String> sentences, String text) {
        int wordCount = words.size();
        int sentenceCount = Math.max(sentences.size(), 1);
        int syllableCount = countSyllablesInText(words);

        // Standard Flesch-Kincaid Grade Level formula
        return 0.39 * ((double) wordCount / sentenceCount) + 11.8 * ((double) syllableCount / wordCount) - 15.59;
    }

    private int countSyllablesInText(List<String> words) {
        int totalSyllables = 0;
        for (String word : words) {
            totalSyllables += countWordSyllables(word);
        }
        return Math.max(totalSyllables, 1);
    }

    private int countWordSyllables(String word) {
        String w = word.toLowerCase().replaceAll("[^a-z]", "");
        if (w.length() <= 3) return 1;
        w = w.replaceAll("e$", "");
        String[] syllables = w.split("[^aeiouy]+");
        int count = 0;
        for (String s : syllables) {
            if (!s.isBlank()) count++;
        }
        return Math.max(count, 1);
    }

    private Map<String, Integer> getTopWordFrequencies(List<String> words, int limit) {
        Map<String, Integer> freqMap = new HashMap<>();
        // Basic stop words filter to highlight meaningful vocabulary in the display report
        Set<String> stopWords = Set.of("the", "and", "a", "to", "of", "in", "is", "it", "that", "was", "for", "on");

        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 2) {
                freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            }
        }

        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Counts occurrences of a fixed set of function words (the, and, of, her,
     * his, etc). Unlike getTopWordFrequencies, this deliberately KEEPS
     * stopwords, because function-word usage rate is the strongest signal
     * for authorship comparison (see Burrows' Delta method).
     */
    private Map<String, Integer> getFunctionWordFrequencies(List<String> words) {
        Map<String, Integer> freq = new LinkedHashMap<>();
        for (String fw : FUNCTION_WORDS) {
            freq.put(fw, 0);
        }
        for (String word : words) {
            if (freq.containsKey(word)) {
                freq.merge(word, 1, Integer::sum);
            }
        }
        return freq;
    }
}