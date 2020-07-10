package org.vate.graphics.image;

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
	
	private static final int DCM_555_RED_MASK = 0x7C00;
	private static final int DCM_555_GRN_MASK = 0x03E0;
	private static final int DCM_555_BLU_MASK = 0x001F;
	
	private static final IndexColorModel byteIndexed216ColorModel = VTIndexedColorModel.create216ColorModel();
	// private static final IndexColorModel byteIndexed125ColorModel = VTIndexColorModel.create125ColorModel();
	private static final IndexColorModel byteIndexed64ColorModel = VTIndexedColorModel.create64ColorModel();
	private static final IndexColorModel byteIndexed27ColorModel = VTIndexedColorModel.create27ColorModel();
	private static final IndexColorModel byteIndexed16ColorModel = VTIndexedColorModel.create16ColorModel();
	
	private static final IndexColorModel bytePacked4Bit16ColorModel = VTIndexedColorModel.createPacked4Bit16ColorModel();
	
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
	
	public static final BufferedImage newImage(int width, int height, int type, int colors, DataBuffer recyclableBuffer)
	{
		// recyclableStorage.getRaster().getDataBuffer().get
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 0x77);
				return image;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				if (colors == 216)
				{
					BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 129);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 62);
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 86);
					return image;
				}
				else if (colors == 27)
				{
					BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 129);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 62);
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 13);
					return image;
				}
				else if (colors == 16)
				{
					BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 129);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 62);
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 1);
					return image;
				}
				else
				{
					BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 129);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 62);
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 21);
					return image;
				}
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				// Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).getData(),
				// (short) 0x4E73);
				// Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).getData(),
				// (short) 0x4210);
				Arrays.fill(((DataBufferUShort) image.getRaster().getDataBuffer()).getData(), (short) 0x294A);
				return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0x00999999);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0x00808080);
				Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0x00555555);
				return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0xFF999999);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0xFF808080);
				Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0xFF555555);
				return image;
			}
		}
		return null;
	}
	
	/* public BufferedImage createImage(InputStream in, DataBuffer
	 * recyclableBuffer) throws IOException {
	 * littleEndianInputStream.setIntputStream(in); int type =
	 * littleEndianInputStream.readInt(); int colors =
	 * littleEndianInputStream.readInt(); int width =
	 * littleEndianInputStream.readInt(); int height =
	 * littleEndianInputStream.readInt(); //int total = 0; //int remaining =
	 * width * height; //int size = width * height; switch (type) { case
	 * BufferedImage.TYPE_BYTE_INDEXED: { if (colors == 216) { BufferedImage
	 * image = buildBufferedImage(width, height, type, colors,
	 * recyclableBuffer);
	 * //Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData
	 * (), (byte) 129);
	 * //Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData
	 * (), (byte) 62);
	 * Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData()
	 * , (byte) 86); return image; } else if (colors == 27) { BufferedImage
	 * image = buildBufferedImage(width, height, type, colors,
	 * recyclableBuffer);
	 * //Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData
	 * (), (byte) 129);
	 * //Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData
	 * (), (byte) 62);
	 * Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData()
	 * , (byte) 13); return image; } else { BufferedImage image =
	 * buildBufferedImage(width, height, type, colors, recyclableBuffer);
	 * Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData()
	 * , (byte) 21);
	 * //Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData
	 * (), (byte) 13); return image; }
	 * } case BufferedImage.TYPE_USHORT_555_RGB: { BufferedImage image =
	 * buildBufferedImage(width, height, type, colors, recyclableBuffer);
	 * //Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).
	 * getData() , (short) 0x4E73);
	 * //Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).
	 * getData() , (short) 0x4210);
	 * Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).getData
	 * (), (short) 0x294A); return image; } case BufferedImage.TYPE_INT_RGB: {
	 * BufferedImage image = buildBufferedImage(width, height, type, colors,
	 * recyclableBuffer);
	 * //Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(
	 * ), 0x00999999);
	 * //Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(
	 * ), 0x00808080);
	 * Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
	 * 0x00555555); return image; } case BufferedImage.TYPE_INT_ARGB: {
	 * BufferedImage image = buildBufferedImage(width, height, type, colors,
	 * recyclableBuffer);
	 * //Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(
	 * ), 0xFF999999);
	 * //Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(
	 * ), 0xFF808080);
	 * Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
	 * 0xFF555555); return image; } } return null; } */
	
	// public final BufferedImage readImageData(InputStream in, BufferedImage
	// image)
	// throws IOException
	// {
	// littleEndianInputStream.setIntputStream(in);
	// int type = image.getType();
	// //int colors = image.getColorModel().getNumColorComponents();
	// int width = image.getWidth();
	// int height = image.getHeight();
	// //int readed = 0;
	// //int total = 0;
	// //int remaining = width * height;
	// int size = width * height;
	//
	// switch (type)
	// {
	// case BufferedImage.TYPE_BYTE_BINARY:
	// {
	// byte[] data =
	// ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
	// size = size / 2;
	// for (int i = 0; i < size;i++)
	// {
	// decodePixelByte(littleEndianInputStream, data, i, width);
	// }
	// return image;
	// }
	// case BufferedImage.TYPE_BYTE_INDEXED:
	// {
	// byte[] data =
	// ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
	// for (int i = 0; i < size;i++)
	// {
	// decodePixelByte(littleEndianInputStream, data, i, width);
	// }
	// return image;
	// }
	// case BufferedImage.TYPE_USHORT_555_RGB:
	// {
	// short[] data =
	// ((DataBufferUShort)image.getRaster().getDataBuffer()).getData();
	// for (int i = 0; i < size;i++)
	// {
	// decodePixelShort(littleEndianInputStream, data, i, width);
	// }
	// return image;
	// }
	// case BufferedImage.TYPE_INT_RGB:
	// {
	// int[] data =
	// ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	// for (int i = 0; i < size;i++)
	// {
	// decodePixelSubInt(littleEndianInputStream, data, i, width);
	// }
	// return image;
	// }
	// case BufferedImage.TYPE_INT_ARGB:
	// {
	// int[] data =
	// ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	// for (int i = 0; i < size;i++)
	// {
	// decodePixelInt(littleEndianInputStream, data, i, width);
	// }
	// return image;
	// }
	// }
	// return null;
	// }
	
	public final BufferedImage read(InputStream in, DataBuffer recyclableBuffer) throws IOException
	{
		littleEndianInputStream.setIntputStream(in);
		int type = littleEndianInputStream.readInt();
		int colors = littleEndianInputStream.readInt();
		int width = littleEndianInputStream.readInt();
		int height = littleEndianInputStream.readInt();
		// int depth = image.getColorModel().getPixelSize();
		// int readed = 0;
		// int total = 0;
		// int remaining = width * height;
		int size = width * height;
		
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				size = size / 2;
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					decodeTwoPixelByte(littleEndianInputStream, data, i);
				}
				return image;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				BufferedImage image;
				if (colors == 216)
				{
					image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				}
				else if (colors == 27)
				{
					image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				}
				else if (colors == 16)
				{
					image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				}
				else
				{
					image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				}
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					decodePixelByte(littleEndianInputStream, data, i, width);
				}
				return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					decodePixelShort(littleEndianInputStream, data, i, width);
				}
				return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					decodePixelSubInt(littleEndianInputStream, data, i, width);
				}
				return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				BufferedImage image = buildBufferedImage(width, height, type, colors, recyclableBuffer);
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					decodePixelInt(littleEndianInputStream, data, i, width);
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
		int width = image.getWidth();
		int height = image.getHeight();
		int size = width * height;
		// int total = 0;
		littleEndianOutputStream.writeInt(type);
		littleEndianOutputStream.writeInt(colors);
		littleEndianOutputStream.writeInt(width);
		littleEndianOutputStream.writeInt(height);
		// littleEndianOutputStream.flush();
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				size = size / 2;
				for (int i = 0; i < size; i++)
				{
					encodeTwoPixelByte(littleEndianOutputStream, data, i);
				}
				littleEndianOutputStream.flush();
				break;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					encodePixelByte(littleEndianOutputStream, data, i, width);
				}
				littleEndianOutputStream.flush();
				break;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					encodePixelShort(littleEndianOutputStream, data, i, width);
				}
				break;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					encodePixelSubInt(littleEndianOutputStream, data, i, width);
				}
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
				for (int i = 0; i < size; i++)
				{
					encodePixelInt(littleEndianOutputStream, data, i, width);
				}
				break;
			}
		}
	}
	
	/* public static BufferedImage createCopy(BufferedImage source, int
	 * dataType, boolean disposeSource) { int width = source.getWidth(); int
	 * height = source.getHeight(); int colors =
	 * source.getColorModel().getNumColorComponents(); switch (dataType) { case
	 * DataBuffer.TYPE_BYTE: { BufferedImage copy = buildBufferedImage(width,
	 * height, type, colors, recyclableBuffer);
	 * //copy.getRaster().setDataElements(0, 0, width, height,
	 * source.getRaster().getDataElements(0, 0, width, height, null));
	 * copy.setRGB(0, 0, width, height, source.getRGB(0, 0, width, height, null,
	 * 0, width), 0, width); if (disposeSource) { source.flush(); source = null;
	 * } return copy; } case DataBuffer.TYPE_USHORT: { BufferedImage copy =
	 * buildBufferedImage(width, height, type, colors, recyclableBuffer);
	 * //copy.getRaster().setDataElements(0, 0, width, height,
	 * source.getRaster().getDataElements(0, 0, width, height, null));
	 * copy.setRGB(0, 0, width, height, source.getRGB(0, 0, width, height, null,
	 * 0, width), 0, width); if (disposeSource) { source.flush(); source = null;
	 * } return copy; } case DataBuffer.TYPE_INT: { BufferedImage copy =
	 * buildBufferedImage(width, height, type, colors, recyclableBuffer);
	 * //copy.getRaster().setDataElements(0, 0, width, height,
	 * source.getRaster().getDataElements(0, 0, width, height, null));
	 * copy.setRGB(0, 0, width, height, source.getRGB(0, 0, width, height, null,
	 * 0, width), 0, width); if (disposeSource) { source.flush(); source = null;
	 * } return copy; } } return null; } */
	
	private static final WritableRaster buildRaster(int width, int height, int type, DataBuffer recyclableBuffer)
	{
		int nextSize = ((width + 1) * (height + 1));
		int neededSize = ((width + 1) * (height + 1));
		WritableRaster createdRaster = null;
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				nextSize = nextSize / 2;
				neededSize = neededSize / 2;
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, 4, null);
					// Arrays.fill(((DataBufferByte)recyclableBuffer).getData(),
					// (byte) 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferByte(nextSize), width, height, 4, null);
				}
				break;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createInterleavedRaster(recyclableBuffer, width, height, width, 1, new int[1], null);
					// Arrays.fill(((DataBufferByte)recyclableBuffer).getData(),
					// (byte) 0);
				}
				else
				{
					createdRaster = Raster.createInterleavedRaster(new DataBufferByte(nextSize), width, height, width, 1, new int[1], null);
				}
				break;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferUShort && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, ushort15bitRGBColorModel.getMasks(), null);
					// Arrays.fill(((DataBufferUShort)recyclableBuffer).getData(),
					// (short) 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferUShort(nextSize), width, height, width, ushort15bitRGBColorModel.getMasks(), null);
				}
				break;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, int24bitRGBColorModel.getMasks(), null);
					// Arrays.fill(((DataBufferInt)recyclableBuffer).getData(), 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, width, int24bitRGBColorModel.getMasks(), null);
				}
				break;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
				{
					createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, width, int32bitRGBColorModel.getMasks(), null);
					// Arrays.fill(((DataBufferInt)recyclableBuffer).getData(), 0);
				}
				else
				{
					createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, width, int32bitRGBColorModel.getMasks(), null);
				}
				break;
			}
		}
		return createdRaster;
	}
	
	private static final BufferedImage buildBufferedImage(int width, int height, int type, int colors, DataBuffer recyclableBuffer)
	{
		BufferedImage image = null;
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				image = new BufferedImage(bytePacked4Bit16ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				break;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				if (colors == 216)
				{
					image = new BufferedImage(byteIndexed216ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 27)
				{
					image = new BufferedImage(byteIndexed27ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				}
				else if (colors == 16)
				{
					image = new BufferedImage(byteIndexed16ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				}
				else
				{
					image = new BufferedImage(byteIndexed64ColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				}
				break;
				// return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				image = new BufferedImage(ushort15bitRGBColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				image = new BufferedImage(int24bitRGBColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				image = new BufferedImage(int32bitRGBColorModel, buildRaster(width, height, type, recyclableBuffer), false, null);
				break;
				// return image;
			}
		}
		return image;
	}
	
	public static final void clearBuffer(byte[] buffer, int type, int colors)
	{
		if (colors == 216)
		{
			Arrays.fill(buffer, (byte) 86);
		}
		else if (colors == 27)
		{
			Arrays.fill(buffer, (byte) 13);
		}
		else if (colors == 16)
		{
			Arrays.fill(buffer, (byte) 1);
		}
		else
		{
			Arrays.fill(buffer, (byte) 21);
		}
	}
	
	public static final void clearBuffer(short[] buffer, int type, int colors)
	{
		Arrays.fill(buffer, (short) 0x294A);
	}
	
	public static final void clearBuffer(int[] buffer, int type, int colors)
	{
		if (type == BufferedImage.TYPE_INT_ARGB)
		{
			Arrays.fill(buffer, 0xFF555555);
		}
		else
		{
			Arrays.fill(buffer, 0x00555555);
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
		ColorModel colorModel = image.getColorModel();
		if (colorModel instanceof IndexColorModel)
		{
			colors = ((IndexColorModel) colorModel).getMapSize();
		}
		
		switch (type)
		{
			case BufferedImage.TYPE_BYTE_BINARY:
			{
				Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 0x77);
				break;
			}
			case BufferedImage.TYPE_BYTE_INDEXED:
			{
				// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
				// (byte) 129);
				// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
				// (byte) 62);
				if (colors == 216)
				{
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 86);
				}
				else if (colors == 27)
				{
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 13);
				}
				else if (colors == 16)
				{
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 1);
				}
				else
				{
					Arrays.fill(((DataBufferByte) image.getRaster().getDataBuffer()).getData(), (byte) 21);
					// Arrays.fill(((DataBufferByte)image.getRaster().getDataBuffer()).getData(),
					// (byte) 13);
				}
				break;
				// return image;
			}
			case BufferedImage.TYPE_USHORT_555_RGB:
			{
				// Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).getData(),
				// (short) 0x4E73);
				// Arrays.fill(((DataBufferUShort)image.getRaster().getDataBuffer()).getData(),
				// (short) 0x4210);
				Arrays.fill(((DataBufferUShort) image.getRaster().getDataBuffer()).getData(), (short) 0x294A);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_RGB:
			{
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0x00999999);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0x00808080);
				Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0x00555555);
				break;
				// return image;
			}
			case BufferedImage.TYPE_INT_ARGB:
			{
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0xFF999999);
				// Arrays.fill(((DataBufferInt)image.getRaster().getDataBuffer()).getData(),
				// 0xFF808080);
				Arrays.fill(((DataBufferInt) image.getRaster().getDataBuffer()).getData(), 0xFF555555);
				break;
				// return image;
			}
		}
	}
	
	private static final void encodeTwoPixelByte(VTLittleEndianOutputStream out, byte[] pixelData, int position) throws IOException
	{
		out.write((byte) (pixelData[position]));
		// out.write((byte) (pixelData[position]));
	}

	
	private static final void encodePixelByte(VTLittleEndianOutputStream out, byte[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] & 0xff : 0;
		top = position >= width ? pixelData[position - width] & 0xff : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] & 0xff : 0;
		out.write((pixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))));
		// out.write((byte) (pixelData[position]));
	}
	
	private static final void encodePixelShort(VTLittleEndianOutputStream out, short[] pixelData, int position, int width) throws IOException
	{
		short left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		out.writeShort((short) (pixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))));
		// out.writeShort((short) (pixelData[position]));
	}
	
	private static final void encodePixelSubInt(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		// pixelData[position] &= 0x00FFFFFF;
		out.writeSubInt((pixelData[position] - ((Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))) & 0x00FFFFFF)));
	}
	
	private static final void encodePixelInt(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		out.writeInt((pixelData[position] - Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))));
		// out.writeInt((pixelData[position]));
	}
	
	private static final void decodeTwoPixelByte(VTLittleEndianInputStream in, byte[] pixelData, int position) throws IOException
	{
		pixelData[position] = (byte) (in.readByte());
	}
	
	private static final void decodePixelByte(VTLittleEndianInputStream in, byte[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] & 0xff : 0;
		top = position >= width ? pixelData[position - width] & 0xff : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] & 0xff : 0;
		pixelData[position] = (byte) (in.readByte() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
		// pixelData[position] = (byte) (in.readByte());
	}
	
	private static final void decodePixelShort(VTLittleEndianInputStream in, short[] pixelData, int position, int width) throws IOException
	{
		short left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		pixelData[position] = (short) (in.readShort() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
		// pixelData[position] = (short) (in.readShort());
	}
	
	private static final void decodePixelSubInt(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		pixelData[position] = (in.readSubInt() + ((Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag))) & 0x00FFFFFF)) & 0x00FFFFFF;
	}
	
	private static final void decodePixelInt(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
	{
		int left, top, diag;
//		left = pixelData[position - 1];
//		top = pixelData[position - width];
//		diag = pixelData[position - 1 - width];
		left = position > 0 ? pixelData[position - 1] : 0;
		top = position >= width ? pixelData[position - width] : 0;
		diag = position - 1 >= width ? pixelData[position - 1 - width] : 0;
		pixelData[position] = (in.readInt() + Math.max(Math.min(left, top), Math.min(Math.max(left, top), left + top - diag)));
		// pixelData[position] = (in.readInt());
	}
	/* private static final int to24bitInt(int value) { return -(value &
	 * 0x00800000) + (value & ~0x00800000); }
	 * private static final int from24bitInt(int value) { if ((value &
	 * 0x00800000) != 0) { value |= 0xFF000000; } else { value &= 0x00FFFFFF; }
	 * return value; } */
}