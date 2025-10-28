package com.bloomfilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for hashing and index generation used by Bloom filter implementations.
 * Implements the MurmurHash3 128-bit algorithm and provides a double-hashing strategy
 * for generating multiple hash indices.
 */
public final class HashUtils {
    private HashUtils() {
        // Prevent instantiation
    }

    /**
     * Computes a 128-bit MurmurHash3 hash for the given string using UTF-8 encoding.
     *
     * @param input the input string to hash
     * @return a two-element long array representing the 128-bit hash
     */
    public static long[] hash128(String input) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        return murmurhash3_x64_128(bytes, 0, bytes.length, 0);
    }

    /**
     * Computes a 128-bit MurmurHash3 hash for the given byte array.
     *
     * @param data the input data to hash
     * @return a two-element long array representing the 128-bit hash
     */
    public static long[] hash128(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data");
        }
        return murmurhash3_x64_128(data, 0, data.length, 0);
    }

    /**
     * Generates an array of hash indices using the double hashing technique.
     *
     * The formula used is h_i(x) = (hash1 + i * hash2) mod m, where m is the
     * size of the underlying bit array. This method ensures that the indices
     * are non-negative and within the bounds of the bit array size.
     *
     * @param hash the two-element long array containing the base hash values
     * @param numHashFunctions the number of hash indices to generate
     * @param bitArraySize the size of the bit array
     * @return an array of indices into the bit array
     */
    public static int[] generateIndices(long[] hash, int numHashFunctions, int bitArraySize) {
        if (hash == null || hash.length < 2) {
            throw new IllegalArgumentException("hash must contain at least two longs");
        }
        int[] indices = new int[numHashFunctions];
        long hash1 = hash[0];
        long hash2 = hash[1];
        for (int i = 0; i < numHashFunctions; i++) {
            long combined = hash1 + (long) i * hash2;
            int idx = (int) Math.floorMod(combined, bitArraySize);
            indices[i] = idx;
        }
        return indices;
    }

    /**
     * Core implementation of the MurmurHash3 x64 128-bit hashing algorithm.
     *
     * @param key the data to hash
     * @param offset the starting offset in the data
     * @param len the number of bytes to hash
     * @param seed the seed to initialize the hash state
     * @return a two-element array containing the 128-bit hash split into two longs
     */
    private static long[] murmurhash3_x64_128(byte[] key, int offset, int len, long seed) {
        final int nblocks = len >> 4; // process blocks of 16 bytes

        long h1 = seed;
        long h2 = seed;

        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        // body
        ByteBuffer buffer = ByteBuffer.wrap(key);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < nblocks; i++) {
            int i16 = offset + i * 16;
            long k1 = buffer.getLong(i16);
            long k2 = buffer.getLong(i16 + 8);

            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        // tail
        long k1 = 0;
        long k2 = 0;
        int tailStart = offset + nblocks * 16;
        int remaining = len & 15;

        switch (remaining) {
            case 15:
                k2 ^= ((long) key[tailStart + 14] & 0xffL) << 48;
            case 14:
                k2 ^= ((long) key[tailStart + 13] & 0xffL) << 40;
            case 13:
                k2 ^= ((long) key[tailStart + 12] & 0xffL) << 32;
            case 12:
                k2 ^= ((long) key[tailStart + 11] & 0xffL) << 24;
            case 11:
                k2 ^= ((long) key[tailStart + 10] & 0xffL) << 16;
            case 10:
                k2 ^= ((long) key[tailStart + 9] & 0xffL) << 8;
            case 9:
                k2 ^= ((long) key[tailStart + 8] & 0xffL);
                k2 *= c2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= c1;
                h2 ^= k2;
            case 8:
                k1 ^= ((long) key[tailStart + 7] & 0xffL) << 56;
            case 7:
                k1 ^= ((long) key[tailStart + 6] & 0xffL) << 48;
            case 6:
                k1 ^= ((long) key[tailStart + 5] & 0xffL) << 40;
            case 5:
                k1 ^= ((long) key[tailStart + 4] & 0xffL) << 32;
            case 4:
                k1 ^= ((long) key[tailStart + 3] & 0xffL) << 24;
            case 3:
                k1 ^= ((long) key[tailStart + 2] & 0xffL) << 16;
            case 2:
                k1 ^= ((long) key[tailStart + 1] & 0xffL) << 8;
            case 1:
                k1 ^= ((long) key[tailStart] & 0xffL);
                k1 *= c1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        }

        // finalization
        h1 ^= len;
        h2 ^= len;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[]{h1, h2};
    }

    /**
     * Finalization mix function for 64-bit hash values.
     *
     * @param k the input value
     * @return the mixed value
     */
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
}
