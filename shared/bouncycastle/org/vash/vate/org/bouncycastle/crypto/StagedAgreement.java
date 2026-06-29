package org.vash.vate.org.bouncycastle.crypto;

import org.vash.vate.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface StagedAgreement
    extends BasicAgreement
{
    AsymmetricKeyParameter calculateStage(CipherParameters pubKey);
}
