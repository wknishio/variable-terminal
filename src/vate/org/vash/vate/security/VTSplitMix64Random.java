package org.vash.vate.security;

import org.apache.commons.rng.core.source64.SplitMix64;

public class VTSplitMix64Random extends VTLongProviderRandom
{
  private static final long serialVersionUID = 1L;
  private final SplitMix64 splitMix64;
  
  public VTSplitMix64Random(long seed)
  {
    super();
    splitMix64 = new SplitMix64(seed);
    this.setLongProvider(splitMix64);
  }
  
  public void setSeed(byte[] seed)
  {
    
  }
  
  public void setSeed(long seed)
  {
    
  }
  
}