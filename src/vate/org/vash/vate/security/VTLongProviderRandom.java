package org.vash.vate.security;

import java.util.Random;

import org.apache.commons.rng.core.source64.LongProvider;

public abstract class VTLongProviderRandom extends Random
{
  private static final long serialVersionUID = 1L;
  
  private LongProvider longProvider;
  
  public VTLongProviderRandom()
  {
    
  }
  
  public void setLongProvider(LongProvider longProvider)
  {
    this.longProvider = longProvider;
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
    longProvider.nextBytes(bytes);
  }
  
  public int nextInt()
  {
    return longProvider.nextInt();
  }
  
  public long nextLong()
  {
    return longProvider.nextLong();
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