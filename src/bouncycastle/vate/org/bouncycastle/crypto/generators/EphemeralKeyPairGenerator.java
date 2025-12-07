package vate.org.bouncycastle.crypto.generators;

import vate.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import vate.org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import vate.org.bouncycastle.crypto.EphemeralKeyPair;
import vate.org.bouncycastle.crypto.KeyEncoder;

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
