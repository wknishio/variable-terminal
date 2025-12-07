package org.bouncycastlevt.crypto;

import org.bouncycastlevt.crypto.params.AsymmetricKeyParameter;

public interface KeyEncoder
{
    byte[] getEncoded(AsymmetricKeyParameter keyParameter);
}
