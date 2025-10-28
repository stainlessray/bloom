package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PartitionedBloomFilter<T> extends AbstractBloomFilter<T> {

    private final ClassicBloomFilter<T>[] partitions;
    private final int numPartitions;
    private final int partitionSize;

    @SuppressWarnings("unchecked")
    public PartitionedBloomFilter(int numPartitions, int partitionSize, int numHashFunctions) {
        super(numPartitions * partitionSize, numHashFunctions);
        if (numPartitions <= 0 || partitionSize <= 0)
            throw new IllegalArgumentException("numPartitions and partitionSize must be positive");
        this.numPartitions = numPartitions;
        this.partitionSize = partitionSize;
        this.partitions = (ClassicBloomFilter<T>[]) new ClassicBloomFilter<?>[numPartitions];
        for (int i = 0; i < numPartitions; i++) {
            partitions[i] = new ClassicBloomFilter<>(partitionSize, numHashFunctions);
            partitions[i].setVerbose(verbose);
        }
    }

    private int choosePartition(T element) {
        long[] hash = HashUtils.hash128(element.toString());
        int idx = (int) Math.floorMod(hash[0], numPartitions);
        if (verbose)
            System.out.printf("Partition chosen for '%s' → %d%n", element, idx);
        return idx;
    }

    @Override
    public void add(T element) {
        if (element == null) throw new NullPointerException("element");
        int idx = choosePartition(element);
        partitions[idx].setVerbose(verbose);
        if (verbose) System.out.printf("Adding '%s' to partition %d%n", element, idx);
        partitions[idx].add(element);
        itemCount++;
    }

    @Override
    public boolean mightContain(T element) {
        if (element == null) throw new NullPointerException("element");
        int idx = choosePartition(element);
        partitions[idx].setVerbose(verbose);
        if (verbose)
            System.out.printf("Checking membership for '%s' in partition %d%n", element, idx);
        boolean result = partitions[idx].mightContain(element);
        if (verbose)
            System.out.printf(" → Partition %d result = %s%n", idx, result);
        return result;
    }

    @Override
    public void remove(T element) {
        if (element == null) throw new NullPointerException("element");
        int idx = choosePartition(element);
        if (verbose) System.out.printf("Removing '%s' from partition %d%n", element, idx);
        try {
            partitions[idx].remove(element);
            if (itemCount > 0) itemCount--;
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("remove not supported by this partition");
        }
    }

    @Override
    public void clear() {
        if (verbose) System.out.println("Clearing all partitions...");
        for (ClassicBloomFilter<T> partition : partitions) {
            partition.clear();
        }
        itemCount = 0;
        if (verbose) System.out.println(" → All partitions cleared.");
    }

    @Override
    protected int[] getHashIndices(T element) {
        throw new UnsupportedOperationException("Delegated to sub-filters");
    }

    @Override protected void setBit(int index) { throw new UnsupportedOperationException(); }
    @Override protected boolean getBit(int index) { throw new UnsupportedOperationException(); }
    @Override protected void clearBit(int index) { throw new UnsupportedOperationException(); }

    @Override
    public byte[] toBytes() {
        if (verbose) System.out.println("Serializing PartitionedBloomFilter...");
        byte[][] partsBytes = new byte[numPartitions][];
        int total = 4 + 4 + 4 + 8;
        for (int i = 0; i < numPartitions; i++) {
            partsBytes[i] = partitions[i].toBytes();
            total += 4 + partsBytes[i].length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(total).order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(numPartitions);
        buffer.putInt(partitionSize);
        buffer.putInt(hashCount);
        buffer.putLong(itemCount);
        for (int i = 0; i < numPartitions; i++) {
            buffer.putInt(partsBytes[i].length);
            buffer.put(partsBytes[i]);
        }
        if (verbose) System.out.println(" → Serialization complete.");
        return buffer.array();
    }

    @Override
    public void fromBytes(byte[] data) {
        if (verbose) System.out.println("Deserializing PartitionedBloomFilter...");
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int savedPartitions = buffer.getInt();
        int savedPartitionSize = buffer.getInt();
        int savedNumHash = buffer.getInt();
        long savedCount = buffer.getLong();
        if (savedPartitions != numPartitions ||
                savedPartitionSize != partitionSize ||
                savedNumHash != hashCount) {
            throw new IllegalArgumentException("Serialized data does not match configuration");
        }
        for (int i = 0; i < numPartitions; i++) {
            int len = buffer.getInt();
            byte[] bytes = new byte[len];
            buffer.get(bytes);
            partitions[i].fromBytes(bytes);
        }
        this.itemCount = savedCount;
        if (verbose) System.out.println(" → Deserialization complete.");
    }
}
