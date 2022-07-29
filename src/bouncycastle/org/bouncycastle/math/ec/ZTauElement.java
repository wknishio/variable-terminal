package org.bouncycastle.math.ec;

import java.math.BigInteger;

/**
 * Class representing an element of  <b>Z</b>[&tau;]</code>. Let
 *  &lambda;</code> be an element of  <b>Z</b>[&tau;]</code>. Then
 *  &lambda;</code> is given as  &lambda; = u + v&tau;</code>. The
 * components  u</code> and  v</code> may be used directly, there
 * are no accessor methods.
 * Immutable class.
 */
class ZTauElement
{
    /**
     * The &quot;real&quot; part of  &lambda;</code>.
     */
    public final BigInteger u;

    /**
     * The &quot; &tau;</code>-adic&quot; part of  &lambda;</code>.
     */
    public final BigInteger v;

    /**
     * Constructor for an element  &lambda;</code> of
     *  <b>Z</b>[&tau;]</code>.
     * @param u The &quot;real&quot; part of  &lambda;</code>.
     * @param v The &quot; &tau;</code>-adic&quot; part of
     *  &lambda;</code>.
     */
    public ZTauElement(BigInteger u, BigInteger v)
    {
        this.u = u;
        this.v = v;
    }
}
