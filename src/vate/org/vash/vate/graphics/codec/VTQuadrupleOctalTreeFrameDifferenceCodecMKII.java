package org.vash.vate.graphics.codec;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

import org.vash.vate.graphics.image.VTImageDataUtils;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

@SuppressWarnings("unused")
public final class VTQuadrupleOctalTreeFrameDifferenceCodecMKII
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
  private ByteArrayOutputStream block1DataBuffer;
  private ByteArrayOutputStream block2DataBuffer;
  private ByteArrayOutputStream block3DataBuffer;
  private ByteArrayOutputStream pixelDataBuffer;
  private VTLittleEndianOutputStream pixelDataBufferStream;
  // preloaded data streams
  private VTByteArrayInputStream btin = new VTByteArrayInputStream(new byte[1024]);
  private VTByteArrayInputStream bpin = new VTByteArrayInputStream(new byte[4096 * 4]);
  private VTLittleEndianInputStream ltin = new VTLittleEndianInputStream(btin);
  private VTLittleEndianInputStream lpin = new VTLittleEndianInputStream(bpin);
  private VTLittleEndianInputStream lin = new VTLittleEndianInputStream(null);
  private VTLittleEndianOutputStream lout = new VTLittleEndianOutputStream(null);
  private BitSet block1BitSet = new BitSet(1024 * 8);
  // private BitSet pixelBitSet = new BitSet(1024 * 8);
  private Rectangle transferArea = new Rectangle(0, 0, 1, 1);
  private int m1;
  private int limitX;
  private int limitY;
  private int offset;
  
  public final void dispose()
  {
    block1DataBuffer = null;
    block2DataBuffer = null;
    block3DataBuffer = null;
    pixelDataBuffer = null;
  }
  
  public VTQuadrupleOctalTreeFrameDifferenceCodecMKII()
  {
    this.block1DataBuffer = new ByteArrayOutputStream();
    this.block2DataBuffer = new ByteArrayOutputStream();
    this.block3DataBuffer = new ByteArrayOutputStream();
    this.pixelDataBuffer = new ByteArrayOutputStream();
    this.pixelDataBufferStream = new VTLittleEndianOutputStream(pixelDataBuffer);
  }
  
//	private static final int paethPredictorInt(int a, int b, int c)
//	{
//		int p = a + b - c;
//		int pa = Math.abs(p - a);
//		int pb = Math.abs(p - b);
//		int pc = Math.abs(p - c);
//		int min = Math.min(pa, Math.min(pb, pc));
//		if (min == pa)
//		{
//			return a;
//		}
//		else if (min == pb)
//		{
//			return b;
//		}
//		else
//		{
//			return c;
//		}
//	}
//	
//	private static final long paethPredictorLong(long a, long b, long c)
//	{
//		long p = a + b - c;
//		long pa = Math.abs(p - a);
//		long pb = Math.abs(p - b);
//		long pc = Math.abs(p - c);
//		long min = Math.min(pa, Math.min(pb, pc));
//		if (min == pa)
//		{
//			return a;
//		}
//		else if (min == pb)
//		{
//			return b;
//		}
//		else
//		{
//			return c;
//		}
//	}
  private static final void encodePixel8(final VTLittleEndianOutputStream out, final byte[] newPixelData, final int position, final int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    pred1 = (((diag1 + top1 + left1) * 342) >>> 10);
    // pred1 = Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1),
    // Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    out.write(newPixelData[position] ^ pred1);
  }
  
  private static final void encodePixel15(final VTLittleEndianOutputStream out, final short[] newPixelData, final int position, final int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    pred1 = (((diag1 + top1 + left1) * 43691) >>> 17);
    // pred1 = Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1),
    // Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    out.writeShort(newPixelData[position] ^ pred1);
  }
  
  private static final void encodePixel24(final VTLittleEndianOutputStream out, final int[] newPixelData, final int position, final int width) throws IOException
  {
    long left1, top1, diag1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    int pred1 = (int) (((diag1 + top1 + left1) * 22369622) >>> 26);
    // int pred1 = Math.max(Math.min(left1, top1), Math.min(Math.max(left1,
    // top1), Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    out.writeSubInt(newPixelData[position] ^ pred1);
  }
  
//  private static final void encodePixel(final VTLittleEndianOutputStream out, final long[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
//  {
//    long left1, top1, diag1;
//    diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : 0;
//    top1 = y > 0 ? newPixelData[position - width] : 0;
//    left1 = x > 0 ? newPixelData[position - 1] : 0;
//    out.writeLong(newPixelData[position] ^ ((diag1 + ((top1 + left1) >> 1)) >> 1));
//  }
  
  private static final void decodePixel8(final VTLittleEndianInputStream in, final byte[] newPixelData, final int position, final int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    pred1 = (((diag1 + top1 + left1) * 342) >>> 10);
    // pred1 = Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1),
    // Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    newPixelData[position] = (byte) ((in.read() ^ pred1) /* & 0xFF */);
  }
  
  private static final void decodePixel15(final VTLittleEndianInputStream in, final short[] newPixelData, final int position, final int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    pred1 = (((diag1 + top1 + left1) * 43691) >>> 17);
    // pred1 = Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1),
    // Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    newPixelData[position] = (short) ((in.readShort() ^ pred1) /* & 0x7FFF */);
  }
  
  private static final void decodePixel24(final VTLittleEndianInputStream in, final int[] newPixelData, final int position, final int width) throws IOException
  {
    long left1, top1, diag1;
    // top1 = y > 0 ? newPixelData[position - width] : 0;
    // left1 = x > 0 ? newPixelData[position - 1] : top1;
    // top1 = y > 0 ? newPixelData[position - width] : left1;
    // diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : top1;
    left1 = newPixelData[position - 1];
    top1 = newPixelData[position - width];
    diag1 = newPixelData[position - width - 1];
    int pred1 = (int) (((diag1 + top1 + left1) * 22369622) >>> 26);
    // int pred1 = (int) Math.max(Math.min(left1, top1),
    // Math.min(Math.max(left1, top1), Math.abs(left1 + top1 - diag1)));
    // pred1 = (top1 + left1) >> 1;
    newPixelData[position] = (in.readSubInt() ^ pred1 /* & 0x00FFFFFF */);
  }
  
//  private static final void decodePixel(final VTLittleEndianInputStream in, final long[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
//  {
//    long left1, top1, diag1, pred1;
//    diag1 = x > 0 && y > 0 ? newPixelData[position - width - 1] : 0;
//    top1 = y > 0 ? newPixelData[position - width] : 0;
//    left1 = x > 0 ? newPixelData[position - 1] : 0;
//    newPixelData[position] = ((in.readLong() ^ ((diag1 + ((top1 + left1) >> 1)) >> 1)) /* & 0x7FFFFFFFFFFFFFFFL */);
//  }
  
  private final void encodeBlock4Tree8(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d4 = 0;
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel8(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
      // pixelDataBuffer.writeTo(block3DataBuffer);
      // pixelDataBuffer.reset();
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
    for (;;)
    {
      if (block1BitSet.get(m1++))
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
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if ((d4 & c4) != 0)
        {
          decodePixel8(in, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree8(in, oldPixelData, newPixelData);
        }
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree8(final VTLittleEndianInputStream in, final byte[] oldPixelData, final byte[] newPixelData) throws IOException
  {
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree8(in, oldPixelData, newPixelData);
        }
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
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree8(lpin, oldPixelData, newPixelData);
          }
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
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel15(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
      // pixelDataBuffer.writeTo(block3DataBuffer);
      // pixelDataBuffer.reset();
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
    for (;;)
    {
      if (block1BitSet.get(m1++))
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
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if ((d4 & c4) != 0)
        {
          decodePixel15(in, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree15(in, oldPixelData, newPixelData);
        }
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree15(final VTLittleEndianInputStream in, final short[] oldPixelData, final short[] newPixelData) throws IOException
  {
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree15(in, oldPixelData, newPixelData);
        }
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
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree15(lpin, oldPixelData, newPixelData);
          }
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
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if (oldPixelData[p1] != newPixelData[p1])
        {
          d4 |= c4;
          encodePixel24(pixelDataBufferStream, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
    if (d4 != 0)
    {
      d3 |= c3;
      block3DataBuffer.write(d4);
      // pixelDataBuffer.writeTo(block3DataBuffer);
      // pixelDataBuffer.reset();
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
    for (;;)
    {
      if (block1BitSet.get(m1++))
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
    d4 = ltin.read();
    c4 = 1;
    p1 = offset + x1 + y1;
    // s4 = x1;
    switch (x1 + microblockStepX - limitX)
    {
      default:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 1:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 2:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 3:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 4:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 5:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 6:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
        c4 <<= 1;
        p1 += pixelStepX;
      case 7:
        if ((d4 & c4) != 0)
        {
          decodePixel24(in, newPixelData, p1, pixelStepY);
        }
    }
    // x1 = s4;
  }
  
  private final void decodeBlock3Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d3 = ltin.read();
    c3 = 1;
    s3 = x1;
    switch ((x1 + macroblockStepX - limitX) >> 3)
    {
      default:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 1:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 2:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 3:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 4:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 5:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 6:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
        c3 <<= 1;
        x1 += microblockStepX;
      case 7:
        if ((d3 & c3) != 0)
        {
          decodeBlock4Tree24(in, oldPixelData, newPixelData);
        }
    }
    x1 = s3;
  }
  
  private final void decodeBlock2Tree24(final VTLittleEndianInputStream in, final int[] oldPixelData, final int[] newPixelData) throws IOException
  {
    d2 = ltin.read();
    c2 = 1;
    s2 = y1;
    switch (y1 + microblockStepY <= limitY ? 8 : r2)
    {
      default:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 1:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 2:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 3:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 4:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 5:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 6:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
        c2 <<= 1;
        y1 += pixelStepY;
      case 7:
        if ((d2 & c2) != 0)
        {
          decodeBlock3Tree24(in, oldPixelData, newPixelData);
        }
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
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 1:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 2:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 3:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 4:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 5:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 6:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
          c1 <<= 1;
          y1 += microblockStepY;
        case 7:
          if ((d1 & c1) != 0)
          {
            decodeBlock2Tree24(lpin, oldPixelData, newPixelData);
          }
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
  
  public final void encodeFrame8(final OutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int width, final int height, final int padding, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + padding + ((areaY + padding) * (width + padding));
    int size = (width * areaHeight);
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    m1 = 0;
    transferArea.x = areaX + padding;
    transferArea.y = areaY + padding;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width + padding, height + padding, transferArea, 64, 64, block1BitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(size);
    // lout.writeInt(areaX);
    lout.writeInt(areaWidth);
    encodeBlock0Tree8(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width + padding, height + padding, transferArea);
    // System.arraycopy(newPixelData, oldPixelData, size -
    // areaX);
    // System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
  }
  
  public final void decodeFrame8(final InputStream in, final byte[] oldPixelData, final byte[] newPixelData, final int width, final int height, final int padding) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    int size = lin.readInt();
    // int areaX = lin.readInt();
    int areaWidth = lin.readInt();
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    decodeBlock0Tree8(lin, oldPixelData, newPixelData);
//    int areaHeight = size / width;
//    int areaY = (offset - areaX) / width;
//    transferArea.x = areaX;
//    transferArea.y = areaY;
//    transferArea.width = areaWidth;
//    transferArea.height = areaHeight;
//    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
  
  public final void encodeFrame15(final OutputStream out, final short[] oldPixelData, final short[] newPixelData, final int width, final int height, final int padding, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + padding + ((areaY + padding) * (width + padding));
    int size = (width * areaHeight);
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    m1 = 0;
    transferArea.x = areaX + padding;
    transferArea.y = areaY + padding;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width + padding, height + padding, transferArea, 64, 64, block1BitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(size);
    // lout.writeInt(areaX);
    lout.writeInt(areaWidth);
    encodeBlock0Tree15(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width + padding, height + padding, transferArea);
    // System.arraycopy(newPixelData, oldPixelData, size -
    // areaX);
    // System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
  }
  
  public final void decodeFrame15(final InputStream in, final short[] oldPixelData, final short[] newPixelData, final int width, final int height, final int padding) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    int size = lin.readInt();
    // int areaX = lin.readInt();
    int areaWidth = lin.readInt();
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    decodeBlock0Tree15(lin, oldPixelData, newPixelData);
//    int areaHeight = size / width;
//    int areaY = (offset - areaX) / width;
//    transferArea.x = areaX;
//    transferArea.y = areaY;
//    transferArea.width = areaWidth;
//    transferArea.height = areaHeight;
//    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
  
  public final void encodeFrame24(final OutputStream out, final int[] oldPixelData, final int[] newPixelData, final int width, final int height, final int padding, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    offset = areaX + padding + ((areaY + padding) * (width + padding));
    int size = (width * areaHeight);
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    m1 = 0;
    transferArea.x = areaX + padding;
    transferArea.y = areaY + padding;
    transferArea.width = areaWidth;
    transferArea.height = areaHeight;
    VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width + padding, height + padding, transferArea, 64, 64, block1BitSet);
    lout.setOutputStream(out);
    lout.writeInt(offset);
    lout.writeInt(size);
    // lout.writeInt(areaX);
    lout.writeInt(areaWidth);
    encodeBlock0Tree24(lout, oldPixelData, newPixelData);
    lout.flush();
    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width + padding, height + padding, transferArea);
    // System.arraycopy(newPixelData, oldPixelData, size -
    // areaX);
    // System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
  }
  
  public final void decodeFrame24(final InputStream in, final int[] oldPixelData, final int[] newPixelData, final int width, final int height, final int padding) throws IOException
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
    pixelStepY = width + padding;
    microblockStepY = pixelStepY * 8;
    macroblockStepY = microblockStepY * 8;
    lin.setIntputStream(in);
    offset = lin.readInt();
    int size = lin.readInt();
    // int areaX = lin.readInt();
    int areaWidth = lin.readInt();
    limitX = areaWidth;
    limitY = size;
    r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
    r2 = 8 - (((size) / width) % 8);
    decodeBlock0Tree24(lin, oldPixelData, newPixelData);
//    int areaHeight = size / width;
//    int areaY = (offset - areaX) / width;
//    transferArea.x = areaX;
//    transferArea.y = areaY;
//    transferArea.width = areaWidth;
//    transferArea.height = areaHeight;
//    VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
  }
}