package org.vash.vate.graphics.image;

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

import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public final class VTImageIO
{
  private static final int DCM_AAA_RED_MASK = 0x3FF00000; // 00111111111100000000000000000000
  private static final int DCM_AAA_GRN_MASK = 0x000FFC00; // 00000000000011111111110000000000
  private static final int DCM_AAA_BLU_MASK = 0x000003FF; // 00000000000000000000001111111111
  
  private static final int DCM_999_RED_MASK = 0x07FC0000; // 00000111111111000000000000000000
  private static final int DCM_999_GRN_MASK = 0x0003FE00; // 00000000000000111111111000000000
  private static final int DCM_999_BLU_MASK = 0x000001FF; // 00000000000000000000000111111111
  
  private static final int DCM_888_ALP_MASK = 0xFF000000; // 11111111000000000000000000000000
  private static final int DCM_888_RED_MASK = 0x00FF0000; // 00000000111111110000000000000000
  private static final int DCM_888_GRN_MASK = 0x0000FF00; // 00000000000000001111111100000000
  private static final int DCM_888_BLU_MASK = 0x000000FF; // 00000000000000000000000011111111
  
  private static final int DCM_777_RED_MASK = 0x1FC000; // 000111111100000000000000
  private static final int DCM_777_GRN_MASK = 0x003F80; // 000000000011111110000000
  private static final int DCM_777_BLU_MASK = 0x00007F; // 000000000000000001111111
  
  private static final int DCM_666_RED_MASK = 0x03F000; // 000000111111000000000000
  private static final int DCM_666_GRN_MASK = 0x000FC0; // 000000000000111111000000
  private static final int DCM_666_BLU_MASK = 0x00003F; // 000000000000000000111111
  
  private static final int DCM_555_RED_MASK = 0x7C00; // 0111110000000000
  private static final int DCM_555_GRN_MASK = 0x03E0; // 0000001111100000
  private static final int DCM_555_BLU_MASK = 0x001F; // 0000000000011111
  
  private static final int DCM_444_RED_MASK = 0x0F00; // 0000111100000000
  private static final int DCM_444_GRN_MASK = 0x00F0; // 0000000011110000
  private static final int DCM_444_BLU_MASK = 0x000F; // 0000000000001111
  
  private static final int DCM_333_RED_MASK = 0x01C0; // 0000000111000000
  private static final int DCM_333_GRN_MASK = 0x0038; // 0000000000111000
  private static final int DCM_333_BLU_MASK = 0x0007; // 0000000000000111
  
  private static final int DCM_222_RED_MASK = 0x0030; // 0000000000110000
  private static final int DCM_222_GRN_MASK = 0x000C; // 0000000000001100
  private static final int DCM_222_BLU_MASK = 0x0003; // 0000000000000011
  
  private static final int DCM_111_RED_MASK = 0x0004; // 0000000000000100
  private static final int DCM_111_GRN_MASK = 0x0002; // 0000000000000010
  private static final int DCM_111_BLU_MASK = 0x0001; // 0000000000000001
  
  // public static final int TYPE_USHORT_444_RGB =
  // BufferedImage.TYPE_USHORT_555_RGB << 1;
  
  private static final IndexColorModel byteIndexed216ColorModel = VTIndexedColorModel.create216ColorModel();
  private static final IndexColorModel byteIndexed125ColorModel = VTIndexedColorModel.create125ColorModel();
  private static final IndexColorModel byteIndexed64ColorModel = VTIndexedColorModel.create64ColorModel();
  //private static final IndexColorModel byteIndexed8ColorModel = VTIndexedColorModel.create8ColorModel();
  private static final IndexColorModel byteIndexed27ColorModel = VTIndexedColorModel.create27ColorModel();
  
  private static final IndexColorModel byteIndexed8ColorModelGrayscale = VTIndexedColorModel.create8ColorModelGrayscale();
  private static final IndexColorModel byteIndexed16ColorModelGrayscale = VTIndexedColorModel.create16ColorModelGrayscale();
  //private static final IndexColorModel byteIndexed4ColorModelGrayscale = VTIndexedColorModel.create4ColorModelGrayscale();
  
  // private static final IndexColorModel bytePacked4Bit16ColorModel =
  // VTIndexedColorModel.createPacked4Bit16ColorModel();
  
  private static final DirectColorModel int24bitRGBColorModel = new DirectColorModel(24, DCM_888_RED_MASK, DCM_888_GRN_MASK, DCM_888_BLU_MASK, 0);
  
  private static final DirectColorModel int21bitRGBColorModel = new DirectColorModel(21, DCM_777_RED_MASK, DCM_777_GRN_MASK, DCM_777_BLU_MASK, 0);
  
  private static final DirectColorModel int18bitRGBColorModel = new DirectColorModel(18, DCM_666_RED_MASK, DCM_666_GRN_MASK, DCM_666_BLU_MASK, 0);
  
  private static final DirectColorModel ushort15bitRGBColorModel = new DirectColorModel(15, DCM_555_RED_MASK, DCM_555_GRN_MASK, DCM_555_BLU_MASK, 0);
  
  private static final DirectColorModel ushort12bitRGBColorModel = new DirectColorModel(12, DCM_444_RED_MASK, DCM_444_GRN_MASK, DCM_444_BLU_MASK, 0);
  
  private static final DirectColorModel ushort9bitRGBColorModel = new DirectColorModel(9, DCM_333_RED_MASK, DCM_333_GRN_MASK, DCM_333_BLU_MASK, 0);
  
  private static final DirectColorModel byte3bitRGBColorModel = new DirectColorModel(3, DCM_111_RED_MASK, DCM_111_GRN_MASK, DCM_111_BLU_MASK, 0);
  
  private static final DirectColorModel byte6bitRGBColorModel = new DirectColorModel(6, DCM_222_RED_MASK, DCM_222_GRN_MASK, DCM_222_BLU_MASK, 0);
  
  private static final DirectColorModel int32bitARGBColorModel = new DirectColorModel(32, DCM_888_RED_MASK, DCM_888_GRN_MASK, DCM_888_BLU_MASK, DCM_888_ALP_MASK);
  
  private static final DirectColorModel int27bitRGBColorModel = new DirectColorModel(27, DCM_999_RED_MASK, DCM_999_GRN_MASK, DCM_999_BLU_MASK, 0);
  
  private static final DirectColorModel int30bitRGBColorModel = new DirectColorModel(30, DCM_AAA_RED_MASK, DCM_AAA_GRN_MASK, DCM_AAA_BLU_MASK, 0);
  
  public static final BufferedImage createImage(int x, int y, int width, int height, int type, int colors, DataBuffer recyclableBuffer)
  {
    // recyclableStorage.getRaster().getDataBuffer().get
    switch (type)
    {
      // case BufferedImage.TYPE_BYTE_BINARY:
      // {
      // BufferedImage image = buildBufferedImage(width, height, type, colors,
      // recyclableBuffer);
      // Arrays.fill(((DataBufferByte)
      // image.getRaster().getDataBuffer()).getData(),
      // (byte) 0x77);
      // return image;
      // }
      case BufferedImage.TYPE_CUSTOM:
      {
        if (colors == 1073741824)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 134217728)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 2097152)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 262144)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 4096)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 512)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 64)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
        if (colors == 8)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
          return image;
        }
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        return image;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        return image;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        return image;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        return image;
      }
    }
    return null;
  }
  
  @SuppressWarnings("all")
  public static final BufferedImage readImage(InputStream in, DataBuffer recyclableBuffer) throws IOException
  {
    VTLittleEndianInputStream littleEndianInputStream = new VTLittleEndianInputStream(in);
    // littleEndianInputStream.setIntputStream(in);
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
      // case BufferedImage.TYPE_BYTE_BINARY:
      // {
      // BufferedImage image = buildBufferedImage(width, height, type, colors,
      // recyclableBuffer);
      // size = size / 2;
      // byte[] data = ((DataBufferByte)
      // image.getRaster().getDataBuffer()).getData();
      // for (int i = 0; i < size; i++)
      // {
      // decodeTwoPixelByte(littleEndianInputStream, data, i);
      // }
      // return image;
      // }
      case BufferedImage.TYPE_CUSTOM:
      {
        if (colors == 1073741824)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel30(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 134217728)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel27(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 2097152)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel24(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 262144)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel24(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 4096)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel15(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 512)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel15(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 64)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel8(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        if (colors == 8)
        {
          BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
          byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            decodePixel8(littleEndianInputStream, data, position, width);
          }
          return image;
        }
        break;
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          decodePixel8(littleEndianInputStream, data, position, width);
        }
        return image;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          decodePixel15(littleEndianInputStream, data, position, width);
        }
        return image;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          decodePixel24(littleEndianInputStream, data, position, width);
        }
        return image;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        BufferedImage image = buildBufferedImage(x, y, width, height, type, colors, recyclableBuffer);
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          decodePixel32(littleEndianInputStream, data, position, width);
        }
        return image;
      }
    }
    return null;
  }
  
  @SuppressWarnings("all")
  public static final void writeImage(OutputStream out, BufferedImage image) throws IOException
  {
    VTLittleEndianOutputStream littleEndianOutputStream = new VTLittleEndianOutputStream(out);
    // littleEndianOutputStream.setOutputStream(out);
    int type = image.getType();
    int colors = 0;
    ColorModel colorModel = image.getColorModel();
    if (colorModel instanceof IndexColorModel)
    {
      colors = ((IndexColorModel) colorModel).getMapSize();
    }
    if (colorModel instanceof DirectColorModel)
    {
      colors = 1 << ((DirectColorModel) colorModel).getPixelSize();
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
      // case BufferedImage.TYPE_BYTE_BINARY:
      // {
      // byte[] data = ((DataBufferByte)
      // image.getRaster().getDataBuffer()).getData();
      // size = size / 2;
      // for (int i = 0; i < size; i++)
      // {
      // encodeTwoPixelByte(littleEndianOutputStream, data, i);
      // }
      // littleEndianOutputStream.flush();
      // break;
      // }
      case BufferedImage.TYPE_CUSTOM:
      {
        if (colors == 1073741824)
        {
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel30(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 134217728)
        {
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel27(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 2097152)
        {
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel24(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 262144)
        {
          int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel24(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 4096)
        {
          short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel15(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 512)
        {
          short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel15(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 64)
        {
          byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel8(littleEndianOutputStream, data, position, width);
          }
        }
        if (colors == 8)
        {
          byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
          for (int position = x + (y * width); position < size; position++)
          {
            encodePixel8(littleEndianOutputStream, data, position, width);
          }
        }
        break;
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          encodePixel8(littleEndianOutputStream, data, position, width);
        }
        break;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        short[] data = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          encodePixel15(littleEndianOutputStream, data, position, width);
        }
        break;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          encodePixel24(littleEndianOutputStream, data, position, width);
        }
        break;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int position = x + (y * width); position < size; position++)
        {
          encodePixel32(littleEndianOutputStream, data, position, width);
        }
        break;
      }
    }
  }
  
  private static final WritableRaster buildRaster(int x, int y, int width, int height, int type, int colors, DataBuffer recyclableBuffer)
  {
    int parentX = 0;
    int parentY = 0;
    if (x > 0 || y > 0)
    {
      parentX = x;
      parentY = y;
      width = width + x;
      height = height + y;
      x = 0;
      y = 0;
    }
    int stride = width;
    int nextSize = ((width + x) * (height + y));
    int neededSize = ((width + x) * (height + y));
    
    WritableRaster createdRaster = null;
    switch (type)
    {
      // case BufferedImage.TYPE_BYTE_BINARY:
      // {
      // nextSize = nextSize / 2;
      // neededSize = neededSize / 2;
      // if (recyclableBuffer != null && recyclableBuffer instanceof
      // DataBufferByte &&
      // recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize()
      // <=
      // neededSize * 4)
      // {
      // createdRaster = Raster.createPackedRaster(recyclableBuffer, width,
      // height, 4,
      // null);
      // }
      // else
      // {
      // createdRaster = Raster.createPackedRaster(new DataBufferByte(nextSize),
      // width, height, 4, null);
      // }
      // break;
      // }
      case BufferedImage.TYPE_CUSTOM:
      {
        if (colors == 1073741824)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int30bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int30bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 134217728)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int27bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int27bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 2097152)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int21bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int21bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 262144)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int18bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int18bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 4096)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferUShort && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, ushort12bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferUShort(nextSize), width, height, stride, ushort12bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 512)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferUShort && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, ushort9bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferUShort(nextSize), width, height, stride, ushort9bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 64)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, byte6bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferByte(nextSize), width, height, stride, byte6bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        if (colors == 8)
        {
          if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
          {
            createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, byte3bitRGBColorModel.getMasks(), new Point(x, y));
          }
          else
          {
            createdRaster = Raster.createPackedRaster(new DataBufferByte(nextSize), width, height, stride, byte3bitRGBColorModel.getMasks(), new Point(x, y));
          }
        }
        break;
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferByte && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
        {
          createdRaster = Raster.createInterleavedRaster(recyclableBuffer, width, height, stride, 1, new int[1], new Point(x, y));
        }
        else
        {
          createdRaster = Raster.createInterleavedRaster(new DataBufferByte(nextSize), width, height, stride, 1, new int[1], new Point(x, y));
        }
        break;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferUShort && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
        {
          createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, ushort15bitRGBColorModel.getMasks(), new Point(x, y));
        }
        else
        {
          createdRaster = Raster.createPackedRaster(new DataBufferUShort(nextSize), width, height, stride, ushort15bitRGBColorModel.getMasks(), new Point(x, y));
        }
        break;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
        {
          createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int24bitRGBColorModel.getMasks(), new Point(x, y));
        }
        else
        {
          createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int24bitRGBColorModel.getMasks(), new Point(x, y));
        }
        break;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        if (recyclableBuffer != null && recyclableBuffer instanceof DataBufferInt && recyclableBuffer.getSize() >= neededSize && recyclableBuffer.getSize() <= neededSize * 4)
        {
          createdRaster = Raster.createPackedRaster(recyclableBuffer, width, height, stride, int32bitARGBColorModel.getMasks(), new Point(x, y));
        }
        else
        {
          createdRaster = Raster.createPackedRaster(new DataBufferInt(nextSize), width, height, stride, int32bitARGBColorModel.getMasks(), new Point(x, y));
        }
        break;
      }
    }
    if (parentX > 0 || parentY > 0)
    {
      createdRaster = createdRaster.createWritableChild(parentX, parentY, width - parentX, height - parentY, 0, 0, null);
      // image = image.getSubimage(subImageX, subImageY, width, height);
    }
    return createdRaster;
  }
  
  private static final BufferedImage buildBufferedImage(int x, int y, int width, int height, int type, int colors, DataBuffer recyclableBuffer)
  {
    BufferedImage image = null;
    switch (type)
    {
      // case BufferedImage.TYPE_BYTE_BINARY:
      // {
      // image = new BufferedImage(bytePacked4Bit16ColorModel,
      // buildRaster(width,
      // height, type, recyclableBuffer), false, null);
      // break;
      // }
      case BufferedImage.TYPE_CUSTOM:
      {
        if (colors == 1073741824)
        {
          image = new BufferedImage(int30bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 134217728)
        {
          image = new BufferedImage(int27bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 2097152)
        {
          image = new BufferedImage(int21bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 262144)
        {
          image = new BufferedImage(int18bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 4096)
        {
          image = new BufferedImage(ushort12bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 512)
        {
          image = new BufferedImage(ushort9bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 64)
        {
          image = new BufferedImage(byte6bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        if (colors == 8)
        {
          image = new BufferedImage(byte3bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        break;
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        if (colors == 216)
        {
          image = new BufferedImage(byteIndexed216ColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        else if (colors == 125)
        {
          image = new BufferedImage(byteIndexed125ColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        else if (colors == 27)
        {
          image = new BufferedImage(byteIndexed27ColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        else if (colors == 16)
        {
          image = new BufferedImage(byteIndexed16ColorModelGrayscale, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        else if (colors == 8)
        {
          image = new BufferedImage(byteIndexed8ColorModelGrayscale, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
//        else if (colors == 32)
//        {
//          image = new BufferedImage(byteIndexed32ColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
//        }
        else
        {
          image = new BufferedImage(byteIndexed64ColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        }
        break;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        image = new BufferedImage(ushort15bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        break;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        image = new BufferedImage(int24bitRGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        break;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        image = new BufferedImage(int32bitARGBColorModel, buildRaster(x, y, width, height, type, colors, recyclableBuffer), false, null);
        break;
      }
    }
    return image;
  }
  
  public static final void clearBufferWhite(byte[] buffer, int type, int colors, int start)
  {
    if (colors == 216)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 215);
    }
    else if (colors == 125)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 124);
    }
    else if (colors == 27)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 26);
    }
    else if (colors == 16)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 15);
    }
    else if (colors == 8)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 7);
    }
    else if (colors == 32)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 31);
    }
    else
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 63);
    }
  }
  
  public static final void clearBufferWhite(short[] buffer, int type, int colors, int start)
  {
    if (colors == 4096)
    {
      Arrays.fill(buffer, start, buffer.length, (short) 4095);
    }
    else if (colors == 512)
    {
      Arrays.fill(buffer, start, buffer.length, (short) 511);
    }
    else
    {
      Arrays.fill(buffer, start, buffer.length, (short) 32767);
    }
  }
  
  public static final void clearBufferWhite(int[] buffer, int type, int colors, int start)
  {
    if (type == BufferedImage.TYPE_INT_ARGB)
    {
      Arrays.fill(buffer, start, buffer.length, 0xFFFFFFFF);
    }
    else
    {
      if (colors == 16777216)
      {
        Arrays.fill(buffer, start, buffer.length, 0x00FFFFFF);
      }
      if (colors == 1073741824)
      {
        Arrays.fill(buffer, start, buffer.length, 0x3FFFFFFF);
      }
      if (colors == 134217728)
      {
        Arrays.fill(buffer, start, buffer.length, 0x07FFFFFF);
      }
      if (colors == 2097152)
      {
        Arrays.fill(buffer, start, buffer.length, 0x001FFFFF);
      }
      if (colors == 262144)
      {
        Arrays.fill(buffer, start, buffer.length, 0x0003FFFF);
      }
    }
  }
  
  public static final void clearBufferGray(byte[] buffer, int type, int colors, int start)
  {
    if (colors == 216)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 86);
    }
    else if (colors == 125)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 62);
    }
    else if (colors == 27)
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 13);
    }
    else if (colors == 16)
    {
      //Arrays.fill(buffer, start, buffer.length, (byte) 1);
      Arrays.fill(buffer, start, buffer.length, (byte) 6);
    }
    else if (colors == 8)
    {
      //Arrays.fill(buffer, start, buffer.length, (byte) 0);
      Arrays.fill(buffer, start, buffer.length, (byte) 3);
    }
    else if (colors == 32)
    {
      //Arrays.fill(buffer, start, buffer.length, (byte) 2);
      Arrays.fill(buffer, start, buffer.length, (byte) 14);
    }
    else
    {
      Arrays.fill(buffer, start, buffer.length, (byte) 21);
    }
  }
  
  public static final void clearBufferGray(short[] buffer, int type, int colors, int start)
  {
    if (colors == 4096)
    {
      Arrays.fill(buffer, start, buffer.length, (short) 0x0555);
    }
    else if (colors == 512)
    {
      Arrays.fill(buffer, start, buffer.length, (short) 0xDB);
    }
    else
    {
      Arrays.fill(buffer, start, buffer.length, (short) 0x294A);
    }
  }
  
  public static final void clearBufferGray(int[] buffer, int type, int colors, int start)
  {
    if (type == BufferedImage.TYPE_INT_ARGB)
    {
      Arrays.fill(buffer, start, buffer.length, 0xFF555555);
    }
    else
    {
      if (colors == 16777216)
      {
        Arrays.fill(buffer, start, buffer.length, 0x00555555);
      }
      if (colors == 1073741824)
      {
        Arrays.fill(buffer, start, buffer.length, 0x15555555);
      }
      if (colors == 134217728)
      {
        Arrays.fill(buffer, start, buffer.length, 0x02A954AA);
      }
      if (colors == 2097152)
      {
        Arrays.fill(buffer, start, buffer.length, 0x0A952A);
      }
      if (colors == 262144)
      {
        Arrays.fill(buffer, start, buffer.length, 0x15555);
      }
    }
  }
  
  public static final void clearBufferBlack(byte[] buffer, int type, int colors, int start)
  {
    Arrays.fill(buffer, start, buffer.length, (byte) 0);
  }
  
  public static final void clearBufferBlack(short[] buffer, int type, int colors, int start)
  {
    Arrays.fill(buffer, start, buffer.length, (short) 0);
  }
  
  public static final void clearBufferBlack(int[] buffer, int type, int colors, int start)
  {
    Arrays.fill(buffer, start, buffer.length, (int) 0);
  }
  
  public static final void clearBufferWhite(DataBuffer buffer, int type, int colors, int start)
  {
    if (buffer instanceof DataBufferByte)
    {
      clearBufferWhite(((DataBufferByte) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferUShort)
    {
      clearBufferWhite(((DataBufferUShort) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferInt)
    {
      clearBufferWhite(((DataBufferInt) buffer).getData(), type, colors, start);
    }
  }
  
  public static final void clearBufferGray(DataBuffer buffer, int type, int colors, int start)
  {
    if (buffer instanceof DataBufferByte)
    {
      clearBufferGray(((DataBufferByte) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferUShort)
    {
      clearBufferGray(((DataBufferUShort) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferInt)
    {
      clearBufferGray(((DataBufferInt) buffer).getData(), type, colors, start);
    }
  }
  
  public static final void clearBufferBlack(DataBuffer buffer, int type, int colors, int start)
  {
    if (buffer instanceof DataBufferByte)
    {
      clearBufferBlack(((DataBufferByte) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferUShort)
    {
      clearBufferBlack(((DataBufferUShort) buffer).getData(), type, colors, start);
    }
    if (buffer instanceof DataBufferInt)
    {
      clearBufferBlack(((DataBufferInt) buffer).getData(), type, colors, start);
    }
  }
  
  public static final void clearBuffer(DataBuffer buffer, int type, int colors, int start)
  {
    clearBufferGray(buffer, type, colors, start);
  }
  
  public static final void clearBuffer(byte[] buffer, int type, int colors, int start)
  {
    clearBufferGray(buffer, type, colors, start);
  }
  
  public static final void clearBuffer(short[] buffer, int type, int colors, int start)
  {
    clearBufferGray(buffer, type, colors, start);
  }
  
  public static final void clearBuffer(int[] buffer, int type, int colors, int start)
  {
    clearBufferGray(buffer, type, colors, start);
  }
  
  public static final void clearImage(BufferedImage image)
  {
    if (image == null)
    {
      return;
    }
    int type = image.getType();
    int colors = 0;
    // int x = image.getMinX();
    // int y = image.getMinY();
    ColorModel colorModel = image.getColorModel();
    if (colorModel instanceof IndexColorModel)
    {
      colors = ((IndexColorModel) colorModel).getMapSize();
    }
    if (colorModel instanceof DirectColorModel)
    {
      colors = 1 << ((DirectColorModel) colorModel).getPixelSize();
    }
    
    switch (type)
    {
      case BufferedImage.TYPE_CUSTOM:
      {
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        break;
      }
      case BufferedImage.TYPE_BYTE_INDEXED:
      {
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        break;
      }
      case BufferedImage.TYPE_USHORT_555_RGB:
      {
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        break;
      }
      case BufferedImage.TYPE_INT_RGB:
      {
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        break;
      }
      case BufferedImage.TYPE_INT_ARGB:
      {
        clearBuffer(image.getRaster().getDataBuffer(), type, colors, 0);
        break;
      }
    }
  }
  
  private static final void encodePixel8(VTLittleEndianOutputStream out, byte[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.write(pixelData[position] ^ (pred1));
  }
  
  private static final void encodePixel15(VTLittleEndianOutputStream out, short[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.writeShort(pixelData[position] ^ (pred1));
  }
  
  private static final void encodePixel24(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.writeSubInt(pixelData[position] ^ (int) ((pred1) /* & 0x00FFFFFF */));
  }
  
  private static final void encodePixel32(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
  {
    long left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.writeInt(pixelData[position] ^ (int) (pred1));
  }
  
  private static final void decodePixel8(VTLittleEndianInputStream in, byte[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = (byte) ((in.read() ^ (pred1)) /* & 0xFF */);
  }
  
  private static final void decodePixel15(VTLittleEndianInputStream in, short[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = (short) ((in.readShort() ^ (pred1)) /* & 0x7FFF */);
  }
  
  private static final void decodePixel24(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = ((in.readSubInt() ^ (int) ((pred1) /* & 0x00FFFFFF */)));
  }
  
  private static final void decodePixel32(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
  {
    long left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = (in.readInt() ^ (int) (pred1));
  }
  
  private static final void encodePixel27(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.writeInt(pixelData[position] ^ (int) (pred1));
  }
  
  private static final void decodePixel27(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = (in.readInt() ^ (int) (pred1));
  }
  
  private static final void encodePixel30(VTLittleEndianOutputStream out, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    out.writeInt(pixelData[position] ^ (int) (pred1));
  }
  
  private static final void decodePixel30(VTLittleEndianInputStream in, int[] pixelData, int position, int width) throws IOException
  {
    int left1, top1, diag1, pred1;
    
    diag1 = position - 1 >= width ? pixelData[position - width - 1] : 0;
    top1 = position >= width ? pixelData[position - width] : diag1;
    left1 = position > 0 ? pixelData[position - 1] : top1;
    
    pred1 = (left1 + top1 + diag1 + (left1 >> 1) + (top1 >> 1)) >> 2;
    
    pixelData[position] = (in.readInt() ^ (int) (pred1));
  }
}