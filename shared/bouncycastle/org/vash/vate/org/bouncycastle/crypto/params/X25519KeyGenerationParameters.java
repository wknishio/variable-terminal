package org.vash.vate.org.bouncycastle.crypto.params;

import java.security.SecureRandom;

import org.vash.vate.org.bouncycastle.crypto.KeyGenerationParameters;

public class X25519KeyGenerationParameters
    extends KeyGenerationParameters
{
    public X25519KeyGenerationParameters(SecureRandom random)
    {
        super(random, 255);
    }
}
