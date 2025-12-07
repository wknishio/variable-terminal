package org.bouncycastlevt.crypto.prng;

import org.bouncycastlevt.crypto.prng.drbg.SP80090DRBG;

interface DRBGProvider
{
    String getAlgorithm();

    SP80090DRBG get(EntropySource entropySource);
}
