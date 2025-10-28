package com.bloomfilter.demo;

import com.bloomfilter.ClassicBloomFilter;
import com.bloomfilter.CountingBloomFilter;
import com.bloomfilter.PartitionedBloomFilter;

import java.util.LinkedHashMap;
import java.util.Map;

public class MasterVisualDemo {

    public static void main(String[] args) {
        System.out.println("\n=======================================");
        System.out.println(" BLOOM FILTER FAMILY — VISUAL DEMO ");
        System.out.println("=======================================\n");

        Map<String, Stats> results = new LinkedHashMap<>();

        results.put("Classic", runClassic());
        results.put("Counting", runCounting());
        results.put("Partitioned", runPartitioned());

        printSummary(results);

        System.out.println("\n=======================================");
        System.out.println(" DEMO COMPLETE ");
        System.out.println("=======================================\n");
    }

    private static Stats runClassic() {
        System.out.println("\n--- [ClassicBloomFilter] ---");
        ClassicBloomFilter<String> filter = new ClassicBloomFilter<>(64, 3);
        filter.setVerbose(true);

        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");

        System.out.println("\nQuery:");
        System.out.printf("apple?  %s%n", filter.mightContain("apple"));
        System.out.printf("grape?  %s%n", filter.mightContain("grape"));

        double fpr = filter.estimateFalsePositiveRate();
        long bits = 64;
        long bytes = bits / 8;

        return new Stats("Classic", fpr, filter.getEstimatedCount(), bits, bytes);
    }

    private static Stats runCounting() {
        System.out.println("\n--- [CountingBloomFilter] ---");
        CountingBloomFilter<String> filter = new CountingBloomFilter<>(64, 3);
        filter.setVerbose(true);

        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");

        System.out.println("\nBefore removal:");
        System.out.printf("banana? %s%n", filter.mightContain("banana"));

        System.out.println("\nRemoving banana...");
        filter.remove("banana");

        System.out.println("After removal:");
        System.out.printf("banana? %s%n", filter.mightContain("banana"));
        System.out.printf("apple?  %s%n", filter.mightContain("apple"));

        double fpr = filter.estimateFalsePositiveRate();
        long bits = 64;                     // logical bits
        long bytes = bits / 8 * Integer.BYTES;  // approximate counter memory

        return new Stats("Counting", fpr, filter.getEstimatedCount(), bits, bytes);
    }

    private static Stats runPartitioned() {
        System.out.println("\n--- [PartitionedBloomFilter] ---");
        PartitionedBloomFilter<String> filter = new PartitionedBloomFilter<>(4, 32, 3);
        filter.setVerbose(true);

        filter.add("apple");
        filter.add("banana");
        filter.add("cherry");
        filter.add("date");

        System.out.println("\nQuery:");
        System.out.printf("apple?  %s%n", filter.mightContain("apple"));
        System.out.printf("banana? %s%n", filter.mightContain("banana"));
        System.out.printf("grape?  %s%n", filter.mightContain("grape"));

        double fpr = filter.estimateFalsePositiveRate();
        long bits = 4L * 32L;
        long bytes = bits / 8;

        return new Stats("Partitioned", fpr, filter.getEstimatedCount(), bits, bytes);
    }

    // ---------------- Summary ----------------

    private static void printSummary(Map<String, Stats> results) {
        System.out.println("\n=======================================");
        System.out.println(" SUMMARY COMPARISON ");
        System.out.println("=======================================\n");

        System.out.printf("%-15s %-10s %-12s %-12s %-12s%n",
                "Filter", "Count", "Bits", "Bytes(est)", "FPR(est)");
        System.out.println("-----------------------------------------------------------");

        for (Stats s : results.values()) {
            System.out.printf("%-15s %-10d %-12d %-12d %-12.6f%n",
                    s.name, s.count, s.bits, s.bytes, s.fpr);
        }

        System.out.println("\nApproximate False-Positive Rate Comparison:");
        double maxFpr = results.values().stream()
                .mapToDouble(r -> r.fpr)
                .max().orElse(1.0);

        for (Stats s : results.values()) {
            int barLen = (int) Math.round((s.fpr / maxFpr) * 40);
            String bar = "█".repeat(Math.max(barLen, 1));
            System.out.printf("%-12s | %s %.6f%n", s.name, bar, s.fpr);
        }
    }

    // ---------------- Helper Record ----------------
    private static class Stats {
        String name;
        double fpr;
        long count;
        long bits;
        long bytes;

        Stats(String name, double fpr, long count, long bits, long bytes) {
            this.name = name;
            this.fpr = fpr;
            this.count = count;
            this.bits = bits;
            this.bytes = bytes;
        }
    }
}
