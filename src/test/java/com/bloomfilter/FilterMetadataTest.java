package com.bloomfilter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link FilterMetadata}.
 * Focuses strictly on verifying the output of {@link FilterMetadata#summary()}.
 */
class FilterMetadataTest {

    @Test
    @DisplayName("summary() should produce correctly formatted metadata header")
    void testSummaryFormat() {
        // Arrange
        String algorithm = "ClassicBloomFilter";
        int bits = 64;
        int hashes = 3;
        String source = "data/fruit.txt";

        // Act
        FilterMetadata metadata = new FilterMetadata(algorithm, bits, hashes, source);
        String summary = metadata.summary();

        // Assert: basic structural validation
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.startsWith("Algorithm=" + algorithm), "Should start with Algorithm field");
        assertTrue(summary.contains("Bits=" + bits), "Should contain Bits field");
        assertTrue(summary.contains("Hashes=" + hashes), "Should contain Hashes field");
        assertTrue(summary.contains("Source=" + source), "Should contain Source field");
        assertTrue(summary.contains("Created="), "Should contain Created field");

        // Assert: format integrity (ISO_LOCAL_DATE_TIME suffix)
        String expectedPrefix = String.format(
                "Algorithm=%s | Bits=%d | Hashes=%d | Source=%s | Created=",
                algorithm, bits, hashes, source
        );
        assertTrue(summary.startsWith(expectedPrefix), "Summary prefix must match expected layout");

        // Extract timestamp portion for sanity check
        String createdPart = summary.substring(summary.lastIndexOf('=') + 1);
        assertDoesNotThrow(() -> LocalDateTime.parse(createdPart, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Created timestamp must be valid ISO_LOCAL_DATE_TIME");
    }

    @Test
    @DisplayName("toString() should delegate to summary()")
    void testToStringDelegatesToSummary() {
        FilterMetadata metadata = new FilterMetadata("ClassicBloomFilter", 64, 3, "data/fruit.txt");
        assertEquals(metadata.summary(), metadata.toString(), "toString() should match summary()");
    }
}
