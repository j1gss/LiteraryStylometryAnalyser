# Project Statement

## Problem Statement

Deciding whether two texts were written by the same person usually comes
down to a reader's impression — something about the tone or vocabulary
feels similar, or it doesn't. That works fine for a single comparison, but
it doesn't hold up across large amounts of text, it isn't reproducible
between readers, and it's hard to defend if someone pushes back on the
call. Cases like disputed authorship, plagiarism concerns, or checking
whether ghostwritten text matches a claimed author all run into this
problem.

This project is a Java command-line tool that measures style instead of
guessing at it. It pulls out features like lexical diversity (MATTR),
sentence length, Flesch-Kincaid readability, and function-word usage
patterns, then uses those to build a profile of a single document or a
similarity score between two. The similarity score is built on Burrows'
Delta, an established technique from authorship-attribution research, so
the output is a repeatable number rather than a subjective read.

## Scope of the Project

The tool works on plain-text English novels or similar prose. It can
profile a single document or compare two, and it can export a profile to a
text report. All of this runs from a command-line menu.

It doesn't handle other languages — the tokenizer and function-word list
are English-specific — and it only accepts `.txt` files, so a PDF or DOCX
would need to be converted first. It also doesn't compare more than two
documents at once, and it isn't meant to produce a legal-grade proof of
authorship. The similarity score is evidence, not a verdict, and should be
read that way.

## Target Users

Students and literature enthusiasts curious about computational approaches
to writing style fall into this category, along with educators who teach
stylometry or digital-humanities courses and want a working example to show
students. Writers and editors who want to measure their own style, and
researchers looking for a small, dependency-free reference implementation
of these techniques, are also a reasonable fit.

## High-Level Features

The single-text mode reports word count, MATTR, average sentence length,
Flesch-Kincaid grade, and top word frequencies, with an option to export
the results. The comparison mode computes an authorship similarity score
between two texts, built mainly on Burrows' Delta over function-word usage
and backed up by the scalar features. Both metrics are designed to stay
valid even when the two texts are very different lengths — a 46,000-word
novel against a 190,000-word one, for instance — which was a real problem
in an earlier version of this project (see the report's testing and
challenges sections). The comparison approach was checked against known
same-author and different-author book pairs before being trusted, rather
than assumed to work.
