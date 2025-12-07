package org.bouncycastlevt.crypto.modes;

import org.bouncycastlevt.crypto.BlockCipher;

/**
 * An {@link AEADCipher} based on a {@link BlockCipher}.
 */
public interface AEADBlockCipher
    extends AEADCipher
{
    /**
     * return the {@link BlockCipher} this object wraps.
     *
     * @return the {@link BlockCipher} this object wraps.
     */
    public BlockCipher getUnderlyingCipher();
}
