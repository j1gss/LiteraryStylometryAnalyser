package com.stylometry;

import java.util.List;
import java.util.Map;

/**
 * @param typeTokenRatio     NOTE: typeTokenRatio now holds the MATTR (Moving-Average Type-Token Ratio) value rather than raw TTR, so it is comparable across texts of different lengths. See MetricsCalculator.calculateMATTR() for details.
 * @param functionWordChunks Per-chunk function-word counts and the word count of each chunk. functionWordChunks.get(i) corresponds to a chunk of chunkSizes.get(i) words. Used by AuthorshipComparator to build the mean/std baseline needed for Burrows' Delta.
 */
public record StylometricMetrics(double typeTokenRatio, double avgSentenceLength, double fleschKincaidGrade,
                                 Map<String, Integer> topFrequencies, Map<String, Integer> functionWordFrequencies,
                                 List<Map<String, Integer>> functionWordChunks, List<Integer> chunkSizes,
                                 int totalWordCount) {

    @Override
    public String toString() {
        String sb = String.format("Total Word Count         : %d%n", totalWordCount) +
                String.format("MATTR (Lexical Richness) : %.4f%n", typeTokenRatio) +
                String.format("Avg Sentence Length      : %.2f words/sentence%n", avgSentenceLength) +
                String.format("Flesch-Kincaid Grade     : %.2f%n", fleschKincaidGrade) +
                "Top Words (Frequencies)  : " + topFrequencies + "\n";
        return sb;
    }
}