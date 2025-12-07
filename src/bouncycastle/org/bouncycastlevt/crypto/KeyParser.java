package org.bouncycastlevt.crypto;

import java.io.IOException;
import java.io.InputStream;

import org.bouncycastlevt.crypto.params.AsymmetricKeyParameter;

public interface KeyParser
{
    AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException;
}
