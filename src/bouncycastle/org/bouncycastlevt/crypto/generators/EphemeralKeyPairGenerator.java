package org.bouncycastlevt.crypto.generators;

import org.bouncycastlevt.crypto.AsymmetricCipherKeyPair;
import org.bouncycastlevt.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastlevt.crypto.EphemeralKeyPair;
import org.bouncycastlevt.crypto.KeyEncoder;

public class EphemeralKeyPairGenerator
{
    private AsymmetricCipherKeyPairGenerator gen;
    private KeyEncoder keyEncoder;

    public EphemeralKeyPairGenerator(AsymmetricCipherKeyPairGenerator gen, KeyEncoder keyEncoder)
    {
        this.gen = gen;
        this.keyEncoder = keyEncoder;
    }

    public EphemeralKeyPair generate()
    {
        AsymmetricCipherKeyPair eph = gen.generateKeyPair();

        // Encode the ephemeral public key
         return new EphemeralKeyPair(eph, keyEncoder);
    }
}
