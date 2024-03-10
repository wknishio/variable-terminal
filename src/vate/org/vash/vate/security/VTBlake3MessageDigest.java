package org.vash.vate.security;

import java.security.MessageDigest;
import org.bouncycastle.crypto.digests.Blake3Digest;
import org.bouncycastle.crypto.params.Blake3Parameters;

public class VTBlake3MessageDigest extends MessageDigest
{
  // private static final Blake3Digest BLAKE3 = new Blake3Digest();
  private final Blake3Digest blake3 = new Blake3Digest(64);
  
  public VTBlake3MessageDigest()
  {
    super("BLAKE3");
    //this.blake3.init(null);
  }
  
  public VTBlake3MessageDigest(final byte[] seed)
  {
    super("BLAKE3");
    setSeed(seed);
  }
  
  public void setSeed(final byte[] seed)
  {
    blake3.init(Blake3Parameters.context(seed));
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
    blake3.doFinal(digest, 0, digest.length);
    return digest;
  }
  
  public byte[] digest(final int digestSizeBytes)
  {
    byte[] digest = new byte[digestSizeBytes];
    blake3.doFinal(digest, 0, digest.length);
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
    blake3.update(input);
  }
  
  protected void engineUpdate(final byte[] input, final int offset, final int len)
  {
    blake3.update(input, offset, len);
  }
  
  protected byte[] engineDigest()
  {
    byte[] digest = new byte[64];
    blake3.doFinal(digest, 0, digest.length);
    return digest;
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
