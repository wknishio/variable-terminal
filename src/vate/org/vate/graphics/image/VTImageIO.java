package org.vate.graphics.image;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.vate.stream.endian.VTLittleEndianInputStream;
import org.vate.stream.endian.VTLittleEndianOutputStream;

public final class VTImageIO
{
	private static final int DCM_RED_MASK = 0x00ff0000;
	private static final int DCM_GREEN_MASK = 0x0000ff00;
	private static final int DCM_BLUE_MASK = 0x000000ff;
	private static final int DCM_ALPHA_MASK = 0xff000000;
	
	private static final int DCM_555_RED_MASK = 0x7C00; // 0111110000000000
	private static final int DCM_555_GRN_MASK = 0x03E0; // 0000001111100000
	private static final int DCM_555_BLU_MASK = 0x001F; // 0000000000011111
	
	private static final IndexColorModel byteIndexed216ColorModel = VTIndexedColorModel.create216ColorModel();
	// private static final IndexColorModel byteIndexed125ColorModel = VTIndexColorModel.create125ColorModel();
	private static final IndexColorModel byteIndexed64ColorModel = VTIndexedColorModel.create64ColorModel();
	private static final IndexColorModel byteIndexed32ColorModel = VTIndexedColorModel.create32ColorModel();
	private static final IndexColorModel byteIndexed16ColorModel = VTIndexedColorModel.create16ColorModel();
	private static final IndexColorModel byteIndexed8ColorModel = VTIndexedColorModel.create8ColorModel();
	private static final IndexColorModel byteIndexed27ColorModel = VTIndexedColorModel.create27ColorModel();

	
	//private static final IndexColorModel bytePacked4Bit16ColorModel = VTIndexedColorModel.createPacked4Bit16ColorModel();
	
	private static final DirectColorModel int24bitRGBColorModel = new DirectColorModel(24,
	DCM_RED_MASK, // Red
	DCM_GREEN_MASK, // Green
	DCM_BLUE_MASK, // Blue
	0x0 // Alpha
	);
	
	private static final DirectColorModel ushort15bitRGBColorModel = new DirectColorModel(15,
	DCM_555_RED_MASK,
	DCM_555_GRN_MASK,
	DCM_555_BLU_MASK);
	
	private static final DirectColorModel int32bitRGBColorModel = new DirectColorModel(32,
	DCM_RED_MASK, // Red
	DCM_GREEN_MASK, // Green
	DCM_BLUE_MASK, // Blue
	DCM_ALPHA_MASK // Alpha
	);
	
	private final VTLittleEndianInputStream littleEndianInputStream = new VTLittleEndianInputStream(null);
	private final VTLittleEndianOutputStream littleEndianOutputStream = new VTLittleEndianOutputStream(null);
	
	/* public Color getBackgroundColor(int type) { switch (type) { case
	 * BufferedImage.TYPE_BYTE_INDEXED: {
	 * } case BufferedImage.TYPE_USHORT_555_RGB: {
	 * } case BufferedImage.TYPE_INT_RGB: {
	 * } case BufferedImage.TYPE_INT_ARGB: {
	 * } } return null; } */
	
	public static final BufferedImage newImage(int x, int y, int width, int height, int type, int colors, DataBuffer recyclableBuffer)
	{
		// recyclableStorage.getRaster().getDataBuffer().get
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				//Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 0x77);
				//return image;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				//Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 86);
				clearBuffer(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * width));
				return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				//Arrays.fill(((DataBufferUShort) image.getRaster().getDataBuffer()).getData(), (short) 0x294A);
				clearBuffer(((DataBufferUShort) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * width));
				return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				//Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0x00555555);
				clearBuffer(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * width));
				return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				//Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0xFF555555);
				clearBuffer(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * width));
				return image;
			}
		}
		return null;
	}
	
	public final BufferedImage read(InputStream in, DataBuffer recyclableBuffer) throws IOException
	{
		littleEndianInputStream.setIntputStream(in);
		int type = littleEndianInputStream.readInt();
		int colors = littleEndianInputStream.readInt();
		int x = littleEndianInputStream.readInt();
		int y = littleEndianInputStream.readInt();
		int width = littleEndianInputStream.readInt();
		int height = littleEndianInputStream.readInt();
		
		// int depth = image.getColorModel().getPixelSize();
		// int readed = 0;
		// int total = 0;
		// int remaining = width * height;
		int size = width * height;
		
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				//size = size / 2;
				//byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				//for (int i = 0; i < size; i++)
				//{
					//decodeTwoPixelByte(littleEndianInputStream, data, i);
				//}
				//return image;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);;
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					decodePixelByte(littleEndianInputStream, data, position, width);
				}
				return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					decodePixelShort(littleEndianInputStream, data, position, width);
				}
				return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					decodePixelSubInt(littleEndianInputStream, data, position, width);
				}
				return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					decodePixelInt(littleEndianInputStream, data, position, width);
				}
				return image;
			}
		}
		return null;
	}
	
	public final void write(OutputStream out, BufferedImage image) throws IOException
	{
		littleEndianOutputStream.setOutputStream(out);
		int type = image.getType();
		int colors = 0;
		ColorModel colorModel = image.getColorModel();
		if (colorModel instanceof IndexColorModel)
		{
			colors = ((IndexColorModel) colorModel).getMapSize();
		}
		int x = image.getMinX();
		int y = image.getMinY();
		int width = image.getWidth();
		int height = image.getHeight();
		int size = width * height;
		// int total = 0;
		littleEndianOutputStream.writeInt(type);
		littleEndianOutputStream.writeInt(colors);
		littleEndianOutputStream.writeInt(x);
		littleEndianOutputStream.writeInt(y);
		littleEndianOutputStream.writeInt(width);
		littleEndianOutputStream.writeInt(height);
		// littleEndianOutputStream.flush();
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				//size = size / 2;
				//for (int i = 0; i < size; i++)
				//{
					//encodeTwoPixelByte(littleEndianOutputStream, data, i);
				//}
				//littleEndianOutputStream.flush();
				//break;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					encodePixelByte(littleEndianOutputStream, data, position, width);
				}
				littleEndianOutputStream.flush();
				break;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					encodePixelShort(littleEndianOutputStream, data, position, width);
				}
				break;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					encodePixelSubInt(littleEndianOutputStream, data, position, width);
				}
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int position = x + (y * width); position < size; position++)
				{
					encodePixelInt(littleEndianOutputStream, data, position, width);
				}
				break;
			}
		}
	}
	
	private static final WritableRaster buildRaster(int x, int y, int width, int height, int type, DataBuffer recyclableBuffer)
	{
		int nextSize = ((width + x) * (height + y));
		int neededSize = ((width + x) * (height + y));
		WritableRaster createdRaster = null;
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//nextSize = nextSize / 2;
				//neededSize = neededSize / 2;
				//if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				//{
					//createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, 4, null);
				//}
				//else
				//{
					//createdRaster = Raster.createPackedRaster(new DataBufferByte(nextSize), width, height, 4, null);
				//}
				//break;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createInterleavedRaster(recyclableBuffer, width, height, width, 1, new int[1], new Point(x, y));
					// Arrays.fill(((DataBufferByte)recyclableBuffer).getData(),
					// (byte) 0);
				}
				else
				{
					createdRaster = Raster.createInterleavedRaster(new DataBufferByte(nextSize), width, height, width, 1, new int[1], new Point(x, y));
				}
				break;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferUShort && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, ushort15bitRGBColorModel.getMasks(), new Point(x, y));
					// Arrays.fill(((DataBufferUShort)recyclableBuffer).getData(),
					// (short) 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferUShort(nextSize), width, height, width, ushort15bitRGBColorModel.getMasks(), new Point(x, y));
				}
				break;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, int24bitRGBColorModel.getMasks(), new Point(x, y));
					// Arrays.fill(((DataBufferInt)recyclableBuffer).getData(), 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, width, int24bitRGBColorModel.getMasks(), new Point(x, y));
				}
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, int32bitRGBColorModel.getMasks(), new Point(x, y));
					// Arrays.fill(((DataBufferInt)recyclableBuffer).getData(), 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, width, int32bitRGBColorModel.getMasks(), new Point(x, y));
				}
				break;
			}
		}
		return createdRaster;
	}
	
	private static final BufferedImage buildBufferedImage(int x, int y, int width, int height, int type, int colors, DataBuffer recyclableBuffer)
	{
		BufferedImage image = null;
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//image = new BufferedImage(bytePacked4Bit16ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				//break;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				if (colors == 216)
				{
					image = new BufferedImage(byteIndexed216ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 27)
				{
					image = new BufferedImage(byteIndexed27ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 16)
				{
					image = new BufferedImage(byteIndexed16ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 8)
				{
					image = new BufferedImage(byteIndexed8ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 32)
				{
					image = new BufferedImage(byteIndexed32ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				else
				{
					image = new BufferedImage(byteIndexed64ColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				}
				break;
				// return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				image = new BufferedImage(ushort15bitRGBColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				image = new BufferedImage(int24bitRGBColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				image = new BufferedImage(int32bitRGBColorModel, buildRaster(x, y, width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
		}
		return image;
	}
	
	public static final void clearBuffer(byte[] buffer, int type, int colors, int start)
	{
		//Arrays.fill(buffer, (byte) 0);
		if (colors == 216)
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 86);
		}
		else if (colors == 27)
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 13);
		}
		else if (colors == 16)
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 1);
		}
		else if (colors == 8)
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 0);
		}
		else if (colors == 32)
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 2);
		}
		else
		{
			Arrays.fill(buffer, start, buffer.length, (byte) 21);
		}
	}
	
	public static final void clearBuffer(short[] buffer, int type, int colors, int start)
	{
		//Arrays.fill(buffer, (short) 0);
		Arrays.fill(buffer, start, buffer.length, (short) 0x294A);
	}
	
	public static final void clearBuffer(int[] buffer, int type, int colors, int start)
	{
		//Arrays.fill(buffer, 0);
		if (type == BufferedImage.TYPE_INT_ARGB)
		{
			Arrays.fill(buffer, start, buffer.length, 0xFF555555);
		}
		else
		{
			Arrays.fill(buffer, start, buffer.length, 0x00555555);
		}
	}
	
	public static final void clearImage(BufferedImage image)
	{
		if (image == null)
		{
			return;
		}
		int type = image.getType();
		int colors = 0;
		int x = image.getMinX();
		int y = image.getMinY();
		ColorModel colorModel = image.getColorModel();
		if (colorModel instanceof IndexColorModel)
		{
			colors = ((IndexColorModel) colorModel).getMapSize();
		}
		
		switch (type)
		{
			//case BufferedImage.TYPE_BYTE_BINARY:
			//{
				//Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 0x77);
				//break;
			//}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				clearBuffer(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * image.getWidth()));
				break;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				clearBuffer(((DataBufferUShort) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * image.getWidth()));
				break;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				clearBuffer(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * image.getWidth()));
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				clearBuffer(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), type, colors, x + (y * image.getWidth()));
				break;
			}
		}
	}
	
	private static final void encodePixelByte(VTLittleEndianOutputStream out, byte[] pixelData, int position, int width) throws IOException
	{
		byte left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		out.write(pixelData[position] - Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	
	private static final void encodePixelShort(VTLittleEndianOutputStream out, short[] pixelData, int position, int width) throws IOException
	{
		short left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		out.writeShort(pixelData[position] - Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	
	private static final void encodePixelSubInt(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
	{
		int left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		out.writeSubInt(pixelData[position] - Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	
	private static final void encodePixelInt(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
	{
		int left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		out.writeInt(pixelData[position] - Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	
	//private static final void decodeTwoPixelByte(VTLittleEndianInputStream in, byte[] pixelData, int position) throws IOException
	//{
		//pixelData[position] = (byte) (in.readByte());
	//}
	
	private static final void decodePixelByte(VTLittleEndianInputStream in, byte[] pixelData, int position, int width) throws IOException
	{
		byte left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		pixelData[position] = (byte) (in.readByte() + Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	
	private static final void decodePixelShort(VTLittleEndianInputStream in, short[] pixelData, int position, int width) throws IOException
	{
		short left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		pixelData[position] = (short) ((in.readShort() + Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1))) & 0x00007FFF);
	}
	
	private static final void decodePixelSubInt(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
	{
		int left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		pixelData[position] = (in.readSubInt() + Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1))) & 0x00FFFFFF;
	}
	
	private static final void decodePixelInt(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
	{
		int left1, top1, diag1;
		diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
		top1 = position >= width ? pixelData[position - width] : 0;
		left1 = position > 0 ? pixelData[position - 1] : 0;
		pixelData[position] = (in.readInt() + Math.max(Math.min(left1, top1), Math.min(Math.max(left1, top1), left1 + top1 - diag1)));
	}
	/* private static final int to24bitInt(int value) { return -(value &
	 * 0x00800000) + (value & ~0x00800000); }
	 * private static final int from24bitInt(int value) { if ((value &
	 * 0x00800000) != 0) { value |= 0xFF000000; } else { value &= 0x00FFFFFF; }
	 * return value; } */
}