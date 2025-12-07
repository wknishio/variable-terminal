package org.bouncycastlevt.math.field;

public interface PolynomialExtensionField extends ExtensionField
{
    Polynomial getMinimalPolynomial();
}
