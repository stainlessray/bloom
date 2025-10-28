package com.bloomfilter;

import com.bloomfilter.ClassicBloomFilter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Bloom filter implementations. Validates core functional
 * correctness: no false negatives, clear resets state, and serialization
 * round trip preserves filter state. These tests focus on the classic
 * implementation as representative; additional tests could be parameterized
 * for other variants.
 */
public class BloomFilterTest {
    /**
     * Ensure that every element added to the filter can be found. Bloom
     * filters must never return false for items that have been added (no
     * false negatives).
     */
    @Test
    public void testNoFalseNegatives() {
        ClassicBloomFilter<String> filter = new ClassicBloomFilter<>(1000, 3);
        String[] elements = {"apple", "banana", "cherry", "date", "elderberry"};
        for (String e : elements) {
            filter.add(e);
        }
        for (String e : elements) {
            assertTrue("False negative for " + e, filter.mightContain(e));
        }
    }

    /**
     * Verify that calling clear() resets the filter's internal state such
     * that previously added elements are no longer found and the estimated
     * count returns to zero.
     */
    @Test
    public void testClearResetsState() {
        ClassicBloomFilter<String> filter = new ClassicBloomFilter<>(1000, 3);
        filter.add("test");
        assertTrue(filter.mightContain("test"));
        filter.clear();
        assertFalse(filter.mightContain("test"));
        assertEquals(0, filter.getEstimatedCount());
    }

    /**
     * Ensure that serializing a filter and then deserializing into a new
     * filter yields an identical state. This test checks both the mightContain
     * behavior and the estimated count after a round trip.
     */
    @Test
    public void testSerializationRoundTrip() {
        ClassicBloomFilter<String> filter = new ClassicBloomFilter<>(1000, 3);
        filter.add("alpha");
        filter.add("beta");
        filter.add("gamma");
        byte[] data = filter.toBytes();
        ClassicBloomFilter<String> copy = new ClassicBloomFilter<>(1000, 3);
        copy.fromBytes(data);
        assertTrue(copy.mightContain("alpha"));
        assertTrue(copy.mightContain("beta"));
        assertTrue(copy.mightContain("gamma"));
        assertEquals(filter.getEstimatedCount(), copy.getEstimatedCount());
    }
}
