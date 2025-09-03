package org.vtbouncycastle.crypto.agreement;

import java.math.BigInteger;

import org.vtbouncycastle.crypto.BasicAgreement;
import org.vtbouncycastle.crypto.CipherParameters;
import org.vtbouncycastle.crypto.params.AsymmetricKeyParameter;
import org.vtbouncycastle.crypto.params.DHParameters;
import org.vtbouncycastle.crypto.params.DHPrivateKeyParameters;
import org.vtbouncycastle.crypto.params.DHPublicKeyParameters;
import org.vtbouncycastle.crypto.params.ParametersWithRandom;

/**
 * a Diffie-Hellman key agreement class.
 * <p>
 * note: This is only the basic algorithm, it doesn't take advantage of
 * long term public keys if they are available. See the DHAgreement class
 * for a "better" implementation.
 */
public class DHBasicAgreement
    implements BasicAgreement
{
    private static final BigInteger ONE = BigInteger.valueOf(1);

    private DHPrivateKeyParameters  key;
    private DHParameters            dhParams;

    public void init(
        CipherParameters    param)
    {
        AsymmetricKeyParameter  kParam;

        if (param instanceof ParametersWithRandom)
        {
            ParametersWithRandom rParam = (ParametersWithRandom)param;
            kParam = (AsymmetricKeyParameter)rParam.getParameters();
        }
        else
        {
            kParam = (AsymmetricKeyParameter)param;
        }

        if (!(kParam instanceof DHPrivateKeyParameters))
        {
            throw new IllegalArgumentException("DHEngine expects DHPrivateKeyParameters");
        }

        this.key = (DHPrivateKeyParameters)kParam;
        this.dhParams = key.getParameters();
    }

    public int getFieldSize()
    {
        return (key.getParameters().getP().bitLength() + 7) / 8;
    }

    /**
     * given a short term public key from a given party calculate the next
     * message in the agreement sequence. 
     */
    public BigInteger calculateAgreement(
        CipherParameters   pubKey)
    {
        DHPublicKeyParameters   pub = (DHPublicKeyParameters)pubKey;

        if (!pub.getParameters().equals(dhParams))
        {
            throw new IllegalArgumentException("Diffie-Hellman public key has wrong parameters.");
        }

        BigInteger p = dhParams.getP();

        BigInteger peerY = pub.getY();
        if (peerY == null || peerY.compareTo(ONE) <= 0 || peerY.compareTo(p.subtract(ONE)) >= 0)
        {
            throw new IllegalArgumentException("Diffie-Hellman public key is weak");
        }

        BigInteger result = peerY.modPow(key.getX(), p);
        if (result.equals(ONE))
        {
            throw new IllegalStateException("Shared key can't be 1");
        }

        return result;
    }
}
