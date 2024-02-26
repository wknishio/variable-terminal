package org.vash.vate.security;

import java.security.SecureRandom;

import org.apache.commons.rng.core.source64.SplitMix64;

public class VTSplitMix64Random extends VTLongProviderRandom
{
  private static final long serialVersionUID = 1L;
    
  public VTSplitMix64Random(SecureRandom secureRandom)
  {
    super();
    long seed = secureRandom.nextLong();
    SplitMix64 splitMix64 = new SplitMix64(seed);
    this.setLongProvider(splitMix64);
  }
  
  public void setSeed(byte[] seed)
  {
    
  }
  
  public void setSeed(long seed)
  {
    
  }
  
}