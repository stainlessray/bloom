package com.bloomfilter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class for persisting Bloom filters and word lists safely.
 * Adds baseline input validation and directory handling.
 */
public final class FilterIO {

    private FilterIO() {}

    // ------------------------------------------------------------
    // SAVE / LOAD FILTERS
    // ------------------------------------------------------------

    /** Saves a Bloom filter's serialized bytes to disk. */
    public static void saveToFile(MembershipFilter<?> filter, String path) throws IOException {
        Path filePath = Paths.get(path);
        ensureParentExists(filePath);
        try (FileOutputStream out = new FileOutputStream(filePath.toFile())) {
            out.write(filter.toBytes());
        }
    }

    /** Loads a Bloom filter's serialized state from disk. */
    public static <T> void loadFromFile(MembershipFilter<T> filter, String path) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + path);
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException("File not readable: " + path);
        }
        byte[] data = Files.readAllBytes(filePath);
        filter.fromBytes(data);
    }

    // ------------------------------------------------------------
    // LOAD / SAVE WORD LISTS
    // ------------------------------------------------------------

    /** Loads a simple text word list (one item per line, '#' for comments). */
    public static List<String> loadWordList(String path) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + path);
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException("File not readable: " + path);
        }

        List<String> words = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(line);
                }
            }
        }
        return words;
    }

    /** Writes a collection of words to disk (one per line). */
    public static void saveWordList(Collection<String> words, String path) throws IOException {
        Path filePath = Paths.get(path);
        ensureParentExists(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            for (String word : words) {
                writer.write(word);
                writer.newLine();
            }
        }
    }

    // ------------------------------------------------------------
    // INTERNAL UTILITIES
    // ------------------------------------------------------------

    /** Ensures the parent directory exists before writing a file. */
    private static void ensureParentExists(Path filePath) throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
            System.out.printf("[Created directory] %s%n", parent);
        }
    }
}
