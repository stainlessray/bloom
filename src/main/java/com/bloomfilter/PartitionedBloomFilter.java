package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Partitioned Bloom filter implementation that distributes elements across multiple sub-filters
 * to mitigate saturation. Each element is deterministically routed to a single partition based
 * on its hash value, ensuring that partitions remain sparse and predictable.
 *
 * @param <T> the type of elements stored in the filter
 */
public class PartitionedBloomFilter<T> extends AbstractBloomFilter<T> {

    private final ClassicBloomFilter<T>[] partitions;
    private final int numPartitions;
    private final int partitionSize;

    /**
     * Creates a new PartitionedBloomFilter with the specified number of partitions, partition size
     * and number of hash functions.
     *
     * @param numPartitions    the number of partitions
     * @param partitionSize    the size of each partition's bit array
     * @param numHashFunctions the number of hash functions for each partition
     */
    @SuppressWarnings("unchecked")
    public PartitionedBloomFilter(int numPartitions, int partitionSize, int numHashFunctions) {
        super(numPartitions * partitionSize, numHashFunctions);
        if (numPartitions <= 0) {
            throw new IllegalArgumentException("numPartitions must be positive");
        }
        if (partitionSize <= 0) {
            throw new IllegalArgumentException("partitionSize must be positive");
        }
        this.numPartitions = numPartitions;
        this.partitionSize = partitionSize;
        this.partitions = (ClassicBloomFilter<T>[]) new ClassicBloomFilter<?>[numPartitions];
        for (int i = 0; i < numPartitions; i++) {
            partitions[i] = new ClassicBloomFilter<>(partitionSize, numHashFunctions);
        }
    }

    /**
     * Determines which partition an element should be routed to based on its hash value.
     *
     * @param element the element to hash
     * @return the index of the partition
     */
    private int choosePartition(T element) {
        long[] hash = HashUtils.hash128(element.toString());
        return (int) Math.floorMod(hash[0], numPartitions);
    }

    @Override
    public void add(T element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        int idx = choosePartition(element);
        partitions[idx].add(element);
        itemCount++;
    }
    @Override
    public void remove(T element) {
        if (element == null) {
         @Override
    public void remove(T element) {
        if (element == null) {
            throw new NullPointerException("element");
        }
        int idx = choosePartition(element);
        if (partitions[idx] instanceof CountingBloomFilter) {
            ((CountingBloomFilter<T>) partitions[idx]).remove(element);
            if (itemCount > 0) {
                itemCount--;
            }
        } else {
            throw new UnsupportedOperationException("remove is not supported by PartitionedBloomFilter");
        }
    }


    @Override
    public void clear() {
        for (ClassicBloomFilter<T> partition : partitions) {
            partition.clear();
        }
        itemCount = 0;
    }

    @Override
    protected int[] getHashIndices(T element) {
        throw new UnsupportedOperationException("PartitionedBloomFilter delegates hashing to sub-filters");
    }

    @Override
    protected void setBit(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean getBit(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void clearBit(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] toBytes() {
        byte[][] partsBytes = new byte[numPartitions][];
        int total = 4 + 4 + 4 + 8; // numPartitions, partitionSize, numHashFunctions, itemCount
        for (int i = 0; i < numPartitions; i++) {
            partsBytes[i] = partitions[i].toBytes();
            total += 4 + partsBytes[i].length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(total).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(numPartitions);
        buffer.putInt(partitionSize);
        buffer.putInt(numHashFunctions);
        buffer.putLong(itemCount);
        for (int i = 0; i < numPartitions; i++) {
            buffer.putInt(partsBytes[i].length);
            buffer.put(partsBytes[i]);
        }
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int savedPartitions = buffer.getInt();
        int savedPartitionSize = buffer.getInt();
        int savedNumHash = buffer.getInt();
        long savedCount = buffer.getLong();
        if (savedPartitions != this.numPartitions || savedPartitionSize != this.partitionSize || savedNumHash != this.numHashFunctions) {
            throw new IllegalArgumentException("Serialized data does not match filter configuration");
        }
        for (int i = 0; i < numPartitions; i++) {
            int len = buffer.getInt();
            byte[] bytes = new byte[len];
            buffer.get(bytes);
            partitions[i].fromBytes(bytes);
        }
        this.itemCount = savedCount;
    }

    @Override
    public double estimateFalsePositiveRate() {
        double m = (double) bitArraySize;
        double k = (double) numHashFunctions;
        double n = (double) itemCount;
        return Math.pow(1.0 - Math.exp(-k * n / m), k);
    }
}
