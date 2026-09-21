package org.vash.vate.security;

import java.security.MessageDigest;

import org.vash.vate.org.apache.commons.codec.digest.Blake3;

public class VTBlake3StandardMessageDigest extends MessageDigest
{
  // private static final Blake3Digest BLAKE3 = new Blake3Digest();
  private Blake3 blake3 = Blake3.initHash();
  private final byte[] single = new byte[1];
  
  public VTBlake3StandardMessageDigest()
  {
    super("BLAKE3");
    //this.blake3.init(null);
  }
  
  public VTBlake3StandardMessageDigest(final byte[] seed)
  {
    super("BLAKE3");
    setSeed(seed);
  }
  
  public void setSeed(final byte[] seed)
  {
    blake3 = Blake3.initKeyDerivationFunction(seed);
  }
  
  public void reset()
  {
    blake3.reset();
  }
  
  public void update(final byte[]... arrays)
  {
    for (byte[] data : arrays)
    {
      if (data != null && data.length > 0)
      {
        blake3.update(data, 0, data.length);
      }
    }
  }
  
  public void update(final byte[] data, final int off, final int len)
  {
    if (data != null && data.length > 0)
    {
      blake3.update(data, off, len);
    }
  }
  
  public byte[] digest(final int digestSizeBytes, final byte[] data)
  {
    update(data);
    byte[] digest = new byte[digestSizeBytes];
    blake3.doFinalize(digest, 0, digest.length);
    return digest;
  }
  
  public byte[] digest(final int digestSizeBytes)
  {
    byte[] digest = new byte[digestSizeBytes];
    blake3.doFinalize(digest, 0, digest.length);
    return digest;
  }
  
  //public byte[] digest()
  //{
    //return digest(64);
  //}
  
  //public byte[] digest(byte[] data)
  //{
    //return digest(64, data);
  //}
  
  public long digestLong()
  {
    byte[] longBuffer = digest(8);
    return ((longBuffer[0] & 0xFFL) | ((longBuffer[1] & 0xFFL) << 8) | ((longBuffer[2] & 0xFFL) << 16) | ((longBuffer[3] & 0xFFL) << 24) | ((longBuffer[4] & 0xFFL) << 32) | ((longBuffer[5] & 0xFFL) << 40) | ((longBuffer[6] & 0xFFL) << 48) | ((longBuffer[7] & 0xFFL) << 56));
  }
  
  protected void engineUpdate(final byte input)
  {
    single[0] = input;
    blake3.update(single);
  }
  
  protected void engineUpdate(final byte[] input, final int offset, final int len)
  {
    blake3.update(input, offset, len);
  }
  
  protected byte[] engineDigest()
  {
    byte[] digest = new byte[32];
    blake3.doFinalize(digest, 0, digest.length);
    return digest;
  }
  
  protected void engineReset()
  {
    blake3.reset();
  }
  
  protected int engineGetDigestLength()
  {
    return 32;
  }
}
