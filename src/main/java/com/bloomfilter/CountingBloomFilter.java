package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Counting Bloom filter implementation supporting deletions by maintaining an array of counters.
 *
 * @param <T> the type of elements stored in the filter
 */
public class CountingBloomFilter<T> extends AbstractBloomFilter<T> {

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
     * Removes an element from the filter by decrementing its counters.
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

    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < counters.length; i++) {
            counters[i] = 0;
        }
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + counters.length * 4).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(bitArraySize);
        buffer.putInt(numHashFunctions);
        buffer.putLong(itemCount);
        buffer.putInt(counters.length);
        for (int i = 0; i < counters.length; i++) {
            buffer.putInt(counters[i]);
        }
        return buffer.array();
    }

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
