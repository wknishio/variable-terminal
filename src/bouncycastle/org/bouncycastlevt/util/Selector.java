package org.bouncycastlevt.util;

public interface Selector
    extends Cloneable
{
    boolean match(Object obj);

    Object clone();
}
