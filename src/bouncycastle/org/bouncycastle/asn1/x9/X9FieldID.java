package org.bouncycastle.asn1.x9;

import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * ASN.1 def for Elliptic-Curve Field ID structure. See
 * X9.62, for further details.
 */
public class X9FieldID
    extends ASN1Object
    implements X9ObjectIdentifiers
{
    private ASN1ObjectIdentifier     id;
    private ASN1Primitive parameters;

    /**
     * Constructor for elliptic curves over prime fields
     *  F 2</sub></code>.
     * @param primeP The prime  p</code> defining the prime field.
     */
    public X9FieldID(BigInteger primeP)
    {
        this.id = prime_field;
        this.parameters = new ASN1Integer(primeP);
    }

    /**
     * Constructor for elliptic curves over binary fields
     *  F 2 m</sup></sub></code>.
     * @param m  The exponent  m</code> of
     *  F 2 m</sup></sub></code>.
     * @param k1 The integer  k1</code> where  x m</sup> +
     * x k1</sup> + 1</code>
     * represents the reduction polynomial  f(z)</code>.
     */
    public X9FieldID(int m, int k1)
    {
        this(m, k1, 0, 0);
    }

    /**
     * Constructor for elliptic curves over binary fields
     *  F 2 m</sup></sub></code>.
     * @param m  The exponent  m</code> of
     *  F 2 m</sup></sub></code>.
     * @param k1 The integer  k1</code> where  x m</sup> +
     * x k3</sup> + x k2</sup> + x k1</sup> + 1</code>
     * represents the reduction polynomial  f(z)</code>.
     * @param k2 The integer  k2</code> where  x m</sup> +
     * x k3</sup> + x k2</sup> + x k1</sup> + 1</code>
     * represents the reduction polynomial  f(z)</code>.
     * @param k3 The integer  k3</code> where  x m</sup> +
     * x k3</sup> + x k2</sup> + x k1</sup> + 1</code>
     * represents the reduction polynomial  f(z)</code>..
     */
    public X9FieldID(int m, int k1, int k2, int k3)
    {
        this.id = characteristic_two_field;
        ASN1EncodableVector fieldIdParams = new ASN1EncodableVector(3);
        fieldIdParams.add(new ASN1Integer(m));
        
        if (k2 == 0) 
        {
            if (k3 != 0)
            {
                throw new IllegalArgumentException("inconsistent k values");
            }

            fieldIdParams.add(tpBasis);
            fieldIdParams.add(new ASN1Integer(k1));
        } 
        else 
        {
            if (k2 <= k1 || k3 <= k2)
            {
                throw new IllegalArgumentException("inconsistent k values");
            }

            fieldIdParams.add(ppBasis);
            ASN1EncodableVector pentanomialParams = new ASN1EncodableVector(3);
            pentanomialParams.add(new ASN1Integer(k1));
            pentanomialParams.add(new ASN1Integer(k2));
            pentanomialParams.add(new ASN1Integer(k3));
            fieldIdParams.add(new DERSequence(pentanomialParams));
        }
        
        this.parameters = new DERSequence(fieldIdParams);
    }

    private X9FieldID(
        ASN1Sequence  seq)
    {
        this.id = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
        this.parameters = seq.getObjectAt(1).toASN1Primitive();
    }

    public static X9FieldID getInstance(Object obj)
    {
        if (obj instanceof X9FieldID)
        {
            return (X9FieldID)obj;
        }

        if (obj != null)
        {
            return new X9FieldID(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public ASN1ObjectIdentifier getIdentifier()
    {
        return id;
    }

    public ASN1Primitive getParameters()
    {
        return parameters;
    }

    /**
     * Produce a DER encoding of the following structure.
     *  
     *  FieldID ::= SEQUENCE {
     *      fieldType       FIELD-ID.&amp;id({IOSet}),
     *      parameters      FIELD-ID.&amp;Type({IOSet}{&#64;fieldType})
     *  }
     * </pre>
     */
    public ASN1Primitive toASN1Primitive()
    {
        ASN1EncodableVector v = new ASN1EncodableVector(2);

        v.add(this.id);
        v.add(this.parameters);

        return new DERSequence(v);
    }
}
