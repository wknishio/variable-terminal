package org.vate.graphics.codec;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.vate.graphics.image.VTImageDataUtils;
import org.vate.stream.array.VTByteArrayInputStream;
import org.vate.stream.endian.VTLittleEndianInputStream;
import org.vate.stream.endian.VTLittleEndianOutputStream;

public final class VTQuadrupleOctalTreeFrameDifferenceCodecV5
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
	private int l3 = -1;
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
	// private int pixelDataLength;
	// length of difference map data
	private int macroblockTreeDataLength;
	private byte bleft;
	private byte btop;
	private byte bdiag;
	private short sleft;
	private short stop;
	private short sdiag;
	private int ileft;
	private int itop;
	private int idiag;
	// data buffers
	private ByteArrayOutputStream macroblockDataBuffer;
	private ByteArrayOutputStream macrolineDataBuffer;
	private ByteArrayOutputStream microblockDataBuffer;
	private ByteArrayOutputStream pixelDataBuffer;
	private VTLittleEndianOutputStream pixelDataBufferStream;
	// preloaded difference map data streams
	private VTByteArrayInputStream din = new VTByteArrayInputStream(new byte[1024]);
	private Rectangle transferArea = new Rectangle(0, 0, 1, 1);
	
	public void dispose()
	{
		macroblockDataBuffer = null;
		macrolineDataBuffer = null;
		microblockDataBuffer = null;
		pixelDataBuffer = null;
	}
	
	public VTQuadrupleOctalTreeFrameDifferenceCodecV5()
	{
		this.macroblockDataBuffer = new ByteArrayOutputStream();
		this.macrolineDataBuffer = new ByteArrayOutputStream();
		this.microblockDataBuffer = new ByteArrayOutputStream();
		this.pixelDataBuffer = new ByteArrayOutputStream();
		this.pixelDataBufferStream = new VTLittleEndianOutputStream(pixelDataBuffer);
	}
	
	public void setPixelDataBuffer(ByteArrayOutputStream pixelDataBuffer)
	{
		this.pixelDataBuffer = pixelDataBuffer;
	}
	
	/* private static final int encodePixelDirect(final
	 * VTLittleEndianOutputStream out, final byte[] oldPixelData, final byte[]
	 * newPixelData, final int position, final int x, final int y, final int
	 * width, int mask) throws IOException { if (oldPixelData[position] ==
	 * newPixelData[position]) { return 0; } byte left, top, diag; left = x > 0
	 * ? newPixelData[position - 1] : 0; top = y > 0 ? newPixelData[position -
	 * width] : 0; diag = x > 0 && y > 0 ? newPixelData[position - 1 - width] :
	 * 0; out.write((byte) (newPixelData[position] - Math.max(Math.min(left,
	 * top), Math.min(Math.max(left, top), diag)))); return mask; } private
	 * static final int encodePixelDynamic(final VTLittleEndianOutputStream
	 * out, final byte[] oldPixelData, final byte[] newPixelData, final int
	 * position, final int x, final int y, final int width, int mask) throws
	 * IOException { if (oldPixelData[position] == newPixelData[position]) {
	 * return 0; } out.write((byte) (newPixelData[position] ^
	 * oldPixelData[position])); return mask; } private static final int
	 * encodePixelDirect(final VTLittleEndianOutputStream out, final short[]
	 * oldPixelData, final short[] newPixelData, final int position, final int
	 * x, final int y, final int width, int mask) throws IOException { if
	 * (oldPixelData[position] == newPixelData[position]) { return 0; } short
	 * left, top, diag; left = x > 0 ? newPixelData[position - 1] : 0; top = y >
	 * 0 ? newPixelData[position - width] : 0; diag = x > 0 && y > 0 ?
	 * newPixelData[position - 1 - width] : 0; out.writeShort((short)
	 * (newPixelData[position] - Math.max(Math.min(left, top),
	 * Math.min(Math.max(left, top), diag)))); return mask; } private static
	 * final int encodePixelDynamic(final VTLittleEndianOutputStream out, final
	 * short[] oldPixelData, final short[] newPixelData, final int position,
	 * final int x, final int y, final int width, int mask) throws IOException {
	 * if (oldPixelData[position] == newPixelData[position]) { return 0; }
	 * out.writeShort((short) (newPixelData[position] ^
	 * oldPixelData[position])); return mask; } private static final int
	 * encodePixelDirect(final VTLittleEndianOutputStream out, final int[]
	 * oldPixelData, final int[] newPixelData, final int position, final int x,
	 * final int y, final int width, int mask) throws IOException { if
	 * (oldPixelData[position] == newPixelData[position]) { return 0; } int
	 * left, top, diag; left = x > 0 ? newPixelData[position - 1] : 0; top = y >
	 * 0 ? newPixelData[position - width] : 0; diag = x > 0 && y > 0 ?
	 * newPixelData[position - 1 - width] : 0;
	 * out.writeSubInt((newPixelData[position] - (Math.max(Math.min(left, top),
	 * Math.min(Math.max(left, top), diag))))); return mask; } private static
	 * final int encodePixelDynamic(final VTLittleEndianOutputStream out, final
	 * int[] oldPixelData, final int[] newPixelData, final int position, final
	 * int x, final int y, final int width, int mask) throws IOException { if
	 * (oldPixelData[position] == newPixelData[position]) { return 0; }
	 * out.writeSubInt((newPixelData[position] ^ oldPixelData[position]));
	 * return mask; } private static final void decodePixelDirect(final
	 * VTLittleEndianInputStream in, final byte[] newPixelData, final int
	 * position, final int x, final int y, final int width, int mask, int bits)
	 * throws IOException { if ((bits & mask) == 0) { return; } byte left, top,
	 * diag; left = x > 0 ? newPixelData[position - 1] : 0; top = y > 0 ?
	 * newPixelData[position - width] : 0; diag = x > 0 && y > 0 ?
	 * newPixelData[position - 1 - width] : 0; newPixelData[position] = (byte)
	 * (in.readUnsignedByte() + Math.max(Math.min(left, top), Math.min(Math.max(left,
	 * top), diag))); } private static final void decodePixelDynamic(final
	 * VTLittleEndianInputStream in, final byte[] newPixelData, final int
	 * position, final int x, final int y, final int width, int mask, int bits)
	 * throws IOException { if ((bits & mask) == 0) { return; }
	 * newPixelData[position] ^= (byte) (in.readUnsignedByte()); } private static final
	 * void decodePixelDirect(final VTLittleEndianInputStream in, final short[]
	 * newPixelData, final int position, final int x, final int y, final int
	 * width, int mask, int bits) throws IOException { if ((bits & mask) == 0) {
	 * return; } short left, top, diag; left = x > 0 ? newPixelData[position -
	 * 1] : 0; top = y > 0 ? newPixelData[position - width] : 0; diag = x > 0 &&
	 * y > 0 ? newPixelData[position - 1 - width] : 0; newPixelData[position] =
	 * (short) (in.readShort() + Math.max(Math.min(left, top),
	 * Math.min(Math.max(left, top), diag))); } private static final void
	 * decodePixelDynamic(final VTLittleEndianInputStream in, final short[]
	 * newPixelData, final int position, final int x, final int y, final int
	 * width, int mask, int bits) throws IOException { if ((bits & mask) == 0) {
	 * return; } newPixelData[position] ^= (in.readShort()); } private static
	 * final void decodePixelDirect(final VTLittleEndianInputStream in, final
	 * int[] newPixelData, final int position, final int x, final int y, final
	 * int width, int mask, int bits) throws IOException { if ((bits & mask) ==
	 * 0) { return; } int left, top, diag; left = x > 0 ? newPixelData[position
	 * - 1] : 0; top = y > 0 ? newPixelData[position - width] : 0; diag = x > 0
	 * && y > 0 ? newPixelData[position - 1 - width] : 0; newPixelData[position]
	 * = (in.readSubInt() + (Math.max(Math.min(left, top),
	 * Math.min(Math.max(left, top), diag)))) & 0x00FFFFFF; } private static
	 * final void decodePixelDynamic(final VTLittleEndianInputStream in, final
	 * int[] newPixelData, final int position, final int x, final int y, final
	 * int width, int mask, int bits) throws IOException {
	 * newPixelData[position] ^= (in.readSubInt()); } */
	public final void encodeFrame(VTLittleEndianOutputStream out, byte[] oldPixelData, byte[] newPixelData, int width, int height, int depth, boolean dynamic, boolean separated, int areaX, int areaY, int areaWidth, int areaHeight) throws IOException
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
		l3 = -1;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, Byte.SIZE);
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
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// int pixelDataLength = pixelNumber;
		int offset = areaX + (areaY * width);
		int limit = (width * areaHeight);
		l3 = 8 - (((limit) / width) % 8);
		if (!dynamic)
		{
			if (!separated)
			{
				out.write(1);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(2);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				out.write(3);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(4);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		out.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, limit -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectInterleaved(VTLittleEndianOutputStream out, byte[] oldPixelData, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDirectSeparated(VTLittleEndianOutputStream out, byte[] oldPixelData, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.write((byte) (newPixelData[p1] - Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicInterleaved(VTLittleEndianOutputStream out, byte[] oldPixelData, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicSeparated(VTLittleEndianOutputStream out, byte[] oldPixelData, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.write((byte) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								// pixelDataBuffer.writeTo(microblockDataBuffer);
								// pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(VTLittleEndianInputStream in, byte[] newPixelData, int width, int height, int depth) throws IOException
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
		l3 = -1;
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
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// int pixelDataLength = pixelNumber;
		int type = in.readUnsignedByte();
		int limit = in.readInt();
		int offset = in.readInt();
		int areaWidth = in.readInt();
		l3 = 8 - (((limit) / width) % 8);
		if (type != -1 && type == 1)
		{
			decodeFrameDirectInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(in, newPixelData, limit, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectInterleaved(VTLittleEndianInputStream in, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDirectSeparated(VTLittleEndianInputStream in, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														bleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														btop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														bdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (byte) (in.readUnsignedByte() + Math.max(Math.min(bleft, btop), Math.min(Math.max(bleft, btop), bdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicInterleaved(VTLittleEndianInputStream in, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicSeparated(VTLittleEndianInputStream in, byte[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (byte) (in.readUnsignedByte());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	public final void encodeFrame(VTLittleEndianOutputStream out, short[] oldPixelData, short[] newPixelData, int width, int height, int depth, boolean dynamic, boolean separated, int areaX, int areaY, int areaWidth, int areaHeight) throws IOException
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
		l3 = -1;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, Short.SIZE);
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
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// int pixelDataLength = pixelNumber;
		int offset = areaX + (areaY * width);
		int limit = (width * areaHeight);
		l3 = 8 - (((limit) / width) % 8);
		if (!dynamic)
		{
			if (!separated)
			{
				out.write(1);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(2);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				out.write(3);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(4);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		out.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, limit -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectInterleaved(VTLittleEndianOutputStream out, short[] oldPixelData, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDirectSeparated(VTLittleEndianOutputStream out, short[] oldPixelData, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] - Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			// Write macroblock difference map data
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicInterleaved(VTLittleEndianOutputStream out, short[] oldPixelData, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicSeparated(VTLittleEndianOutputStream out, short[] oldPixelData, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeShort((short) (newPixelData[p1] ^ oldPixelData[p1]));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			// Write macroblock difference map data
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(VTLittleEndianInputStream in, short[] newPixelData, int width, int height, int depth) throws IOException
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
		l3 = -1;
		// bitsPerElement = Math.min(depth, Short.SIZE);
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
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// pixelDataLength = pixelNumber;
		int type = in.readUnsignedByte();
		int limit = in.readInt();
		int offset = in.readInt();
		int areaWidth = in.readInt();
		l3 = 8 - (((limit) / width) % 8);
		if (type != -1 && type == 1)
		{
			decodeFrameDirectInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(in, newPixelData, limit, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectInterleaved(VTLittleEndianInputStream in, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDirectSeparated(VTLittleEndianInputStream in, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														sleft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														stop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														sdiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (short) (in.readShort() + Math.max(Math.min(sleft, stop), Math.min(Math.max(sleft, stop), sdiag)));
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicInterleaved(VTLittleEndianInputStream in, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicSeparated(VTLittleEndianInputStream in, short[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readShort());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	public final void encodeFrame(VTLittleEndianOutputStream out, int[] oldPixelData, int[] newPixelData, int width, int height, int depth, boolean dynamic, boolean separated, int areaX, int areaY, int areaWidth, int areaHeight) throws IOException
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
		l3 = -1;
		macroblockDataBuffer.reset();
		macrolineDataBuffer.reset();
		microblockDataBuffer.reset();
		pixelDataBuffer.reset();
		// bitsPerElement = Math.min(depth, Integer.SIZE);
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
		int limit = (width * areaHeight);
		l3 = 8 - (((limit) / width) % 8);
		// pixelNumber = width * height;
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// pixelDataLength = pixelNumber;
		if (!dynamic)
		{
			if (!separated)
			{
				out.write(1);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(2);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDirectSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		else
		{
			if (!separated)
			{
				out.write(3);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicInterleaved(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
			else
			{
				out.write(4);
				out.writeInt(limit);
				out.writeInt(offset);
				out.writeInt(areaWidth);
				encodeFrameDynamicSeparated(out, oldPixelData, newPixelData, limit, offset, areaWidth);
			}
		}
		out.flush();
		transferArea.x = areaX;
		transferArea.y = areaY;
		transferArea.width = areaWidth;
		transferArea.height = areaHeight;
		VTImageDataUtils.copyArea(newPixelData, oldPixelData, 0, width, height, transferArea);
		// System.arraycopy(newPixelData, offset, oldPixelData, offset, limit -
		// areaX);
		// System.arraycopy(newPixelData, 0, oldPixelData, 0, width * height);
	}
	
	private final void encodeFrameDirectInterleaved(VTLittleEndianOutputStream out, int[] oldPixelData, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDirectSeparated(VTLittleEndianOutputStream out, int[] oldPixelData, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
										itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
										idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
										pixelDataBufferStream.writeSubInt((newPixelData[p1] - (Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag)))));
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			// Write macroblock difference map data
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicInterleaved(VTLittleEndianOutputStream out, int[] oldPixelData, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
								pixelDataBuffer.writeTo(microblockDataBuffer);
								pixelDataBuffer.reset();
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			// Write macroblock difference map data
			out.write(d1);
			if (d1 != 0)
			{
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void encodeFrameDynamicSeparated(VTLittleEndianOutputStream out, int[] oldPixelData, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			d1 = 0;
			for (c1 = 1;; c1 <<= 1)
			{
				d2 = 0;
				for (c2 = 1;; c2 <<= 1)
				{
					d3 = 0;
					c3 = 1;
					switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
					{
						default:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 1:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 2:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 3:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 4:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 5:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 6:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							c3 <<= 1;
							y1 += pixelStepY;
						}
						case 7:
						{
							d4 = 0;
							c4 = 1;
							p1 = offset + x1 + y1;
							switch ((x1 + microblockStepX) - areaWidth)
							{
								default:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 1:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 2:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 3:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 4:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 5:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 6:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
									c4 <<= 1;
									p1 += pixelStepX;
									x1 += pixelStepX;
								}
								case 7:
								{
									if (oldPixelData[p1] != newPixelData[p1])
									{
										d4 |= c4;
										pixelDataBufferStream.writeSubInt(oldPixelData[p1] ^ newPixelData[p1]);
									}
								}
							}
							x1 -= (x1 & (microblockStepX - 1));
							if (d4 != 0)
							{
								d3 |= c3;
								microblockDataBuffer.write(d4);
							}
							y1 += pixelStepY;
						}
					}
					switch (c3)
					{
						case 1:
							y1 -= pixelStepY;
							break;
						case 2:
							y1 -= pixelStepY * 2;
							break;
						case 4:
							y1 -= pixelStepY * 3;
							break;
						case 8:
							y1 -= pixelStepY * 4;
							break;
						case 16:
							y1 -= pixelStepY * 5;
							break;
						case 32:
							y1 -= pixelStepY * 6;
							break;
						case 64:
							y1 -= pixelStepY * 7;
							break;
						default:
							y1 -= microblockStepY;
							break;
					}
					if (d3 != 0)
					{
						d2 |= c2;
						// Write microblock difference map data
						macrolineDataBuffer.write(d3);
						microblockDataBuffer.writeTo(macrolineDataBuffer);
						microblockDataBuffer.reset();
					}
					// Iterate microblock X and detect X axis out-of-bounds!
					x1 += microblockStepX;
					if (c2 == 128)
					{
						x1 -= macroblockStepX;
						break;
					}
					if (x1 >= areaWidth)
					{
						x1 -= (x1 & (macroblockStepX - 1));
						break;
					}
				}
				if (d2 != 0)
				{
					d1 |= c1;
					// Write macroblock scanline difference map data
					macroblockDataBuffer.write(d2);
					macrolineDataBuffer.writeTo(macroblockDataBuffer);
					macrolineDataBuffer.reset();
				}
				// Iterate microblock Y and detect Y axis out-of-bounds!
				y1 += microblockStepY;
				if (c1 == 128)
				{
					y1 -= macroblockStepY;
					break;
				}
				if (y1 >= pixelDataLength)
				{
					switch (c1)
					{
						case 1:
							y1 -= microblockStepY;
							break;
						case 2:
							y1 -= microblockStepY * 2;
							break;
						case 4:
							y1 -= microblockStepY * 3;
							break;
						case 8:
							y1 -= microblockStepY * 4;
							break;
						case 16:
							y1 -= microblockStepY * 5;
							break;
						case 32:
							y1 -= microblockStepY * 6;
							break;
						default:
							y1 -= microblockStepY * 7;
					}
					break;
				}
			}
			out.write(d1);
			// Write macroblock difference map data
			if (d1 != 0)
			{
				out.writeUnsignedShort(macroblockDataBuffer.size());
				macroblockDataBuffer.writeTo(out);
				macroblockDataBuffer.reset();
				pixelDataBuffer.writeTo(out);
				pixelDataBuffer.reset();
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	public final void decodeFrame(VTLittleEndianInputStream in, int[] newPixelData, int width, int height, int depth) throws IOException
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
		l3 = -1;
		// bitsPerElement = Math.min(depth, Integer.SIZE);
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
		// pixelDataLength = pixelNumber * elementsPerPixel;
		// pixelDataLength = pixelNumber;
		int type = in.readUnsignedByte();
		int limit = in.readInt();
		int offset = in.readInt();
		int areaWidth = in.readInt();
		l3 = 8 - (((limit) / width) % 8);
		if (type != -1 && type == 1)
		{
			decodeFrameDirectInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 2)
		{
			decodeFrameDirectSeparated(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 3)
		{
			decodeFrameDynamicInterleaved(in, newPixelData, limit, offset, areaWidth);
		}
		else if (type != -1 && type == 4)
		{
			decodeFrameDynamicSeparated(in, newPixelData, limit, offset, areaWidth);
		}
	}
	
	private final void decodeFrameDirectInterleaved(VTLittleEndianInputStream in, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDirectSeparated(VTLittleEndianInputStream in, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														ileft = x1 > 0 ? newPixelData[p1 - 1] : 0;
														itop = y1 > 0 ? newPixelData[p1 - pixelStepY] : 0;
														idiag = x1 > 0 && y1 > 0 ? newPixelData[p1 - 1 - pixelStepY] : 0;
														newPixelData[p1] = (in.readSubInt() + Math.max(Math.min(ileft, itop), Math.min(Math.max(ileft, itop), idiag))) & 0x00FFFFFF;
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicInterleaved(VTLittleEndianInputStream in, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = in.readUnsignedByte();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								// Read microblock difference map data
								d3 = in.readUnsignedByte();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = in.readUnsignedByte();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
	
	private final void decodeFrameDynamicSeparated(VTLittleEndianInputStream in, int[] newPixelData, int pixelDataLength, int offset, int areaWidth) throws IOException
	{
		// For each macroblock
		for (;;)
		{
			// Read macroblock difference map data
			d1 = in.readUnsignedByte();
			// If macroblock has changes
			if (d1 > 0)
			{
				macroblockTreeDataLength = in.readUnsignedShort();
				if (macroblockTreeDataLength > din.buf().length)
				{
					din.buf(new byte[macroblockTreeDataLength]);
				}
				in.readFully(din.buf(), 0, macroblockTreeDataLength);
				din.count(macroblockTreeDataLength);
				din.pos(0);
				// For each macroblock scanline
				for (c1 = 1;; c1 <<= 1)
				{
					// If macroblock scanline has changes
					if ((d1 & (c1)) != 0)
					{
						// Read macroblock scanline difference map data
						d2 = din.read();
						// For each microblock
						for (c2 = 1;; c2 <<= 1)
						{
							// If microblock has changes
							if ((d2 & (c2)) != 0)
							{
								d3 = din.read();
								c3 = 1;
								switch (y1 + microblockStepY <= pixelDataLength ? 8 : l3)
								{
									default:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 1:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 2:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 3:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 4:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 5:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 6:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										c3 <<= 1;
										y1 += pixelStepY;
									}
									case 7:
									{
										if ((d3 & (c3)) != 0)
										{
											d4 = din.read();
											c4 = 1;
											p1 = offset + x1 + y1;
											switch ((x1 + microblockStepX) - areaWidth)
											{
												default:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 1:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 2:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 3:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 4:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 5:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 6:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
													c4 <<= 1;
													p1 += pixelStepX;
													x1 += pixelStepX;
												}
												case 7:
												{
													if ((d4 & (c4)) != 0)
													{
														newPixelData[p1] ^= (in.readSubInt());
													}
												}
											}
											x1 -= (x1 & (microblockStepX - 1));
										}
										y1 += pixelStepY;
									}
								}
								switch (c3)
								{
									case 1:
										y1 -= pixelStepY;
										break;
									case 2:
										y1 -= pixelStepY * 2;
										break;
									case 4:
										y1 -= pixelStepY * 3;
										break;
									case 8:
										y1 -= pixelStepY * 4;
										break;
									case 16:
										y1 -= pixelStepY * 5;
										break;
									case 32:
										y1 -= pixelStepY * 6;
										break;
									case 64:
										y1 -= pixelStepY * 7;
										break;
									default:
										y1 -= microblockStepY;
										break;
								}
							}
							// Iterate microblock X and detect X axis
							// out-of-bounds!
							x1 += microblockStepX;
							if (c2 == 128)
							{
								x1 -= macroblockStepX;
								break;
							}
							if (x1 >= areaWidth)
							{
								x1 -= (x1 & (macroblockStepX - 1));
								break;
							}
						}
					}
					// Iterate microblock Y and detect Y axis out-of-bounds!
					y1 += microblockStepY;
					if (c1 == 128)
					{
						y1 -= macroblockStepY;
						break;
					}
					if (y1 >= pixelDataLength)
					{
						switch (c1)
						{
							case 1:
								y1 -= microblockStepY;
								break;
							case 2:
								y1 -= microblockStepY * 2;
								break;
							case 4:
								y1 -= microblockStepY * 3;
								break;
							case 8:
								y1 -= microblockStepY * 4;
								break;
							case 16:
								y1 -= microblockStepY * 5;
								break;
							case 32:
								y1 -= microblockStepY * 6;
								break;
							default:
								y1 -= microblockStepY * 7;
						}
						break;
					}
				}
			}
			// Iterate macroblock X and detect X axis out-of-bounds!
			x1 += macroblockStepX;
			if (x1 >= areaWidth)
			{
				x1 = 0;
				// Iterate macroblock Y and detect Y axis out-of-bounds!
				y1 += macroblockStepY;
				if (y1 >= pixelDataLength)
				{
					break;
				}
			}
		}
	}
}