package org.vash.vate.security;

import java.security.SecureRandom;

import org.apache.commons.rng.core.source32.MiddleSquareWeylSequence;

public class VTMiddleSquareWeylSequenceDigestRandom extends VTIntProviderDigestRandom
{
  private static final long serialVersionUID = 1L;
    
  public VTMiddleSquareWeylSequenceDigestRandom(SecureRandom secureRandom)
  {
    super();
    long[] keys = new long[3];
    for (int i = 0 ; i < keys.length; i++)
    {
      keys[i] = secureRandom.nextLong();
    }
    MiddleSquareWeylSequence middleSquareWeylSequence = new MiddleSquareWeylSequence(keys);
    this.setIntProvider(middleSquareWeylSequence);
  }
  
  public void setSeed(byte[] seed)
  {
    
  }
  
  public void setSeed(long seed)
  {
    
  }
  
}