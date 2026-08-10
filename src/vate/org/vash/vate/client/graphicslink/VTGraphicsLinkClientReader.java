package org.vash.vate.client.graphicslink;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import org.vash.vate.VTSystem;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.com.sixlegs.png.iio.*;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.graphics.codec.VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII;
import static org.vash.vate.graphics.codec.VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII.CUSTOM_CODEC_PADDING_SIZE;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.stream.limit.VTSizedInputStream;

public class VTGraphicsLinkClientReader implements Runnable
{
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
  private ImageReader pngImageReader;
  private ImageReader jpgImageReader;
  private ImageInputStream imageInputStream;
  private VTIncrementalIIOReadUpdateListener incrementalImageReader = new VTIncrementalIIOReadUpdateListener();
  private VTSizedInputStream limitedInputStream;
  private DataBuffer recyclableCurrentDataBuffer;
  private DataBuffer recyclableNextDataBuffer;
  
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
    if (nextImageDataBuffer != null)
    {
      try
      {
        nextImageDataBuffer.flush();
      }
      catch (Throwable t)
      {
        
      }
      nextImageDataBuffer = null;
    }
    if (currentImageDataBuffer != null)
    {
      try
      {
        currentImageDataBuffer.flush();
      }
      catch (Throwable t)
      {
        
      }
      currentImageDataBuffer = null;
    }
    if (currentImageGraphics != null)
    {
      try
      {
        currentImageGraphics.dispose();
      }
      catch (Throwable t)
      {
        
      }
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
    
    if (jpgImageReader != null)
    {
      jpgImageReader.removeAllIIOReadUpdateListeners();
      jpgImageReader.dispose();
      jpgImageReader = null;
    }
    
    currentImageReader = null;
    
    if (imageInputStream != null)
    {
      try
      {
        imageInputStream.close();
      }
      catch (Throwable e)
      {
        
      }
      imageInputStream = null;
    }
    
    lastImageBufferByte = null;
    lastImageBufferUShort = null;
    lastImageBufferInt = null;
    
    // previousImageBufferByte = null;
    // previousImageBufferUShort = null;
    // previousImageBufferInt = null;
    
    recyclableCurrentDataBuffer = null;
    recyclableNextDataBuffer = null;
    
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
        VTMainConsole.print("\rVT>Remote graphics link start on client failed!\nVT>");
      }
      else
      {
        vtCustomCodec = new VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII();
        pngImageReader = new PngImageReader(new PngImageReaderSpi());
        jpgImageReader = ImageIO.getImageReadersByFormatName("jpeg").next();
        limitedInputStream = new VTSizedInputStream(connection.getGraphicsDirectImageDataInputStream());
        pngImageReader.addIIOReadUpdateListener(incrementalImageReader);
        jpgImageReader.addIIOReadUpdateListener(incrementalImageReader);
        VTMainConsole.print("\rVT>Remote graphics link started!\nVT>");
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
            imageInputStream = ImageIO.createImageInputStream(limitedInputStream);
            writer.requestInterfaceRefresh();
            if (currentImageDataBuffer != null)
            {
              try
              {
                currentImageDataBuffer.flush();
              }
              catch (Throwable t)
              {
                
              }
              currentImageDataBuffer = null;
            }
            if (currentImageGraphics != null)
            {
              try
              {
                currentImageGraphics.dispose();
              }
              catch (Throwable t)
              {
                
              }
              currentImageGraphics = null;
            }
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
            {
              currentImageReader = jpgImageReader;
            }
            else
            {
              currentImageReader = pngImageReader;
            }
            int type = connection.getGraphicsControlDataInputStream().readInt();
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            
            currentImageDataBuffer = VTImageIO.createImage(0, 0, width, height, type, colors, recyclableCurrentDataBuffer);
            recyclableCurrentDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            currentImageGraphics = currentImageDataBuffer.createGraphics();
            currentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
            writer.setRemoteGraphics(currentImageDataBuffer);
            
            boolean synchronous = writer.isSynchronousRefresh();
            if (!synchronous)
            {
              writer.notifyAsynchronousRepainter();
            }
            int blockCount = connection.getGraphicsControlDataInputStream().readInt();
            int blockMaxWidth = connection.getGraphicsControlDataInputStream().readInt();
            int blockMaxHeight = connection.getGraphicsControlDataInputStream().readInt();
            nextImageDataBuffer = VTImageIO.createImage(0, 0, blockMaxWidth, blockMaxHeight, (currentImageReader == jpgImageReader ? (colors > 16 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY) : type), colors, recyclableNextDataBuffer);
            recyclableNextDataBuffer = nextImageDataBuffer.getRaster().getDataBuffer();
            
            for (int i = 0; i < blockCount; i++)
            {
              int blockSize = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockX = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockY = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockWidth = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockHeight = connection.getGraphicsDirectImageDataInputStream().readInt();
              nextImageDataBuffer = VTImageIO.createImage(0, 0, blockWidth, blockHeight, (currentImageReader == jpgImageReader ? (colors > 16 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY) : type), colors, recyclableNextDataBuffer);
              limitedInputStream.size(blockSize);
              incrementalImageReader.setOffsetX(blockX);
              incrementalImageReader.setOffsetY(blockY);
              ImageReadParam imageReadParam = currentImageReader.getDefaultReadParam();
              imageReadParam.setDestination(nextImageDataBuffer);
              currentImageReader.setInput(imageInputStream, true, false);
              nextImageDataBuffer = currentImageReader.read(0, imageReadParam);
            }
            if (imageInputStream != null)
            {
              try
              {
                imageInputStream.close();
              }
              catch (Throwable e)
              {
                
              }
            }
            if (nextImageDataBuffer != null)
            {
              nextImageDataBuffer.flush();
            }
            nextImageDataBuffer = null;
            writer.refreshRemoteGraphics(currentImageDataBuffer);
//            System.runFinalization();
//            System.gc();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_STANDARD_DIFFERENTIAL_FRAME:
          {
            imageInputStream = ImageIO.createImageInputStream(limitedInputStream);
            writer.requestInterfaceRefresh();
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_JPG)
            {
              currentImageReader = jpgImageReader;
            }
            else
            {
              currentImageReader = pngImageReader;
            }
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            int type = connection.getGraphicsControlDataInputStream().readInt();
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int blockCount = connection.getGraphicsControlDataInputStream().readInt();
            int blockMaxWidth = connection.getGraphicsControlDataInputStream().readInt();
            int blockMaxHeight = connection.getGraphicsControlDataInputStream().readInt();
            nextImageDataBuffer = VTImageIO.createImage(0, 0, blockMaxWidth, blockMaxHeight, (currentImageReader == jpgImageReader ? (colors > 16 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY) : type), colors, recyclableNextDataBuffer);
            recyclableNextDataBuffer = nextImageDataBuffer.getRaster().getDataBuffer();
            
            for (int i = 0; i < blockCount; i++)
            {
              int blockSize = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockX = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockY = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockWidth = connection.getGraphicsDirectImageDataInputStream().readInt();
              int blockHeight = connection.getGraphicsDirectImageDataInputStream().readInt();
              nextImageDataBuffer = VTImageIO.createImage(0, 0, blockWidth, blockHeight, (currentImageReader == jpgImageReader ? (colors > 16 ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY) : type), colors, recyclableNextDataBuffer);
              limitedInputStream.size(blockSize);
              incrementalImageReader.setOffsetX(blockX);
              incrementalImageReader.setOffsetY(blockY);
              ImageReadParam imageReadParam = currentImageReader.getDefaultReadParam();
              imageReadParam.setDestination(nextImageDataBuffer);
              currentImageReader.setInput(imageInputStream, true, false);
              nextImageDataBuffer = currentImageReader.read(0, imageReadParam);
            }
            if (imageInputStream != null)
            {
              try
              {
                imageInputStream.close();
              }
              catch (Throwable e)
              {
                
              }
            }
            if (nextImageDataBuffer != null)
            {
              nextImageDataBuffer.flush();
            }
            nextImageDataBuffer = null;
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CUSTOM_REFRESH_FRAME:
          {
            writer.requestInterfaceRefresh();
            if (currentImageDataBuffer != null)
            {
              try
              {
                currentImageDataBuffer.flush();
              }
              catch (Throwable t)
              {
                
              }
              currentImageDataBuffer = null;
            }
            if (currentImageGraphics != null)
            {
              try
              {
                currentImageGraphics.dispose();
              }
              catch (Throwable t)
              {
                
              }
              currentImageGraphics = null;
            }
            int coding = connection.getGraphicsControlDataInputStream().read();
            int type = connection.getGraphicsControlDataInputStream().readInt();
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            currentImageDataBuffer = VTImageIO.createImage(CUSTOM_CODEC_PADDING_SIZE, CUSTOM_CODEC_PADDING_SIZE, width, height, type, colors, recyclableCurrentDataBuffer);
            recyclableCurrentDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            
            switch (type)
            {
              case BufferedImage.TYPE_CUSTOM:
              {
                if (colors == 4096 || colors == 512)
                {
                  lastImageBufferByte = null;
                  lastImageBufferUShort = ((DataBufferUShort) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                  lastImageBufferInt = null;
                }
                if (colors == 64 || colors == 16 || colors == 8 || colors == 4)
                {
                  lastImageBufferByte = ((DataBufferByte) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                  lastImageBufferUShort = null;
                  lastImageBufferInt = null;
                }
                if (colors == 262144 || colors == 2097152)
                {
                  lastImageBufferByte = null;
                  lastImageBufferUShort = null;
                  lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                }
                break;
              }
              case BufferedImage.TYPE_BYTE_INDEXED:
              {
                lastImageBufferByte = ((DataBufferByte) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                lastImageBufferUShort = null;
                lastImageBufferInt = null;
                break;
              }
              case BufferedImage.TYPE_USHORT_555_RGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = ((DataBufferUShort) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                lastImageBufferInt = null;
                break;
              }
              case BufferedImage.TYPE_INT_RGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = null;
                lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                break;
              }
              case BufferedImage.TYPE_INT_ARGB:
              {
                lastImageBufferByte = null;
                lastImageBufferUShort = null;
                lastImageBufferInt = ((DataBufferInt) currentImageDataBuffer.getRaster().getDataBuffer()).getData();
                break;
              }
            }
            writer.setRemoteGraphics(currentImageDataBuffer);
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
            if (coding == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
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
            else
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
            writer.refreshRemoteGraphics(currentImageDataBuffer);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_CUSTOM_DIFFERENTIAL_FRAME:
          {
            writer.requestInterfaceRefresh();
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            if (connection.getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_IMAGE_ENCODING_FORMAT_GZD)
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
            else
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
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_SESSION_FINISHED:
          {
            stopped = true;
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REMOTE_INTERFACE_AREA_CHANGE:
          {
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            writer.resizeRemoteGraphics(width, height);
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_NOT_NEEDED:
          {
            writer.requestInterfaceRefresh();
            writer.notModifiedRemoteGraphics();
            break;
          }
          case VTSystem.VT_GRAPHICS_LINK_IMAGE_REFRESH_MODE_INTERRUPTED:
          {
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