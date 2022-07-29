package org.bouncycastle.crypto;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface EncapsulatedSecretGenerator
{
    /**
     * Generate an exchange pair based on the recipient public key.
     *
     * @return An SecretWithEncapsulation derived from the recipient public key.
     */
    SecretWithEncapsulation generateEncapsulated(AsymmetricKeyParameter recipientKey);
}
