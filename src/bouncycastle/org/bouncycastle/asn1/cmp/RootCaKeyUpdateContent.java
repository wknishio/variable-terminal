package org.bouncycastle.asn1.cmp;

import java.util.Iterator;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * GenMsg:    {id-it 20}, RootCaCertValue | < absent >
 * GenRep:    {id-it 18}, RootCaKeyUpdateContent | < absent >
 * <p>
 * RootCaCertValue ::= CMPCertificate
 * <p>
 * RootCaKeyUpdateValue ::= RootCaKeyUpdateContent
 * <p>
 * RootCaKeyUpdateContent ::= SEQUENCE {
 * newWithNew       CMPCertificate,
 * newWithOld   [0] CMPCertificate OPTIONAL,
 * oldWithNew   [1] CMPCertificate OPTIONAL
 * }
 */
public class RootCaKeyUpdateContent
    extends ASN1Object
{
    private final CMPCertificate newWithNew;
    private final CMPCertificate newWithOld;
    private final CMPCertificate oldWithNew;

    public RootCaKeyUpdateContent(CMPCertificate newWithMew, CMPCertificate newWithOld, CMPCertificate oldWithNew)
    {
        this.newWithNew = newWithMew;
        this.newWithOld = newWithOld;
        this.oldWithNew = oldWithNew;
    }

    private RootCaKeyUpdateContent(ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 3)
        {
            throw new IllegalArgumentException("expected sequence of 1 to 3 elements only");
        }

        CMPCertificate newWithNew;
        CMPCertificate newWithOld = null;
        CMPCertificate oldWithNew = null;

        Iterator  encodable = seq.iterator();

        newWithNew = CMPCertificate.getInstance(encodable.next());
        while (encodable.hasNext())
        {
            ASN1TaggedObject ato = ASN1TaggedObject.getInstance(encodable.next());
            if (ato.getTagNo() == 0)
            {
                newWithOld = CMPCertificate.getInstance(ato, true);
            }
            else if (ato.getTagNo() == 1)
            {
                oldWithNew = CMPCertificate.getInstance(ato, true);
            }
        }

        this.newWithNew = newWithNew;
        this.newWithOld = newWithOld;
        this.oldWithNew = oldWithNew;

    }

    public static RootCaKeyUpdateContent getInstance(Object o)
    {
        if (o instanceof RootCaKeyUpdateContent)
        {
            return (RootCaKeyUpdateContent)o;
        }
        if (o != null)
        {
            return new RootCaKeyUpdateContent(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    public CMPCertificate getNewWithNew()
    {
        return newWithNew;
    }

    public CMPCertificate getNewWithOld()
    {
        return newWithOld;
    }

    public CMPCertificate getOldWithNew()
    {
        return oldWithNew;
    }

    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector avec = new ASN1EncodableVector();
        avec.add(newWithNew);

        if (newWithOld != null)
        {
            avec.add(new DERTaggedObject(true, 0, newWithOld));
        }
        if (oldWithNew != null)
        {
            avec.add(new DERTaggedObject(true, 1, oldWithNew));
        }
        return new DERSequence(avec);
    }
}
