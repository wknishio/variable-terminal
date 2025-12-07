package org.bouncycastlevt.math.field;

public interface ExtensionField extends FiniteField
{
    FiniteField getSubfield();

    int getDegree();
}
