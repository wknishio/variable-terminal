package org.bouncycastlevt.crypto.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bouncycastlevt.crypto.KeyParser;
import org.bouncycastlevt.crypto.params.AsymmetricKeyParameter;
import org.bouncycastlevt.crypto.params.DHParameters;
import org.bouncycastlevt.crypto.params.DHPublicKeyParameters;
import org.bouncycastlevt.util.io.Streams;

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
