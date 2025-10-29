package com.bloomfilter;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FilterIO {

    /** Save any filter using standardized binary serialization (ByteBuffer format). */
    public static void saveToFile(MembershipFilter<?> filter, String filename) throws IOException {
        Path path = Paths.get(filename);
        Files.createDirectories(path.getParent());

        if (!(filter instanceof AbstractBloomFilter<?> af)) {
            throw new IOException("Unsupported filter type for standardized save.");
        }

        byte[] data = af.toBytes();
        Files.write(path, data);
        System.out.printf("[Saved standardized filter] %s (%d bytes)%n",
                path.toAbsolutePath(), data.length);
    }

    /** Load any standardized binary filter back into the given filter instance. */
    public static void loadFromFile(MembershipFilter<?> filter, String filename) throws IOException {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) throw new IOException("File not found: " + filename);

        byte[] data = Files.readAllBytes(path);
        if (!(filter instanceof AbstractBloomFilter<?> af)) {
            throw new IOException("Unsupported filter type for standardized load.");
        }

        try {
            af.fromBytes(data);
            System.out.println("Standardized filter loaded successfully.");
        } catch (Exception e) {
            throw new IOException("Error loading standardized filter: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------------
    // Word list ingestion utilities
    // ------------------------------------------------------------------------

    /** Loads a text file into a list of strings (trimmed, non-empty lines). */
    public static List<String> loadWordList(String filename) throws IOException {
        return Files.readAllLines(Paths.get(filename))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .toList();
    }

    /** Ingest a text list and produce a standardized binary filter in the given output folder. */
    public static void ingestListToBinary(MembershipFilter<String> filter, String input, String outputDir) throws IOException {
        List<String> words = loadWordList(input);
        System.out.printf("Ingesting %d words from %s...%n", words.size(), input);

        for (String w : words) {
            filter.add(w);
        }

        String listName = Paths.get(input).getFileName().toString().replaceFirst("\\.txt$", "");
        String algo = filter.getClass().getSimpleName();
        String sizeInfo = "";

        if (filter instanceof AbstractBloomFilter<?> af) {
            int m = af.getBitArraySize();
            int k = af.getHashCount();
            sizeInfo = "_m" + m + "_k" + k;
        }

        if (filter instanceof PartitionedBloomFilter<?> pbf) {
            sizeInfo = String.format("_p%dx%d_k%d", pbf.getPartitionCount(),
                    pbf.getPartitionSize(), pbf.getHashCount());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = String.format("%s/%s_%s%s_v%s.bin", outputDir, listName, algo, sizeInfo, timestamp);

        Path outPath = Paths.get(filename);
        Files.createDirectories(outPath.getParent());

        assert filter instanceof AbstractBloomFilter<?>;
        byte[] data = filter.toBytes();
        Files.write(outPath, data);

        System.out.printf("[Standardized binary created] %s%n", outPath.toAbsolutePath());
        System.out.printf("Algorithm=%s | Bits=%d | Hashes=%d | Source=%s | Created=%s%n",
                algo,
                ((AbstractBloomFilter<?>) filter).getBitArraySize(),
                ((AbstractBloomFilter<?>) filter).getHashCount(),
                input,
                LocalDateTime.now());
        System.out.println("Ingestion complete and saved to " + outputDir);
    }
    // ------------------------------------------------------------------------
    // Metadata inspection
    // ------------------------------------------------------------------------

    /**
     * Reads standardized filter metadata without loading the full filter.
     * Works for any filter created with the standardized binary format.
     */
    public static void metadata(String filename) throws IOException {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) throw new IOException("File not found: " + filename);

        byte[] header = Files.readAllBytes(path);
        if (header.length < 16) throw new IOException("File too short to contain metadata");

        // These are the first fields written in AbstractBloomFilter / PartitionedBloomFilter
        int first = ((header[0] & 0xFF) << 24) | ((header[1] & 0xFF) << 16)
                | ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
        int second = ((header[4] & 0xFF) << 24) | ((header[5] & 0xFF) << 16)
                | ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        int third = ((header[8] & 0xFF) << 24) | ((header[9] & 0xFF) << 16)
                | ((header[10] & 0xFF) << 8) | (header[11] & 0xFF);

        System.out.printf("Metadata (approx): m=%d | k=%d | n≈%d%n", first, second, third);
    }

}
