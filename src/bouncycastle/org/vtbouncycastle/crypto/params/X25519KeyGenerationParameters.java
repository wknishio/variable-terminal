package org.vtbouncycastle.crypto.params;

import java.security.SecureRandom;

import org.vtbouncycastle.crypto.KeyGenerationParameters;

public class X25519KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X25519KeyGenerationParameters(SecureRandom random)
    {
        super(random, 255);
    }
}
