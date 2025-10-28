package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Arrays;

public class ClassicBloomFilter<T> extends AbstractBloomFilter<T> {

    private BitSet bitSet;

    public ClassicBloomFilter(int bitArraySize, int numHashFunctions) {
        super(bitArraySize, numHashFunctions);
        this.bitSet = new BitSet(bitArraySize);
    }

    @Override
    protected int[] getHashIndices(T element) {
        if (element == null) throw new NullPointerException("element");
        long[] hash = HashUtils.hash128(element.toString());
        int[] indices = HashUtils.generateIndices(hash, numHashFunctions, bitArraySize);
        if (verbose) {
            System.out.printf("Hashing '%s' → %s%n", element, Arrays.toString(indices));
        }
        return indices;
    }

    @Override
    protected void setBit(int index) {
        bitSet.set(index);
        if (verbose) System.out.printf("   set bit[%d]%n", index);
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
        if (verbose)
            System.out.printf("Serializing ClassicBloomFilter (size=%d, hashes=%d, count=%d)%n",
                    bitArraySize, numHashFunctions, itemCount);
        byte[] bitData = bitSet.toByteArray();
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + bitData.length)
                .order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(bitArraySize);
        buffer.putInt(numHashFunctions);
        buffer.putLong(itemCount);
        buffer.putInt(bitData.length);
        buffer.put(bitData);
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] data) {
        if (verbose) System.out.println("Deserializing ClassicBloomFilter...");
        if (data == null) throw new NullPointerException("data");
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int savedSize = buffer.getInt();
        int savedNumHash = buffer.getInt();
        long savedCount = buffer.getLong();
        int bytesLen = buffer.getInt();
        byte[] bitBytes = new byte[bytesLen];
        buffer.get(bitBytes);
        if (savedSize != this.bitArraySize || savedNumHash != this.numHashFunctions)
            throw new IllegalArgumentException("Serialized data does not match filter configuration");
        this.itemCount = savedCount;
        this.bitSet = BitSet.valueOf(bitBytes);
        if (verbose) System.out.println(" → Deserialization complete.");
    }
}
