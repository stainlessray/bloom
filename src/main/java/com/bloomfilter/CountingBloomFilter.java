package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class CountingBloomFilter<T> extends AbstractBloomFilter<T> {

    private int[] counters;

    public CountingBloomFilter(int bitArraySize, int numHashFunctions) {
        super(bitArraySize, numHashFunctions);
        this.counters = new int[bitArraySize];
    }

    @Override
    protected int[] getHashIndices(T element) {
        if (element == null) throw new NullPointerException("element");
        long[] hash = HashUtils.hash128(element.toString());
        int[] indices = HashUtils.generateIndices(hash, hashCount, bitArraySize);
        if (verbose)
            System.out.printf("Hashing '%s' → %s%n", element, Arrays.toString(indices));
        return indices;
    }

    @Override
    protected void setBit(int index) {
        if (counters[index] < Integer.MAX_VALUE) counters[index]++;
        if (verbose) System.out.printf("   increment counter[%d] = %d%n", index, counters[index]);
    }

    @Override
    protected boolean getBit(int index) {
        return counters[index] > 0;
    }

    @Override
    protected void clearBit(int index) {
        counters[index] = 0;
    }

    @Override
    public void remove(T element) {
        if (element == null) throw new NullPointerException("element");
        int[] indices = getHashIndices(element);
        if (verbose)
            System.out.printf("Removing element '%s'%n", element);
        for (int index : indices) {
            if (counters[index] > 0) counters[index]--;
            if (verbose) System.out.printf("   decrement counter[%d] = %d%n", index, counters[index]);
        }
        if (itemCount > 0) itemCount--;
    }

    @Override
    public byte[] toBytes() {
        if (verbose)
            System.out.printf("Serializing CountingBloomFilter (size=%d, hashes=%d, count=%d)%n",
                    bitArraySize, hashCount, itemCount);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + counters.length * 4)
                .order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(bitArraySize);
        buffer.putInt(hashCount);
        buffer.putLong(itemCount);
        buffer.putInt(counters.length);
        for (int value : counters) buffer.putInt(value);
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] data) {
        if (verbose) System.out.println("Deserializing CountingBloomFilter...");
        if (data == null) throw new NullPointerException("data");
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int savedSize = buffer.getInt();
        int savedNumHash = buffer.getInt();
        long savedCount = buffer.getLong();
        int length = buffer.getInt();
        if (savedSize != this.bitArraySize || savedNumHash != this.hashCount)
            throw new IllegalArgumentException("Serialized data does not match filter configuration");
        int[] newCounters = new int[length];
        for (int i = 0; i < length; i++) newCounters[i] = buffer.getInt();
        this.itemCount = savedCount;
        this.counters = newCounters;
        if (verbose) System.out.println(" → Deserialization complete.");
    }
}
