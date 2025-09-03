package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * ProtectedPart ::= SEQUENCE {
 *          header    PKIHeader,
 *          body      PKIBody
 *      }
 * </pre>
 */
public class ProtectedPart
    extends ASN1Object
{
    private final PKIHeader header;
    private final PKIBody body;

    private ProtectedPart(ASN1Sequence seq)
    {
        header = PKIHeader.getInstance(seq.getObjectAt(0));
        body = PKIBody.getInstance(seq.getObjectAt(1));
    }

    public ProtectedPart(PKIHeader header, PKIBody body)
    {
        this.header = header;
        this.body = body;
    }

    public static ProtectedPart getInstance(Object o)
    {
        if (o instanceof ProtectedPart)
        {
            return (ProtectedPart)o;
        }

        if (o != null)
        {
            return new ProtectedPart(ASN1Sequence.getInstance(o));
        }

        return null;
    }

    public PKIHeader getHeader()
    {
        return header;
    }

    public PKIBody getBody()
    {
        return body;
    }

    /**
     * <pre>
     * ProtectedPart ::= SEQUENCE {
     *                    header    PKIHeader,
     *                    body      PKIBody
     * }
     * </pre>
     *
     * @return a basic ASN.1 object representation.
     */
    public ASN1Primitive toASN1Primitive()
    {
        return new DERSequence(header, body);
    }
}
