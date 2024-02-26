package org.vash.vate.security;

import java.security.SecureRandom;

import org.apache.commons.rng.core.source32.MiddleSquareWeylSequence;

public class VTMiddleSquareWeylSequenceRandom extends VTIntProviderRandom
{
  private static final long serialVersionUID = 1L;
    
  public VTMiddleSquareWeylSequenceRandom(SecureRandom secureRandom)
  {
    super();
    long[] seed = new long[3];
    for (int i = 0 ; i < seed.length; i++)
    {
      seed[i] = secureRandom.nextLong();
    }
    MiddleSquareWeylSequence middleSquareWeylSequence = new MiddleSquareWeylSequence(seed);
    this.setIntProvider(middleSquareWeylSequence);
  }
  
  public void setSeed(byte[] seed)
  {
    
  }
  
  public void setSeed(long seed)
  {
    
  }
  
}