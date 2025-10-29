package com.bloomfilter;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FilterIO}.
 * Focuses on file operations, validation logic, and metadata inspection.
 */
class FilterIOTest {

    private static class DummyFilter extends AbstractBloomFilter<String> {

        private byte[] data = new byte[]{1, 2, 3, 4};

        public DummyFilter() {
            super(8, 2); // arbitrary small values
        }

        // Core bit array hooks (no-op stubs)
        @Override
        protected int[] getHashIndices(String element) {
            // Deterministic stub for testing
            return new int[]{0, 1};
        }

        @Override
        protected void setBit(int index) {
            // No bit array used in dummy
        }

        @Override
        protected boolean getBit(int index) {
            return true; // Always true for simplicity
        }

        @Override
        protected void clearBit(int index) {
            // No-op
        }

        // Serialization stubs
        @Override
        public byte[] toBytes() {
            return data;
        }

        @Override
        public void fromBytes(byte[] bytes) {
            this.data = bytes;
        }
    }



    private Path tempDir;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("filterio-test");
    }

    @AfterEach
    void teardown() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> p.toFile().delete());
    }

    @Test
    @DisplayName("saveToFile should write bytes and create directories")
    void testSaveToFile() throws IOException {
        DummyFilter filter = new DummyFilter();
        Path out = tempDir.resolve("test.bin");

        FilterIO.saveToFile(filter, out.toString());

        assertTrue(Files.exists(out), "Output file should exist after save");
        byte[] content = Files.readAllBytes(out);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, content, "File content should match filter bytes");
    }

    @Test
    @DisplayName("saveToFile should reject unsupported filter types")
    void testSaveToFileUnsupportedType() {
        MembershipFilter<String> badFilter = new MembershipFilter<>() {
            @Override
            public void add(String element) { }

            @Override
            public boolean mightContain(String element) {
                return false;
            }

            /**
             * Clears all state in the filter, resetting it to its initial empty state.
             */
            @Override
            public void clear() {

            }

            /**
             * Returns the number of elements added to the filter since the last clear. Note that for
             * probabilistic filters this is a simple counter and may not reflect the number of unique
             * elements.
             *
             * @return estimated number of added elements
             */
            @Override
            public long getEstimatedCount() {
                return 0;
            }

            /**
             * Serializes the filter's internal state into a byte array. The returned data must be
             * sufficient to fully reconstruct the filter using {@link #fromBytes(byte[])}.
             *
             * @return serialized representation of this filter
             */
            @Override
            public byte[] toBytes() {
                return new byte[0];
            }

            /**
             * Restores the filter's state from the provided serialized representation. Implementations
             * must validate the input and throw an appropriate exception on invalid data.
             *
             * @param data serialized data produced by {@link #toBytes()}
             */
            @Override
            public void fromBytes(byte[] data) {

            }
        }; // lambda dummy, not AbstractBloomFilter
        IOException ex = assertThrows(IOException.class,
                () -> FilterIO.saveToFile(badFilter, tempDir.resolve("bad.bin").toString()));
        assertTrue(ex.getMessage().contains("Unsupported filter type"));
    }

    @Test
    @DisplayName("loadFromFile should restore bytes correctly")
    void testLoadFromFile() throws IOException {
        DummyFilter filter = new DummyFilter();
        Path file = tempDir.resolve("test-load.bin");
        Files.write(file, new byte[]{9, 8, 7, 6});

        FilterIO.loadFromFile(filter, file.toString());
        assertArrayEquals(new byte[]{9, 8, 7, 6}, filter.toBytes(),
                "Filter bytes should be replaced by loaded content");
    }

    @Test
    @DisplayName("loadFromFile should throw if file does not exist")
    void testLoadFromFileMissing() {
        DummyFilter filter = new DummyFilter();
        assertThrows(IOException.class, () -> FilterIO.loadFromFile(filter, tempDir.resolve("missing.bin").toString()));
    }

    @Test
    @DisplayName("loadFromFile should reject unsupported filter types")
    void testLoadFromFileUnsupportedType() throws IOException {
        Path file = tempDir.resolve("invalid.bin");
        Files.write(file, new byte[]{1, 2, 3});
        MembershipFilter<String> badFilter = new MembershipFilter<>() {
            @Override
            public void add(String element) { }

            @Override
            public boolean mightContain(String element) {
                return false;
            }

            /**
             * Clears all state in the filter, resetting it to its initial empty state.
             */
            @Override
            public void clear() {

            }

            /**
             * Returns the number of elements added to the filter since the last clear. Note that for
             * probabilistic filters this is a simple counter and may not reflect the number of unique
             * elements.
             *
             * @return estimated number of added elements
             */
            @Override
            public long getEstimatedCount() {
                return 0;
            }

            /**
             * Serializes the filter's internal state into a byte array. The returned data must be
             * sufficient to fully reconstruct the filter using {@link #fromBytes(byte[])}.
             *
             * @return serialized representation of this filter
             */
            @Override
            public byte[] toBytes() {
                return new byte[0];
            }

            /**
             * Restores the filter's state from the provided serialized representation. Implementations
             * must validate the input and throw an appropriate exception on invalid data.
             *
             * @param data serialized data produced by {@link #toBytes()}
             */
            @Override
            public void fromBytes(byte[] data) {

            }
        };

        IOException ex = assertThrows(IOException.class,
                () -> FilterIO.loadFromFile(badFilter, file.toString()));
        assertTrue(ex.getMessage().contains("Unsupported filter type"));
    }

    @Test
    @DisplayName("loadWordList should trim and ignore empty or commented lines")
    void testLoadWordList() throws IOException {
        Path listFile = tempDir.resolve("words.txt");
        Files.write(listFile, List.of(" apple ", " ", "#comment", "banana", "cherry"));
        List<String> words = FilterIO.loadWordList(listFile.toString());

        assertEquals(List.of("apple", "banana", "cherry"), words);
    }

    @Test
    @DisplayName("metadata should print decoded integers or throw on invalid file")
    void testMetadataValidAndInvalid() throws IOException {
        Path file = tempDir.resolve("meta.bin");

        // Write 16 bytes = at least four ints (big-endian)
        byte[] header = new byte[16];
        header[3] = 1;   // 1
        header[7] = 2;   // 2
        header[11] = 3;  // 3
        header[15] = 4;  // 4 (extra to exceed 16-byte check)
        Files.write(file, header);

        // Should not throw now
        assertDoesNotThrow(() -> FilterIO.metadata(file.toString()));

        // Too short file should throw
        Path shortFile = tempDir.resolve("short.bin");
        Files.write(shortFile, new byte[]{1, 2, 3});
        assertThrows(IOException.class, () -> FilterIO.metadata(shortFile.toString()));

        // Missing file should throw
        Path missing = tempDir.resolve("missing.bin");
        assertThrows(IOException.class, () -> FilterIO.metadata(missing.toString()));
    }

}
