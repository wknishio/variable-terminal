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

public final class VTQuadrupleOctalTreeFrameDifferenceCodecV8
{
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
	
	public VTQuadrupleOctalTreeFrameDifferenceCodecV8()
	{
		this.macroblockDataBuffer = new ByteArrayOutputStream();
		this.macrolineDataBuffer = new ByteArrayOutputStream();
		this.microblockDataBuffer = new ByteArrayOutputStream();
		this.pixelDataBuffer = new ByteArrayOutputStream();
		this.pixelDataBufferStream = new VTLittleEndianOutputStream(pixelDataBuffer);
	}
	
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left, top, diag;
		left = x > 0 ? newPixelData[position - 1] & 0xff : 0;
		top = y > 0 ? newPixelData[position - width] & 0xff : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] & 0xff : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		out.write((byte) (newPixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))));
	}
	
	private static final void encodePixelDynamic(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int position) throws IOException
	{
		out.write((byte) (newPixelData[position] ^ oldPixelData[position]));
		// oldPixelData[position] = newPixelData[position];
	}
	
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		short left, top, diag;
		left = x > 0 ? newPixelData[position - 1] : 0;
		top = y > 0 ? newPixelData[position - width] : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		out.writeShort((short) (newPixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))));
	}
	
	private static final void encodePixelDynamic(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int position) throws IOException
	{
		out.writeShort((short) (newPixelData[position] ^ oldPixelData[position]));
	}
	
	private static final void encodePixelDirect(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left, top, diag;
		left = x > 0 ? newPixelData[position - 1] : 0;
		top = y > 0 ? newPixelData[position - width] : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		out.writeSubInt(newPixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
	}
	
	private static final void encodePixelDynamic(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int position) throws IOException
	{
		out.writeSubInt((newPixelData[position] ^ oldPixelData[position]));
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final byte[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left, top, diag;
		left = x > 0 ? newPixelData[position - 1] & 0xff : 0;
		top = y > 0 ? newPixelData[position - width] & 0xff : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] & 0xff : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		newPixelData[position] = (byte) (in.readUnsignedByte() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
		// newPixelData[position] = (byte) in.readUnsignedByte();
	}
	
	private static final void decodePixelDynamic(final VTLittleEndianInputStream in, final byte[] newPixelData, final int position) throws IOException
	{
		newPixelData[position] ^= (byte) (in.readUnsignedByte());
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final short[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		short left, top, diag;
		left = x > 0 ? newPixelData[position - 1] : 0;
		top = y > 0 ? newPixelData[position - width] : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		newPixelData[position] = (short) (in.readShort() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
	}
	
	private static final void decodePixelDynamic(final VTLittleEndianInputStream in, final short[] newPixelData, final int position) throws IOException
	{
		newPixelData[position] ^= (in.readShort());
	}
	
	private static final void decodePixelDirect(final VTLittleEndianInputStream in, final int[] newPixelData, final int position, final int x, final int y, final int width) throws IOException
	{
		int left, top, diag;
		left = x > 0 ? newPixelData[position - 1] : 0;
		top = y > 0 ? newPixelData[position - width] : 0;
		diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] : 0;
		// int pred = Math.max(Math.min(left, top), Math.min(Math.max(left,
		// top), left +
		// top - diag));
		newPixelData[position] = (in.readSubInt() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))) & 0x00FFFFFF;
	}
	
	private static final void decodePixelDynamic(final VTLittleEndianInputStream in, final int[] newPixelData, final int position) throws IOException
	{
		newPixelData[position] ^= (in.readSubInt());
	}
	
	private final void encodeMicrolineDirectMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicrolineDynamicMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDynamicSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicroblockDirectMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMacrolineDirectMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacroblockDirectMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
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
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			out.flush();
		}
	}
	
	private final void decodeMicrolineDirectMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
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
	
	private final void decodeMacroblockDynamicMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void decodeMacroblockDynamicSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
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
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void encodeMicrolineDirectMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicrolineDynamicMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDynamicSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicroblockDirectMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMacrolineDirectMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacroblockDirectMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
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
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			out.flush();
		}
	}
	
	private final void decodeMicrolineDirectMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
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
	
	private final void decodeMacroblockDynamicMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void decodeMacroblockDynamicSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
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
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void encodeMicrolineDirectMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDirect(pixelDataBufferStream, oldPixelData, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicrolineDynamicMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
		if (d4 != 0)
		{
			d3 |= c3;
			microblockDataBuffer.write(d4);
			pixelDataBuffer.writeTo(microblockDataBuffer);
			pixelDataBuffer.reset();
		}
	}
	
	private final void encodeMicrolineDynamicSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = 0;
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if (oldPixelData[p1] != newPixelData[p1])
			{
				d4 |= c4;
				encodePixelDynamic(pixelDataBufferStream, oldPixelData, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
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
	
	private final void encodeMicroblockDirectMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDirectSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicMixed(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMicroblockDynamicSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d3 = 0;
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			encodeMicrolineDynamicSeparated(out, oldPixelData, newPixelData, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
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
	
	private final void encodeMacrolineDirectMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDirectSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDirectSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacrolineDynamicSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = 0;
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			encodeMicroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
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
	
	private final void encodeMacroblockDirectMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
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
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
				y1 += microblockStepY;
			}
		}
		y1 = s1;
		out.write(d1);
		if (d1 != 0)
		{
			macroblockDataBuffer.writeTo(out);
			macroblockDataBuffer.reset();
			out.flush();
		}
	}
	
	private final void encodeMacroblockDynamicSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d1 = 0;
		c1 = 1;
		s1 = y1;
		switch (y1 + macroblockStepY <= size ? 8 : r1)
		{
			default:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 1:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 2:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 3:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 4:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 5:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 6:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
				c1 <<= 1;
				y1 += microblockStepY;
			}
			case 7:
			{
				encodeMacrolineDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			out.flush();
		}
	}
	
	private final void decodeMicrolineDirectMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDirect(in, newPixelData, p1, x1, y1, pixelStepY);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = in.readUnsignedByte();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicrolineDynamicSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int offset, final int areaWidth) throws IOException
	{
		d4 = tin.read();
		c4 = 1;
		p1 = offset + x1 + y1;
		s4 = x1;
		int i = x1 + microblockStepX - areaWidth;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d4 & c4) != 0)
			{
				decodePixelDynamic(in, newPixelData, p1);
			}
			if (++i >= 8)
			{
				break;
			}
			c4 <<= 1;
			x1 += pixelStepX;
			p1 += pixelStepX;
		}
		x1 = s4;
	}
	
	private final void decodeMicroblockDirectMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDirectSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = in.readUnsignedByte();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicMixed(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMicroblockDynamicSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read microblock difference map data
		d3 = tin.read();
		c3 = 1;
		s3 = y1;
		int i = y1 + microblockStepY <= size ? 0 : r3;
		for (;;)
		{
			if ((d3 & c3) != 0)
			{
				decodeMicrolineDynamicSeparated(in, newPixelData, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c3 <<= 1;
			y1 += pixelStepY;
		}
		y1 = s3;
	}
	
	private final void decodeMacrolineDirectMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDirectSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDirectSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = in.readUnsignedByte();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacrolineDynamicSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		d2 = tin.read();
		c2 = 1;
		s2 = x1;
		int i = (x1 + macroblockStepX - areaWidth) >> 3;
		i = i < 0 ? 0 : i;
		for (;;)
		{
			if ((d2 & c2) != 0)
			{
				decodeMicroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
			}
			if (++i >= 8)
			{
				break;
			}
			c2 <<= 1;
			x1 += microblockStepX;
		}
		x1 = s2;
	}
	
	private final void decodeMacroblockDirectMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDirectMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
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
	
	private final void decodeMacroblockDynamicMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// Read macroblock difference map data
		d1 = in.readUnsignedByte();
		c1 = 1;
		s1 = y1;
		// If macroblock has changes
		if (d1 > 0)
		{
			switch (y1 + macroblockStepY <= size ? 8 : r1)
			{
				default:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicMixed(in, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	private final void decodeMacroblockDynamicSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
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
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 1:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 2:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 3:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 4:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 5:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 6:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					c1 <<= 1;
					y1 += microblockStepY;
				}
				case 7:
				{
					if ((d1 & c1) != 0)
					{
						decodeMacrolineDynamicSeparated(plin, newPixelData, size, offset, areaWidth);
					}
					y1 += microblockStepY;
				}
			}
			y1 = s1;
		}
	}
	
	public final void encodeFrame(final OutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int width, final int height, final boolean dynamic, final boolean separated, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
		if (!dynamic)
		{
			if (!separated)
			{
				lout.write(1);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(2);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				lout.write(3);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(4);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicMixed(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicSeparated(final VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			decodeFrameDirectMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(lin, newPixelData, size, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicMixed(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicSeparated(final VTLittleEndianInputStream in, final byte[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
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
	
	public final void encodeFrame(final OutputStream out, final short[] oldPixelData, final short[] newPixelData, final int width, final int height, final boolean dynamic, final boolean separated, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
		if (!dynamic)
		{
			if (!separated)
			{
				lout.write(1);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(2);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				lout.write(3);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(4);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicMixed(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicSeparated(final VTLittleEndianOutputStream out, final short[] oldPixelData, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			decodeFrameDirectMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(lin, newPixelData, size, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicMixed(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicSeparated(final VTLittleEndianInputStream in, final short[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
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
	
	public final void encodeFrame(final OutputStream out, final int[] oldPixelData, final int[] newPixelData, final int width, final int height, final boolean dynamic, final boolean separated, final int areaX, final int areaY, final int areaWidth, final int areaHeight) throws IOException
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
		if (!dynamic)
		{
			if (!separated)
			{
				lout.write(1);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(2);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDirectSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				lout.write(3);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicMixed(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
			else
			{
				lout.write(4);
				lout.writeInt(size);
				lout.writeInt(offset);
				lout.writeInt(areaWidth);
				encodeFrameDynamicSeparated(lout, oldPixelData, newPixelData, size, offset, areaWidth);
			}
		}
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, size -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDirectMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicMixed(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicMixed(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
	
	private final void encodeFrameDynamicSeparated(final VTLittleEndianOutputStream out, final int[] oldPixelData, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			if (macroblockBitSet.get(m1++))
			{
				encodeMacroblockDynamicSeparated(out, oldPixelData, newPixelData, size, offset, areaWidth);
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
			decodeFrameDirectMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicMixed(lin, newPixelData, size, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(lin, newPixelData, size, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDirectMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicMixed(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicMixed(in, newPixelData, size, offset, areaWidth);
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
	
	private final void decodeFrameDynamicSeparated(final VTLittleEndianInputStream in, final int[] newPixelData, final int size, final int offset, final int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			decodeMacroblockDynamicSeparated(in, newPixelData, size, offset, areaWidth);
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