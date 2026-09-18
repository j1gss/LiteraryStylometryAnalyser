package com.stylometry;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public List<String> tokenizeWords(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        // Remove punctuation, lowercase, and split by whitespace
        String cleaned = text.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase();
        String[] tokens = cleaned.split("\\s+");
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isBlank()) words.add(token);
        }
        return words;
    }

    public List<String> tokenizeSentences(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();
        // Split on terminal punctuation
        String[] rawSentences = text.split("(?<=[.!?])\\s+");
        List<String> sentences = new ArrayList<>();
        for (String s : rawSentences) {
            if (!s.isBlank()) sentences.add(s.trim());
        }
        return sentences;
    }
}