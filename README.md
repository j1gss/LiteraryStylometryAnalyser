# Literary Stylometry & Authorship Analyzer

A Java command-line tool that measures writing style. It reads a plain-text
file and works out things like sentence length, vocabulary richness, and
readability, or takes two files and estimates how likely they are to share
an author.

## Overview

Most people judge whether two pieces of writing "sound the same" by feel.
That doesn't scale past a book or two, and two people can disagree without
either being wrong. This project replaces that guesswork with numbers: word
counts, a length-independent lexical diversity score (MATTR), sentence
length, a Flesch-Kincaid readability grade, and a similarity score between
two texts based on function-word usage.

The comparison method is Burrows' Delta, which is the same statistic used
in real authorship-attribution work, including the analysis that helped
settle who wrote which of the disputed Federalist Papers. It checks how far
a text's usage of common words like "the," "and," or "but" deviates from a
baseline, rather than comparing raw word counts directly.

## Features

Running the program gives you two options.

The first analyzes a single text and prints its word count, MATTR score,
average sentence length, Flesch-Kincaid grade, and top word frequencies. You
can also export that profile to a text file.

The second compares two texts and prints a similarity percentage. Under the
hood it splits each text into 1,000-word chunks, tracks how about 40 common
function words behave across those chunks, and uses that to compute Burrows'
Delta. That score gets combined with the scalar features (MATTR, sentence
length, readability) into one final number.

Testing against real novels shows the tool consistently rates two books by
the same author higher (82-86% in testing) than two books by different
authors (67-70%). Details and numbers are in `statement.md` and the project
report.

## Technologies / Tools Used

Written in plain Java 17, no external libraries. Just `java.util`,
`java.nio.file`, and `java.util.stream` from the standard library.

## Project Structure

```
com/stylometry/
├── StylometryApp.java           # CLI entry point / menu
├── TextCorpus.java              # Holds a text's raw content + tokenized words/sentences
├── Tokenizer.java                # Splits raw text into words and sentences
├── MetricsCalculator.java        # Computes MATTR, sentence length, Flesch-Kincaid,
│                                  #   word frequencies, and function-word chunk data
├── StylometricMetrics.java       # Data object holding a single text's computed metrics
├── AuthorshipComparator.java     # Computes Burrows' Delta + composite similarity score
└── ReportWriter.java             # Exports a single-text profile to a .txt file
```

## Steps to Install & Run

You need a JDK, version 17 or later. Check with `java -version` and
`javac -version`.

Compile from the project root (the folder containing `com/`):

```bash
javac com/stylometry/*.java -d build
```

Run it:

```bash
java -cp build com.stylometry.StylometryApp
```

You'll get a menu:

```
   LITERARY STYLOMETRY & AUTHORSHIP ANALYZER

Select an option:
1. Analyze Single Text File
2. Compare Two Texts for Authorship Similarity
3. Exit
> Choice:
```

Option 1 asks for a file path and prints the profile, with a chance to
export it. Option 2 asks for two file paths and prints the similarity score.
Text files should be plain UTF-8 `.txt` — public-domain novels from
[Project Gutenberg](https://www.gutenberg.org) work well for testing.

## Instructions for Testing

Download a couple of novels as `.txt` files. Run Option 2 on two books by
the same author first — that's your positive control, and the score should
land on the higher side. Then run it on two books by different authors as a
negative control; the score should drop noticeably. If the gap between those
two runs isn't there, something's off with the comparison logic rather than
the books you picked. Option 1 is worth running too, mostly as a sanity
check that word counts and readability numbers look reasonable for a text
whose length you already know.

## Screenshots

![Menu]([https://github.com/j1gss/LiteraryStylometryAnalyser/blob/master/ss/Menu.png])
![Choice 1]([https://github.com/j1gss/LiteraryStylometryAnalyser/blob/master/ss/Choice%201.png])
![Choice 2-Same Author]([https://github.com/j1gss/LiteraryStylometryAnalyser/blob/master/ss/Choice%202-Same%20Author.png])
![Choice 2-Different Authors]([https://github.com/j1gss/LiteraryStylometryAnalyser/blob/master/ss/Choice%202-Different%20Authors.png])
