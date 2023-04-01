package org.vash.vate.security;

import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.Blake3Digest;
import org.bouncycastle.crypto.params.Blake3Parameters;

public class VTBlake3DigestRandom extends java.security.SecureRandom
{
  private static final long serialVersionUID = 1L;
  private Blake3Digest blake3;
  private RandomGenerator generator;
  
  public VTBlake3DigestRandom()
  {
    blake3 = new Blake3Digest(64);
    generator = new DigestRandomGenerator(blake3);
    byte[] secureSeed = new byte[64];
    new SecureRandom().nextBytes(secureSeed);
    setSeed(secureSeed);
  }
  
  public VTBlake3DigestRandom(byte[] inSeed)
  {
    blake3 = new Blake3Digest(64);
    generator = new DigestRandomGenerator(blake3);
    setSeed(inSeed);
  }
  
  public void setSeed(byte[] seed)
  {
    if (blake3 == null)
    {
      return;
    }
    blake3.init(Blake3Parameters.context(seed));
  }
  
  public void setSeed(long seed)
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
    blake3.init(Blake3Parameters.context(seedBytes));
  }
  
  // public methods overriding random
  public void nextBytes(byte[] bytes)
  {
    generator.nextBytes(bytes);
  }
  
  public int nextInt()
  {
    byte[] intBytes = new byte[4];
    nextBytes(intBytes);
    int result = 0;
    for (int i = 0; i < 4; i++)
    {
      result = (result << 8) + (intBytes[i] & 0xff);
    }
    return result;
  }
  
  public long nextLong()
  {
    byte[] intBytes = new byte[8];
    nextBytes(intBytes);
    long result = 0;
    for (int i = 0; i < 4; i++)
    {
      result = (result << 8) + (intBytes[i] & 0xff);
    }
    return result;
  }
  
  public float nextFloat()
  {
    return Float.intBitsToFloat(nextInt());
  }
  
  public double nextDouble()
  {
    return Double.longBitsToDouble(nextLong());
  }
  
//  protected int next(int numBits)
//  {
//    int size = (numBits + 7) / 8;
//    byte[] bytes = new byte[size];
//    nextBytes(bytes);
//    int result = 0;
//    for (int i = 0; i < size; i++)
//    {
//        result = (result << 8) + (bytes[i] & 0xff);
//    }
//    return result & ((1 << numBits) - 1);
//  }
}
