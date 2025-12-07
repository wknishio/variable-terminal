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

package vate.org.apache.commons.rng.core.source32;

import vate.org.apache.commons.rng.core.util.NumberFactory;

/**
 * This abstract class is a base for algorithms from the Xor-Shift-Rotate family of 32-bit
 * generators with 128-bits of state.
 *
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 * @since 1.3
 */
abstract class AbstractXoShiRo128 extends IntProvider {
    /** Size of the state vector. */
    private static final int SEED_SIZE = 4;
    /** The coefficients for the jump function. */
    private static final int[] JUMP_COEFFICIENTS = {
        0x8764000b, 0xf542d2d3, 0x6fa035c3, 0x77f2db5b
    };
    /** The coefficients for the long jump function. */
    private static final int[] LONG_JUMP_COEFFICIENTS = {
        0xb523952e, 0x0b6f099f, 0xccf5a0ef, 0x1c580662
    };

    // State is maintained using variables rather than an array for performance

    /** State 0 of the generator. */
    protected int state0;
    /** State 1 of the generator. */
    protected int state1;
    /** State 2 of the generator. */
    protected int state2;
    /** State 3 of the generator. */
    protected int state3;

    /**
     * Creates a new instance.
     *
     * @param seed Initial seed.
     * If the length is larger than 4, only the first 4 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set. A seed containing all zeros will create a non-functional generator.
     */
    AbstractXoShiRo128(int[] seed) {
        if (seed.length < SEED_SIZE) {
            final int[] state = new int[SEED_SIZE];
            fillState(state, seed);
            setState(state);
        } else {
            setState(seed);
        }
    }

    /**
     * Creates a new instance using a 4 element seed.
     * A seed containing all zeros will create a non-functional generator.
     *
     * @param seed0 Initial seed element 0.
     * @param seed1 Initial seed element 1.
     * @param seed2 Initial seed element 2.
     * @param seed3 Initial seed element 3.
     */
    AbstractXoShiRo128(int seed0, int seed1, int seed2, int seed3) {
        state0 = seed0;
        state1 = seed1;
        state2 = seed2;
        state3 = seed3;
    }

    /**
     * Creates a copy instance.
     *
     * @param source Source to copy.
     */
    protected AbstractXoShiRo128(AbstractXoShiRo128 source) {
        super(source);
        state0 = source.state0;
        state1 = source.state1;
        state2 = source.state2;
        state3 = source.state3;
    }

    /**
     * Copies the state from the array into the generator state.
     *
     * @param state the new state
     */
    private void setState(int[] state) {
        state0 = state[0];
        state1 = state[1];
        state2 = state[2];
        state3 = state[3];
    }

    /** {@inheritDoc} */
    
    protected byte[] getStateInternal() {
        return composeStateInternal(NumberFactory.makeByteArray(
                                        new int[] {state0, state1, state2, state3}),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, SEED_SIZE * 4);

        setState(NumberFactory.makeIntArray(c[0]));

        super.setStateInternal(c[1]);
    }

    /** {@inheritDoc} */
    
    public int next() {
        final int result = nextOutput();

        final int t = state1 << 9;

        state2 ^= state0;
        state3 ^= state1;
        state1 ^= state2;
        state0 ^= state3;

        state2 ^= t;

        state3 = Integer.rotateLeft(state3, 11);

        return result;
    }

    /**
     * Use the current state to compute the next output from the generator.
     * The output function shall vary with respect to different generators.
     * This method is called from {@link #next()} before the current state is updated.
     *
     * @return the next output
     */
    protected abstract int nextOutput();

    /**
     * Create a copy.
     *
     * @return the copy
     */
    protected abstract AbstractXoShiRo128 copy();

    /**
     * Perform the jump to advance the generator state. Resets the cached state of the generator.
     *
     * @param jumpCoefficients Jump coefficients.
     */
    private void performJump(int[] jumpCoefficients) {
        int s0 = 0;
        int s1 = 0;
        int s2 = 0;
        int s3 = 0;
        for (final int jc : jumpCoefficients) {
            for (int b = 0; b < 32; b++) {
                if ((jc & (1 << b)) != 0) {
                    s0 ^= state0;
                    s1 ^= state1;
                    s2 ^= state2;
                    s3 ^= state3;
                }
                next();
            }
        }
        state0 = s0;
        state1 = s1;
        state2 = s2;
        state3 = s3;
        resetCachedState();
    }
}
