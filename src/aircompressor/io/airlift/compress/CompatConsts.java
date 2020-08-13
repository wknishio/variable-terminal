package io.airlift.compress;

public class CompatConsts
{
	public static final int ARRAY_BYTE_BASE_OFFSET;
	static
	{
		ARRAY_BYTE_BASE_OFFSET = sun.misc.Unsafe.getUnsafe().arrayBaseOffset(byte[].class);
	}
}
