package com.bloomfilter;

/**
 * Skeletal implementation of the {@link MembershipFilter} contract providing
 * common state management, workflow logic, and false positive rate estimation
 * for Bloom filter variants. Concrete subclasses must implement storage and
 * hashing-specific details via the abstract methods.
 *
 * @param <T> element type handled by the filter
 */
public abstract class AbstractBloomFilter<T> implements MembershipFilter<T> {
    /** Number of bits (or counters) in the underlying storage. */
    protected final int bitArraySize;
    /** Number of hash functions used to map elements to indices. */
    protected final int numHashFunctions;
    /** Count of elements added to the filter. */
    protected long itemCount;

    /**
     * Constructs a filter with the given size and number of hash functions.
     *
     * @param bitArraySize number of bits or counters in the backing structure
     * @param numHashFunctions number of hash functions to use
     * @throws IllegalArgumentException if either parameter is non-positive
     */
    protected AbstractBloomFilter(int bitArraySize, int numHashFunctions) {
        if (bitArraySize <= 0) {
            throw new IllegalArgumentException("bitArraySize must be positive");
        }
        if (numHashFunctions <= 0) {
            throw new IllegalArgumentException("numHashFunctions must be positive");
        }
        this.bitArraySize = bitArraySize;
        this.numHashFunctions = numHashFunctions;
        this.itemCount = 0;
    }

    @Override
    public void add(T element) {
        int[] indices = getHashIndices(element);
        for (int index : indices) {
            setBit(index);
        }
        itemCount++;
    }

    @Override
    public boolean mightContain(T element) {
        int[] indices = getHashIndices(element);
        for (int index : indices) {
            if (!getBit(index)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < bitArraySize; i++) {
            clearBit(i);
        }
        itemCount = 0;
    }

    @Override
    public long getEstimatedCount() {
        return itemCount;
    }

    @Override
    public double estimateFalsePositiveRate() {
        double m = bitArraySize;
        double k = numHashFunctions;
        double n = itemCount;
        if (m <= 0 || k <= 0) {
            return Double.NaN;
        }
        return Math.pow(1 - Math.exp(-k * n / m), k);
    }

    /**
     * Generates an array of indices to set/check for the given element using the filter's
     * hashing strategy. The returned indices must be in the range [0, bitArraySize).
     *
     * @param element element to hash
     * @return array of indices corresponding to hash functions
     */
    protected abstract int[] getHashIndices(T element);

    /**
     * Sets the bit (or increments the counter) at the specified index.
     *
     * @param index index to modify
     */
    protected abstract void setBit(int index);

    /**
     * Returns whether the bit (or counter) at the specified index is set (i.e. non-zero).
     *
     * @param index index to check
     * @return {@code true} if the bit/counter is non-zero
     */
    protected abstract boolean getBit(int index);

    /**
     * Clears the bit (or counter) at the specified index. Used by {@link #clear()}.
     *
     * @param index index to reset
     */
    protected abstract void clearBit(int index);

    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException(
                "Concrete filters must implement serialization");
    }

    @Override
    public void fromBytes(byte[] data) {
        throw new UnsupportedOperationException(
                "Concrete filters must implement deserialization");
    }
}
