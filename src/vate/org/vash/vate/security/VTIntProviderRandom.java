package org.vash.vate.security;

import java.util.Random;

import org.apache.commons.rng.core.source32.IntProvider;

public abstract class VTIntProviderRandom extends Random
{
  private static final long serialVersionUID = 1L;
  
  private IntProvider intProvider;
  
  public VTIntProviderRandom()
  {
    
  }
  
  public void setIntProvider(IntProvider intProvider)
  {
    this.intProvider = intProvider;
  }
  
  public void setSeed(byte[] seed)
  {
    
  }
  
  public void setSeed(long seed)
  {
    
  }
  
  // public methods overriding random
  public void nextBytes(byte[] bytes)
  {
    intProvider.nextBytes(bytes);
  }
  
  public int nextInt()
  {
    return intProvider.nextInt();
  }
  
  public long nextLong()
  {
    return intProvider.nextLong();
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