package org.bouncycastle.asn1.icao;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;

public class LDSVersionInfo
    extends ASN1Object
{
    private ASN1PrintableString ldsVersion;
    private ASN1PrintableString unicodeVersion;

    public LDSVersionInfo(String ldsVersion, String unicodeVersion)
    {
        this.ldsVersion = new DERPrintableString(ldsVersion);
        this.unicodeVersion = new DERPrintableString(unicodeVersion);
    }

    private LDSVersionInfo(ASN1Sequence seq)
    {
        if (seq.size() != 2)
        {
            throw new IllegalArgumentException("sequence wrong size for LDSVersionInfo");
        }

        this.ldsVersion = ASN1PrintableString.getInstance(seq.getObjectAt(0));
        this.unicodeVersion = ASN1PrintableString.getInstance(seq.getObjectAt(1));
    }

    public static LDSVersionInfo getInstance(Object obj)
    {
        if (obj instanceof LDSVersionInfo)
        {
            return (LDSVersionInfo)obj;
        }
        else if (obj != null)
        {
            return new LDSVersionInfo(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public String getLdsVersion()
    {
        return ldsVersion.getString();
    }

    public String getUnicodeVersion()
    {
        return unicodeVersion.getString();
    }

    /**
     * <pre>
     * LDSVersionInfo ::= SEQUENCE {
     *    ldsVersion PRINTABLE STRING
     *    unicodeVersion PRINTABLE STRING
     *  }
     * </pre>
     * @return  an ASN.1 primitive composition of this LDSVersionInfo.
     */
    public ASN1Primitive toASN1Primitive()
    {
        return new DERSequence(ldsVersion, unicodeVersion);
    }
}
