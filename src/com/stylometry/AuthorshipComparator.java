package com.stylometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthorshipComparator {

    public double computeSimilarity(StylometricMetrics m1, StylometricMetrics m2) {
        // --- Scalar feature differences (normalized to 0.0-1.0 relative gaps) ---
        double mattrDiff = Math.abs(m1.typeTokenRatio() - m2.typeTokenRatio())
                / Math.max(m1.typeTokenRatio(), m2.typeTokenRatio());

        double sentLenDiff = Math.abs(m1.avgSentenceLength() - m2.avgSentenceLength())
                / Math.max(m1.avgSentenceLength(), m2.avgSentenceLength());

        double fkDiff = Math.abs(m1.fleschKincaidGrade() - m2.fleschKincaidGrade())
                / Math.max(Math.abs(m1.fleschKincaidGrade()), Math.max(Math.abs(m2.fleschKincaidGrade()), 1.0));

        double compositeDistance = (mattrDiff * 0.5) + (sentLenDiff * 0.3) + (fkDiff * 0.2);
        double scalarSimilarity = Math.max(0.0, 1.0 - compositeDistance); // 0.0-1.0

        // --- Burrows' Delta over function words (the dominant signal) ---
        // WHY NOT PLAIN COSINE SIMILARITY: cosine similarity between two raw
        // function-word rate vectors is dominated by whichever words have the
        // largest raw rate ("the", "and", "of"...), which are common at similar
        // rates across almost ALL English prose regardless of author. That made
        // every English-vs-English comparison score suspiciously high.
        //
        // Burrows' Delta fixes this by z-scoring each word's rate against a
        // baseline mean/standard-deviation built from many samples (chunks) of
        // both texts, then averaging the absolute z-score gap per word. Words
        // that vary little across all English text (so a small raw difference
        // is actually meaningful) get amplified; words that are naturally noisy
        // get down-weighted. This is the standard technique in real authorship
        // attribution research.
        double delta = computeBurrowsDelta(m1, m2);

        // Delta has no fixed upper bound (0 = identical style, typically
        // 1-3+ for genuinely different authors). Map it to a 0-100% similarity
        // with smooth exponential decay so delta=0 -> 100% and it falls off
        // as delta grows, without ever going negative.
        double deltaSimilarity = Math.exp(-delta); // 0.0-1.0

        // Delta carries most of the weight; the scalar stats are a secondary,
        // supporting signal.
        double combined = (deltaSimilarity * 0.7) + (scalarSimilarity * 0.3);

        return Math.max(0.0, Math.min(combined * 100.0, 100.0));
    }

    private double computeBurrowsDelta(StylometricMetrics m1, StylometricMetrics m2) {
        // Pool every chunk's per-word rate from BOTH texts to build the
        // mean/std baseline for each function word.
        List<Map<String, Integer>> chunks1 = m1.functionWordChunks();
        List<Integer> sizes1 = m1.chunkSizes();
        List<Map<String, Integer>> chunks2 = m2.functionWordChunks();
        List<Integer> sizes2 = m2.chunkSizes();

        double deltaSum = 0.0;
        int wordsUsed = 0;

        for (String word : MetricsCalculator.FUNCTION_WORDS) {
            List<Double> allChunkRates = new ArrayList<>();
            List<Double> text1Rates = new ArrayList<>();
            List<Double> text2Rates = new ArrayList<>();

            for (int i = 0; i < chunks1.size(); i++) {
                double rate = chunks1.get(i).getOrDefault(word, 0) / (double) sizes1.get(i);
                allChunkRates.add(rate);
                text1Rates.add(rate);
            }
            for (int i = 0; i < chunks2.size(); i++) {
                double rate = chunks2.get(i).getOrDefault(word, 0) / (double) sizes2.get(i);
                allChunkRates.add(rate);
                text2Rates.add(rate);
            }

            double mean = average(allChunkRates);
            double std = populationStdDev(allChunkRates, mean);

            // A word with (near) zero variance across every chunk carries no
            // discriminating information (or would cause a divide-by-zero) -
            // skip it rather than let it distort the average.
            if (std < 1e-9) continue;

            double text1AvgRate = average(text1Rates);
            double text2AvgRate = average(text2Rates);

            double z1 = (text1AvgRate - mean) / std;
            double z2 = (text2AvgRate - mean) / std;

            deltaSum += Math.abs(z1 - z2);
            wordsUsed++;
        }

        return wordsUsed == 0 ? 0.0 : deltaSum / wordsUsed;
    }

    private double average(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.size();
    }

    private double populationStdDev(List<Double> values, double mean) {
        if (values.isEmpty()) return 0.0;
        double sumSq = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / values.size());
    }
}