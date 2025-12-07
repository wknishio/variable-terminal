package org.bouncycastlevt.crypto;

/**
 * Ciphers that implement StatelessProcessing are thread safe for encrypt/decrypt - just not initialisation.
 */
public interface StatelessProcessing
{
    BlockCipher newInstance();
}
