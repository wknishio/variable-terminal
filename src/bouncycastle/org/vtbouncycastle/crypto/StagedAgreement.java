package org.vtbouncycastle.crypto;

import org.vtbouncycastle.crypto.params.AsymmetricKeyParameter;

public interface StagedAgreement
    extends BasicAgreement
{
    AsymmetricKeyParameter calculateStage(CipherParameters pubKey);
}
