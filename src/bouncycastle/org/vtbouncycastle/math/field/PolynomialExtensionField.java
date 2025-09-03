package org.vtbouncycastle.math.field;

public interface PolynomialExtensionField extends ExtensionField
{
    Polynomial getMinimalPolynomial();
}
