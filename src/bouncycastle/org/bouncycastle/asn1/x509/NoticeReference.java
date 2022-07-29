package org.bouncycastle.asn1.x509;

import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 *  NoticeReference</code> class, used in
 *  CertificatePolicies</code> X509 V3 extensions
 * (in policy qualifiers).
 * 
 *  
 *  NoticeReference ::= SEQUENCE {
 *      organization     DisplayText,
 *      noticeNumbers    SEQUENCE OF INTEGER }
 *
 * </pre> 
 * 
 * @see PolicyQualifierInfo
 * @see PolicyInformation
 */
public class NoticeReference 
    extends ASN1Object
{
    private DisplayText organization;
    private ASN1Sequence noticeNumbers;

    private static ASN1EncodableVector convertVector(Vector numbers)
    {
        ASN1EncodableVector av = new ASN1EncodableVector(numbers.size());

        Enumeration it = numbers.elements();
        while (it.hasMoreElements())
        {
            Object o = it.nextElement();
            ASN1Integer di;

            if (o instanceof BigInteger)
            {
                di = new ASN1Integer((BigInteger)o);
            }
            else if (o instanceof Integer)
            {
                di = new ASN1Integer(((Integer)o).intValue());
            }
            else
            {
                throw new IllegalArgumentException();
            }

            av.add(di);
        }
        return av;
    }

   /**
    * Creates a new  NoticeReference</code> instance.
    *
    * @param organization a  String</code> value
    * @param numbers a  Vector</code> value
    */
   public NoticeReference(
       String organization,
       Vector numbers) 
   {
       this(organization, convertVector(numbers));
   }

    /**
    * Creates a new  NoticeReference</code> instance.
    *
    * @param organization a  String</code> value
    * @param noticeNumbers an  ASN1EncodableVector</code> value
    */
   public NoticeReference(
       String organization,
       ASN1EncodableVector noticeNumbers)
   {
       this(new DisplayText(organization), noticeNumbers);
   }

   /**
    * Creates a new  NoticeReference</code> instance.
    *
    * @param organization displayText
    * @param noticeNumbers an  ASN1EncodableVector</code> value
    */
   public NoticeReference(
       DisplayText  organization,
       ASN1EncodableVector noticeNumbers)
   {
       this.organization = organization;
       this.noticeNumbers = new DERSequence(noticeNumbers);
   }

   /**
    * Creates a new  NoticeReference</code> instance.
    * <p>Useful for reconstructing a  NoticeReference</code>
    * instance from its encodable/encoded form. 
    *
    * @param as an  ASN1Sequence</code> value obtained from either
    * calling @{link toASN1Primitive()} for a  NoticeReference</code>
    * instance or from parsing it from a DER-encoded stream. 
    */
   private NoticeReference(
       ASN1Sequence as) 
   {
       if (as.size() != 2)
       {
            throw new IllegalArgumentException("Bad sequence size: "
                    + as.size());
       }

       organization = DisplayText.getInstance(as.getObjectAt(0));
       noticeNumbers = ASN1Sequence.getInstance(as.getObjectAt(1));
   }

   public static NoticeReference getInstance(
       Object as) 
   {
      if (as instanceof NoticeReference)
      {
          return (NoticeReference)as;
      }
      else if (as != null)
      {
          return new NoticeReference(ASN1Sequence.getInstance(as));
      }

      return null;
   }
   
   public DisplayText getOrganization()
   {
       return organization;
   }
   
   public ASN1Integer[] getNoticeNumbers()
   {
       ASN1Integer[] tmp = new ASN1Integer[noticeNumbers.size()];

       for (int i = 0; i != noticeNumbers.size(); i++)
       {
           tmp[i] = ASN1Integer.getInstance(noticeNumbers.getObjectAt(i));
       }

       return tmp;
   }
   
   /**
    * Describe  toASN1Object</code> method here.
    *
    * @return a  ASN1Primitive</code> value
    */
   public ASN1Primitive toASN1Primitive()
   {
      ASN1EncodableVector av = new ASN1EncodableVector(2);
      av.add (organization);
      av.add (noticeNumbers);
      return new DERSequence (av);
   }
}
