package org.bouncycastlevt.crypto.params;

import java.security.SecureRandom;

import org.bouncycastlevt.crypto.KeyGenerationParameters;

public class Ed25519KeyGenerationParameters
    extends KeyGenerationParameters
{
    public Ed25519KeyGenerationParameters(SecureRandom random)
    {
        super(random, 256);
    }
}
