package org.vtbouncycastle.crypto.prng;

import org.vtbouncycastle.crypto.prng.drbg.SP80090DRBG;

interface DRBGProvider
{
    String getAlgorithm();

    SP80090DRBG get(EntropySource entropySource);
}
