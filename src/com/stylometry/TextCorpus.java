package com.stylometry;

import java.util.List;

public record TextCorpus(String title, String author, String rawContent, List<String> words, List<String> sentences) {
}