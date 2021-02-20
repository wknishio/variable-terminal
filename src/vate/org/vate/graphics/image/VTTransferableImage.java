package org.vate.graphics.image;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.bric.image.VTARGBPixelGrabber;

public class VTTransferableImage implements Transferable, ClipboardOwner
{
  private BufferedImage bufferedImage;
  private VTImageIO vtImageIO;
  private DataBuffer recyclableDataBuffer;

  public VTTransferableImage(VTImageIO vtImageIO, DataBuffer recyclableDataBuffer)
  {
    this.vtImageIO = vtImageIO;
    this.recyclableDataBuffer = recyclableDataBuffer;
  }

  public VTTransferableImage(VTImageIO imageIO, Image image, DataBuffer recyclableDataBuffer)
  {
    this.vtImageIO = imageIO;
    this.recyclableDataBuffer = recyclableDataBuffer;
    int width = image.getWidth(null);
    int height = image.getHeight(null);
    this.bufferedImage = VTImageIO.newImage(0, 0, width, height, BufferedImage.TYPE_INT_ARGB, 0, recyclableDataBuffer);
    VTARGBPixelGrabber grabber = new VTARGBPixelGrabber();
    grabber.setImage(image);
    grabber.getPixels(((DataBufferInt) this.bufferedImage.getRaster().getDataBuffer()).getData());
    grabber.dispose();
  }

  public DataBuffer getRecyclableDataBuffer()
  {
    return recyclableDataBuffer;
  }

  public void write(OutputStream out) throws IOException
  {
    vtImageIO.write(out, bufferedImage);
  }

  public void read(InputStream in) throws IOException
  {
    bufferedImage = vtImageIO.read(in, recyclableDataBuffer);
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    bufferedImage.flush();
    bufferedImage = null;
    System.runFinalization();
    System.gc();
  }

  public void flush()
  {
    bufferedImage.flush();
    bufferedImage = null;
    System.runFinalization();
    System.gc();
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    if (flavor.equals(DataFlavor.imageFlavor))
    {
      return bufferedImage;
    }
    throw new UnsupportedFlavorException(flavor);
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[] { DataFlavor.imageFlavor };
  }

  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return flavor.equals(DataFlavor.imageFlavor);
  }
}