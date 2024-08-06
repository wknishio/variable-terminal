package org.vash.vate.security;

public class VTArrayComparator
{
  public static boolean arrayEquals(byte[] array1, byte[] array2)
  {
    return arrayEquals(array1, array2, 0, Math.min(array1.length, array2.length));
  }
  
  public static boolean arrayEquals(byte[] array1, byte[] array2, int offset, int length)
  {
    int bits = 0;
    int limit = offset + length;
    for (int i = offset; i < limit; i++)
    {
      bits |= array1[i] ^ array2[i];
    }
    return bits == 0;
  }
  
  public static boolean arrayEquals(char[] array1, char[] array2, int offset, int length)
  {
    int bits = 0;
    int limit = offset + length;
    for (int i = offset; i < limit; i++)
    {
      bits |= array1[i] ^ array2[i];
    }
    return bits == 0;
  }
}