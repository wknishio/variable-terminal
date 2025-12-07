package org.bouncycastlevt.crypto.params;

import java.security.SecureRandom;

import org.bouncycastlevt.crypto.KeyGenerationParameters;

public class Ed448KeyGenerationParameters
    extends KeyGenerationParameters
{
    public Ed448KeyGenerationParameters(SecureRandom random)
    {
        super(random, 448);
    }
}
