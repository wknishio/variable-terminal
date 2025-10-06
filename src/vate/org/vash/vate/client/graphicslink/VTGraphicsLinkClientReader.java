package org.vash.vate.client.graphicslink;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import org.vash.vate.VTSystem;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.graphics.codec.VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.stream.limit.VTLimitedInputStream;
import com.sixlegs.png.iio.*;

public class VTGraphicsLinkClientReader implements Runnable
{
  private static final int CODEC_PADDING_SIZE = VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII.CODEC_PADDING_SIZE;
  private volatile boolean stopped;
  private boolean failed;
  private int currentDataType;
  private byte[] lastImageBufferByte;
  // private byte[] previousImageBufferByte;
  private short[] lastImageBufferUShort;
  // private short[] previousImageBufferUShort;
  private int[] lastImageBufferInt;
  // private int[] previousImageBufferInt;
  private BufferedImage currentImageDataBuffer;
  private Graphics2D currentImageGraphics;
  private BufferedImage nextImageDataBuffer;
  private VTGraphicsLinkClientSession session;
  private VTClientConnection connection;
  private VTGraphicsLinkClientWriter writer;
  private VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII vtCustomCodec;
  private ImageReader currentImageReader;
  //private ImageReadParam currentImageReaderParam;
  private ImageReader pngImageReader;
  private ImageReader jpegImageReader;
  //private ImageReadParam jpgReaderParam;
  private ImageInputStream imageStream;
  private VTIncrementalIIOReadUpdateListener incrementalImageReader = new VTIncrementalIIOReadUpdateListener();
  private VTLimitedInputStream limitedInputStream;
  private DataBuffer recyclableDataBuffer;
  // private long startTime, endTime;
  
  private class VTIncrementalIIOReadUpdateListener implements IIOReadUpdateListener
  {
    private int offsetX = 0;
    private int offsetY = 0;
    
    public void setOffsetX(int offsetX)
    {
      this.offsetX = offsetX;
    }
    
    public void setOffsetY(int offsetY)
    {
      this.offsetY = offsetY;
    }
    
    public void passStarted(ImageReader source, BufferedImage theImage, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands)
    {
      
    }
    
    public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands)
    {
      currentImageGraphics.drawImage(theImage, offsetX + minX, offsetY + minY, offsetX + minX + width, offsetY + minY + height, minX, minY, minX + width, minY + height, null);
    }
    
    public void passComplete(ImageReader source, BufferedImage theImage)
    {
      //currentImageGraphics.drawImage(theImage, offsetX, offsetY, null);
    }
    
    public void thumbnailPassStarted(ImageReader source, BufferedImage theThumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands)
    {
      
    }
    
    public void thumbnailUpdate(ImageReader source, BufferedImage theThumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands)
    {
      
    }
    
    public void thumbnailPassComplete(ImageReader source, BufferedImage theThumbnail)
    {
      
    }
  }
  
  public VTGraphicsLinkClientReader(VTGraphicsLinkClientSession session)
  {
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.stopped = true;
    this.failed = false;
  }
  
  public void dispose()
  {
    stopped = true;
    failed = false;
    // imageBuffer = null;
    if (nextImageDataBuffer != null)
    {
      nextImageDataBuffer.flush();
      nextImageDataBuffer = null;
    }
    if (currentImageDataBuffer != null)
    {
      currentImageDataBuffer.flush();
      currentImageDataBuffer = null;
    }
    if (currentImageGraphics != null)
    {
      currentImageGraphics.dispose();
      currentImageGraphics = null;
    }
    if (vtCustomCodec != null)
    {
      vtCustomCodec.dispose();
    }
    
    if (pngImageReader != null)
    {
      pngImageReader.removeAllIIOReadUpdateListeners();
      pngImageReader.dispose();
      pngImageReader = null;
    }
    
    if (jpegImageReader != null)
    {
      jpegImageReader.removeAllIIOReadUpdateListeners();
      jpegImageReader.dispose();
      jpegImageReader = null;
    }
    
    currentImageReader = null;
    
    if (imageStream != null)
    {
      try
      {
        imageStream.close();
      }
      catch (Throwable e)
      {
        
      }
      imageStream = null;
    }
    
    lastImageBufferByte = null;
    lastImageBufferUShort = null;
    lastImageBufferInt = null;
    
    // previousImageBufferByte = null;
    // previousImageBufferUShort = null;
    // previousImageBufferInt = null;
    
    recyclableDataBuffer = null;
    
    vtCustomCodec = null;
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public void setFailed(boolean failed)
  {
    this.failed = failed;
  }
  
  public boolean isFailed()
  {
    return failed;
  }
  
  public void setWriter(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void run()
  {
    synchronized (this)
    {
      while (!stopped && !writer.isOpen())
      {
        try
        {
          wait();
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    }
    try
    {
      if (failed)
      {
        VTMainConsole.print("\nVT>Remote graphics link failed to start on client!\nVT>");
      }
      else
      {
        VTMainConsole.print("\nVT>Remote graphics link started!\nVT>");
        vtCustomCodec = new VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII();
        // pngImageReader = ImageIO.getImageReadersByFormatName("PNG").next();
        pngImageReader = new PngImageReader(new PngImageReaderSpi());
        jpegImageReader = ImageIO.getImageReadersByFormatName("JPEG").next();
        limitedInputStream = new VTLimitedInputStream(connection.getGraphicsDirectImageDataInputStream());
        pngImageReader.addIIOReadUpdateListener(incrementalImageReader);
        jpegImageReader.addIIOReadUpdateListener(incrementalImageReader);
        //jpgReaderParam = jpegImageReader.getDefaultReadParam();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      stopped = true;
    }
    while (!stopped)
    {
      try
      {
        switch (connection.getGraphicsControlDataInputStream().read())
        {
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_STANDARD_REFRESH_FRAME:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_INDEPENDENT_FRAME_IMAGE");
            writer.requestInterfaceRefresh();
            if (currentImageDataBuffer != null)
            {
              currentImageDataBuffer.flush();
              currentImageDataBuffer = null;
            }
            if (currentImageGraphics != null)
            {
              currentImageGraphics.dispose();
              currentImageGraphics = null;
            }
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
            {
              currentImageReader = jpegImageReader;
              //currentImageReaderParam = jpgReaderParam;
            }
            else
            {
              currentImageReader = pngImageReader;
              //currentImageReaderParam = null;
            }
            int type = connection.getGraphicsControlDataInputStream().readInt();
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            
            currentImageDataBuffer = VTImageIO.createImage(0, 0, width, height, type, colors, recyclableDataBuffer);
            recyclableDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            currentImageGraphics = currentImageDataBuffer.createGraphics();
            currentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
            writer.setRemoteGraphics(currentImageDataBuffer);
            
            boolean synchronous = writer.isSynchronousRefresh();
            if (!synchronous)
            {
              writer.notifyAsynchronousRepainter();
            }
            int count = connection.getGraphicsControlDataInputStream().readInt();
            
            for (int i = 0; i < count; i++)
            {
              int size = connection.getGraphicsDirectImageDataInputStream().readInt();
              int x = connection.getGraphicsDirectImageDataInputStream().readInt();
              int y = connection.getGraphicsDirectImageDataInputStream().readInt();
              // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_NEW_FRAME_IMAGE");
              if (imageStream != null)
              {
                try
                {
                  imageStream.close();
                }
                catch (Throwable e)
                {
                  
                }
                imageStream = null;
              }
              limitedInputStream.setLimit(size);
              imageStream = ImageIO.createImageInputStream(limitedInputStream);
              currentImageReader.setInput(imageStream, true, false);
              incrementalImageReader.setOffsetX(x);
              incrementalImageReader.setOffsetY(y);
              nextImageDataBuffer = currentImageReader.read(0, null);
              limitedInputStream.empty();
              if (nextImageDataBuffer != null)
              {
                nextImageDataBuffer.flush();
              }
              nextImageDataBuffer = null;
            }
            writer.refreshRemoteGraphics(currentImageDataBuffer);
//            System.runFinalization();
//            System.gc();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_STANDARD_DIFFERENTIAL_FRAME:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE");
            writer.requestInterfaceRefresh();
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
            {
              currentImageReader = jpegImageReader;
              //currentImageReaderParam = jpgReaderParam;
            }
            else
            {
              currentImageReader = pngImageReader;
              //currentImageReaderParam = null;
            }
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            @SuppressWarnings("unused")
            int type = connection.getGraphicsControlDataInputStream().readInt();
            @SuppressWarnings("unused")
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int count = connection.getGraphicsControlDataInputStream().readInt();
            
            for (int i = 0; i < count; i++)
            {
              int size = connection.getGraphicsDirectImageDataInputStream().readInt();
              int x = connection.getGraphicsDirectImageDataInputStream().readInt();
              int y = connection.getGraphicsDirectImageDataInputStream().readInt();
              if (imageStream != null)
              {
                try
                {
                  imageStream.close();
                }
                catch (Throwable e)
                {
                  
                }
                imageStream = null;
              }
              limitedInputStream.setLimit(size);
              imageStream = ImageIO.createImageInputStream(limitedInputStream);
              currentImageReader.setInput(imageStream, true, false);
              incrementalImageReader.setOffsetX(x);
              incrementalImageReader.setOffsetY(y);
              nextImageDataBuffer = currentImageReader.read(0, null);
              limitedInputStream.empty();
              if (nextImageDataBuffer != null)
              {
                nextImageDataBuffer.flush();
              }
              nextImageDataBuffer = null;
            }
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CUSTOM_REFRESH_FRAME:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_INDEPENDENT_FRAME_CUSTOM");
            writer.requestInterfaceRefresh();
            if (currentImageDataBuffer != null)
            {
              currentImageDataBuffer.flush();
              currentImageDataBuffer = null;
            }
            if (currentImageGraphics != null)
            {
              currentImageGraphics.dispose();
              currentImageGraphics = null;
            }
            int coding = connection.getGraphicsControlDataInputStream().read();
            int type, colors, width, height;
            type = connection.getGraphicsControlDataInputStream().readInt();
            colors = connection.getGraphicsControlDataInputStream().readInt();
            width = connection.getGraphicsControlDataInputStream().readInt();
            height = connection.getGraphicsControlDataInputStream().readInt();
            //System.out.println("type:" + type);
            //System.out.println("colors:" + colors);
            currentImageDataBuffer = VTImageIO.createImage(CODEC_PADDING_SIZE, CODEC_PADDING_SIZE, width, height, type, colors, recyclableDataBuffer);
            recyclableDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            
            switch (type)
            {
              case BufferedImage.TYPE_CUSTOM:
              {
                if (colors == 4096 || colors == 512)
                {
                  lastImageBufferByte = null;
                  lastImageBufferUShort = ((DataBufferUShort) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                  lastImageBufferInt = null;
//                previousImageBufferByte = null;
//                if (previousImageBufferUShort == null || previousImageBufferUShort.length < lastImageBufferUShort.length)
//                {
//                  previousImageBufferUShort = new short[lastImageBufferUShort.length];
//                }
//                previousImageBufferInt = null;
//                VTImageIO.clearBuffer(previousImageBufferUShort, BufferedImage.TYPE_USHORT_555_RGB, colors, 0);
                }
                if (colors == 64 || colors == 16 || colors == 8 || colors == 4)
                {
                  lastImageBufferByte = ((DataBufferByte) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                  lastImageBufferUShort = null;
                  lastImageBufferInt = null;
//                if (previousImageBufferByte == null || previousImageBufferByte.length < lastImageBufferByte.length)
//                {
//                  previousImageBufferByte = new byte[lastImageBufferByte.length];
//                }
//                previousImageBufferUShort = null;
//                previousImageBufferInt = null;
//                VTImageIO.clearBuffer(previousImageBufferByte, BufferedImage.TYPE_BYTE_INDEXED, colors, 0);
                }
                if (colors == 262144 || colors == 2097152)
                {
                  lastImageBufferByte = null;
                  lastImageBufferUShort = null;
                  lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
//                if (previousImageBufferInt == null || previousImageBufferInt.length < lastImageBufferInt.length)
//                {
//                  previousImageBufferInt = new int[lastImageBufferInt.length];
//                }
//                VTImageIO.clearBuffer(previousImageBufferInt, BufferedImage.TYPE_INT_RGB, colors, 0);
                }
                break;
              }
              case BufferedImage.TYPE_BYTE_INDEXED:
              {
                lastImageBufferByte = ((DataBufferByte) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                lastImageBufferUShort = null;
                lastImageBufferInt = null;
//              if (previousImageBufferByte == null || previousImageBufferByte.length < lastImageBufferByte.length)
//              {
//                previousImageBufferByte = new byte[lastImageBufferByte.length];
//              }
//              previousImageBufferUShort = null;
//              previousImageBufferInt = null;
//              VTImageIO.clearBuffer(previousImageBufferByte, BufferedImage.TYPE_BYTE_INDEXED, colors, 0);
                break;
              }
              case BufferedImage.TYPE_USHORT_555_RGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = ((DataBufferUShort) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                lastImageBufferInt = null;
//              previousImageBufferByte = null;
//              if (previousImageBufferUShort == null || previousImageBufferUShort.length < lastImageBufferUShort.length)
//              {
//                previousImageBufferUShort = new short[lastImageBufferUShort.length];
//              }
//              previousImageBufferInt = null;
//              VTImageIO.clearBuffer(previousImageBufferUShort, BufferedImage.TYPE_USHORT_555_RGB, colors, 0);
                break;
              }
              case BufferedImage.TYPE_INT_RGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = null;
                lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
//              previousImageBufferByte = null;
//              previousImageBufferUShort = null;
//              if (previousImageBufferInt == null || previousImageBufferInt.length < lastImageBufferInt.length)
//              {
//                previousImageBufferInt = new int[lastImageBufferInt.length];
//              }
//              VTImageIO.clearBuffer(previousImageBufferInt, BufferedImage.TYPE_INT_RGB, colors, 0);
                break;
              }
              case BufferedImage.TYPE_INT_ARGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = null;
                lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
//              previousImageBufferByte = null;
//              previousImageBufferUShort = null;
//              if (previousImageBufferInt == null || previousImageBufferInt.length < lastImageBufferInt.length)
//              {
//                previousImageBufferInt = new int[lastImageBufferInt.length];
//              }
//              VTImageIO.clearBuffer(previousImageBufferInt, BufferedImage.TYPE_INT_RGB, colors, 0);
                break;
              }
            }
            writer.setRemoteGraphics(currentImageDataBuffer);
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
            // startTime = System.currentTimeMillis();
            if (coding == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
            {
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame8(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferByte, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame15(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferUShort, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame24(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferInt, width, height);
              }
            }
            else
            {
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame8(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferByte, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame15(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferUShort, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame24(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferInt, width, height);
              }
            }
            writer.refreshRemoteGraphics(currentImageDataBuffer);
            // endTime = System.currentTimeMillis();
            // System.out.println("new frame decoding time: " + (endTime
            // - startTime));
//            System.runFinalization();
//            System.gc();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CUSTOM_DIFFERENTIAL_FRAME:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM");
            writer.requestInterfaceRefresh();
            // startTime = System.currentTimeMillis();
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
            {
              currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame8(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferByte, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame15(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferUShort, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame24(connection.getGraphicsFastImageDataInputStream(), null, lastImageBufferInt, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
            }
            else
            {
              currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame8(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferByte, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame15(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferUShort, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame24(connection.getGraphicsHeavyImageDataInputStream(), null, lastImageBufferInt, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
            }
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            // endTime = System.currentTimeMillis();
            // System.out.println("differential frame decoding time: " +
            // (endTime - startTime));
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_SESSION_FINISHED:
          {
            stopped = true;
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REMOTE_INTERFACE_AREA_CHANGE:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE");
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            writer.resizeRemoteGraphics(width, height);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_NOT_NEEDED:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_REFRESH_NOT_NEEDED");
            writer.requestInterfaceRefresh();
            writer.notModifiedRemoteGraphics();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_INTERRUPTED:
          {
            // System.out.println("VT_GRAPHICS_LINK_GRAPHICS_REFRESH_MODE_INTERRUPTED");
            writer.requestInterfaceRefresh();
            writer.notModifiedRemoteGraphics();
            break;
          }
          default:
          {
            stopped = true;
            break;
          }
        }
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
        // e.printStackTrace(VTConsole.getSystemOut());
        stopped = true;
        break;
      }
    }
    synchronized (session)
    {
      session.notify();
    }
  }
}