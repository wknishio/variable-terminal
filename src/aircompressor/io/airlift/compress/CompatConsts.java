package io.airlift.compress;

public class CompatConsts
{
	public static final int ARRAY_BYTE_BASE_OFFSET;
	static
	{
		ARRAY_BYTE_BASE_OFFSET = UnsafeUtil.UNSAFE.arrayBaseOffset(byte[].class);
	}
}
