package org.vash.vate.graphics.image;

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
  // private VTImageIO imageIO;
  private DataBuffer recyclableDataBuffer;
  
  public VTTransferableImage(DataBuffer recyclableDataBuffer)
  {
    // this.imageIO = imageIO;
    this.recyclableDataBuffer = recyclableDataBuffer;
  }
  
  public VTTransferableImage(Image image, DataBuffer recyclableDataBuffer)
  {
    // this.imageIO = imageIO;
    this.recyclableDataBuffer = recyclableDataBuffer;
    int width = image.getWidth(null);
    int height = image.getHeight(null);
    this.bufferedImage = VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_INT_ARGB, 0, recyclableDataBuffer);
    VTARGBPixelGrabber grabber = new VTARGBPixelGrabber();
    grabber.setImage(image);
    grabber.getPixels(((DataBufferInt) this.bufferedImage.getRaster().getDataBuffer()).getData());
    //int[] pixels = grabber.getPixels();
    //bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
    grabber.dispose();
    
  }
  
  public DataBuffer getRecyclableDataBuffer()
  {
    return recyclableDataBuffer;
  }
  
  public void write(OutputStream out) throws IOException
  {
    VTImageIO.writeImage(out, bufferedImage);
  }
  
  public void read(InputStream in) throws IOException
  {
    bufferedImage = VTImageIO.readImage(in, recyclableDataBuffer);
  }
  
  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    bufferedImage.flush();
    bufferedImage = null;
//    System.runFinalization();
//    System.gc();
  }
  
  public void flush()
  {
    bufferedImage.flush();
    bufferedImage = null;
//    System.runFinalization();
//    System.gc();
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