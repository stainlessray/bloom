package com.bloomfilter;

import org.junit.jupiter.api.Test;

class CountingBloomFilterTest {

    @Test
    void visualCountingFilterDemo() {
        System.out.println("\n=== CountingBloomFilter Visual Demo ===");
        CountingBloomFilter<String> filter = new CountingBloomFilter<>(32, 3);
        filter.setVerbose(true);

        System.out.println("\n--- Adding Elements ---");
        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");

        System.out.println("\n--- Query Before Removal ---");
        System.out.printf("banana? %s%n", filter.mightContain("banana"));

        System.out.println("\n--- Removing banana ---");
        filter.remove("banana");

        System.out.println("\n--- Query After Removal ---");
        System.out.printf("banana? %s%n", filter.mightContain("banana"));
        System.out.printf("apple?  %s%n", filter.mightContain("apple"));

        System.out.println("\n--- Serialize / Deserialize ---");
        byte[] bytes = filter.toBytes();
        CountingBloomFilter<String> copy = new CountingBloomFilter<>(32, 3);
        copy.setVerbose(true);
        copy.fromBytes(bytes);

        System.out.println("\n--- Query on Deserialized Copy ---");
        System.out.printf("apple? %s%n", copy.mightContain("apple"));
        System.out.printf("banana? %s%n", copy.mightContain("banana"));

        System.out.println("\n--- False-Positive Rate ---");
        System.out.printf("Estimated FPR: %.6f%n", filter.estimateFalsePositiveRate());
    }
}
