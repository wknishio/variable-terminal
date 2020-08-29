package org.vate.graphics.codec;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

import org.vate.graphics.image.VTImageDataUtils;
import org.vate.stream.array.VTByteArrayInputStream;
import org.vate.stream.endian.VTLittleEndianInputStream;
import org.vate.stream.endian.VTLittleEndianOutputStream;

public final class VTQuadrupleOctalTreeFrameDifferenceCodecV10
{
	private static final int DCM_RED_MASK = 0x00ff0000;
	private static final int DCM_GREEN_MASK = 0x0000ff00;
	private static final int DCM_BLUE_MASK = 0x000000ff;
	private static final int DCM_ALPHA_MASK = 0xff000000;
	
	private static final int DCM_555_RED_MASK = 0x7C00; // 0111110000000000
	private static final int DCM_555_GRN_MASK = 0x03E0; // 0000001111100000
	private static final int DCM_555_BLU_MASK = 0x001F; // 0000000000011111
	
	// iterators
	// macroblock count
	// private int c0 = 0;
	// macroblock scanline count
	private int c1 = 0;
	// microblock count
	private int c2 = 0;
	// microblock scanline count
	private int c3 = 0;
	// pixel count
	private int c4 = 0;
	// macroblock pos X
	private int x1 = 0;
	// macroblock pos Y
	private int y1 = 0;
	// pixel position
	private int p1 = 0;
	// pixel bit align
	// private int p2 = 0;
	// border remainder
	private int r1 = -1;
	//private int r2 = -1;
	private int r3 = -1;
	// border subtraction
	private int s1 = -1;
	private int s2 = -1;
	private int s3 = -1;
	private int s4 = -1;
	// difference data holders
	// macroblock difference data
	private int d1 = 0;
	// macroblock scanline difference data
	private int d2 = 0;
	// microblock difference data
	private int d3 = 0;
	// microblock scanline difference data
	private int d4 = 0;
	// pixel difference data
	// private int d5 = 0;
	// step helpers
	// X axis macroblock step
	private static final int macroblockStepX = 64;
	// X axis microblock step
	private static final int microblockStepX = 8;
	// X axis pixel step
	private static final int pixelStepX = 1;
	// Y axis macroblock step
	private int macroblockStepY;
	// Y axis microblock step
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
	private int macroblockTreeDataLength;
	private int macroblockPixelDataLength;
	// data buffers
	private ByteArrayOutputStream macroblockDataBuffer;
	private ByteArrayOutputStream macrolineDataBuffer;
	private ByteArrayOutputStream microblockDataBuffer;
	private ByteArrayOutputStream pixelDataBuffer;
	private VTLittleEndianOutputStream pixelDataBufferStream;
	// preloaded data streams
	private VTByteArrayInputStream tin = new VTByteArrayInputStream(new byte[1024]);
	private VTByteArrayInputStream pin = new VTByteArrayInputStream(new byte[4096 * 4]);
	private VTLittleEndianInputStream lin = new VTLittleEndianInputStream(null);
	//private VTLittleEndianInputStream tlin = new VTLittleEndianInputStream(tin);
	private VTLittleEndianInputStream plin = new VTLittleEndianInputStream(pin);
	private VTLittleEndianOutputStream lout = new VTLittleEndianOutputStream(null);
	private BitSet macroblockBitSet = new BitSet(1024 * 8);
	private Rectangle transferArea = new Rectangle(0, 0, 1, 1);
	private int m1;
	
	public final void dispose()
	{
		macroblockDataBuffer = null;
		macrolineDataBuffer = null;
		microblockDataBuffer = null;
		pixelDataBuffer = null;
	}
	
	public VTQuadrupleOctalTreeFrameDifferenceCodecV10()
	{
		this.macroblockDataBuffer = new ByteArrayOutputStream();
		this.macrolineDataBuffer = new ByteArrayOutputStream();
		this.microblockDataBuffer = new ByteArrayOutputStream();
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
	
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] & 0xff : 0;
		top1 = y > 0 ? newPixelData[position - width] & 0xff : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] & 0xff : 0;
		out.write((byte) (newPixelData[position] - ((left1 + top1) >> 1)));
	}
		
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		short left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		out.writeShort((short) (newPixelData[position] - ((left1 + top1) >> 1)));
		//out.writeShort(paethFilterShort(newPixelData, position, x, y, width));
	}
		
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		out.writeSubInt(newPixelData[position] - ((left1 + top1) >> 1));
	}
		
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		long left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		out.writeLong(newPixelData[position] - ((left1 + top1) >> 1));
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final byte[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] & 0xff : 0;
		top1 = y > 0 ? newPixelData[position - width] & 0xff : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] & 0xff : 0;
		newPixelData[position] = (byte) (in.readUnsignedByte() + ((left1 + top1) >> 1));
		// newPixelData[position] = (byte) in.readUnsignedByte();
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final short[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		short left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		newPixelData[position] = (short) (in.readShort() + ((left1 + top1) >> 1));
		//newPixelData[position] = paethUnfilterShort(newPixelData, position, x, y, width, in.readShort());
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final int[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		newPixelData[position] = (in.readSubInt() + ((left1 + top1) >> 1)) & 0x00FFFFFF;
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final long[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		long left1, top1;
		left1 = x > 0 ? newPixelData[position - 1] : 0;
		top1 = y > 0 ? newPixelData[position - width] : 0;
		//diag1 = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		newPixelData[position] = (in.readLong() + ((left1 + top1) >> 1)) & 0x7FFFFFFFFFFFFFFFL;
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			// pixelDataBuffer.writeTo(microblockDataBuffer);
			// pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				y1 += pixelStepY;
			}
		}
		y1 = s3;
		if (d3 != 0)
		{
			d2 |= c2;
			macrolineDataBuffer.write(d3);
			microblockDataBuffer.writeTo(macrolineDataBuffer);
			microblockDataBuffer.reset();
		}
	}
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		x1 = s2;
		if (d2 != 0)
		{
			d1 |= c1;
			// Write macroblock scanline difference map data
			macroblockDataBuffer.write(d2);
			macrolineDataBuffer.writeTo(macroblockDataBuffer);
			macrolineDataBuffer.reset();
		}
	}
	
	private final void encodeMacroblockDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			out.writeUnsignedShort(macroblockDataBuffer.size());
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.writeUnsignedShort(pixelDataBuffer.size());
			pixelDataBuffer.writeTo(out);
			pixelDataBuffer.reset();
			//out.flush();
		}
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				y1 += pixelStepY;
			}
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
			}
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			macroblockTreeDataLength = in.readUnsignedShort();
			if (macroblockTreeDataLength > tin.buf().length)
			{
				tin.buf(new byte[macroblockTreeDataLength]);
			}
			in.readFully(tin.buf(), 0, macroblockTreeDataLength);
			tin.count(macroblockTreeDataLength);
			tin.pos(0);
			
			macroblockPixelDataLength = in.readUnsignedShort();
			if (macroblockPixelDataLength > pin.buf().length)
			{
				pin.buf(new byte[macroblockPixelDataLength]);
			}
			in.readFully(pin.buf(), 0, macroblockPixelDataLength);
			pin.count(macroblockPixelDataLength);
			pin.pos(0);
			
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			// pixelDataBuffer.writeTo(microblockDataBuffer);
			// pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				y1 += pixelStepY;
			}
		}
		y1 = s3;
		if (d3 != 0)
		{
			d2 |= c2;
			macrolineDataBuffer.write(d3);
			microblockDataBuffer.writeTo(macrolineDataBuffer);
			microblockDataBuffer.reset();
		}
	}
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		x1 = s2;
		if (d2 != 0)
		{
			d1 |= c1;
			// Write macroblock scanline difference map data
			macroblockDataBuffer.write(d2);
			macrolineDataBuffer.writeTo(macroblockDataBuffer);
			macrolineDataBuffer.reset();
		}
	}
	
	private final void encodeMacroblockDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			out.writeUnsignedShort(macroblockDataBuffer.size());
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.writeUnsignedShort(pixelDataBuffer.size());
			pixelDataBuffer.writeTo(out);
			pixelDataBuffer.reset();
			//out.flush();
		}
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				y1 += pixelStepY;
			}
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
			}
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			macroblockTreeDataLength = in.readUnsignedShort();
			if (macroblockTreeDataLength > tin.buf().length)
			{
				tin.buf(new byte[macroblockTreeDataLength]);
			}
			in.readFully(tin.buf(), 0, macroblockTreeDataLength);
			tin.count(macroblockTreeDataLength);
			tin.pos(0);
			
			macroblockPixelDataLength = in.readUnsignedShort();
			if (macroblockPixelDataLength > pin.buf().length)
			{
				pin.buf(new byte[macroblockPixelDataLength]);
			}
			in.readFully(pin.buf(), 0, macroblockPixelDataLength);
			pin.count(macroblockPixelDataLength);
			pin.pos(0);
			
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			// pixelDataBuffer.writeTo(microblockDataBuffer);
			// pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				y1 += pixelStepY;
			}
		}
		y1 = s3;
		if (d3 != 0)
		{
			d2 |= c2;
			macrolineDataBuffer.write(d3);
			microblockDataBuffer.writeTo(macrolineDataBuffer);
			microblockDataBuffer.reset();
		}
	}
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		x1 = s2;
		if (d2 != 0)
		{
			d1 |= c1;
			// Write macroblock scanline difference map data
			macroblockDataBuffer.write(d2);
			macrolineDataBuffer.writeTo(macroblockDataBuffer);
			macrolineDataBuffer.reset();
		}
	}
	
	private final void encodeMacroblockDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			out.writeUnsignedShort(macroblockDataBuffer.size());
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.writeUnsignedShort(pixelDataBuffer.size());
			pixelDataBuffer.writeTo(out);
			pixelDataBuffer.reset();
			//out.flush();
		}
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				y1 += pixelStepY;
			}
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
			}
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			macroblockTreeDataLength = in.readUnsignedShort();
			if (macroblockTreeDataLength > tin.buf().length)
			{
				tin.buf(new byte[macroblockTreeDataLength]);
			}
			in.readFully(tin.buf(), 0, macroblockTreeDataLength);
			tin.count(macroblockTreeDataLength);
			tin.pos(0);
			
			macroblockPixelDataLength = in.readUnsignedShort();
			if (macroblockPixelDataLength > pin.buf().length)
			{
				pin.buf(new byte[macroblockPixelDataLength]);
			}
			in.readFully(pin.buf(), 0, macroblockPixelDataLength);
			pin.count(macroblockPixelDataLength);
			pin.pos(0);
			
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if (oldPixelData[p1] != newPixelData[p1])
				{
					d4 |= c4;
					encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			// pixelDataBuffer.writeTo(microblockDataBuffer);
			// pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
				y1 += pixelStepY;
			}
		}
		y1 = s3;
		if (d3 != 0)
		{
			d2 |= c2;
			macrolineDataBuffer.write(d3);
			microblockDataBuffer.writeTo(macrolineDataBuffer);
			microblockDataBuffer.reset();
		}
	}
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		x1 = s2;
		if (d2 != 0)
		{
			d1 |= c1;
			// Write macroblock scanline difference map data
			macroblockDataBuffer.write(d2);
			macrolineDataBuffer.writeTo(macroblockDataBuffer);
			macrolineDataBuffer.reset();
		}
	}
	
	private final void encodeMacroblockDirectSeparated(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			out.writeUnsignedShort(macroblockDataBuffer.size());
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.writeUnsignedShort(pixelDataBuffer.size());
			pixelDataBuffer.writeTo(out);
			pixelDataBuffer.reset();
			//out.flush();
		}
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final long[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		switch (x1 + microblockStepX - areaWidth)
		{
			default:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 1:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 2:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 3:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 4:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 5:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 6:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
				c4 <<= 1;
				x1 += pixelStepX;
				p1 += pixelStepX;
			}
			case 7:
			{
				if ((d4 & c4) != 0)
				{
					decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
				}
			}
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		switch (y1 + microblockStepY <= size ? 8 : r3)
		{
			default:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 1:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 2:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 3:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 4:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 5:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 6:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				c3 <<= 1;
				y1 += pixelStepY;
			}
			case 7:
			{
				if ((d3 & c3) != 0)
				{
					decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
				}
				y1 += pixelStepY;
			}
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		//switch (x1 + macroblockStepX <= areaWidth ? 8 : r2)
		switch ((x1 + macroblockStepX - areaWidth) >> 3)
		{
			default:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 1:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 2:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 3:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 4:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 5:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 6:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
				c2 <<= 1;
				x1 += microblockStepX;
			}
			case 7:
			{
				if ((d2 & c2) != 0)
				{
					decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
				}
			}
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectSeparated(final VTLittleEndianInputStream in, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			macroblockTreeDataLength = in.readUnsignedShort();
			if (macroblockTreeDataLength > tin.buf().length)
			{
				tin.buf(new byte[macroblockTreeDataLength]);
			}
			in.readFully(tin.buf(), 0, macroblockTreeDataLength);
			tin.count(macroblockTreeDataLength);
			tin.pos(0);
			
			macroblockPixelDataLength = in.readUnsignedShort();
			if (macroblockPixelDataLength > pin.buf().length)
			{
				pin.buf(new byte[macroblockPixelDataLength]);
			}
			in.readFully(pin.buf(), 0, macroblockPixelDataLength);
			pin.count(macroblockPixelDataLength);
			pin.pos(0);
			
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	public final void encodeFrame(final OutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int width, final int height, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, final byte.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepX = 1;
		// pixelStepY = elementsPerPixel * width;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// int size = pixelNumber;
		int offset = areaX + (areaY * width);
		int size = (width * areaHeight);
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		// System.out.println("width:" + width);
		// System.out.println("areaWidth:" + areaWidth);
		// System.out.println("areaHeight:" + areaHeight);
		// System.out.println("l1:" + l1);
		// System.out.println("l2:" + l2);
		m1 = 0;
		VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width, height, new Rectangle(areaX, areaY, areaWidth, areaHeight), 64, 64, macroblockBitSet);
		lout.setOutputStream(out);
		lout.writeInt(size);
		lout.writeInt(offset);
		lout.writeInt(areaWidth);
		encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
		lout.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				out.write(0);
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(final InputStream in, final byte[] newPixelData, final int width, final int height) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		// d5 = 0;
		// bitsPerElement = 8;
		// elementsPerPixel = depth / 8;
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = depth / 8;
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// int size = pixelNumber;
		lin.setIntputStream(in);
		//int type = lin.readUnsignedByte();
		int size = lin.readInt();
		int offset = lin.readInt();
		int areaWidth = lin.readInt();
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
	}
	
	private final void decodeFrameDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void encodeFrame(final OutputStream out, final short[] oldPixelData, final short[] newPixelData, final int width, final int height, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, final short.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// int size = pixelNumber;
		int offset = areaX + (areaY * width);
		int size = (width * areaHeight);
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		m1 = 0;
		VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width, height, new Rectangle(areaX, areaY, areaWidth, areaHeight), 64, 64, macroblockBitSet);
		lout.setOutputStream(out);
		lout.writeInt(size);
		lout.writeInt(offset);
		lout.writeInt(areaWidth);
		encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
		lout.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				out.write(0);
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(final InputStream in, final short[] newPixelData, final int width, final int height) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		// d5 = 0;
		// bitsPerElement = Math.min(depth, final short.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// size = pixelNumber;
		lin.setIntputStream(in);
		//int type = lin.readUnsignedByte();
		int size = lin.readInt();
		int offset = lin.readInt();
		int areaWidth = lin.readInt();
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
	}
	
	private final void decodeFrameDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void encodeFrame(final OutputStream out, final int[] oldPixelData, final int[] newPixelData, final int width, final int height, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, final integer.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		int offset = areaX + (areaY * width);
		int size = (width * areaHeight);
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// size = pixelNumber;
		m1 = 0;
		VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width, height, new Rectangle(areaX, areaY, areaWidth, areaHeight), 64, 64, macroblockBitSet);
		lout.setOutputStream(out);
		lout.writeInt(size);
		lout.writeInt(offset);
		lout.writeInt(areaWidth);
		encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
		lout.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
		
	private final void encodeFrameDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				out.write(0);
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(final InputStream in, final int[] newPixelData, final int width, final int height) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		// d5 = 0;
		// bitsPerElement = Math.min(depth, final integer.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// size = pixelNumber;
		lin.setIntputStream(in);
		//int type = lin.readUnsignedByte();
		int size = lin.readInt();
		int offset = lin.readInt();
		int areaWidth = lin.readInt();
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
	}
		
	private final void decodeFrameDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void encodeFrame(final OutputStream out, final long[] oldPixelData, final long[] newPixelData, final int width, final int height, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, final integer.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		int offset = areaX + (areaY * width);
		int size = (width * areaHeight);
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// size = pixelNumber;
		m1 = 0;
		VTImageDataUtils.compareBlockArea(oldPixelData, newPixelData, width, height, new Rectangle(areaX, areaY, areaWidth, areaHeight), 64, 64, macroblockBitSet);
		lout.setOutputStream(out);
		lout.writeInt(size);
		lout.writeInt(offset);
		lout.writeInt(areaWidth);
		encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
		lout.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
		
	private final void encodeFrameDirectSeparated(final VTLittleEndianOutputStream out, final long[] oldPixelData, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				out.write(0);
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(final InputStream in, final long[] newPixelData, final int width, final int height) throws IOException
	{
		c1 = 0;
		c2 = 0;
		c3 = 0;
		c4 = 0;
		x1 = 0;
		y1 = 0;
		p1 = 0;
		// p2 = 0;
		d1 = 0;
		d2 = 0;
		d3 = 0;
		d4 = 0;
		// d5 = 0;
		// bitsPerElement = Math.min(depth, final integer.SIZE);
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = elementsPerPixel;
		// pixelStepY = elementsPerPixel * width;
		// elementsPerPixel = Math.max(depth / bitsPerElement, 1);
		// pixelStepX = 1;
		pixelStepY = width;
		// microblockStepX = pixelStepX * 8;
		microblockStepY = pixelStepY * 8;
		// macroblockStepX = microblockStepX * 8;
		macroblockStepY = microblockStepY * 8;
		// pixelNumber = width * height;
		// size = pixelNumber * elementsPerPixel;
		// size = pixelNumber;
		lin.setIntputStream(in);
		int type = lin.readUnsignedByte();
		int size = lin.readInt();
		int offset = lin.readInt();
		int areaWidth = lin.readInt();
		r1 = 8 - ((int) Math.ceil((((double) size) / width) / 8)) % 8;
		//r2 = 8 - ((int) Math.ceil(((double) areaWidth) / 8)) % 8;
		r3 = 8 - (((size) / width) % 8);
		//s1 = (8 - r1) * microblockStepY;
		//s3 = (8 - r3) * pixelStepY;
		if (type != -1 && type == 1)
		{
			//decodeFrameDirectMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			//decodeFrameDynamicMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			//decodeFrameDynamicSeparated(lin, newPixelData, size, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectSeparated(final VTLittleEndianInputStream in, final long[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= size)
				{
					break;
				}
			}
		}
	}
	

}