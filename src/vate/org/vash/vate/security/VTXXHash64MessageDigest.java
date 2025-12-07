package org.vash.vate.security;

import java.security.MessageDigest;

import vate.net.jpountz.xxhash.StreamingXXHash64;

public class VTXXHash64MessageDigest extends MessageDigest
{
  private final StreamingXXHash64 xxhash64;
  private final byte[] longBuffer = new byte[8];
  
  public VTXXHash64MessageDigest(StreamingXXHash64 xxhash64)
  {
    super("XXHash64");
    this.xxhash64 = xxhash64;
  }
  
  public void reset()
  {
    xxhash64.reset();
  }
  
  public void update(byte[]... arrays)
  {
    for (byte[] data : arrays)
    {
      if (data != null && data.length > 0)
      {
        xxhash64.update(data, 0, data.length);
      }
    }
  }
  
  public void update(byte data)
  {
    engineUpdate(data);
  }
  
  public void update(byte[] data, int off, int len)
  {
    if (data != null && data.length > 0)
    {
      xxhash64.update(data, off, len);
    }
  }
  
  public byte[] digest()
  {
    return engineDigest();
  }
  
  public long digestLong()
  {
    return xxhash64.getValue();
  }
  
  protected void engineUpdate(byte input)
  {
    engineUpdate(new byte[] {input}, 0, 1);
  }
  
  protected void engineUpdate(byte[] input, int offset, int len)
  {
    xxhash64.update(input, offset, len);
  }
  
  protected byte[] engineDigest()
  {
    long l = xxhash64.getValue();
    longBuffer[0] = (byte) l;
    longBuffer[1] = (byte) (l >> 8);
    longBuffer[2] = (byte) (l >> 16);
    longBuffer[3] = (byte) (l >> 24);
    longBuffer[4] = (byte) (l >> 32);
    longBuffer[5] = (byte) (l >> 40);
    longBuffer[6] = (byte) (l >> 48);
    longBuffer[7] = (byte) (l >> 56);
    return longBuffer;
  }
  
  protected void engineReset()
  {
    xxhash64.reset();
  }
  
  protected int engineGetDigestLength()
  {
    return 8;
  }
}
