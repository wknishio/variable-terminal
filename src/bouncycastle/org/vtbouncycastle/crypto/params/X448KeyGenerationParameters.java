package org.vtbouncycastle.crypto.params;

import java.security.SecureRandom;

import org.vtbouncycastle.crypto.KeyGenerationParameters;

public class X448KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X448KeyGenerationParameters(SecureRandom random)
    {
        super(random, 448);
    }
}
