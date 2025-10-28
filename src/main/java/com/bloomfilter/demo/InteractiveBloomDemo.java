package com.bloomfilter.demo;

import com.bloomfilter.*;

import java.util.BitSet;
import java.util.Scanner;

/**
 * Interactive console demo for learning Bloom filters.
 * Commands:
 *   mode classic|counting|partitioned
 *   add <word>
 *   check <word>
 *   remove <word>
 *   clear
 *   info
 *   help
 *   exit
 */
public class InteractiveBloomDemo {

    private static MembershipFilter<String> filter;
    private static String mode = "classic";

    public static void main(String[] args) {
        System.out.println("\n=======================================");
        System.out.println(" BLOOM FILTER INTERACTIVE DEMO ");
        System.out.println("=======================================\n");

        changeMode("classic");

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : null;

            switch (cmd) {
                case "add":
                    if (arg == null) {
                        System.out.println("Usage: add <word>");
                        break;
                    }
                    filter.add(arg);
                    visualize();
                    break;

                case "check":
                case "query":
                    if (arg == null) {
                        System.out.println("Usage: check <word>");
                        break;
                    }
                    boolean result = filter.mightContain(arg);
                    System.out.printf("Result: %s → %s%n",
                            arg, result ? "possibly in set" : "definitely not");
                    visualize();
                    break;

                case "remove":
                    if (arg == null) {
                        System.out.println("Usage: remove <word>");
                        break;
                    }
                    if (filter instanceof CountingBloomFilter<?> cf) {
                        @SuppressWarnings("unchecked")
                        CountingBloomFilter<String> countFilter = (CountingBloomFilter<String>) cf;
                        countFilter.remove(arg);
                        visualize();
                    } else {
                        System.out.println("Removal only supported in 'counting' mode.");
                    }
                    break;


                case "clear":
                    filter.clear();
                    System.out.println("Filter cleared.");
                    visualize();
                    break;

                case "mode":
                    if (arg == null) {
                        System.out.printf("Current mode: %s%n", mode);
                        break;
                    }
                    changeMode(arg.toLowerCase());
                    visualize();
                    break;

                case "info":
                    printInfo();
                    break;

                case "help":
                    printHelp();
                    break;

                case "exit":
                case "quit":
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println("Unknown command. Type 'help' for list.");
            }
        }
    }

    // ------------------------------------------------------------------------

    private static void changeMode(String newMode) {
        switch (newMode) {
            case "classic" -> filter = new ClassicBloomFilter<>(64, 3);
            case "counting" -> filter = new CountingBloomFilter<>(64, 3);
            case "partitioned" -> filter = new PartitionedBloomFilter<>(4, 32, 3);
            default -> {
                System.out.println("Unknown mode. Options: classic, counting, partitioned");
                return;
            }
        }
        ((AbstractBloomFilter<?>) filter).setVerbose(false);
        mode = newMode;
        System.out.printf("Switched to %s mode.%n", mode);
    }

    private static void printInfo() {
        if (!(filter instanceof AbstractBloomFilter<?> af)) {
            System.out.println("No info available.");
            return;
        }
        System.out.printf("Mode: %s | Elements added: %d | Est. FPR: %.6f%n",
                mode, af.getEstimatedCount(), af.estimateFalsePositiveRate());
    }

    private static void printHelp() {
        System.out.println("""
            Commands:
              add <word>        – insert element
              check <word>      – test membership
              remove <word>     – remove (only in counting mode)
              clear             – reset filter
              mode <type>       – switch between classic|counting|partitioned
              info              – show current statistics
              help              – show this list
              exit              – quit the demo
            """);
    }

    // ------------------------------------------------------------------------
    // Visualization

    private static void visualize() {
        System.out.println();
        if (filter instanceof ClassicBloomFilter<?> cbf) {
            visualizeClassic(cbf);
        } else if (filter instanceof CountingBloomFilter<?> ctf) {
            visualizeCounting(ctf);
        } else if (filter instanceof PartitionedBloomFilter<?> pbf) {
            visualizePartitioned(pbf);
        }
    }

    private static void visualizeClassic(ClassicBloomFilter<?> f) {
        try {
            var bitsetField = ClassicBloomFilter.class.getDeclaredField("bitSet");
            bitsetField.setAccessible(true);
            BitSet bits = (BitSet) bitsetField.get(f);
            int size = f.getBitArraySize();
            System.out.print("Bits: ");
            for (int i = 0; i < size; i++) {
                System.out.print(bits.get(i) ? "█" : "·");
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("[Visualization unavailable]");
        }
    }

    private static void visualizeCounting(CountingBloomFilter<?> f) {
        try {
            var counterField = CountingBloomFilter.class.getDeclaredField("counters");
            counterField.setAccessible(true);
            int[] counters = (int[]) counterField.get(f);
            System.out.print("Counters: ");
            for (int c : counters) {
                if (c == 0) System.out.print("·");
                else if (c < 10) System.out.print(c);
                else System.out.print("*");
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("[Visualization unavailable]");
        }
    }

    private static void visualizePartitioned(PartitionedBloomFilter<?> f) {
        try {
            var partsField = PartitionedBloomFilter.class.getDeclaredField("partitions");
            partsField.setAccessible(true);
            Object[] parts = (Object[]) partsField.get(f);
            System.out.println("Partitions:");
            for (int i = 0; i < parts.length; i++) {
                var bitsetField = ClassicBloomFilter.class.getDeclaredField("bitSet");
                bitsetField.setAccessible(true);
                BitSet bits = (BitSet) bitsetField.get(parts[i]);
                System.out.printf("  P%d: ", i);
                for (int j = 0; j < bits.length(); j++) {
                    System.out.print(bits.get(j) ? "█" : "·");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("[Visualization unavailable]");
        }
    }
}
