package com.bloomfilter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Utility class for persisting Bloom filters and word lists safely.
 * Includes baseline input validation and cross-population helpers.
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
        System.out.printf("[Saved filter] %s (%d bytes)%n",
                filePath.toAbsolutePath(), Files.size(filePath));
    }

    /** Loads a Bloom filter's serialized state from disk. */
    public static <T> void loadFromFile(MembershipFilter<T> filter, String path) throws IOException {
        Path filePath = Paths.get(path);
        validateReadable(filePath);
        byte[] data = Files.readAllBytes(filePath);
        filter.fromBytes(data);
        System.out.printf("[Loaded filter] %s (%d bytes)%n",
                filePath.toAbsolutePath(), data.length);
    }

    // ------------------------------------------------------------
    // LOAD / SAVE WORD LISTS
    // ------------------------------------------------------------

    /** Loads a simple text word list (one item per line, '#' for comments). */
    public static List<String> loadWordList(String path) throws IOException {
        Path filePath = Paths.get(path);
        validateReadable(filePath);

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
        System.out.printf("[Saved list] %s (%d items)%n",
                filePath.toAbsolutePath(), words.size());
    }

    // ------------------------------------------------------------
    // CROSS-FILTER POPULATION
    // ------------------------------------------------------------

    /** Populates any filter with all words from a text list. */
    public static void populateFromList(MembershipFilter<String> filter, String path) throws IOException {
        var words = loadWordList(path);
        System.out.printf("Populating %s with %d items from %s%n",
                filter.getClass().getSimpleName(), words.size(), path);
        for (String w : words) filter.add(w);
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

    /** Validates that a path exists and is readable. */
    private static void validateReadable(Path path) throws IOException {
        if (!Files.exists(path))
            throw new FileNotFoundException("File not found: " + path);
        if (!Files.isReadable(path))
            throw new IOException("File not readable: " + path);
    }

    // ------------------------------------------------------------
// INGESTION & STANDARDIZATION
// ------------------------------------------------------------

    /**
     * Builds a Bloom filter from a plain text list and saves as standardized binary.
     */
    public static void ingestListToBinary(MembershipFilter<String> filter, String txtPath, String binPath)
            throws IOException {
        var words = loadWordList(txtPath);
        System.out.printf("Ingesting %d words from %s...%n", words.size(), txtPath);

        for (String w : words) filter.add(w);

        // --- Save metadata ---
        FilterMetadata meta = new FilterMetadata(
                filter.getClass().getSimpleName(),
                (filter instanceof AbstractBloomFilter<?> af) ? af.getBitArraySize() : -1,
                (filter instanceof AbstractBloomFilter<?> af) ? af.getHashCount() : -1,
                txtPath
        );

        Path filePath = Paths.get(binPath);
        ensureParentExists(filePath);

        try (FileOutputStream out = new FileOutputStream(filePath.toFile());
             ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(meta);
            oos.writeObject(filter.toBytes());
        }

        System.out.printf("[Standardized binary created] %s%n%s%n",
                filePath.toAbsolutePath(), meta.summary());
    }

    /**
     * Loads a Bloom filter and metadata from a standardized binary (.bin).
     */
    @SuppressWarnings("unchecked")
    public static <T> FilterLoadResult<T> loadStandardizedBinary(MembershipFilter<T> filter, String path)
            throws IOException, ClassNotFoundException {
        Path filePath = Paths.get(path);
        validateReadable(filePath);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            FilterMetadata meta = (FilterMetadata) ois.readObject();
            byte[] data = (byte[]) ois.readObject();
            filter.fromBytes(data);
            return new FilterLoadResult<>(meta, filter);
        }
    }

    /** Small holder for loaded filter and its metadata. */
    public record FilterLoadResult<T>(FilterMetadata metadata, MembershipFilter<T> filter) {}

}
