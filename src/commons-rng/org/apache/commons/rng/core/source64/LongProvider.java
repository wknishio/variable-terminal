/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.rng.core.source64;

import org.apache.commons.rng.core.util.NumberFactory;
import org.apache.commons.rng.core.BaseProvider;
import org.apache.commons.rng.core.source32.RandomIntSource;

/**
 * Base class for all implementations that provide a {@code long}-based
 * source randomness.
 */
public abstract class LongProvider
    extends BaseProvider
    implements RandomLongSource {

    /** Empty boolean source. This is the location of the sign-bit after 63 right shifts on
     * the boolean source. */
    private static final long EMPTY_BOOL_SOURCE = 1;
    /** Empty int source. This requires a negative value as the sign-bit is used to
     * trigger a refill. */
    private static final long EMPTY_INT_SOURCE = -1;

    /**
     * Provides a bit source for booleans.
     *
     * <p>A cached value from a call to {@link #next()}.
     *
     * <p>Only stores 63-bits when full as 1 bit has already been consumed.
     * The sign bit is a flag that shifts down so the source eventually equals 1
     * when all bits are consumed and will trigger a refill.
     */
    private long booleanSource = EMPTY_BOOL_SOURCE;

    /**
     * Provides a source for ints.
     *
     * <p>A cached half-value value from a call to {@link #next()}.
     * The int is stored in the lower 32 bits with zeros in the upper bits.
     * When empty this is set to negative to trigger a refill.
     */
    private long intSource = EMPTY_INT_SOURCE;

    /**
     * Creates a new instance.
     */
    public LongProvider() {
        super();
    }

    /**
     * Creates a new instance copying the state from the source.
     *
     * <p>This provides base functionality to allow a generator to create a copy, for example
     * for use in the {@link org.apache.commons.rng.JumpableUniformRandomProvider
     * JumpableUniformRandomProvider} interface.
     *
     * @param source Source to copy.
     * @since 1.3
     */
    protected LongProvider(LongProvider source) {
        booleanSource = source.booleanSource;
        intSource = source.intSource;
    }

    /**
     * Reset the cached state used in the default implementation of {@link #nextBoolean()}
     * and {@link #nextInt()}.
     *
     * <p>This should be used when the state is no longer valid, for example after a jump
     * performed for the {@link org.apache.commons.rng.JumpableUniformRandomProvider
     * JumpableUniformRandomProvider} interface.</p>
     *
     * @since 1.3
     */
    protected void resetCachedState() {
        booleanSource = EMPTY_BOOL_SOURCE;
        intSource = EMPTY_INT_SOURCE;
    }

    /** {@inheritDoc} */
    
    protected byte[] getStateInternal() {
        final long[] state = {booleanSource, intSource};
        return composeStateInternal(NumberFactory.makeByteArray(state),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, 2 * (Long.SIZE / 8));
        final long[] state = NumberFactory.makeLongArray(c[0]);
        booleanSource   = state[0];
        intSource       = state[1];
        super.setStateInternal(c[1]);
    }

    /** {@inheritDoc} */
    
    public long nextLong() {
        return next();
    }

    /** {@inheritDoc} */
    
    public int nextInt() {
        long bits = intSource;
        if (bits < 0) {
            // Refill
            bits = next();
            // Store high 32 bits, return low 32 bits
            intSource = bits >>> 32;
            return (int) bits;
        }
        // Reset and return previous low bits
        intSource = -1;
        return (int) bits;
    }

    /** {@inheritDoc} */
    
    public boolean nextBoolean() {
        long bits = booleanSource;
        if (bits == 1) {
            // Refill
            bits = next();
            // Store a refill flag in the sign bit and the unused 63 bits, return lowest bit
            booleanSource = Long.MIN_VALUE | (bits >>> 1);
            return (bits & 0x1) == 1;
        }
        // Shift down eventually triggering refill, return current lowest bit
        booleanSource = bits >>> 1;
        return (bits & 0x1) == 1;
    }
    
    /**
     * Generates random bytes and places them into a user-supplied array.
     *
     * <p>
     * The array is filled with bytes extracted from random {@code int} values.
     * This implies that the number of random bytes generated may be larger than
     * the length of the byte array.
     * </p>
     *
     * @param source Source of randomness.
     * @param bytes Array in which to put the generated bytes. Cannot be null.
     * @param start Index at which to start inserting the generated bytes.
     * @param len Number of bytes to insert.
     */
    static void nextBytesFill(RandomLongSource source,
                              byte[] bytes,
                              int start,
                              int len) {
        int index = start; // Index of first insertion.

        // Index of first insertion plus multiple of 8 part of length
        // (i.e. length with 3 least significant bits unset).
        final int indexLoopLimit = index + (len & 0x7ffffff8);

        // Start filling in the byte array, 8 bytes at a time.
        while (index < indexLoopLimit) {
            final long random = source.next();
            bytes[index++] = (byte) random;
            bytes[index++] = (byte) (random >>> 8);
            bytes[index++] = (byte) (random >>> 16);
            bytes[index++] = (byte) (random >>> 24);
            bytes[index++] = (byte) (random >>> 32);
            bytes[index++] = (byte) (random >>> 40);
            bytes[index++] = (byte) (random >>> 48);
            bytes[index++] = (byte) (random >>> 56);
        }

        final int indexLimit = start + len; // Index of last insertion + 1.

        // Fill in the remaining bytes.
        if (index < indexLimit)
        {
            long random = source.next();
            while (true) {
                bytes[index++] = (byte) random;
                if (index < indexLimit) {
                    random >>>= 8;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Checks if the sub-range from fromIndex (inclusive) to fromIndex + size (exclusive) is
     * within the bounds of range from 0 (inclusive) to length (exclusive).
     *
     * <p>This function provides the functionality of
     * {@code java.utils.Objects.checkFromIndexSize} introduced in JDK 9. The
     * <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Objects.html#checkFromIndexSize(int,int,int)">Objects</a>
     * javadoc has been reproduced for reference.
     *
     * <p>The sub-range is defined to be out of bounds if any of the following inequalities
     * is true:
     * <ul>
     * <li>{@code fromIndex < 0}
     * <li>{@code size < 0}
     * <li>{@code fromIndex + size > length}, taking into account integer overflow
     * <li>{@code length < 0}, which is implied from the former inequalities
     * </ul>
     *
     * @param fromIndex the lower-bound (inclusive) of the sub-interval
     * @param size the size of the sub-range
     * @param length the upper-bound (exclusive) of the range
     * @return the fromIndex
     * @throws IndexOutOfBoundsException if the sub-range is out of bounds
     */
    private static int checkFromIndexSize(int fromIndex, int size, int length) {
        // check for any negatives,
        // or overflow safe length check given the values are all positive
        // remaining = length - fromIndex
        if ((fromIndex | size | length) < 0 || size > length - fromIndex) {
            throw new IndexOutOfBoundsException(
                // Note: %<d is 'relative indexing' to re-use the last argument
                String.format("Range [%d, %<d + %d) out of bounds for length %d",
                    fromIndex, size, length));
        }
        return fromIndex;
    }
    
    /** {@inheritDoc} */
    
    public void nextBytes(byte[] bytes) {
        nextBytesFill(this, bytes, 0, bytes.length);
    }

    /** {@inheritDoc} */
    
    public void nextBytes(byte[] bytes,
                          int start,
                          int len) {
        checkFromIndexSize(start, len, bytes.length);
        nextBytesFill(this, bytes, start, len);
    }
}
