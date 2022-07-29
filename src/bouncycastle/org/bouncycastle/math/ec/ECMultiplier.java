package org.bouncycastle.math.ec;

import java.math.BigInteger;

/**
 * Interface for classes encapsulating a point multiplication algorithm
 * for  ECPoint</code>s.
 */
public interface ECMultiplier
{
    /**
     * Multiplies the  ECPoint p</code> by  k</code>, i.e.
     *  p</code> is added  k</code> times to itself.
     * @param p The  ECPoint</code> to be multiplied.
     * @param k The factor by which  p</code> is multiplied.
     * @return  p</code> multiplied by  k</code>.
     */
    ECPoint multiply(ECPoint p, BigInteger k);
}
