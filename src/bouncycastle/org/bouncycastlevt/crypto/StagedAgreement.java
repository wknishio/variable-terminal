package org.bouncycastlevt.crypto;

import org.bouncycastlevt.crypto.params.AsymmetricKeyParameter;

public interface StagedAgreement
    extends BasicAgreement
{
    AsymmetricKeyParameter calculateStage(CipherParameters pubKey);
}
