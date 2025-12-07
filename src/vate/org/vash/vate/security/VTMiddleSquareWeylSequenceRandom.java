package org.vash.vate.security;

import vate.org.apache.commons.rng.core.source32.MiddleSquareWeylSequence;

public class VTMiddleSquareWeylSequenceRandom extends VTIntProviderRandom
{
  private static final long serialVersionUID = 1L;
    
  public VTMiddleSquareWeylSequenceRandom(final long[] seed)
  {
    super();
    MiddleSquareWeylSequence middleSquareWeylSequence = new MiddleSquareWeylSequence(seed);
    this.setIntProvider(middleSquareWeylSequence);
  }
  
  public void setSeed(final byte[] seed)
  {
    
  }
  
  public void setSeed(final long seed)
  {
    
  }
  
}