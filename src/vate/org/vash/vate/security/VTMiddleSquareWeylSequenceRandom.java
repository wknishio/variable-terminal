package org.vash.vate.security;

import org.apache.commons.rng.core.source32.MiddleSquareWeylSequence;

public class VTMiddleSquareWeylSequenceRandom extends VTIntProviderRandom
{
  private static final long serialVersionUID = 1L;
    
  public VTMiddleSquareWeylSequenceRandom(long[] seed)
  {
    super();
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