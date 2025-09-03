package org.vtbouncycastle.crypto.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.vtbouncycastle.crypto.KeyParser;
import org.vtbouncycastle.crypto.params.AsymmetricKeyParameter;
import org.vtbouncycastle.crypto.params.DHParameters;
import org.vtbouncycastle.crypto.params.DHPublicKeyParameters;
import org.vtbouncycastle.util.io.Streams;

public class DHIESPublicKeyParser
    implements KeyParser
{
    private DHParameters dhParams;

    public DHIESPublicKeyParser(DHParameters dhParams)
    {
        this.dhParams = dhParams;
    }

    public AsymmetricKeyParameter readKey(InputStream stream)
        throws IOException
    {
        byte[] V = new byte[(dhParams.getP().bitLength() + 7) / 8];

        Streams.readFully(stream, V, 0, V.length);

        return new DHPublicKeyParameters(new BigInteger(1, V), dhParams);
    }
}
