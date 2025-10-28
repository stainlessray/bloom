package com.bloomfilter.demo;

import com.bloomfilter.*;

import java.io.IOException;
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
    private static String green(String msg) { return "\u001B[32m" + msg + "\u001B[0m"; }
    private static String red(String msg)   { return "\u001B[31m" + msg + "\u001B[0m"; }


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

                case "save":
                    if (arg == null) {
                        System.out.println("Usage: save <filename>");
                        break;
                    }
                    try {
                        FilterIO.saveToFile(filter, arg);
                        System.out.println(green("Filter saved to " + arg));
                    } catch (IOException e) {
                        System.out.println(red("Error saving: " + e.getMessage()));
                    }
                    break;

                case "load":
                    if (arg == null) {
                        System.out.println(red("Usage: load <filename>"));
                        break;
                    }
                    try {
                        FilterIO.loadFromFile(filter, arg);
                        System.out.println(green("Filter loaded from " + arg));
                        visualize();
                    } catch (IOException e) {
                        System.out.println(red("Error loading: " + e.getMessage()));
                    }
                    break;

                case "loadlist":
                    if (arg == null) {
                        System.out.println(green("Usage: loadlist <filename>"));
                        break;
                    }
                    try {
                        var words = FilterIO.loadWordList(arg);
                        System.out.println(green(String.format("Loaded %d words from %s", words.size(), arg)));
                        for (String w : words) {
                            filter.add(w);
                        }
                        visualize();
                    } catch (IOException e) {
                        System.out.println(red("Error loading list: " + e.getMessage()));
                    }
                    break;

                case "crossload":
                    if (arg == null) {
                        System.out.println(green("Usage: crossload <filename>"));
                        break;
                    }
                    try {
                        FilterIO.populateFromList(filter, arg);
                        System.out.println(green(String.format(
                                "Cross-loaded %s filter with list from %s",
                                mode, arg)));
                        visualize();
                    } catch (IOException e) {
                        System.out.println(red("Error cross-loading: " + e.getMessage()));
                    }
                    break;

                case "ingestlist":
                    if (arg == null) {
                        System.out.println(green("Usage: ingestlist <input.txt> <output.bin>"));
                        break;
                    }
                    String[] files = arg.split("\\s+");
                    if (files.length < 2) {
                        System.out.println(red("Usage: ingestlist <input.txt> <output.bin>"));
                        break;
                    }
                    String inputList = files[0];
                    String outputBin = files[1];
                    try {
                        FilterIO.ingestListToBinary(filter, inputList, outputBin);
                        System.out.println(green("Ingestion complete and saved to " + outputBin));
                        visualize();
                    } catch (IOException e) {
                        System.out.println(red("Error during ingestion: " + e.getMessage()));
                    }
                    break;



                case "help":
                    printHelp();
                    break;

                case "exit":
                case "quit":
                    System.out.println("Goodbye!");
                    return;

                default:
                    System.out.println(red("Unknown command. Type 'help' for list."));
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
              add <word>             – insert element
              check <word>           – test membership
              remove <word>          – remove (only in counting mode)
              clear                  – reset filter
              mode <type>            – switch between classic|counting|partitioned
              info                   – show current statistics
              save <filename>        – save current filter to file
              load <filename>        – load saved filter from file
              loadlist <file>        – load a plain-text word list
              ingestlist <txt> <bin> – convert plain list to standardized binary filter
              crossload <file>       – repopulate this mode from a word list
              help                   – show this list
              exit                   – quit the demo
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
