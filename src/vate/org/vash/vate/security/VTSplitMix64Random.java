package org.vash.vate.security;

import vate.org.apache.commons.rng.core.source64.SplitMix64;

public class VTSplitMix64Random extends VTLongProviderRandom
{
  private static final long serialVersionUID = 1L;
  private final SplitMix64 splitMix64;
  
  public VTSplitMix64Random(final long seed)
  {
    super();
    splitMix64 = new SplitMix64(seed);
    this.setLongProvider(splitMix64);
  }
  
  public void setSeed(final byte[] seed)
  {
    
  }
  
  public void setSeed(final long seed)
  {
    if (splitMix64 != null)
    {
      splitMix64.setSeed(seed);
    }
  }
  
}