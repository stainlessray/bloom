package com.bloomfilter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Metadata header stored with each serialized Bloom filter (.bin).
 */
public class FilterMetadata implements Serializable {
    private final String algorithm;
    private final int bitArraySize;
    private final int hashCount;
    private final String sourceFile;
    private final String createdAt;

    public FilterMetadata(String algorithm, int bitArraySize, int hashCount, String sourceFile) {
        this.algorithm = algorithm;
        this.bitArraySize = bitArraySize;
        this.hashCount = hashCount;
        this.sourceFile = sourceFile;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String summary() {
        return String.format(
                "Algorithm=%s | Bits=%d | Hashes=%d | Source=%s | Created=%s",
                algorithm, bitArraySize, hashCount, sourceFile, createdAt);
    }

    @Override
    public String toString() {
        return summary();
    }
}
