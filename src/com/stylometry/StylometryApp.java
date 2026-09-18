package com.stylometry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class StylometryApp {
    private static final Tokenizer tokenizer = new Tokenizer();
    private static final MetricsCalculator calculator = new MetricsCalculator();
    private static final AuthorshipComparator comparator = new AuthorshipComparator();
    private static final ReportWriter reportWriter = new ReportWriter();

    static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("   LITERARY STYLOMETRY & AUTHORSHIP ANALYZER     ");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Analyze Single Text File");
            System.out.println("2. Compare Two Texts for Authorship Similarity");
            System.out.println("3. Exit");
            System.out.print("> Choice: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String choice = scanner.nextLine().trim();

            if (choice.isEmpty()) {
                continue;
            }

            switch (choice) {
                case "1" -> handleSingleAnalysis(scanner);
                case "2" -> handleComparison(scanner);
                case "3" -> {
                    System.out.println("Exiting Stylometry Analyzer. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice: '" + choice + "'. Please enter 1, 2, or 3.");
            }
        }
    }

    private static void handleSingleAnalysis(Scanner scanner) {
        System.out.print("Enter file path: ");
        String pathStr = scanner.nextLine().trim();
        try {
            String content = Files.readString(Path.of(pathStr));
            TextCorpus corpus = buildCorpus("Work 1", "Unknown", content);
            StylometricMetrics metrics = calculator.computeMetrics(corpus.words(), corpus.sentences(), content);

            System.out.println("\nStylometric Profile");
            System.out.print(metrics);

            System.out.print("\nExport report to file? (y/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.print("Enter target output path (e.g., report.txt): ");
                String outPath = scanner.nextLine().trim();
                reportWriter.exportReport(outPath, metrics.toString());
                System.out.println("Report written successfully to: " + outPath);
            }
        } catch (IOException e) {
            System.out.println("File read/write error: " + e.getMessage());
        }
    }

    private static void handleComparison(Scanner scanner) {
        try {
            System.out.print("Enter Path for Text 1: ");
            String text1 = Files.readString(Path.of(scanner.nextLine().trim()));
            System.out.print("Enter Path for Text 2: ");
            String text2 = Files.readString(Path.of(scanner.nextLine().trim()));

            TextCorpus c1 = buildCorpus("Text 1", "Author A", text1);
            TextCorpus c2 = buildCorpus("Text 2", "Author B", text2);

            StylometricMetrics m1 = calculator.computeMetrics(c1.words(), c1.sentences(), text1);
            StylometricMetrics m2 = calculator.computeMetrics(c2.words(), c2.sentences(), text2);

            double similarity = comparator.computeSimilarity(m1, m2);

            System.out.println("\nComparative Authorship Analysis");
            System.out.printf("Text 1 Words: %d | Avg Sent Len: %.2f | MATTR: %.4f%n",
                    m1.totalWordCount(), m1.avgSentenceLength(), m1.typeTokenRatio());
            System.out.printf("Text 2 Words: %d | Avg Sent Len: %.2f | MATTR: %.4f%n",
                    m2.totalWordCount(), m2.avgSentenceLength(), m2.typeTokenRatio());
            System.out.printf("Calculated Authorship Stylistic Similarity: %.2f%%%n", similarity);

        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }

    private static TextCorpus buildCorpus(String title, String author, String content) {
        List<String> words = tokenizer.tokenizeWords(content);
        List<String> sentences = tokenizer.tokenizeSentences(content);
        return new TextCorpus(title, author, content, words, sentences);
    }
}