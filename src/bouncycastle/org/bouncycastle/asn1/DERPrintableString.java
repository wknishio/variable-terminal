package org.bouncycastle.asn1;

/**
 * DER PrintableString object.
 * <p>
 * X.680 section 37.4 defines PrintableString character codes as ASCII subset of following characters:
 * </p>
 *  
 *  Latin capital letters: 'A' .. 'Z'</li>
 *  Latin small letters: 'a' .. 'z'</li>
 *  Digits: '0'..'9'</li>
 *  Space</li>
 *  Apostrophe: '\''</li>
 *  Left parenthesis: '('</li>
 *  Right parenthesis: ')'</li>
 *  Plus sign: '+'</li>
 *  Comma: ','</li>
 *  Hyphen-minus: '-'</li>
 *  Full stop: '.'</li>
 *  Solidus: '/'</li>
 *  Colon: ':'</li>
 *  Equals sign: '='</li>
 *  Question mark: '?'</li>
 * </ul>
 * <p>
 * Explicit character set escape sequences are not allowed.
 * </p>
 */
public class DERPrintableString
    extends ASN1PrintableString
{
    /**
     * Basic constructor - this does not validate the string
     */
    public DERPrintableString(
        String   string)
    {
        this(string, false);
    }

    /**
     * Constructor with optional validation.
     *
     * @param string the base string to wrap.
     * @param validate whether or not to check the string.
     * @throws IllegalArgumentException if validate is true and the string
     * contains characters that should not be in a PrintableString.
     */
    public DERPrintableString(
        String   string,
        boolean  validate)
    {
        super(string, validate);
    }

    DERPrintableString(byte[] contents, boolean clone)
    {
        super(contents, clone);
    }
}
