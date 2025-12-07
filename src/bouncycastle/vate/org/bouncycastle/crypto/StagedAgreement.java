package vate.org.bouncycastle.crypto;

import vate.org.bouncycastle.crypto.params.AsymmetricKeyParameter;

public interface StagedAgreement
    extends BasicAgreement
{
    AsymmetricKeyParameter calculateStage(CipherParameters pubKey);
}
