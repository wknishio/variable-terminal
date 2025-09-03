package org.vtbouncycastle.crypto;

import java.io.IOException;
import java.io.InputStream;

import org.vtbouncycastle.crypto.params.AsymmetricKeyParameter;

public interface KeyParser
{
    AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException;
}
