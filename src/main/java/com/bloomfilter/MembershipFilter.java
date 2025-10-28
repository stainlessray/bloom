package com.bloomfilter;

/**
 * Central contract for probabilistic set filters. Provides operations to add elements,
 * query membership, manage state and support serialization.
 *
 * @param <T> element type handled by the filter
 */
public interface MembershipFilter<T> {
    /**
     * Adds an element to the filter. Idempotent but not reversible in classic filters.
     *
     * @param element element to add
     */
    void add(T element);

    /**
     * Checks if an element might be contained in the filter.
     *
     * @param element element to check
     * @return {@code true} if the element is possibly in the set, {@code false} if definitely not
     */
    boolean mightContain(T element);

    /**
     * Removes an element from the filter. Only supported by filter implementations that
     * support deletions (e.g. counting Bloom filters). The default implementation
     * throws {@link UnsupportedOperationException}.
     *
     * @param element element to remove
     */
    default void remove(T element) {
        throw new UnsupportedOperationException("remove is not supported by this filter implementation");
    }

    /**
     * Clears all state in the filter, resetting it to its initial empty state.
     */
    void clear();

    /**
     * Returns the number of elements added to the filter since the last clear. Note that for
     * probabilistic filters this is a simple counter and may not reflect the number of unique
     * elements.
     *
     * @return estimated number of added elements
     */
    long getEstimatedCount();

    /**
     * Estimates the current false-positive rate based on the filter's parameters and
     * state. Filters without a statistical model may return {@link Double#NaN}.
     *
     * @return estimated false-positive probability or {@code Double.NaN}
     */
    default double estimateFalsePositiveRate() {
        return Double.NaN;
    }

    /**
     * Serializes the filter's internal state into a byte array. The returned data must be
     * sufficient to fully reconstruct the filter using {@link #fromBytes(byte[])}.
     *
     * @return serialized representation of this filter
     */
    byte[] toBytes();

    /**
     * Restores the filter's state from the provided serialized representation. Implementations
     * must validate the input and throw an appropriate exception on invalid data.
     *
     * @param data serialized data produced by {@link #toBytes()}
     */
    void fromBytes(byte[] data);
}
