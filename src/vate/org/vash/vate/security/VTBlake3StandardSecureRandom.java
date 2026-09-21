package org.vash.vate.security;

import java.security.SecureRandom;
import java.util.Random;

public class VTBlake3StandardSecureRandom extends SecureRandom
{
  private static final long serialVersionUID = 1L;
  private final VTBlake3StandardMessageDigest blake3 = new VTBlake3StandardMessageDigest();
  private final VTDigestRandomGenerator generator = new VTDigestRandomGenerator(blake3);
  
  public VTBlake3StandardSecureRandom()
  {
    
  }
  
  public VTBlake3StandardSecureRandom(Random random)
  {
    byte[] secureSeed = new byte[32];
    random.nextBytes(secureSeed);
    setSeed(secureSeed);
  }
  
  public VTBlake3StandardSecureRandom(final byte[] inSeed)
  {
    setSeed(inSeed);
  }
  
  public void setSeed(final byte[] seed)
  {
    if (blake3 == null)
    {
      return;
    }
    blake3.setSeed(seed);
  }
  
  public void setSeed(final long seed)
  {
    if (blake3 == null)
    {
      return;
    }
    byte[] seedBytes = new byte[8];
    seedBytes[0] = (byte) seed;
    seedBytes[1] = (byte) (seed >> 8);
    seedBytes[2] = (byte) (seed >> 16);
    seedBytes[3] = (byte) (seed >> 24);
    seedBytes[4] = (byte) (seed >> 32);
    seedBytes[5] = (byte) (seed >> 40);
    seedBytes[6] = (byte) (seed >> 48);
    seedBytes[7] = (byte) (seed >> 56);
    blake3.setSeed(seedBytes);
  }
  
  // public methods overriding random
  public void nextBytes(final byte[] bytes)
  {
    generator.nextBytes(bytes);
  }
  
  public int nextInt()
  {
    byte[] intBuffer = new byte[4];
    nextBytes(intBuffer);
    return ((intBuffer[0] & 0xFF) | (intBuffer[1] & 0xFF) << 8 | (intBuffer[2] & 0xFF) << 16 | (intBuffer[3] & 0xFF) << 24);
  }
  
  public long nextLong()
  {
    byte[] longBuffer = new byte[8];
    nextBytes(longBuffer);
    return ((longBuffer[0] & 0xFFL) | ((longBuffer[1] & 0xFFL) << 8) | ((longBuffer[2] & 0xFFL) << 16) | ((longBuffer[3] & 0xFFL) << 24) | ((longBuffer[4] & 0xFFL) << 32) | ((longBuffer[5] & 0xFFL) << 40) | ((longBuffer[6] & 0xFFL) << 48) | ((longBuffer[7] & 0xFFL) << 56));
  }
  
  public float nextFloat()
  {
    return Float.intBitsToFloat(nextInt());
  }
  
  public double nextDouble()
  {
    return Double.longBitsToDouble(nextLong());
  }
}
