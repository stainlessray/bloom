package com.bloomfilter;

import org.junit.jupiter.api.Test;

class ClassicBloomFilterTest {

    @Test
    void visualClassicFilterDemo() {
        System.out.println("\n=== ClassicBloomFilter Visual Demo ===");
        ClassicBloomFilter<String> filter = new ClassicBloomFilter<>(32, 3);
        filter.setVerbose(true);

        System.out.println("\n--- Adding Elements ---");
        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");

        System.out.println("\n--- Query Elements ---");
        System.out.printf("apple?  %s%n", filter.mightContain("apple"));
        System.out.printf("banana? %s%n", filter.mightContain("banana"));
        System.out.printf("grape?  %s%n", filter.mightContain("grape"));

        System.out.println("\n--- Serialize / Deserialize ---");
        byte[] data = filter.toBytes();
        ClassicBloomFilter<String> copy = new ClassicBloomFilter<>(32, 3);
        copy.setVerbose(true);
        copy.fromBytes(data);

        System.out.println("\n--- Query on Deserialized Copy ---");
        System.out.printf("apple?  %s%n", copy.mightContain("apple"));
        System.out.printf("grape?  %s%n", copy.mightContain("grape"));

        System.out.println("\n--- False-Positive Rate ---");
        System.out.printf("Estimated FPR: %.6f%n", filter.estimateFalsePositiveRate());
    }
}
