package org.vash.vate.graphics.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.vash.vate.graphics.image.VTImageDataUtils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class VTWin32JNAScreenShot
{
  public static boolean getPixelData(int x, int y, int width, int height, int[] pixelInt, short[] pixelShort)
  {
    boolean ok = false;
    HWND desktopWindow = USER.GetDesktopWindow();
    HDC windowDC = USER.GetDC(desktopWindow);
    HBITMAP outputBitmap = GDI.CreateCompatibleBitmap(windowDC, width, height);
    try
    {
      HDC blitDC = GDI.CreateCompatibleDC(windowDC);
      try
      {
        HANDLE oldBitmap = GDI.SelectObject(blitDC, outputBitmap);
        try
        {
          GDI.BitBlt(blitDC, 0, 0, width, height, windowDC, x, y, GDI32.SRCCOPY);
        }
        finally
        {
          GDI.SelectObject(blitDC, oldBitmap);
        }
        BITMAPINFO bi = new BITMAPINFO(40);
        bi.bmiHeader.biSize = 40;
        ok = GDI.GetDIBits(blitDC, outputBitmap, 0, height, (byte[]) null, bi, WinGDI.DIB_RGB_COLORS);
        if (ok)
        {
        	BITMAPINFOHEADER bih = bi.bmiHeader;
          bih.biHeight = - Math.abs(bih.biHeight);
          bi.bmiHeader.biCompression = 0;
          //return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
          ok = pixelDataFromBitmap(blitDC, outputBitmap, bi, pixelInt, pixelShort);
        }
        else
        {
          ok = false;
        }
      }
      finally
      {
        GDI.DeleteObject(blitDC);
      }
    }
    finally
    {
      GDI.DeleteObject(outputBitmap);
      USER.ReleaseDC(desktopWindow, windowDC);
    }
    return ok;
  }
  
  private static boolean pixelDataFromBitmap(HDC blitDC, HBITMAP outputBitmap, BITMAPINFO bi, int[] pixelInt, short[] pixelShort)
  {
    boolean ok = false;
    BITMAPINFOHEADER bih = bi.bmiHeader;
    int height = Math.abs(bih.biHeight);
    int width = Math.abs(bih.biWidth);
      
    switch (bih.biBitCount)
    {
      case 16:
      {
      	ok = GDI.GetDIBits(blitDC, outputBitmap, 0, height, pixelShort, bi, 0);
      	VTImageDataUtils.convertRGB555ToRGB888(pixelShort, pixelInt, width * height);
      	break;
      }
      case 32:
      {
        ok = GDI.GetDIBits(blitDC, outputBitmap, 0, height, pixelInt, bi, 0);
        break;
      }
      default:
      {
        throw new IllegalArgumentException("Unsupported bit count: " + bih.biBitCount);
      }
    }
    return ok;
  }
  
  public static BufferedImage getScreenshot(Rectangle bounds) 
  {
  	HWND desktopWindow = USER.GetDesktopWindow();
    HDC windowDC = USER.GetDC(desktopWindow);
    HBITMAP outputBitmap = GDI.CreateCompatibleBitmap(windowDC, bounds.width, bounds.height);
    try
    {
      HDC blitDC = GDI.CreateCompatibleDC(windowDC);
      try
      {
        HANDLE oldBitmap = GDI.SelectObject(blitDC, outputBitmap);
        try
        {
          GDI.BitBlt(blitDC, 0, 0, bounds.width, bounds.height, windowDC, bounds.x, bounds.y, GDI32.SRCCOPY);
        }
        finally
        {
          GDI.SelectObject(blitDC, oldBitmap);
        }
        BITMAPINFO bi = new BITMAPINFO(40);
        bi.bmiHeader.biSize = 40;
        boolean ok = GDI.GetDIBits(blitDC, outputBitmap, 0, bounds.height, (byte[]) null, bi, WinGDI.DIB_RGB_COLORS);
        if (ok)
        {
        	BITMAPINFOHEADER bih = bi.bmiHeader;
          bih.biHeight = - Math.abs(bih.biHeight);
          bi.bmiHeader.biCompression = 0;
          return bufferedImageFromBitmap(blitDC, outputBitmap, bi);
        }
        else
        {
          return null;
        }
      }
      finally
      {
        GDI.DeleteObject(blitDC);
      }
    }
    finally
    {
      GDI.DeleteObject(outputBitmap);
      USER.ReleaseDC(desktopWindow, windowDC);
    }
  }
  
  private static BufferedImage bufferedImageFromBitmap(HDC blitDC, HBITMAP outputBitmap, BITMAPINFO bi)
  {
    BITMAPINFOHEADER bih = bi.bmiHeader;
    int height = Math.abs(bih.biHeight);
    final ColorModel cm;
    final DataBuffer buffer;
    final WritableRaster raster;
    int strideBits =  (bih.biWidth * bih.biBitCount);
    int strideBytesAligned = (((strideBits - 1) | 0x1F) + 1) >> 3;
    final int strideElementsAligned;
    switch (bih.biBitCount)
    {
      case 16:
      {
        strideElementsAligned = strideBytesAligned / 2;
        cm = new DirectColorModel(16, 0x7C00, 0x3E0, 0x1F);
        buffer = new DataBufferUShort(strideElementsAligned * height);
        raster = Raster.createPackedRaster(buffer,
        bih.biWidth, height,
        strideElementsAligned,
        ((DirectColorModel) cm).getMasks(),
        null);
        break;
      }
      case 32:
      {
        strideElementsAligned = strideBytesAligned / 4;
        cm = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF);
        buffer = new DataBufferInt(strideElementsAligned * height);
        raster = Raster.createPackedRaster(buffer,
        bih.biWidth, height,
        strideElementsAligned,
        ((DirectColorModel) cm).getMasks(),
        null);
        break;
      }
      default:
      {
        throw new IllegalArgumentException("Unsupported bit count: " + bih.biBitCount);
      }
    }
    final boolean ok;
    switch (buffer.getDataType())
    {
      case DataBuffer.TYPE_INT:
      {
        int[] pixels = ((DataBufferInt) buffer).getData();
        ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
        break;
      }
      case DataBuffer.TYPE_USHORT:
      {
        short[] pixels = ((DataBufferUShort) buffer).getData();
        ok = GDI.GetDIBits(blitDC, outputBitmap, 0, raster.getHeight(), pixels, bi, 0);
        break;
      }
      default:
      {
        throw new AssertionError("Unexpected buffer element type: " + buffer.getDataType());
      }
    }
    if (ok)
    {
      return new BufferedImage(cm, raster, false, null);
    }
    else
    {
      return null;
    }
  }
    
  private static final User32 USER = User32.INSTANCE;
  
  private static final GDI32 GDI = GDI32.INSTANCE;
  
}

final class GDI32
{
  //GDI32 INSTANCE = (GDI32) Native.loadLibrary(GDI32.class);
  static
  {
    Native.register("gdi32");
  }
  
  static GDI32 INSTANCE = new GDI32();
  
  public native boolean BitBlt(HDC hdcDest, int nXDest, int nYDest, int nWidth, int nHeight, HDC hdcSrc, int nXSrc, int nYSrc, int dwRop);
  
  public native void DeleteObject(HANDLE hObject);
  
  public native void DeleteDC(HDC hDC);
  
  public native HANDLE SelectObject(HDC hDC, HANDLE hGDIObj);
  
  public native HBITMAP CreateCompatibleBitmap(HDC windowDC, int width, int height);
  
  public native HDC CreateCompatibleDC(HDC hDC);
  
  public native boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, byte[] pixels, BITMAPINFO bi, int usage);
  
  public native boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, short[] pixels, BITMAPINFO bi, int usage);
  
  public native boolean GetDIBits(HDC dc, HBITMAP bmp, int startScan, int scanLines, int[] pixels, BITMAPINFO bi, int usage);
  
  public static int SRCCOPY = 0xCC0020;
  
  public native HDC CreateDC(String pwszDriver, String pwszDevice, String pszPort, Pointer ptr);
  
}

final class User32
{
  //User32 INSTANCE = (User32) Native.loadLibrary(User32.class, W32APIOptions.UNICODE_OPTIONS);
  static
  {
    Native.register("user32");
  }
  
  static User32 INSTANCE = new User32();
  
  public native HWND GetDesktopWindow();
  
  public native HDC GetDC(HWND hWnd);
  
  public native int ReleaseDC(HWND hWnd, HDC hDC);
  
}