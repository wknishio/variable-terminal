package org.vash.vate.security;

import java.security.MessageDigest;

import org.vash.vate.org.bouncycastle.util.Arrays;

public class VTDigestRandomGenerator
{
  public VTDigestRandomGenerator(MessageDigest digest)
  {
    this.digest = digest;
    this.seed = new byte[digest.getDigestLength()];
    this.seedCounter = 1;

    this.state = new byte[digest.getDigestLength()];
    this.stateCounter = 1;
  }

  private static long         CYCLE_COUNT = 10;

  private long                stateCounter;
  private long                seedCounter;
  private MessageDigest       digest;
  private byte[]              state;
  private byte[]              seed;

  public void addSeedMaterial(byte[] inSeed)
  {
      synchronized (this)
      {
          if (!Arrays.isNullOrEmpty(inSeed))
          {
              digestUpdate(inSeed);
          }
          digestUpdate(seed);
          digestDoFinal(seed);
      }
  }

  public void addSeedMaterial(long rSeed)
  {
      synchronized (this)
      {
          digestAddCounter(rSeed);
          digestUpdate(seed);

          digestDoFinal(seed);
      }
  }

  public void nextBytes(byte[] bytes)
  {
      nextBytes(bytes, 0, bytes.length);
  }

  public void nextBytes(byte[] bytes, int start, int len)
  {
      synchronized (this)
      {
          int stateOff = 0;

          generateState();

          int end = start + len;
          for (int i = start; i != end; i++)
          {
              if (stateOff == state.length)
              {
                  generateState();
                  stateOff = 0;
              }
              bytes[i] = state[stateOff++];
          }
      }
  }

  private void cycleSeed()
  {
      digestUpdate(seed);
      digestAddCounter(seedCounter++);

      digestDoFinal(seed);
  }

  private void generateState()
  {
      digestAddCounter(stateCounter++);
      digestUpdate(state);
      digestUpdate(seed);

      digestDoFinal(state);

      if ((stateCounter % CYCLE_COUNT) == 0)
      {
          cycleSeed();
      }
  }

  private void digestAddCounter(long seed)
  {
      for (int i = 0; i != 8; i++)
      {
          digest.update((byte)seed);
          seed >>>= 8;
      }
  }

  private void digestUpdate(byte[] inSeed)
  {
      digest.update(inSeed, 0, inSeed.length);
  }

  private void digestDoFinal(byte[] result)
  {
      System.arraycopy(digest.digest(), 0, result, 0, result.length);
      //digest.reset();
  }
}
