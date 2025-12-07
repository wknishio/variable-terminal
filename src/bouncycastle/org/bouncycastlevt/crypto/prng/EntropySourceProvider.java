package org.bouncycastlevt.crypto.prng;

public interface EntropySourceProvider
{
    EntropySource get(final int bitsRequired);
}
