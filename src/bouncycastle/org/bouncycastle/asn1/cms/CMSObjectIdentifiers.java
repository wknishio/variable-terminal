package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;

public interface CMSObjectIdentifiers
{
    /** PKCS#7: 1.2.840.113549.1.7.1 */
    ASN1ObjectIdentifier    data = PKCSObjectIdentifiers.data;
    /** PKCS#7: 1.2.840.113549.1.7.2 */
    ASN1ObjectIdentifier    signedData = PKCSObjectIdentifiers.signedData;
    /** PKCS#7: 1.2.840.113549.1.7.3 */
    ASN1ObjectIdentifier    envelopedData = PKCSObjectIdentifiers.envelopedData;
    /** PKCS#7: 1.2.840.113549.1.7.4 */
    ASN1ObjectIdentifier    signedAndEnvelopedData = PKCSObjectIdentifiers.signedAndEnvelopedData;
    /** PKCS#7: 1.2.840.113549.1.7.5 */
    ASN1ObjectIdentifier    digestedData = PKCSObjectIdentifiers.digestedData;
    /** PKCS#7: 1.2.840.113549.1.7.6 */
    ASN1ObjectIdentifier    encryptedData = PKCSObjectIdentifiers.encryptedData;
    /** PKCS#9: 1.2.840.113549.1.9.16.1.2 -- smime ct authData */
    ASN1ObjectIdentifier    authenticatedData = PKCSObjectIdentifiers.id_ct_authData;
    /** PKCS#9: 1.2.840.113549.1.9.16.1.9 -- smime ct compressedData */
    ASN1ObjectIdentifier    compressedData = PKCSObjectIdentifiers.id_ct_compressedData;
    /** PKCS#9: 1.2.840.113549.1.9.16.1.23 -- smime ct authEnvelopedData */
    ASN1ObjectIdentifier    authEnvelopedData = PKCSObjectIdentifiers.id_ct_authEnvelopedData;
    /** PKCS#9: 1.2.840.113549.1.9.16.1.31 -- smime ct timestampedData*/
    ASN1ObjectIdentifier    timestampedData = PKCSObjectIdentifiers.id_ct_timestampedData;
    ASN1ObjectIdentifier    zlibCompress = PKCSObjectIdentifiers.id_alg_zlibCompress;

    /**
     * The other Revocation Info arc
     * <p>
     * <pre>
     * id-ri OBJECT IDENTIFIER ::= { iso(1) identified-organization(3)
     *        dod(6) internet(1) security(5) mechanisms(5) pkix(7) ri(16) }
     * </pre>
     */
    ASN1ObjectIdentifier    id_ri = X509ObjectIdentifiers.id_pkix.branch("16");

    /** 1.3.6.1.5.5.7.16.2 */
    ASN1ObjectIdentifier    id_ri_ocsp_response = id_ri.branch("2");
    /** 1.3.6.1.5.5.7.16.4 */
    ASN1ObjectIdentifier    id_ri_scvp = id_ri.branch("4");

    /** 1.3.6.1.5.5.7.6 */
    ASN1ObjectIdentifier id_alg = X509ObjectIdentifiers.pkix_algorithms;

    ASN1ObjectIdentifier id_RSASSA_PSS_SHAKE128 = X509ObjectIdentifiers.id_rsassa_pss_shake128;

    ASN1ObjectIdentifier id_RSASSA_PSS_SHAKE256 = X509ObjectIdentifiers.id_rsassa_pss_shake256;

    ASN1ObjectIdentifier id_ecdsa_with_shake128 = X509ObjectIdentifiers.id_ecdsa_with_shake128;

    ASN1ObjectIdentifier id_ecdsa_with_shake256 = X509ObjectIdentifiers.id_ecdsa_with_shake256;

    /**
     * OtherRecipientInfo types
     */
    ASN1ObjectIdentifier id_ori = PKCSObjectIdentifiers.id_smime.branch("13");

    ASN1ObjectIdentifier id_ori_kem = id_ori.branch("3");

    /**
     *    id-alg-cek-hkdf-sha256 OBJECT IDENTIFIER ::= { iso(1) member-body(2)
     *        us(840) rsadsi(113549) pkcs(1) pkcs-9(9) smime(16) alg(3) 31 }
     */
    ASN1ObjectIdentifier id_alg_cek_hkdf_sha256 = PKCSObjectIdentifiers.smime_alg.branch("31");
}
