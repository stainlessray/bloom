package com.bloomfilter;

import java.util.Arrays;

/**
 * Skeletal implementation of the {@link MembershipFilter} contract providing
 * common state management, workflow logic, and false positive rate estimation
 * for Bloom filter variants. Concrete subclasses must implement storage and
 * hashing-specific details via the abstract methods.
 *
 * @param <T> element type handled by the filter
 */
public abstract class AbstractBloomFilter<T> implements MembershipFilter<T> {
    protected final int bitArraySize;

    public int getBitArraySize() {
        return bitArraySize;
    }

    protected final int numHashFunctions;
    protected long itemCount;

    /** Enables console explanations for learning mode. */
    protected boolean verbose = false;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    protected AbstractBloomFilter(int bitArraySize, int numHashFunctions) {
        if (bitArraySize <= 0) throw new IllegalArgumentException("bitArraySize must be positive");
        if (numHashFunctions <= 0) throw new IllegalArgumentException("numHashFunctions must be positive");
        this.bitArraySize = bitArraySize;
        this.numHashFunctions = numHashFunctions;
        this.itemCount = 0;
    }

    @Override
    public void add(T element) {
        int[] indices = getHashIndices(element);
        if (verbose) {
            System.out.printf("Adding element: %s%n", element);
            System.out.printf(" → hash indices: %s%n", Arrays.toString(indices));
        }
        for (int index : indices) setBit(index);
        itemCount++;
        if (verbose) System.out.printf(" → itemCount now: %d%n", itemCount);
    }

    @Override
    public boolean mightContain(T element) {
        int[] indices = getHashIndices(element);
        if (verbose) {
            System.out.printf("Checking membership for: %s%n", element);
            System.out.printf(" → hash indices: %s%n", Arrays.toString(indices));
        }
        for (int index : indices) {
            boolean bit = getBit(index);
            if (verbose) System.out.printf("   bit[%d] = %s%n", index, bit);
            if (!bit) {
                if (verbose) System.out.println(" → Definitely NOT in the set.\n");
                return false;
            }
        }
        if (verbose) System.out.println(" → Possibly in the set (mightContain = true)\n");
        return true;
    }

    @Override
    public void clear() {
        if (verbose) System.out.println("Clearing all bits...");
        for (int i = 0; i < bitArraySize; i++) clearBit(i);
        itemCount = 0;
        if (verbose) System.out.println(" → Filter cleared.");
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
        double fpr = Math.pow(1 - Math.exp(-k * n / m), k);
        if (verbose) System.out.printf("Estimating FPR (m=%f, k=%f, n=%f) = %f%n", m, k, n, fpr);
        return fpr;
    }

    protected abstract int[] getHashIndices(T element);
    protected abstract void setBit(int index);
    protected abstract boolean getBit(int index);
    protected abstract void clearBit(int index);

    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Concrete filters must implement serialization");
    }

    @Override
    public void fromBytes(byte[] data) {
        throw new UnsupportedOperationException("Concrete filters must implement deserialization");
    }
}
