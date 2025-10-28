package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Counting Bloom filter implementation supporting deletions by maintaining an array of counters.
 *
 * @param <T> the type of elements stored in the filter
 */
public class CountingBloomFilter<T> extends AbstractBloomFilter<T> {

    /**
     * Counter array backing the counting Bloom filter. Each position tracks how
     * many times a given bit index has been set. When a counter is zero the
     * corresponding bit is considered unset.
     */
    private int[] counters;

    /**
     * Creates a new CountingBloomFilter with the specified bit array size and number of hash functions.
     *
     * @param bitArraySize     the size of the underlying counter array
     * @param numHashFunctions the number of hash functions to use
     */
    public CountingBloomFilter(int bitArraySize, int numHashFunctions) {
        super(bitArraySize, numHashFunctions);
        this.counters = new int[bitArraySize];
    }

    @Override
    protected int[] getHashIndices(T element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        long[] hash = HashUtils.hash128(element.toString());
        return HashUtils.generateIndices(hash, numHashFunctions, bitArraySize);
    }

    @Override
    protected void setBit(int index) {
        if (counters[index] < Integer.MAX_VALUE) {
            counters[index]++;
        }
    }

    @Override
    protected boolean getBit(int index) {
        return counters[index] > 0;
    }

    @Override
    protected void clearBit(int index) {
        counters[index] = 0;
    }

    /**
     * Removes an element from the filter by decrementing its counters. If a counter
     * is already zero it is left unchanged. The overall item count is reduced as
     * long as it remains positive.
     *
     * @param element the element to remove
     */
    @Override
    public void remove(T element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        int[] indices = getHashIndices(element);
        for (int index : indices) {
            if (counters[index] > 0) {
                counters[index]--;
            }
        }
        if (itemCount > 0) {
            itemCount--;
        }
    }

    /**
     * Resets the filter to an empty state by delegating to the base class. The
     * AbstractBloomFilter implementation invokes {@link #clearBit(int)} for every
     * index, which resets all counters to zero and sets the item count to zero.
     */
    @Override
    public void clear() {
        super.clear();
    }

    /**
     * Serializes the filter's state to a byte array. The format is:
     * <pre>
     * [bitArraySize][numHashFunctions][itemCount][counters.length][counters...]
     * </pre>
     * All integers are stored in big-endian order.
     *
     * @return serialized byte array representing this filter
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + counters.length * 4)
                                      .order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(bitArraySize);
        buffer.putInt(numHashFunctions);
        buffer.putLong(itemCount);
        buffer.putInt(counters.length);
        for (int value : counters) {
            buffer.putInt(value);
        }
        return buffer.array();
    }

    /**
     * Restores the filter's state from a serialized byte array. The serialized data
     * must have been produced by {@link #toBytes()} and match this filter's
     * configuration (bitArraySize and numHashFunctions).
     *
     * @param data the byte array containing the serialized state
     * @throws IllegalArgumentException if the serialized data does not match the filter configuration
     */
    @Override
    public void fromBytes(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int savedSize = buffer.getInt();
        int savedNumHash = buffer.getInt();
        long savedCount = buffer.getLong();
        int length = buffer.getInt();
        if (savedSize != this.bitArraySize || savedNumHash != this.numHashFunctions) {
            throw new IllegalArgumentException("Serialized data does not match filter configuration");
        }
        int[] newCounters = new int[length];
        for (int i = 0; i < length; i++) {
            newCounters[i] = buffer.getInt();
        }
        this.itemCount = savedCount;
        this.counters = newCounters;
    }
}
