package com.bloomfilter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract smoke test for {@link MembershipFilter}.
 * Ensures basic polymorphic expectations and default-method consistency (if any).
 */
class MembershipFilterTest {

    static class MinimalFilter implements MembershipFilter<String> {
        private boolean added;
        @Override public void add(String element) { added = true; }
        @Override public boolean mightContain(String element) { return added; }
        @Override public void clear() { added = false; }
        @Override public long getEstimatedCount() { return added ? 1 : 0; }
        @Override public double estimateFalsePositiveRate() { return 0.0; }
        @Override public byte[] toBytes() { return new byte[]{1,2,3}; }
        @Override public void fromBytes(byte[] data) { assertNotNull(data); }
    }

    @Test
    @DisplayName("Minimal implementation should satisfy the MembershipFilter contract")
    void testContractExecution() {
        MembershipFilter<String> filter = new MinimalFilter();

        assertFalse(filter.mightContain("apple"));
        filter.add("apple");
        assertTrue(filter.mightContain("apple"));
        assertEquals(1, filter.getEstimatedCount());
        assertEquals(0.0, filter.estimateFalsePositiveRate());
        assertDoesNotThrow(() -> filter.fromBytes(filter.toBytes()));
        filter.clear();
        assertEquals(0, filter.getEstimatedCount());
    }
}
