package org.bouncycastle.pqc.math.linearalgebra;


/**
 * This abstract class implements an element of the finite field <i>GF(2) n
 * </sup></i> in either <i>optimal normal basis</i> representation (<i>ONB</i>)
 * or in <i>polynomial</i> representation. It is extended by the classes <a
 * href = GF2nONBElement.html>  GF2nONBElement</tt></a> and <a href =
 * GF2nPolynomialElement.html>  GF2nPolynomialElement</tt> </a>.
 *
 * @see GF2nPolynomialElement
 * @see GF2nONBElement
 * @see GF2nONBField
 */
public abstract class GF2nElement
    implements GFElement
{

    // /////////////////////////////////////////////////////////////////////
    // member variables
    // /////////////////////////////////////////////////////////////////////

    /**
     * holds a pointer to this element's corresponding field.
     */
    protected GF2nField mField;

    /**
     * holds the extension degree <i>n</i> of this element's corresponding
     * field.
     */
    protected int mDegree;

    // /////////////////////////////////////////////////////////////////////
    // pseudo-constructors
    // /////////////////////////////////////////////////////////////////////

    /**
     * @return a copy of this GF2nElement
     */
    public abstract Object clone();

    // /////////////////////////////////////////////////////////////////////
    // assignments
    // /////////////////////////////////////////////////////////////////////

    /**
     * Assign the value 0 to this element.
     */
    abstract void assignZero();

    /**
     * Assigns the value 1 to this element.
     */
    abstract void assignOne();

    // /////////////////////////////////////////////////////////////////////
    // access
    // /////////////////////////////////////////////////////////////////////

    /**
     * Returns whether the rightmost bit of the bit representation is set. This
     * is needed for data conversion according to 1363.
     *
     * @return true if the rightmost bit of this element is set
     */
    public abstract boolean testRightmostBit();

    /**
     * Checks whether the indexed bit of the bit representation is set
     *
     * @param index the index of the bit to test
     * @return  true</tt> if the indexed bit is set
     */
    abstract boolean testBit(int index);

    /**
     * Returns the field of this element.
     *
     * @return the field of this element
     */
    public final GF2nField getField()
    {
        return mField;
    }

    // /////////////////////////////////////////////////////////////////////
    // arithmetic
    // /////////////////////////////////////////////////////////////////////

    /**
     * Returns  this</tt> element + 1.
     *
     * @return  this</tt> + 1
     */
    public abstract GF2nElement increase();

    /**
     * Increases this element by one.
     */
    public abstract void increaseThis();

    /**
     * Compute the difference of this element and  minuend</tt>.
     *
     * @param minuend the minuend
     * @return  this - minuend</tt> (newly created)
     */
    public final GFElement subtract(GFElement minuend)
    {
        return add(minuend);
    }

    /**
     * Compute the difference of this element and  minuend</tt>,
     * overwriting this element.
     *
     * @param minuend the minuend
     */
    public final void subtractFromThis(GFElement minuend)
    {
        addToThis(minuend);
    }

    /**
     * Returns  this</tt> element to the power of 2.
     *
     * @return  this</tt> 2</sup>
     */
    public abstract GF2nElement square();

    /**
     * Squares  this</tt> element.
     */
    public abstract void squareThis();

    /**
     * Compute the square root of this element and return the result in a new
     * {@link GF2nElement}.
     *
     * @return  this 1/2</sup></tt> (newly created)
     */
    public abstract GF2nElement squareRoot();

    /**
     * Compute the square root of this element.
     */
    public abstract void squareRootThis();

    /**
     * Performs a basis transformation of this element to the given GF2nField
     *  basis</tt>.
     *
     * @param basis the GF2nField representation to transform this element to
     * @return this element in the representation of  basis</tt>
     */
    public final GF2nElement convert(GF2nField basis)
    {
        return mField.convert(this, basis);
    }

    /**
     * Returns the trace of this element.
     *
     * @return the trace of this element
     */
    public abstract int trace();

    /**
     * Solves a quadratic equation. 
     * Let z 2</sup> + z =  this</tt>. Then this method returns z.
     *
     * @return z with z 2</sup> + z =  this</tt>
     */
    public abstract GF2nElement solveQuadraticEquation()
        throws RuntimeException;

}
