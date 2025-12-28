package org.vash.vate.graphics.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

import org.vash.vate.graphics.image.VTImageDataUtils;
import org.vash.vate.graphics.image.VTRectangle;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public final class VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII
{
  // iterators
  // block1 count
  // private int c0 = 0;
  // block1 scanline count
  private int c1 = 0;
  // block3 count
  private int c2 = 0;
  // block3 scanline count
  private int c3 = 0;
  // pixel count
  private int c4 = 0;
  // block1 pos X
  private int x1 = 0;
  // block1 pos Y
  private int y1 = 0;
  // pixel position
  private int p1 = 0;
  // pixel bit align
  // private int p2 = 0;
  // border remainder
  private int r1 = -1;
  // private int r2 = -1;
  private int r2 = -1;
  // border subtraction
  private int s1 = -1;
  private int s2 = -1;
  private int s3 = -1;
  // private int s4 = -1;
  // difference data holders
  // block1 difference data
  private int d1 = 0;
  // block1 scanline difference data
  private int d2 = 0;
  // block3 difference data
  private int d3 = 0;
  // block3 scanline difference data
  private int d4 = 0;
  // pixel difference data
  // private int d5 = 0;
  // step helpers
  // X axis block1 step
  private static final int macroblockStepX = 64;
  // X axis block3 step
  private static final int microblockStepX = 8;
  // X axis pixel step
  private static final int pixelStepX = 1;
  // Y axis block1 step
  private int macroblockStepY;
  // Y axis block3 step
  private int microblockStepY;
  // Y axis pixel step
  private int pixelStepY;
  // elements per pixel
  // private int elementsPerPixel;
  // bits per element
  // private int bitsPerElement;
  // data unit counters
  // number of pixels
  // private int pixelNumber;
  // size of total pixel data
  // private int size;
  // length of difference map data
  private int block1TreeDataLength;
  private int block1PixelDataLength;
  // data buffers
  private final ByteArrayOutputStream block1DataBuffer;
  private final ByteArrayOutputStream block2DataBuffer;
  private final ByteArrayOutputStream block3DataBuffer;
  private final ByteArrayOutputStream pixelDataBuffer;
  private final VTLittleEndianOutputStream pixelDataBufferStream;
  // preloaded data streams
  private final VTByteArrayInputStream btin = new VTByteArrayInputStream(new byte[1024]);
  private final VTByteArrayInputStream bpin = new VTByteArrayInputStream(new byte[4096 * 4]);
  private final VTLittleEndianInputStream ltin = new VTLittleEndianInputStream(btin);
  private final VTLittleEndianInputStream lpin = new VTLittleEndianInputStream(bpin);
  private final VTLittleEndianInputStream lin = new VTLittleEndianInputStream(null);
  private final VTLittleEndianOutputStream lout = new VTLittleEndianOutputStream(null);
  private final BitSet blockBitSet = new BitSet(1024 * 8);
  private final BitSet pixelBitSet = new BitSet(1024 * 8192);
  private final VTRectangle transferArea = new VTRectangle(0, 0, 1, 1);
  private int m1;
  private int limitX;
  private int limitY;
  private int offset;
  public static final int CODEC_PADDING_SIZE = 1;
  
//  private static final int MAGIC1 = 0x34384431;
//  private static final int MAGIC2 = 0x34384432;
//  private static final int MAGIC3 = 0x34384433;
//  private static final int MAGIC4 = 0x34384434;
  
  public final void dispose()
  {
//    block1DataBuffer = null;
//    block2DataBuffer = null;
//    block3DataBuffer = null;
//    pixelDataBuffer = null;
  }
  
  public VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII()
  {
    this.block1DataBuffer = new ByteArrayOutputStream();
    this.block2DataBuffer = new ByteArrayOutputStream();
    this.block3DataBuffer = new ByteArrayOutputStream();
    this.pixelDataBuffer = new ByteArrayOutputStream();
    this.pixelDataBufferStream = new VTLittleEndianOutputStream(pixelDataBuffer);
  }
  
  // H G E
  // F C B
  // D A X
  
  private static final int encodePixel8(final VTLittleEndianOutputStream out, final byte[] newPixelData, final int position, final int width, final int d4, final int c4, final boolean diff) throws IOException
  {
    if (!diff)
    {
      return d4;
    }
    
    out.write(((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ newPixelData[position]);
    return d4 | c4;
  }
  
  private static final int encodePixel15(final VTLittleEndianOutputStream out, final short[] newPixelData, final int position, final int width, final int d4, final int c4, final boolean diff) throws IOException
  {
    if (!diff)
    {
      return d4;
    }
    
    out.writeShort(((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ newPixelData[position]);
    return d4 | c4;
  }
  
  private static final int encodePixel24(final VTLittleEndianOutputStream out, final int[] newPixelData, final int position, final int width, final int d4, final int c4, final boolean diff) throws IOException
  {
    if (!diff)
    {
      return d4;
    }
    
    out.writeSubInt(((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ newPixelData[position]);
    return d4 | c4;
  }
  
  private static final int encodePixel30(final VTLittleEndianOutputStream out, final int[] newPixelData, final int position, final int width, final int d4, final int c4, final boolean diff) throws IOException
  {
    if (!diff)
    {
      return d4;
    }
    
    out.writeInt(((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ newPixelData[position]);
    return d4 | c4;
  }
  
  private static final void decodePixel8(final VTLittleEndianInputStream in, final byte[] newPixelData, final int position, final int width, final int d4, final int c4) throws IOException
  {
    if ((d4 & c4) == 0)
    {
      return;
    }
    
    newPixelData[position] = (byte) (((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ in.read());
  }
  
  private static final void decodePixel15(final VTLittleEndianInputStream in, final short[] newPixelData, final int position, final int width, final int d4, final int c4) throws IOException
  {
    if ((d4 & c4) == 0)
    {
      return;
    }
    
    newPixelData[position] = (short) (((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ in.readShort());
  }
  
  private static final void decodePixel24(final VTLittleEndianInputStream in, final int[] newPixelData, final int position, final int width, final int d4, final int c4) throws IOException
  {
    if ((d4 & c4) == 0)
    {
      return;
    }
    
    newPixelData[position] = (((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ in.readSubInt());
  }
  
  private static final void decodePixel30(final VTLittleEndianInputStream in, final int[] newPixelData, final int position, final int width, final int d4, final int c4) throws IOException
  {
    if ((d4 & c4) == 0)
    {
      return;
    }
    
    newPixelData[position] = (((newPixelData[position - 1 - width] >> 1) + ((newPixelData[position - width] + newPixelData[position - 1]) >> 2)) ^ in.readInt());
  }
  
  private final void encodeBlock4Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d4 = 0;
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        d4 = encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
    }
  }
  
  private final void encodeBlock3Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d3 = 0;
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        encodeBlock4Tree8(out, oldPixelData, newPixelData);
    }
    x1 = s3;
    if (d3 != 0)
    {
      d2 |= c2;
      block2DataBuffer.write(d3);
      block3DataBuffer.writeTo(block2DataBuffer);
      block3DataBuffer.reset();
    }
  }
  
  private final void encodeBlock2Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d2 = 0;
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        encodeBlock3Tree8(out, oldPixelData, newPixelData);
    }
    y1 = s2;
    if (d2 != 0)
    {
      d1 |= c1;
      // Write block1 scanline difference map data
      block1DataBuffer.write(d2);
      block2DataBuffer.writeTo(block1DataBuffer);
      block2DataBuffer.reset();
    }
  }
  
  private final void encodeBlock1Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d1 = 0;
    c1 = 1;
    s1 = y1;
    switch (y1 + macroblockStepY <= limitY ? 8 : r1)
    {
      default:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 1:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 2:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 3:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 4:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 5:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 6:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 7:
        encodeBlock2Tree8(out, oldPixelData, newPixelData);
    }
    y1 = s1;
    out.write(d1);
    if (d1 != 0)
    {
      out.writeUnsignedShort(block1DataBuffer.size());
      block1DataBuffer.writeTo(out);
      block1DataBuffer.reset();
      out.writeUnsignedShort(pixelDataBuffer.size());
      pixelDataBuffer.writeTo(out);
      pixelDataBuffer.reset();
      // out.flush();
    }
  }
  
  private final void encodeBlock0Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    // For each block1
    m1 = 0;
    for (;;)
    {
      if (blockBitSet.get(m1++))
      {
        encodeBlock1Tree8(out, oldPixelData, newPixelData);
      }
      else
      {
        out.write(0);
      }
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void decodeBlock4Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    if ((d3 & c3) == 0)
    {
      return;
    }
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        decodePixel8(in, newPixelData, p1, pixelStepY, d4, c4);
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    if ((d2 & c2) == 0)
    {
      return;
    }
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        decodeBlock4Tree8(in, oldPixelData, newPixelData);
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    if ((d1 & c1) == 0)
    {
      return;
    }
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        decodeBlock3Tree8(in, oldPixelData, newPixelData);
    }
    y1 = s2;
  }
  
  private final void decodeBlock1Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d1 = in.readUnsignedByte();
    c1 = 1;
    s1 = y1;
    // If block1 has changes
    if (d1 > 0)
    {
      block1TreeDataLength = in.readUnsignedShort();
      if (block1TreeDataLength > btin.buf().length)
      {
        btin.buf(new byte[block1TreeDataLength]);
      }
      in.readFully(btin.buf(), 0, block1TreeDataLength);
      btin.count(block1TreeDataLength);
      btin.pos(0);
      block1PixelDataLength = in.readUnsignedShort();
      if (block1PixelDataLength > bpin.buf().length)
      {
        bpin.buf(new byte[block1PixelDataLength]);
      }
      in.readFully(bpin.buf(), 0, block1PixelDataLength);
      bpin.count(block1PixelDataLength);
      bpin.pos(0);
      switch (y1 + macroblockStepY <= limitY ? 8 : r1)
      {
        default:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
      }
      y1 = s1;
    }
  }
  
  private final void decodeBlock0Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    // For each block1
    for (;;)
    {
      decodeBlock1Tree8(in, oldPixelData, newPixelData);
      // Iterate block1 X and detect X axis out-of-bounds!
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        // Iterate block1 Y and detect Y axis out-of-bounds!
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void encodeBlock4Tree15(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d4 = 0;
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        d4 = encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
    }
  }
  
  private final void encodeBlock3Tree15(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d3 = 0;
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        encodeBlock4Tree15(out, oldPixelData, newPixelData);
    }
    x1 = s3;
    if (d3 != 0)
    {
      d2 |= c2;
      block2DataBuffer.write(d3);
      block3DataBuffer.writeTo(block2DataBuffer);
      block3DataBuffer.reset();
    }
  }
  
  private final void encodeBlock2Tree15(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d2 = 0;
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        encodeBlock3Tree15(out, oldPixelData, newPixelData);
    }
    y1 = s2;
    if (d2 != 0)
    {
      d1 |= c1;
      // Write block1 scanline difference map data
      block1DataBuffer.write(d2);
      block2DataBuffer.writeTo(block1DataBuffer);
      block2DataBuffer.reset();
    }
  }
  
  private final void encodeBlock1Tree15(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d1 = 0;
    c1 = 1;
    s1 = y1;
    switch (y1 + macroblockStepY <= limitY ? 8 : r1)
    {
      default:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 1:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 2:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 3:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 4:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 5:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 6:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 7:
        encodeBlock2Tree15(out, oldPixelData, newPixelData);
    }
    y1 = s1;
    out.write(d1);
    if (d1 != 0)
    {
      out.writeUnsignedShort(block1DataBuffer.size());
      block1DataBuffer.writeTo(out);
      block1DataBuffer.reset();
      out.writeUnsignedShort(pixelDataBuffer.size());
      pixelDataBuffer.writeTo(out);
      pixelDataBuffer.reset();
      // out.flush();
    }
  }
  
  private final void encodeBlock0Tree15(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    // For each block1
    m1 = 0;
    for (;;)
    {
      if (blockBitSet.get(m1++))
      {
        encodeBlock1Tree15(out, oldPixelData, newPixelData);
      }
      else
      {
        out.write(0);
      }
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void decodeBlock4Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    if ((d3 & c3) == 0)
    {
      return;
    }
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        decodePixel15(in, newPixelData, p1, pixelStepY, d4, c4);
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    if ((d2 & c2) == 0)
    {
      return;
    }
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        decodeBlock4Tree15(in, oldPixelData, newPixelData);
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    if ((d1 & c1) == 0)
    {
      return;
    }
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        decodeBlock3Tree15(in, oldPixelData, newPixelData);
    }
    y1 = s2;
  }
  
  private final void decodeBlock1Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d1 = in.readUnsignedByte();
    c1 = 1;
    s1 = y1;
    // If block1 has changes
    if (d1 > 0)
    {
      block1TreeDataLength = in.readUnsignedShort();
      if (block1TreeDataLength > btin.buf().length)
      {
        btin.buf(new byte[block1TreeDataLength]);
      }
      in.readFully(btin.buf(), 0, block1TreeDataLength);
      btin.count(block1TreeDataLength);
      btin.pos(0);
      block1PixelDataLength = in.readUnsignedShort();
      if (block1PixelDataLength > bpin.buf().length)
      {
        bpin.buf(new byte[block1PixelDataLength]);
      }
      in.readFully(bpin.buf(), 0, block1PixelDataLength);
      bpin.count(block1PixelDataLength);
      bpin.pos(0);
      switch (y1 + macroblockStepY <= limitY ? 8 : r1)
      {
        default:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
      }
      y1 = s1;
    }
  }
  
  private final void decodeBlock0Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    // For each block1
    for (;;)
    {
      decodeBlock1Tree15(in, oldPixelData, newPixelData);
      // Iterate block1 X and detect X axis out-of-bounds!
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        // Iterate block1 Y and detect Y axis out-of-bounds!
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void encodeBlock4Tree24(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d4 = 0;
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        d4 = encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
    }
  }
  
  private final void encodeBlock3Tree24(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d3 = 0;
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        encodeBlock4Tree24(out, oldPixelData, newPixelData);
    }
    x1 = s3;
    if (d3 != 0)
    {
      d2 |= c2;
      block2DataBuffer.write(d3);
      block3DataBuffer.writeTo(block2DataBuffer);
      block3DataBuffer.reset();
    }
  }
  
  private final void encodeBlock2Tree24(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d2 = 0;
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        encodeBlock3Tree24(out, oldPixelData, newPixelData);
    }
    y1 = s2;
    if (d2 != 0)
    {
      d1 |= c1;
      // Write block1 scanline difference map data
      block1DataBuffer.write(d2);
      block2DataBuffer.writeTo(block1DataBuffer);
      block2DataBuffer.reset();
    }
  }
  
  private final void encodeBlock1Tree24(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d1 = 0;
    c1 = 1;
    s1 = y1;
    switch (y1 + macroblockStepY <= limitY ? 8 : r1)
    {
      default:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 1:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 2:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 3:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 4:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 5:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 6:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 7:
        encodeBlock2Tree24(out, oldPixelData, newPixelData);
    }
    y1 = s1;
    out.write(d1);
    if (d1 != 0)
    {
      out.writeUnsignedShort(block1DataBuffer.size());
      block1DataBuffer.writeTo(out);
      block1DataBuffer.reset();
      out.writeUnsignedShort(pixelDataBuffer.size());
      pixelDataBuffer.writeTo(out);
      pixelDataBuffer.reset();
      // out.flush();
    }
  }
  
  private final void encodeBlock0Tree24(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    // For each block1
    m1 = 0;
    for (;;)
    {
      if (blockBitSet.get(m1++))
      {
        encodeBlock1Tree24(out, oldPixelData, newPixelData);
      }
      else
      {
        out.write(0);
      }
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void decodeBlock4Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d3 & c3) == 0)
    {
      return;
    }
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        decodePixel24(in, newPixelData, p1, pixelStepY, d4, c4);
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d2 & c2) == 0)
    {
      return;
    }
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        decodeBlock4Tree24(in, oldPixelData, newPixelData);
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d1 & c1) == 0)
    {
      return;
    }
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        decodeBlock3Tree24(in, oldPixelData, newPixelData);
    }
    y1 = s2;
  }
  
  private final void decodeBlock1Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d1 = in.readUnsignedByte();
    c1 = 1;
    s1 = y1;
    // If block1 has changes
    if (d1 > 0)
    {
      block1TreeDataLength = in.readUnsignedShort();
      if (block1TreeDataLength > btin.buf().length)
      {
        btin.buf(new byte[block1TreeDataLength]);
      }
      in.readFully(btin.buf(), 0, block1TreeDataLength);
      btin.count(block1TreeDataLength);
      btin.pos(0);
      block1PixelDataLength = in.readUnsignedShort();
      if (block1PixelDataLength > bpin.buf().length)
      {
        bpin.buf(new byte[block1PixelDataLength]);
      }
      in.readFully(bpin.buf(), 0, block1PixelDataLength);
      bpin.count(block1PixelDataLength);
      bpin.pos(0);
      switch (y1 + macroblockStepY <= limitY ? 8 : r1)
      {
        default:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
      }
      y1 = s1;
    }
  }
  
  private final void decodeBlock0Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    // For each block1
    for (;;)
    {
      decodeBlock1Tree24(in, oldPixelData, newPixelData);
      // Iterate block1 X and detect X axis out-of-bounds!
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        // Iterate block1 Y and detect Y axis out-of-bounds!
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void encodeBlock4Tree30(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d4 = 0;
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        d4 = encodePixel30(pixelDataBufferStream, newPixelData, p1, pixelStepY, d4, c4, pixelBitSet.get(p1));
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
    }
  }
  
  private final void encodeBlock3Tree30(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d3 = 0;
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        encodeBlock4Tree30(out, oldPixelData, newPixelData);
    }
    x1 = s3;
    if (d3 != 0)
    {
      d2 |= c2;
      block2DataBuffer.write(d3);
      block3DataBuffer.writeTo(block2DataBuffer);
      block3DataBuffer.reset();
    }
  }
  
  private final void encodeBlock2Tree30(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d2 = 0;
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        encodeBlock3Tree30(out, oldPixelData, newPixelData);
    }
    y1 = s2;
    if (d2 != 0)
    {
      d1 |= c1;
      // Write block1 scanline difference map data
      block1DataBuffer.write(d2);
      block2DataBuffer.writeTo(block1DataBuffer);
      block2DataBuffer.reset();
    }
  }
  
  private final void encodeBlock1Tree30(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d1 = 0;
    c1 = 1;
    s1 = y1;
    switch (y1 + macroblockStepY <= limitY ? 8 : r1)
    {
      default:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 1:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 2:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 3:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 4:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 5:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 6:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
        c1 <<= 1;
        y1 += microblockStepY;
      case 7:
        encodeBlock2Tree30(out, oldPixelData, newPixelData);
    }
    y1 = s1;
    out.write(d1);
    if (d1 != 0)
    {
      out.writeUnsignedShort(block1DataBuffer.size());
      block1DataBuffer.writeTo(out);
      block1DataBuffer.reset();
      out.writeUnsignedShort(pixelDataBuffer.size());
      pixelDataBuffer.writeTo(out);
      pixelDataBuffer.reset();
      // out.flush();
    }
  }
  
  private final void encodeBlock0Tree30(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    // For each block1
    m1 = 0;
    for (;;)
    {
      if (blockBitSet.get(m1++))
      {
        encodeBlock1Tree30(out, oldPixelData, newPixelData);
      }
      else
      {
        out.write(0);
      }
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  private final void decodeBlock4Tree30(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d3 & c3) == 0)
    {
      return;
    }
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        decodePixel30(in, newPixelData, p1, pixelStepY, d4, c4);
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree30(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d2 & c2) == 0)
    {
      return;
    }
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        decodeBlock4Tree30(in, oldPixelData, newPixelData);
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree30(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    if ((d1 & c1) == 0)
    {
      return;
    }
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        decodeBlock3Tree30(in, oldPixelData, newPixelData);
    }
    y1 = s2;
  }
  
  private final void decodeBlock1Tree30(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d1 = in.readUnsignedByte();
    c1 = 1;
    s1 = y1;
    // If block1 has changes
    if (d1 > 0)
    {
      block1TreeDataLength = in.readUnsignedShort();
      if (block1TreeDataLength > btin.buf().length)
      {
        btin.buf(new byte[block1TreeDataLength]);
      }
      in.readFully(btin.buf(), 0, block1TreeDataLength);
      btin.count(block1TreeDataLength);
      btin.pos(0);
      block1PixelDataLength = in.readUnsignedShort();
      if (block1PixelDataLength > bpin.buf().length)
      {
        bpin.buf(new byte[block1PixelDataLength]);
      }
      in.readFully(bpin.buf(), 0, block1PixelDataLength);
      bpin.count(block1PixelDataLength);
      bpin.pos(0);
      switch (y1 + macroblockStepY <= limitY ? 8 : r1)
      {
        default:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          decodeBlock2Tree30(lpin, oldPixelData, newPixelData);
      }
      y1 = s1;
    }
  }
  
  private final void decodeBlock0Tree30(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    // For each block1
    for (;;)
    {
      decodeBlock1Tree30(in, oldPixelData, newPixelData);
      // Iterate block1 X and detect X axis out-of-bounds!
      x1 += macroblockStepX;
      if (x1 >= limitX)
      {
        x1 = 0;
        // Iterate block1 Y and detect Y axis out-of-bounds!
        y1 += macroblockStepY;
        if (y1 >= limitY)
        {
          break;
        }
      }
    }
  }
  
  public final void encodeFrame8(final OutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int frameWidth, final int frameHeight, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    block1DataBuffer.reset();
    block2DataBuffer.reset();
    block3DataBuffer.reset();
    pixelDataBuffer.reset();
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + ((areaY) * (pixelStepY));
    //int paddingOffset = PADDING_SIZE + (width * PADDING_SIZE);
    limitX = areaWidth;
    limitY = pixelStepY * areaHeight;
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    transferArea.x = areaX;
    transferArea.y = areaY;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea, 64, 64, blockBitSet, pixelBitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(limitX);
    lout.writeInt(limitY);
    // lout.writeInt(areaX);
    encodeBlock0Tree8(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea);
    //block1BitSet.clear();
    //pixelBitSet.clear();
  }
  
  public final void decodeFrame8(final InputStream in, final byte[] oldPixelData, final byte[] newPixelData, final int frameWidth, final int frameHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    limitX = lin.readInt();
    limitY = lin.readInt();
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    decodeBlock0Tree8(lin, oldPixelData, newPixelData);
    //VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
  
  public final void encodeFrame15(final OutputStream out, final short[] oldPixelData, final short[] newPixelData, final int frameWidth, final int frameHeight, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    block1DataBuffer.reset();
    block2DataBuffer.reset();
    block3DataBuffer.reset();
    pixelDataBuffer.reset();
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + ((areaY) * (pixelStepY));
    //int paddingOffset = PADDING_SIZE + (width * PADDING_SIZE);
    limitX = areaWidth;
    limitY = pixelStepY * areaHeight;
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    transferArea.x = areaX;
    transferArea.y = areaY;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea, 64, 64, blockBitSet, pixelBitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(limitX);
    lout.writeInt(limitY);
    encodeBlock0Tree15(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea);
    //block1BitSet.clear();
    //pixelBitSet.clear();
  }
  
  public final void decodeFrame15(final InputStream in, final short[] oldPixelData, final short[] newPixelData, final int frameWidth, final int frameHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    limitX = lin.readInt();
    limitY = lin.readInt();
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    decodeBlock0Tree15(lin, oldPixelData, newPixelData);
    //VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
  
  public final void encodeFrame24(final OutputStream out, final int[] oldPixelData, final int[] newPixelData, final int frameWidth, final int frameHeight, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    block1DataBuffer.reset();
    block2DataBuffer.reset();
    block3DataBuffer.reset();
    pixelDataBuffer.reset();
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + ((areaY) * (pixelStepY));
    //int paddingOffset = PADDING_SIZE + (width * PADDING_SIZE);
    limitX = areaWidth;
    limitY = pixelStepY * areaHeight;
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    transferArea.x = areaX;
    transferArea.y = areaY;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea, 64, 64, blockBitSet, pixelBitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(limitX);
    lout.writeInt(limitY);
    encodeBlock0Tree24(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea);
    //block1BitSet.clear();
    //pixelBitSet.clear();
  }
  
  public final void decodeFrame24(final InputStream in, final int[] oldPixelData, final int[] newPixelData, final int frameWidth, final int frameHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    limitX = lin.readInt();
    limitY = lin.readInt();
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    decodeBlock0Tree24(lin, oldPixelData, newPixelData);
    //VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
  
  public final void encodeFrame30(final OutputStream out, final int[] oldPixelData, final int[] newPixelData, final int frameWidth, final int frameHeight, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    block1DataBuffer.reset();
    block2DataBuffer.reset();
    block3DataBuffer.reset();
    pixelDataBuffer.reset();
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + ((areaY) * (pixelStepY));
    //int paddingOffset = PADDING_SIZE + (width * PADDING_SIZE);
    limitX = areaWidth;
    limitY = pixelStepY * areaHeight;
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    transferArea.x = areaX;
    transferArea.y = areaY;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea, 64, 64, blockBitSet, pixelBitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(limitX);
    lout.writeInt(limitY);
    encodeBlock0Tree30(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, frameWidth + CODEC_PADDING_SIZE, frameHeight + CODEC_PADDING_SIZE, transferArea);
    //block1BitSet.clear();
    //pixelBitSet.clear();
  }
  
  public final void decodeFrame30(final InputStream in, final int[] oldPixelData, final int[] newPixelData, final int frameWidth, final int frameHeight) throws IOException
  {
    c1 = 0;
    c2 = 0;
    c3 = 0;
    c4 = 0;
    x1 = 0;
    y1 = 0;
    p1 = 0;
    d1 = 0;
    d2 = 0;
    d3 = 0;
    d4 = 0;
    pixelStepY = frameWidth + CODEC_PADDING_SIZE;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    limitX = lin.readInt();
    limitY = lin.readInt();
    r1 = 8 - (((int) Math.ceil((((double) limitY) / microblockStepY))) & 7);
    r2 = 8 - (((limitY) / pixelStepY) & 7);
    decodeBlock0Tree30(lin, oldPixelData, newPixelData);
    //VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
}