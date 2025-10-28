package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

/**
 * Classic Bloom filter implementation using a single bit set.
 *
 * @param <T> the type of elements to be stored in the filter
 */
public class ClassicBloomFilter<T> extends AbstractBloomFilter<T> {

    private BitSet bitSet;

    /**
     * Creates a new ClassicBloomFilter with the specified bit array size and number of hash functions.
     *
     * @param bitArraySize the size of the underlying bit array
     * @param numHashFunctions the number of hash functions to use
     */
    public ClassicBloomFilter(int bitArraySize, int numHashFunctions) {
        super(bitArraySize, numHashFunctions);
        this.bitSet = new BitSet(bitArraySize);
    }

    @Override
    protected int[] getHashIndices(T element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        long[] hash = HashUtils.hash128(element.toString());
        return HashUtils.generateIndices(hash, hashCount, bitArraySize);
    }

    @Override
    protected void setBit(int index) {
        bitSet.set(index);
    }

    @Override
    protected boolean getBit(int index) {
        return bitSet.get(index);
    }

    @Override
    protected void clearBit(int index) {
        bitSet.clear(index);
    }

    @Override
    public byte[] toBytes() {
        byte[] bitData = bitSet.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + bitData.length).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(bitArraySize);
        buffer.putInt(hashCount);
        buffer.putLong(itemCount);
        buffer.putInt(bitData.length);
        buffer.put(bitData);
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
        int bytesLen = buffer.getInt();
        byte[] bitBytes = new byte[bytesLen];
        buffer.get(bitBytes);
        if (savedSize != this.bitArraySize || savedNumHash != this.hashCount) {
            throw new IllegalArgumentException("Serialized data does not match filter configuration");
        }
        this.itemCount = savedCount;
        this.bitSet = BitSet.valueOf(bitBytes);
    }
}
