package com.bloomfilter;

import org.junit.jupiter.api.Test;

class PartitionedBloomFilterTest {

    @Test
    void visualPartitionedFilterDemo() {
        System.out.println("\n=== PartitionedBloomFilter Visual Demo ===");
        PartitionedBloomFilter<String> filter = new PartitionedBloomFilter<>(4, 16, 3);
        filter.setVerbose(true);

        System.out.println("\n--- Adding Elements ---");
        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");
        filter.add("date");

        System.out.println("\n--- Serialize / Deserialize ---");
        byte[] data = filter.toBytes();
        PartitionedBloomFilter<String> copy = new PartitionedBloomFilter<>(4, 16, 3);
        copy.setVerbose(true);
        copy.fromBytes(data);

        System.out.println("\n--- Query Elements ---");
        System.out.printf("apple?  %s%n", copy.mightContain("apple"));
        System.out.printf("banana? %s%n", copy.mightContain("banana"));
        System.out.printf("grape?  %s%n", copy.mightContain("grape"));

        System.out.println("\n--- False-Positive Rate ---");
        System.out.printf("Estimated FPR: %.6f%n", filter.estimateFalsePositiveRate());
    }
}
