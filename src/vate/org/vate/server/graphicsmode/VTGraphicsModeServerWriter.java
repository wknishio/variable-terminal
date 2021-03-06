package org.vate.server.graphicsmode;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.vate.VT;
import org.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vate.graphics.codec.VTQuadrupleOctalTreeFrameDifferenceCodecV9;
import org.vate.graphics.image.VTImageDataUtils;
import org.vate.graphics.image.VTImageIO;
import org.vate.server.connection.VTServerConnection;
import org.vate.stream.array.VTByteArrayOutputStream;

import com.objectplanet.image.PngEncoder;

public class VTGraphicsModeServerWriter implements Runnable
{
  private static final int imageOutputBufferSize = VT.VT_STANDARD_DATA_BUFFER_SIZE;
  private volatile boolean stopped;
  private volatile boolean needRefresh;
  private volatile boolean clearRequested;
  private volatile boolean refreshInterrupted;
  // private volatile boolean open;
  private volatile boolean drawPointer;
  // private volatile boolean dynamicCoding;
  // private volatile boolean separatedCoding;
  private volatile int screenCaptureInterval;
  private volatile int imageCoding;
  private int lastImageCoding;
  private int lastWidth;
  private int lastHeight;
  private int lastDepth;
  private int lastDataType;
  private int interruptedLastWidth;
  private int interruptedLastHeight;
  private int lastColors;
  // private double lastCaptureScale;
  private byte[] lastImageBufferByte;
  private byte[] previousImageBufferByte;
  private short[] lastImageBufferUShort;
  private short[] previousImageBufferUShort;
  private int[] lastImageBufferInt;
  private int[] previousImageBufferInt;
  private volatile Rectangle captureArea;
  private volatile Rectangle resultArea;
  private volatile double captureScale;
  private BufferedImage imageDataBuffer;
  private BufferedImage convertedDataBuffer;
  private Graphics2D convertedGraphics;
  private volatile GraphicsDevice nextDevice;
  private volatile GraphicsDevice currentDevice;
  private VTByteArrayOutputStream imageOutputBuffer = new VTByteArrayOutputStream(imageOutputBufferSize);
  // private ZOutputStream deflateOutputStream;
  // private SnappyOutputStream snappyOutputStream;
  // private VTByteArrayOutputStream compressedOutputBuffer;
  private VTAWTScreenCaptureProvider viewProvider;
  private VTServerConnection connection;
  private VTGraphicsModeServerSession session;
  private VTQuadrupleOctalTreeFrameDifferenceCodecV9 vtCustomCodec;
  // private VTImageIO vtImageIO;
  private Object screenCaptureIntervalSynchronizer;
  private ImageWriter jpgWriter;
  private ImageWriteParam jpgWriterParam;
  private ImageOutputStream jpgImageOutputStream;
  private PngEncoder pngEncoder;
  private DataBuffer recycledDataBuffer;
  // private long startTime, endTime, total, number;

  public VTGraphicsModeServerWriter(VTGraphicsModeServerSession session)
  {
    this.resultArea = new Rectangle(0, 0, 1, 1);
    this.stopped = true;
    this.session = session;
    this.connection = session.getSession().getConnection();
    this.viewProvider = session.getSession().getViewProvider();
    this.drawPointer = true;
    this.screenCaptureInterval = 250;
    this.captureScale = 1;
    this.imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF;
    // this.deviceNumber = 0;
    this.viewProvider.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
    // this.synchronousRefresh = false;
    this.refreshInterrupted = false;
    // this.screenCaptureMode =
    // VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_PARTIAL;
    this.clearRequested = false;
    // this.dynamicCoding = false;
    // this.separatedCoding = true;
    this.screenCaptureIntervalSynchronizer = new Object();
    // this.vtImageIO = new VTImageIO();
    if (GraphicsEnvironment.isHeadless())
    {
      return;
    }
    this.currentDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    this.nextDevice = currentDevice;
    // this.session.getSession().getClipboardTransferTask().setEndingTask(new
    // VTGraphicsModeServerClipboardTransferEndingTask(this));
  }

  public void dispose()
  {
    stopped = true;
    // open = false;
    drawPointer = true;
    needRefresh = false;
    screenCaptureInterval = 250;
    captureScale = 1;
    imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF;
    // deviceNumber = 0;
    viewProvider.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
    // synchronousRefresh = false;
    refreshInterrupted = false;
    // screenCaptureMode =
    // VT.VT_GRAPHICS_MODE_GRAPHICS_CAPTURE_MODE_PARTIAL;
    clearRequested = false;
    // dynamicCoding = false;
    // separatedCoding = true;
    // scaling = false;
    lastWidth = 0;
    lastHeight = 0;
    lastDepth = 0;
    lastDataType = 0;
    lastColors = 0;
    interruptedLastWidth = 0;
    interruptedLastWidth = 0;
    lastImageCoding = 0;
    // interruptedLastWidth = 0;
    // interruptedLastHeight = 0;
    // subImageDataBuffer = null;
    // imageOutputBuffer = null;
    vtCustomCodec = null;
    /*
     * if (deflateOutputStream != null) { try { deflateOutputStream.close(); } catch
     * (Throwable e) { } }
     */

    /*
     * if (snappyOutputStream != null) { try { snappyOutputStream.close(); } catch
     * (Throwable e) { } }
     */

    // compressedOutputBuffer = null;

    if (vtCustomCodec != null)
    {
      vtCustomCodec.dispose();
    }

    if (viewProvider != null)
    {
      viewProvider.dispose();
    }

    if (jpgImageOutputStream != null)
    {
      try
      {
        jpgImageOutputStream.close();
      }
      catch (IOException e)
      {

      }
    }

    pngEncoder = null;
    jpgWriter = null;
    jpgWriterParam = null;
    /*
     * if (imageDataBuffer != null) { imageDataBuffer.flush(); imageDataBuffer =
     * null; }
     */

    if (convertedDataBuffer != null)
    {
      convertedDataBuffer.flush();
      convertedDataBuffer = null;
    }

    if (convertedGraphics != null)
    {
      convertedGraphics.dispose();
      convertedGraphics = null;
    }
  }

  public boolean isStopped()
  {
    return stopped;
  }

  public boolean isScaling()
  {
    return viewProvider.isScaling();
  }

  public double getScaleFactorX()
  {
    return viewProvider.getScaleFactorX();
  }

  public double getScaleFactorY()
  {
    return viewProvider.getScaleFactorY();
  }

  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
    if (stopped)
    {
      synchronized (screenCaptureIntervalSynchronizer)
      {
        screenCaptureIntervalSynchronizer.notify();
      }
      synchronized (this)
      {
        notify();
      }
      /*
       * if (connection.startedConnection()) { try {
       * connection.closeGraphicsModeStreams(); } catch (Throwable e) { } }
       */
      try
      {
        connection.closeGraphicsModeStreams();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
  }

  public void setDrawPointer(boolean drawPointer)
  {
    this.drawPointer = drawPointer;
  }

  public void increaseDrawPointer()
  {
    this.viewProvider.increaseDrawnCursorSize();
  }

  public void decreaseDrawPointer()
  {
    this.viewProvider.decreaseDrawnCursorSize();
  }

  public void normalizeDrawPointer()
  {
    this.viewProvider.normalizeDrawnCursorSize();
  }

  public void setColorQuality(int colorQuality)
  {
    this.viewProvider.setColorQuality(colorQuality);
  }

  /*
   * public void setSynchronousRefresh(boolean synchronousRefresh) {
   * this.synchronousRefresh = synchronousRefresh; setRefreshInterrupted(false); }
   */

  public void setRefreshInterrupted(boolean refreshInterrupted)
  {
    this.refreshInterrupted = refreshInterrupted;
    synchronized (screenCaptureIntervalSynchronizer)
    {
      screenCaptureIntervalSynchronizer.notify();
    }
    synchronized (this)
    {
      notify();
    }
  }

  public void setImageCoding(int imageCoding)
  {
    this.imageCoding = imageCoding;
  }

  public void setScreenCaptureInterval(int screenCaptureInterval)
  {
    this.screenCaptureInterval = screenCaptureInterval;
    synchronized (screenCaptureIntervalSynchronizer)
    {
      screenCaptureIntervalSynchronizer.notify();
    }
  }

  public void setNextDevice(GraphicsDevice nextDevice)
  {
    this.nextDevice = nextDevice;
  }

  /*
   * public void setScreenCaptureMode(int mode) { this.screenCaptureMode = mode; }
   */

  // public void setDynamicCoding(boolean dynamicCoding)
  // {
  // this.dynamicCoding = dynamicCoding;
  // }

  // public void setSeparatedCoding(boolean separatedCoding)
  // {
  // this.separatedCoding = separatedCoding;
  // }

  public void setCaptureArea(Rectangle captureArea, double captureScale)
  {
    this.captureArea = captureArea;
    this.captureScale = captureScale;
  }

  /*
   * public void setReader(VTGraphicsModeServerReader reader) { this.reader =
   * reader; }
   */

  public void requestClear()
  {
    clearRequested = true;
    // viewProvider.forceRefresh();
    // imageDataBuffer = imageIO.newImage(width, height, type)
  }

  public void requestRefresh()
  {
    needRefresh = true;
    synchronized (this)
    {
      notify();
    }
  }

  public void sendRemoteInterfaceAreaChange(int width, int height) throws IOException
  {
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REMOTE_INTERFACE_AREA_CHANGE);
    connection.getGraphicsControlDataOutputStream().writeInt(width);
    connection.getGraphicsControlDataOutputStream().writeInt(height);
    connection.getGraphicsControlDataOutputStream().flush();
  }

  public void sendImageRefresh() throws IOException
  {
    needRefresh = false;
    // startTime = System.currentTimeMillis();
    List<Rectangle> blockAreas = VTImageDataUtils.splitBlockArea(imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    // System.out.println("blocks_before:" + blockAreas.size());
    blockAreas = VTImageDataUtils.mergeNeighbourRectangles(blockAreas);
    // System.out.println("blocks_after:" + blockAreas.size());
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_IMAGE);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.x);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.y);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.width);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.height);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          // System.out.print("(x:" + blockArea.x + ";y:" + blockArea.y + ";w:" +
          // blockArea.width + ";h:" + blockArea.height + ")");
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        // System.out.println();
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        if (convertedDataBuffer == null)
        {
          convertedDataBuffer = VTImageIO.newImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recycledDataBuffer);
          recycledDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
//				while (!convertedGraphics.drawImage(imageDataBuffer, 0, 0, null))
//				{
//					Thread.yield();
//				}
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
    }
    else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      // int total = 0;
      for (Rectangle blockArea : blockAreas)
      {
        imageOutputBuffer.reset();
        // ImageIO.write(imageDataBuffer.getSubimage(trueArea.x,
        // trueArea.y, trueArea.width, trueArea.height), "jpg",
        // jpgImageOutputStream);
        jpgWriter.setOutput(jpgImageOutputStream);
        jpgWriter.write(null, new IIOImage(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), null, null), jpgWriterParam);
        connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
        connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
        connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
        connection.getGraphicsControlDataOutputStream().flush();
        imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        // total += imageOutputBuffer.size();
      }
      connection.getGraphicsDirectImageDataOutputStream().flush();
      // System.out.println("total:" + total);
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        if (convertedDataBuffer == null)
        {
          convertedDataBuffer = VTImageIO.newImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recycledDataBuffer);
          recycledDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
//				while (!convertedGraphics.drawImage(imageDataBuffer, 0, 0, null))
//				{
//					Thread.yield();
//				}
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
    }
    // endTime = System.currentTimeMillis();
    // System.out.println("image encoding time: " + (endTime - startTime));
    // System.out.println("image size:" + imageOutputBuffer.count());
  }

  public void sendImageDifference() throws IOException
  {
    // Rectangle trueArea = new Rectangle(Math.min(resultArea.x,
    // imageDataBuffer.getWidth()), Math.min(resultArea.y,
    // imageDataBuffer.getHeight()), Math.min(resultArea.width,
    // imageDataBuffer.getWidth() - resultArea.x),
    // Math.min(resultArea.height, imageDataBuffer.getHeight() -
    // resultArea.y));
    needRefresh = false;
    // imageOutputBuffer.reset();
    // startTime = System.currentTimeMillis();
    // List<Rectangle> blockAreas =
    // VTImageDataUtils.splitBlockArea(imageDataBuffer.getWidth(),
    // imageDataBuffer.getHeight(), resultArea, 64, 64);
    List<Rectangle> blockAreas = null;
    if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferByte, previousImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferUShort, previousImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferInt, previousImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    // System.out.println("blocks_before:" + blockAreas.size());
    blockAreas = VTImageDataUtils.mergeNeighbourRectangles(blockAreas);
    // System.out.println("blocks_after:" + blockAreas.size());
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.x);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.y);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.width);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.height);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          // System.out.print("(x:" + blockArea.x + ";y:" + blockArea.y + ";w:" +
          // blockArea.width + ";h:" + blockArea.height + ")");
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        // System.out.println();
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        if (convertedDataBuffer == null)
        {
          convertedDataBuffer = VTImageIO.newImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recycledDataBuffer);
          recycledDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
//				while (!convertedGraphics.drawImage(imageDataBuffer, 0, 0, null))
//				{
//					Thread.yield();
//				}
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
    }
    else if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG);
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      // int total = 0;
      for (Rectangle blockArea : blockAreas)
      {
        imageOutputBuffer.reset();
        // ImageIO.write(imageDataBuffer.getSubimage(trueArea.x,
        // trueArea.y, trueArea.width, trueArea.height), "jpg",
        // jpgImageOutputStream);
        jpgWriter.setOutput(jpgImageOutputStream);
        jpgWriter.write(null, new IIOImage(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), null, null), jpgWriterParam);
        connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
        connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
        connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
        connection.getGraphicsControlDataOutputStream().flush();
        imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        // total += imageOutputBuffer.size();
      }
      connection.getGraphicsDirectImageDataOutputStream().flush();
      // System.out.println("total:" + total);
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        if (convertedDataBuffer == null)
        {
          convertedDataBuffer = VTImageIO.newImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recycledDataBuffer);
          recycledDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
//				while (!convertedGraphics.drawImage(imageDataBuffer, 0, 0, null))
//				{
//					Thread.yield();
//				}
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        // int total = 0;
        for (Rectangle blockArea : blockAreas)
        {
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsControlDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsControlDataOutputStream().writeInt(blockArea.y);
          connection.getGraphicsControlDataOutputStream().flush();
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          // total += imageOutputBuffer.size();
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
        // System.out.println("total:" + total);
      }
    }
    // connection.getGraphicsDirectImageDataOutputStream().flush();
    // connection.getGraphicsDirectImageDataOutputStream().close();
  }

  public void sendCustomDifference() throws IOException
  {
    // System.out.println("sendCustomDifference");
    // Rectangle trueArea = new Rectangle(Math.min(resultArea.x,
    // imageDataBuffer.getWidth()), Math.min(resultArea.y,
    // imageDataBuffer.getHeight()), Math.min(resultArea.width,
    // imageDataBuffer.getWidth() - resultArea.x),
    // Math.min(resultArea.height, imageDataBuffer.getHeight() -
    // resultArea.y));
    // Rectangle trueArea = new Rectangle(Math.min(captureArea.x,
    // imageDataBuffer.getWidth()), Math.min(captureArea.y,
    // imageDataBuffer.getHeight()), Math.min(captureArea.width,
    // imageDataBuffer.getWidth() - captureArea.x),
    // Math.min(captureArea.height, imageDataBuffer.getHeight() -
    // captureArea.y));
    // System.out.println("sendCustomDifference");
    needRefresh = false;
    // startTime = System.currentTimeMillis();
    // imageOutputBuffer.reset();
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.x);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.y);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.width);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.height);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF);
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      // vtDifferenceCodec.encodeBufferedFrame(connection.getGraphicsSnappedImageDataOutputStream());
      // imageOutputBuffer.writeTo(connection.getGraphicsSnappedImageDataOutputStream());
      connection.getGraphicsSnappedImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF);
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      // vtDifferenceCodec.encodeBufferedFrame(connection.getGraphicsDeflatedImageDataOutputStream());
      // imageOutputBuffer.writeTo(connection.getGraphicsDeflatedImageDataOutputStream());
      connection.getGraphicsDeflatedImageDataOutputStream().flush();
    }
    // endTime = System.currentTimeMillis();
    // total += endTime - startTime;
    // number++;
    // System.out.println("time:[" + (total / number) + "]");
  }

  public void sendCustomRefresh() throws IOException
  {
    // System.out.println("sendCustomRefresh");
    needRefresh = false;
    // imageOutputBuffer.reset();
    // startTime = System.currentTimeMillis();
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_NEW_FRAME_CUSTOM);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.x);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.y);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.width);
    // connection.getGraphicsControlDataOutputStream().writeInt(resultArea.height);
    // connection.getGraphicsControlDataOutputStream().flush();
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsSnappedImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsSnappedImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame(connection.getGraphicsDeflatedImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsDeflatedImageDataOutputStream().flush();
    }
    // endTime = System.currentTimeMillis();
    // System.out.println("custom encoding time: " + (endTime - startTime));
  }

  public void sendRefreshNotNeeded() throws IOException
  {
    // synchronized (this)
    // {
    needRefresh = false;
    // }
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED);
    connection.getGraphicsControlDataOutputStream().flush();
  }

  public void sendRefreshInterrupted() throws IOException
  {
    // synchronized (this)
    // {
    needRefresh = false;
    // }
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED);
    connection.getGraphicsControlDataOutputStream().flush();
  }

  /*
   * private void sendSessionEnding() { try {
   * connection.getGraphicsControlDataOutputStream().write(VT.
   * VT_GRAPHICS_MODE_SESSION_ENDING);
   * connection.getGraphicsControlDataOutputStream().flush(); } catch (Throwable
   * e) { stopped = true; return; } }
   */

  public void finishClipboardContentsTransfer()
  {
    /*
     * try { connection.resetClipboardStreams(); } catch (Throwable e) { }
     * session.getSession().getClipboardTransferTask().setInputStream(
     * connection.getGraphicsClipboardDataInputStream());
     * session.getSession().getClipboardTransferTask().setOutputStream(
     * connection.getGraphicsClipboardDataOutputStream());
     */
  }

  public void run()
  {
    pngEncoder = new PngEncoder(PngEncoder.COLOR_INDEXED, PngEncoder.BEST_SPEED);
    pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
    vtCustomCodec = new VTQuadrupleOctalTreeFrameDifferenceCodecV9();
    // vtCustomCodec.setPixelDataBuffer(imageOutputBuffer);
    // vtDifferenceCodec.setPixelDataBuffer(imageOutputBuffer);
    try
    {
      // jpgImageOutputStream =
      // ImageIO.createImageOutputStream(imageOutputBuffer);
      jpgImageOutputStream = ImageIO.createImageOutputStream(imageOutputBuffer);
      jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
      jpgWriterParam = jpgWriter.getDefaultWriteParam();
      jpgWriterParam.setCompressionQuality(0.5f);
      // jpgWriter.write(streamMetadata, image, param)
    }
    catch (Throwable e1)
    {

    }
    while (!stopped)
    {
      try
      {
        synchronized (this)
        {
          while (!stopped && (!needRefresh))
          {
            wait();
          }
        }
        if (!stopped)
        {
          if (currentDevice != nextDevice)
          {
            currentDevice = nextDevice;
            viewProvider.setGraphicsDevice(currentDevice);
            clearRequested = true;
          }
          /*
           * if (lastCaptureScale != captureScale) { lastCaptureScale = captureScale;
           * clearRequested = true; }
           */
          if (captureArea.x >= 0 && captureArea.y >= 0)
          {
            // scaled viewport
            // check capture scale
            if (captureScale == 1)
            {
              viewProvider.setScaledDimensions(0, 0, true);
            }
            else
            {
              viewProvider.setScaleFactors(captureScale, captureScale);
            }
            // viewProvider.setScaleFactors(2, 2);
          }
          else if (captureArea.x == -1 && captureArea.y == -1)
          {
            // scaled entire
            // check capture scale
            if (captureScale == 1)
            {
              viewProvider.setScaledDimensions(0, 0, true);
            }
            else
            {
              viewProvider.setScaleFactors(captureScale, captureScale);
            }
            // viewProvider.setScaleFactors(2, 2);
          }
          else if (captureArea.x == -2 && captureArea.y == -2)
          {
            // adjusted proportional
            viewProvider.setScaledDimensions(captureArea.width, captureArea.height, true);
            // viewProvider.setScaledFactors(2.0, 2.0);
          }
          else if (captureArea.x == -3 && captureArea.y == -3)
          {
            // adjusted independent
            viewProvider.setScaledDimensions(captureArea.width, captureArea.height, false);
            // viewProvider.setScaledFactors(2.0, 2.0);
          }
          else
          {
            viewProvider.setScaledDimensions(0, 0, true);
          }
          if (clearRequested)
          {
            viewProvider.clearResources();
            lastWidth = -1;
            lastHeight = -1;
            interruptedLastWidth = -1;
            interruptedLastHeight = -1;
            lastDepth = -1;
            lastColors = -1;
            lastDataType = -1;
            clearRequested = false;
          }
          if (lastImageCoding != imageCoding)
          {
            viewProvider.clearResources();
            lastWidth = -1;
            lastHeight = -1;
            interruptedLastWidth = -1;
            interruptedLastHeight = -1;
            lastDepth = -1;
            lastColors = -1;
            lastDataType = -1;
          }
          if (!refreshInterrupted)
          {
            if (captureArea.width <= 0 || captureArea.height <= 0)
            {
              imageDataBuffer = null;
              sendRefreshNotNeeded();
            }
            else if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
            {
              try
              {
                imageDataBuffer = viewProvider.createScreenCapture(drawPointer, captureArea);
              }
              catch (Throwable t)
              {
                // t.printStackTrace();
                imageDataBuffer = null;
              }
              // imageDataBuffer =
              // viewProvider.createScreenCapture(drawPointer,
              // captureArea);
              // endTime = System.currentTimeMillis();
              // System.out.println("image capture time: " +
              // (endTime - startTime));
            }
            else
            {
              try
              {
                imageDataBuffer = viewProvider.createScreenCapture(drawPointer);
              }
              catch (Throwable t)
              {
                // t.printStackTrace();
                imageDataBuffer = null;
              }
              // imageDataBuffer =
              // viewProvider.createScreenCapture(drawPointer);
              // endTime = System.currentTimeMillis();
              // System.out.println("image capture time: " +
              // (endTime - startTime));
            }
            if (imageDataBuffer != null)
            {
              // System.out.println("colors:" +
              // (imageDataBuffer.getColorModel() instanceof
              // IndexColorModel ?
              // ((IndexColorModel)imageDataBuffer.getColorModel()).getMapSize()
              // : 0));
              if (imageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZOF && imageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_SOF)
              {
                if (imageDataBuffer.getWidth() == lastWidth && imageDataBuffer.getHeight() == lastHeight && imageDataBuffer.getColorModel().getPixelSize() == lastDepth && (imageDataBuffer.getColorModel() instanceof IndexColorModel ? ((IndexColorModel) imageDataBuffer.getColorModel()).getMapSize() == lastColors : viewProvider.getColorCount() == lastColors) && imageDataBuffer.getRaster().getDataBuffer().getDataType() == lastDataType && imageCoding == lastImageCoding
                /* && captureScale == lastCaptureScale */)
                {
                  boolean different = false;
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea);
                  }
                  if (different)
                  {
                    sendImageDifference();
                    if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                    {
                      VTImageDataUtils.copyArea(lastImageBufferByte, previousImageBufferByte, 0, lastWidth, lastHeight, resultArea);
                    }
                    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                    {
                      VTImageDataUtils.copyArea(lastImageBufferUShort, previousImageBufferUShort, 0, lastWidth, lastHeight, resultArea);
                    }
                    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                    {
                      VTImageDataUtils.copyArea(lastImageBufferInt, previousImageBufferInt, 0, lastWidth, lastHeight, resultArea);
                    }
                  }
                  else
                  {
                    sendRefreshNotNeeded();
                  }
                }
                else
                {
                  lastWidth = imageDataBuffer.getWidth();
                  lastHeight = imageDataBuffer.getHeight();
                  interruptedLastWidth = lastWidth;
                  interruptedLastHeight = lastHeight;
                  lastDepth = imageDataBuffer.getColorModel().getPixelSize();
                  lastColors = (imageDataBuffer.getColorModel() instanceof IndexColorModel ? ((IndexColorModel) imageDataBuffer.getColorModel()).getMapSize() : viewProvider.getColorCount());
                  lastDataType = imageDataBuffer.getRaster().getDataBuffer().getDataType();
                  lastImageCoding = imageCoding;
                  // lastCaptureScale = captureScale;

                  if (convertedDataBuffer != null)
                  {
                    convertedDataBuffer.flush();
                    convertedDataBuffer = null;
                  }
                  if (convertedGraphics != null)
                  {
                    convertedGraphics.dispose();
                    convertedGraphics = null;
                  }
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    lastImageBufferByte = ((DataBufferByte) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferByte == null || previousImageBufferByte.length < lastWidth * lastHeight)
                    {
                      previousImageBufferByte = new byte[lastWidth * lastHeight];
                    }
                    lastImageBufferUShort = null;
                    previousImageBufferUShort = null;
                    lastImageBufferInt = null;
                    previousImageBufferInt = null;
                    VTImageIO.clearBuffer(previousImageBufferByte, BufferedImage.TYPE_BYTE_INDEXED, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    lastImageBufferByte = null;
                    previousImageBufferByte = null;
                    lastImageBufferUShort = ((DataBufferUShort) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferUShort == null || previousImageBufferUShort.length < lastWidth * lastHeight)
                    {
                      previousImageBufferUShort = new short[lastWidth * lastHeight];
                    }
                    lastImageBufferInt = null;
                    previousImageBufferInt = null;
                    VTImageIO.clearBuffer(previousImageBufferUShort, BufferedImage.TYPE_USHORT_555_RGB, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    lastImageBufferByte = null;
                    previousImageBufferByte = null;
                    lastImageBufferUShort = null;
                    previousImageBufferUShort = null;
                    lastImageBufferInt = ((DataBufferInt) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferInt == null || previousImageBufferInt.length < lastWidth * lastHeight)
                    {
                      previousImageBufferInt = new int[lastWidth * lastHeight];
                    }
                    VTImageIO.clearBuffer(previousImageBufferInt, BufferedImage.TYPE_INT_RGB, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  sendImageRefresh();
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    VTImageDataUtils.copyArea(lastImageBufferByte, previousImageBufferByte, 0, lastWidth, lastHeight, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    VTImageDataUtils.copyArea(lastImageBufferUShort, previousImageBufferUShort, 0, lastWidth, lastHeight, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    VTImageDataUtils.copyArea(lastImageBufferInt, previousImageBufferInt, 0, lastWidth, lastHeight, resultArea);
                  }
                  System.runFinalization();
                  System.gc();
                }
              }
              else
              {
                if (imageDataBuffer.getWidth() == lastWidth && imageDataBuffer.getHeight() == lastHeight && imageDataBuffer.getColorModel().getPixelSize() == lastDepth && (imageDataBuffer.getColorModel() instanceof IndexColorModel ? ((IndexColorModel) imageDataBuffer.getColorModel()).getMapSize() == lastColors : viewProvider.getColorCount() == lastColors) && imageDataBuffer.getRaster().getDataBuffer().getDataType() == lastDataType && imageCoding == lastImageCoding
                /* && captureScale == lastCaptureScale */)
                {
                  // startTime = System.currentTimeMillis();
                  // lastImageCoding = imageCoding;
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    if (!VTImageDataUtils.deltaArea(previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea))
                    {
                      sendCustomDifference();
                    }
                    else
                    {
                      sendRefreshNotNeeded();
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    if (!VTImageDataUtils.deltaArea(previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea))
                    {
                      sendCustomDifference();
                    }
                    else
                    {
                      sendRefreshNotNeeded();
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    if (!VTImageDataUtils.deltaArea(previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), captureArea, resultArea))
                    {
                      sendCustomDifference();
                    }
                    else
                    {
                      sendRefreshNotNeeded();
                    }
                  }
                }
                else
                {
                  lastWidth = imageDataBuffer.getWidth();
                  lastHeight = imageDataBuffer.getHeight();
                  interruptedLastWidth = lastWidth;
                  interruptedLastHeight = lastHeight;
                  lastDepth = imageDataBuffer.getColorModel().getPixelSize();
                  lastColors = (imageDataBuffer.getColorModel() instanceof IndexColorModel ? ((IndexColorModel) imageDataBuffer.getColorModel()).getMapSize() : viewProvider.getColorCount());
                  lastDataType = imageDataBuffer.getRaster().getDataBuffer().getDataType();
                  lastImageCoding = imageCoding;
                  // lastCaptureScale = captureScale;
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    lastImageBufferByte = ((DataBufferByte) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferByte == null || previousImageBufferByte.length < lastWidth * lastHeight)
                    {
                      previousImageBufferByte = new byte[lastWidth * lastHeight];
                    }
                    lastImageBufferUShort = null;
                    previousImageBufferUShort = null;
                    lastImageBufferInt = null;
                    previousImageBufferInt = null;
                    VTImageIO.clearBuffer(previousImageBufferByte, BufferedImage.TYPE_BYTE_INDEXED, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    lastImageBufferByte = null;
                    previousImageBufferByte = null;
                    lastImageBufferUShort = ((DataBufferUShort) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferUShort == null || previousImageBufferUShort.length < lastWidth * lastHeight)
                    {
                      previousImageBufferUShort = new short[lastWidth * lastHeight];
                    }
                    lastImageBufferInt = null;
                    previousImageBufferInt = null;
                    VTImageIO.clearBuffer(previousImageBufferUShort, BufferedImage.TYPE_USHORT_555_RGB, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    lastImageBufferByte = null;
                    previousImageBufferByte = null;
                    lastImageBufferUShort = null;
                    previousImageBufferUShort = null;
                    lastImageBufferInt = ((DataBufferInt) imageDataBuffer.getRaster().getDataBuffer()).getData();
                    if (previousImageBufferInt == null || previousImageBufferInt.length < lastWidth * lastHeight)
                    {
                      previousImageBufferInt = new int[lastWidth * lastHeight];
                    }
                    VTImageIO.clearBuffer(previousImageBufferInt, BufferedImage.TYPE_INT_RGB, lastColors, 0);
                    if (captureArea.x >= 0 && captureArea.y >= 0)
                    {
                      VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), null, resultArea);
                    }
                    else
                    {
                      resultArea.x = 0;
                      resultArea.y = 0;
                      resultArea.width = lastWidth;
                      resultArea.height = lastHeight;
                    }
                  }
                  sendCustomRefresh();
                  System.runFinalization();
                  System.gc();
                }
              }
            }
            else
            {
              sendRefreshNotNeeded();
            }
          }
          else
          {
            Dimension scaledSize = viewProvider.getCurrentScaledSize();
            if (scaledSize.width != interruptedLastWidth || scaledSize.height != interruptedLastHeight)
            {
              interruptedLastWidth = scaledSize.width;
              interruptedLastHeight = scaledSize.height;
              sendRemoteInterfaceAreaChange(interruptedLastWidth, interruptedLastHeight);
            }
            sendRefreshInterrupted();
          }
          // Manually control the CPU Throttle!
          synchronized (screenCaptureIntervalSynchronizer)
          {
            if (screenCaptureInterval > 0)
            {
              screenCaptureIntervalSynchronizer.wait(screenCaptureInterval);
            }
            else
            {
              screenCaptureIntervalSynchronizer.wait(0, 1);
            }
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
    // sendSessionEnding();
    synchronized (session)
    {
      session.notify();
    }
  }
}