package com.bloomfilter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class HashUtilsTest {

    @Test
    void visualHashDemo() {
        System.out.println("\n=== HashUtils Visual Demo ===");
        String word = "apple";
        long[] hash = HashUtils.hash128(word);
        System.out.printf("Input: %s%n128-bit hash: %s%n", word, Arrays.toString(hash));

        int[] indices = HashUtils.generateIndices(hash, 5, 32);
        System.out.printf("Generated 5 indices (mod 32): %s%n", Arrays.toString(indices));
    }
}
