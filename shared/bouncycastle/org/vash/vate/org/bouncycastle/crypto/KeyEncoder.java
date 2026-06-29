package org.vash.vate.org.bouncycastle.crypto;

import org.vash.vate.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface KeyEncoder
{
    byte[] getEncoded(AsymmetricKeyParameter keyParameter);
}
