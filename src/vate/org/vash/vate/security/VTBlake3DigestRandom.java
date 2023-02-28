package org.vash.vate.security;

import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import java.security.SecureRandom;
import org.bouncycastle.crypto.digests.Blake3Digest;

public class VTBlake3DigestRandom extends java.security.SecureRandom
{
  private static final long serialVersionUID = 1L;
  
  private RandomGenerator generator;
  
  public VTBlake3DigestRandom()
  {
    this(new DigestRandomGenerator(new Blake3Digest()));
    byte[] seed = new byte[64];
    new SecureRandom().nextBytes(seed);
    setSeed(seed);
  }
  
  public VTBlake3DigestRandom(byte[] inSeed)
  {
    this(new DigestRandomGenerator(new Blake3Digest()));
    setSeed(inSeed);
  }
  
  public VTBlake3DigestRandom(RandomGenerator generator)
  {
    this.generator = generator;
  }
  
  public void setSeed(byte[] seed)
  {
    if (generator != null)
    {
      generator.addSeedMaterial(seed);
    }
  }
  
  // public methods overriding random
  public void nextBytes(byte[] bytes)
  {
    generator.nextBytes(bytes);
  }
  
  public void setSeed(long seed)
  {
    if (generator != null)
    {
      generator.addSeedMaterial(seed);
    }
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
