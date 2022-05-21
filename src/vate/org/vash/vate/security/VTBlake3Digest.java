package org.vash.vate.security;

import org.bouncycastle.crypto.digests.Blake3Digest;

public class VTBlake3Digest
{
  private static final Blake3Digest BLAKE3 = new Blake3Digest();
  private Blake3Digest blake3;
  
  public VTBlake3Digest()
  {
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
  
  public static final synchronized byte[] digestBLAKE3(int digestSizeBytes, byte[]... datas)
  {
    BLAKE3.reset();
    for (byte[] data : datas)
    {
      if (data != null && data.length > 0)
      {
        BLAKE3.update(data, 0, data.length);
      }
    }
    byte[] key = new byte[digestSizeBytes];
    BLAKE3.doFinal(key, 0, key.length);
    return key;
  }
}
