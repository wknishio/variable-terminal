package org.vate.client.graphicsmode;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import org.vate.VT;
import org.vate.client.connection.VTClientConnection;
import org.vate.console.VTConsole;
import org.vate.graphics.codec.VTQuadrupleOctalTreeFrameDifferenceCodecV9;
import org.vate.graphics.image.VTImageIO;
import org.vate.stream.limit.VTLimitedInputStream;
import com.sixlegs.png.iio.*;

public class VTGraphicsModeClientReader implements Runnable
{
  private volatile boolean stopped;
  private volatile boolean failed;
  private int currentDataType;
  private byte[] lastImageBufferByte;
  private short[] lastImageBufferUShort;
  private int[] lastImageBufferInt;
  private volatile BufferedImage currentImageDataBuffer;
  private volatile Graphics2D currentImageGraphics;
  private volatile BufferedImage nextImageDataBuffer;
  // private volatile BufferedImage firstImageDataBuffer;
  // private volatile Graphics2D firstImageGraphics;
  // private volatile BufferedImage secondImageDataBuffer;
  // private volatile Graphics2D secondImageGraphics;
  private Rectangle refreshArea = new Rectangle(0, 0, 0, 0);
  private VTGraphicsModeClientSession session;
  private VTClientConnection connection;
  private VTGraphicsModeClientWriter writer;
  private VTQuadrupleOctalTreeFrameDifferenceCodecV9 vtCustomCodec;
  private ImageReader currentImageReader;
  private ImageReader pngImageReader;
  private ImageReader jpegImageReader;
  private ImageInputStream imageStream;
  // private VTImageIO vtImageIO;
  private VTIncrementalIIOReadUpdateListener incrementalImageReader = new VTIncrementalIIOReadUpdateListener();
  private VTLimitedInputStream limitedInputStream;
  // private DataBuffer firstRecyclableDataBufferInt;
  // private DataBuffer secondRecyclableDataBufferInt;
  // private DataBuffer firstRecyclableDataBufferUShort;
  // private DataBuffer secondRecyclableDataBufferUShort;
  // private DataBuffer firstRecyclableDataBufferByte;
  // private DataBuffer secondRecyclableDataBufferByte;

  private DataBuffer recyclableDataBuffer;
  // private DataBuffer secondRecyclableDataBuffer;
  // private long startTime, endTime;

  private class VTIncrementalIIOReadUpdateListener implements IIOReadUpdateListener
  {
    private int offsetX = 0;
    private int offsetY = 0;
    // private int passMinX = 0;
    // private int passMinY = 0;

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
      // this.passMinX = minX;
      // this.passMinY = minY;
      // System.out.println("passStarted:" + pass + "," + minPass + "," + maxPass);
    }

    public void imageUpdate(ImageReader source, BufferedImage theImage, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands)
    {
      // System.out.println("imageUpdate:" + minX + "," + minY + "," + width + "," +
      // height);
      currentImageGraphics.drawImage(theImage, offsetX + minX, offsetY + minY, offsetX + minX + width, offsetY + minY + height, minX, minY, minX + width, minY + height, null);
    }

    public void passComplete(ImageReader source, BufferedImage theImage)
    {
      // System.out.println("passComplete:");
      // currentImageGraphics.drawImage(theImage, offsetX + passMinX, offsetY +
      // passMinY, offsetX + passMinX + theImage.getWidth(), offsetY + passMinY +
      // theImage.getHeight(), passMinX, passMinY, passMinX + theImage.getWidth(),
      // passMinY + theImage.getHeight(), null);
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

  public VTGraphicsModeClientReader(VTGraphicsModeClientSession session)
  {
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.stopped = true;
    this.failed = false;
    // this.vtImageIO = new VTImageIO();
    // this.imageInputBuffer = new VTByteArrayInputStream(imageBuffer);
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
    // lastSize = 0;
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

  public void setWriter(VTGraphicsModeClientWriter writer)
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
        VTConsole.print("\nVT>Remote graphics link failed to start on client!\nVT>");
        // connection.getGraphicsControlDataInputStream().read();
      }
      else
      {
        VTConsole.print("\nVT>Remote graphics link started!\nVT>");
        vtCustomCodec = new VTQuadrupleOctalTreeFrameDifferenceCodecV9();
        // pngImageReader = ImageIO.getImageReadersByFormatName("PNG").next();
        pngImageReader = new PngImageReader(new PngImageReaderSpi());
        jpegImageReader = ImageIO.getImageReadersByFormatName("JPEG").next();
        limitedInputStream = new VTLimitedInputStream(connection.getGraphicsDirectImageDataInputStream());
        pngImageReader.addIIOReadUpdateListener(incrementalImageReader);
        jpegImageReader.addIIOReadUpdateListener(incrementalImageReader);
        // lastSize = 0;
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
          case VT.VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_IMAGE:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_IMAGE");
            // refreshArea.x = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.y = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.width = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.height =
            // connection.getGraphicsControlDataInputStream().readInt();
            writer.requestInterfaceRefresh();
            writer.setRefreshArea(refreshArea);
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
            if (connection.getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
            {
              currentImageReader = jpegImageReader;
            }
            else
            {
              currentImageReader = pngImageReader;
            }
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            currentImageDataBuffer = VTImageIO.newImage(0, 0, width, height, BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
            recyclableDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            currentImageGraphics = currentImageDataBuffer.createGraphics();
            currentImageGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
            writer.setRemoteGraphics(currentImageDataBuffer);

            boolean synchronous = writer.isSynchronousRefresh();
            if (!synchronous)
            {
              writer.notifyAsynchronousRepainter();
            }
            int count = connection.getGraphicsControlDataInputStream().readInt();

            for (int i = 0; i < count; i++)
            {
              int size = connection.getGraphicsControlDataInputStream().readInt();
              int x = connection.getGraphicsControlDataInputStream().readInt();
              int y = connection.getGraphicsControlDataInputStream().readInt();
              // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_IMAGE");
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
              // imageStream = new MemoryCacheImageInputStream(limitedInputStream);
              currentImageReader.setInput(imageStream, true, false);
              incrementalImageReader.setOffsetX(x);
              incrementalImageReader.setOffsetY(y);
              nextImageDataBuffer = currentImageReader.read(0);
              limitedInputStream.empty();
              if (nextImageDataBuffer != null)
              {
                nextImageDataBuffer.flush();
              }
              nextImageDataBuffer = null;
            }
            writer.newFrameRemoteGraphics(currentImageDataBuffer);
            // writer.requestInterfaceRefresh();
            // connection.getGraphicsDirectImageDataInputStream().empty();
            System.runFinalization();
            System.gc();
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_SUBIMAGE");
            // refreshArea.x = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.y = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.width = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.height =
            // connection.getGraphicsControlDataInputStream().readInt();
            writer.requestInterfaceRefresh();
            writer.setRefreshArea(refreshArea);
            if (connection.getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
            {
              currentImageReader = jpegImageReader;
            }
            else
            {
              currentImageReader = pngImageReader;
            }
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_SUBIMAGE");
            int count = connection.getGraphicsControlDataInputStream().readInt();

            for (int i = 0; i < count; i++)
            {
              int size = connection.getGraphicsControlDataInputStream().readInt();
              int x = connection.getGraphicsControlDataInputStream().readInt();
              int y = connection.getGraphicsControlDataInputStream().readInt();
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
              // imageStream = new MemoryCacheImageInputStream(limitedInputStream);
              currentImageReader.setInput(imageStream, true, false);
              // writer.setRemoteGraphics(displayingImageDataBuffer);
              incrementalImageReader.setOffsetX(x);
              incrementalImageReader.setOffsetY(y);
              nextImageDataBuffer = currentImageReader.read(0);
              limitedInputStream.empty();
              if (nextImageDataBuffer != null)
              {
                nextImageDataBuffer.flush();
              }
              nextImageDataBuffer = null;
            }
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            // writer.requestInterfaceRefresh();
            // connection.getGraphicsDirectImageDataInputStream().empty();
            // System.runFinalization();
            // System.gc();
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_CUSTOM:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_CUSTOM");
            // refreshArea.x = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.y = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.width = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.height =
            // connection.getGraphicsControlDataInputStream().readInt();
            writer.requestInterfaceRefresh();
            writer.setRefreshArea(refreshArea);
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
            int type = connection.getGraphicsControlDataInputStream().readInt();
            int colors = connection.getGraphicsControlDataInputStream().readInt();
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            currentImageDataBuffer = VTImageIO.newImage(0, 0, width, height, type, colors, recyclableDataBuffer);
            recyclableDataBuffer = currentImageDataBuffer.getRaster().getDataBuffer();
            // currentImageGraphics = currentImageDataBuffer.createGraphics();
            // currentImageGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
            switch (type)
            {
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
            // startTime = System.currentTimeMillis();
            if (coding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
            {
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferByte, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferUShort, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferInt, width, height);
              }
            }
            else
            {
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferByte, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferUShort, width, height);
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferInt, width, height);
              }
            }
            writer.newFrameRemoteGraphics(currentImageDataBuffer);
            // endTime = System.currentTimeMillis();
            // System.out.println("new frame decoding time: " + (endTime
            // - startTime));
            System.runFinalization();
            System.gc();
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM");
            // refreshArea.x = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.y = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.width = connection.getGraphicsControlDataInputStream().readInt();
            // refreshArea.height =
            // connection.getGraphicsControlDataInputStream().readInt();
            writer.requestInterfaceRefresh();
            writer.setRefreshArea(refreshArea);
            // startTime = System.currentTimeMillis();
            if (!writer.isSynchronousRefresh())
            {
              writer.notifyAsynchronousRepainter();
            }
            if (connection.getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
            {
              currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferByte, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferUShort, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsSnappedImageDataInputStream(), lastImageBufferInt, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
            }
            else
            {
              currentDataType = currentImageDataBuffer.getRaster().getDataBuffer().getDataType();
              if (currentDataType == DataBuffer.TYPE_BYTE)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferByte, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_USHORT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferUShort, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
              else if (currentDataType == DataBuffer.TYPE_INT)
              {
                vtCustomCodec.decodeFrame(connection.getGraphicsDeflatedImageDataInputStream(), lastImageBufferInt, currentImageDataBuffer.getWidth(), currentImageDataBuffer.getHeight());
              }
            }
            writer.differenceRemoteGraphics(currentImageDataBuffer);
            // endTime = System.currentTimeMillis();
            // System.out.println("differential frame decoding time: " +
            // (endTime - startTime));
            break;
          }
          case VT.VT_GRAPHICS_MODE_SESSION_ENDING:
          {
            // System.out.println("VT_GRAPHICS_MODE_SESSION_ENDING");
            stopped = true;
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE");
            int width = connection.getGraphicsControlDataInputStream().readInt();
            int height = connection.getGraphicsControlDataInputStream().readInt();
            writer.resizeRemoteGraphics(width, height);
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED");
            writer.requestInterfaceRefresh();
            writer.notModifiedRemoteGraphics();
            break;
          }
          case VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED:
          {
            // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED");
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
        // e.printStackTrace();
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