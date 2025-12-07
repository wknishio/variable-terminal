package org.bouncycastlevt.crypto.params;

import java.security.SecureRandom;

import org.bouncycastlevt.crypto.KeyGenerationParameters;

public class X448KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X448KeyGenerationParameters(SecureRandom random)
    {
        super(random, 448);
    }
}
