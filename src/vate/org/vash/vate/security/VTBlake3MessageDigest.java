package org.vash.vate.security;

import java.security.MessageDigest;
import org.bouncycastle.crypto.digests.Blake3Digest;

public class VTBlake3MessageDigest extends MessageDigest
{
  // private static final Blake3Digest BLAKE3 = new Blake3Digest();
  private Blake3Digest blake3;
  
  public VTBlake3MessageDigest()
  {
    super("BLAKE3");
    this.blake3 = new Blake3Digest();
  }
  
  public void reset()
  {
    blake3.reset();
  }
  
  public void update(byte[]... arrays)
  {
    for (byte[] data : arrays)
    {
      if (data != null && data.length > 0)
      {
        blake3.update(data, 0, data.length);
      }
    }
  }
  
  public void update(byte[] data, int off, int len)
  {
    if (data != null && data.length > 0)
    {
      blake3.update(data, off, len);
    }
  }
  
  public byte[] digest(int digestSizeBytes, byte[] data)
  {
    update(data);
    byte[] key = new byte[digestSizeBytes];
    blake3.doFinal(key, 0, key.length);
    return key;
  }
  
  public byte[] digest(int digestSizeBytes)
  {
    byte[] key = new byte[digestSizeBytes];
    blake3.doFinal(key, 0, key.length);
    return key;
  }
  
  public byte[] digest()
  {
    return digest(64);
  }
  
  public byte[] digest(byte[] data)
  {
    return digest(64, data);
  }
  
  protected void engineUpdate(byte input)
  {
    blake3.update(input);
  }
  
  protected void engineUpdate(byte[] input, int offset, int len)
  {
    blake3.update(input, offset, len);
  }
  
  protected byte[] engineDigest()
  {
    byte[] key = new byte[64];
    blake3.doFinal(key, 0, key.length);
    return key;
  }
  
  protected void engineReset()
  {
    blake3.reset();
  }
  
  protected int engineGetDigestLength()
  {
    return 64;
  }
}
