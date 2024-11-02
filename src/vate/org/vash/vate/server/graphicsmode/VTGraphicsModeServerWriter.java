package org.vash.vate.server.graphicsmode;

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
import java.io.IOException;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

import org.vash.vate.VT;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.codec.VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII;
import org.vash.vate.graphics.image.VTImageDataUtils;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.objectplanet.image.PngEncoder;
import com.pngencoder.PngEncoder;

public class VTGraphicsModeServerWriter implements Runnable
{
  private static final int CODEC_PADDING_SIZE = VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII.PADDING_SIZE;
  private static final int IMAGE_OUTPUT_BUFFER_SIZE = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private volatile boolean stopped;
  private boolean needRefresh;
  private boolean clearRequested;
  private boolean refreshInterrupted;
  private boolean drawPointer;
  private int screenCaptureInterval;
  private int imageCoding;
  private int lastImageCoding;
  private int lastWidth;
  private int lastHeight;
  private int lastDepth;
  private int lastDataType;
  private int lastColors;
  private int interruptedLastWidth;
  private int interruptedLastHeight;
  private byte[] lastImageBufferByte;
  private byte[] previousImageBufferByte;
  private short[] lastImageBufferUShort;
  private short[] previousImageBufferUShort;
  private int[] lastImageBufferInt;
  private int[] previousImageBufferInt;
  private Rectangle captureArea;
  private Rectangle resultArea;
  private double captureScale;
  private BufferedImage imageDataBuffer;
  private BufferedImage convertedDataBuffer;
  private Graphics2D convertedGraphics;
  private GraphicsDevice nextDevice;
  private GraphicsDevice currentDevice;
  private VTByteArrayOutputStream imageOutputBuffer = new VTByteArrayOutputStream(IMAGE_OUTPUT_BUFFER_SIZE);
  private VTAWTScreenCaptureProvider viewProvider;
  private VTServerConnection connection;
  private VTGraphicsModeServerSession session;
  private VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII vtCustomCodec;
  private Object screenCaptureIntervalSynchronizer;
  private ImageWriter jpgWriter;
  private ImageWriteParam jpgWriterParam;
  private ImageOutputStream jpgImageOutputStream;
  private PngEncoder pngEncoder;
  private DataBuffer recyclableDataBuffer;
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
    this.imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD;
    this.viewProvider.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
    this.refreshInterrupted = false;
    this.clearRequested = false;
    this.screenCaptureIntervalSynchronizer = new Object();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    this.currentDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    // this.currentDevice = null;
    this.nextDevice = currentDevice;
    this.viewProvider.setGraphicsDevice(currentDevice);
  }
  
  public void dispose()
  {
    stopped = true;
    drawPointer = true;
    needRefresh = false;
    screenCaptureInterval = 250;
    captureScale = 1;
    imageCoding = VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD;
    viewProvider.setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
    refreshInterrupted = false;
    clearRequested = false;
    lastWidth = 0;
    lastHeight = 0;
    lastDepth = 0;
    lastDataType = 0;
    lastColors = 0;
    interruptedLastWidth = 0;
    interruptedLastWidth = 0;
    lastImageCoding = 0;
    vtCustomCodec = null;
    
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
      try
      {
        connection.closeGraphicsModeStreams();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
      synchronized (screenCaptureIntervalSynchronizer)
      {
        screenCaptureIntervalSynchronizer.notify();
      }
      synchronized (this)
      {
        notify();
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
  
  public void setCaptureArea(Rectangle captureArea, double captureScale)
  {
    this.captureArea = captureArea;
    this.captureScale = captureScale;
  }
  
  public void requestClear()
  {
    clearRequested = true;
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
    //System.out.println("sendImageRefresh");
    //long startTime = System.nanoTime();
    //long pixels = 0;
    needRefresh = false;
    List<Rectangle> blockAreas = VTImageDataUtils.splitBlockArea(imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    //System.out.println("blocks_before:" + blockAreas.size());
    blockAreas = VTImageDataUtils.mergeNeighbourRectangles(blockAreas);
    //System.out.println("blocks_after:" + blockAreas.size());
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_FRAME_IMAGE);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
    {
      if (lastColors == 16 || lastColors == 8 || lastColors == 4)
      {
        convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_BYTE_GRAY, 0, recyclableDataBuffer);
        recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
        convertedGraphics = convertedDataBuffer.createGraphics();
        convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        imageDataBuffer = convertedDataBuffer;
        jpgWriterParam.setDestinationType(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY));
      }
      else
      {
        jpgWriterParam.setDestinationType(jpgWriter.getDefaultWriteParam().getDestinationType());
      }
      
      IIOMetadata jpgWriterMetadata = setJpegSubsamplingMode444(jpgWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(imageDataBuffer), jpgWriterParam));
      
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG);
      if (lastColors == 16 || lastColors == 8 || lastColors == 4)
      {
        connection.getGraphicsControlDataOutputStream().writeInt(BufferedImage.TYPE_BYTE_GRAY);
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      }
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      for (Rectangle blockArea : blockAreas)
      {
        //pixels += blockArea.width * blockArea.height;
        imageOutputBuffer.reset();
        jpgWriter.setOutput(jpgImageOutputStream);
        BufferedImage subImage = imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height);
        jpgWriter.write(jpgWriterMetadata, new IIOImage(subImage, null, jpgWriterMetadata), jpgWriterParam);
        connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
        connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
        connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
        imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
      }
      connection.getGraphicsDirectImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        for (Rectangle blockArea : blockAreas)
        {
          //pixels += blockArea.width * blockArea.height;
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
        recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
        convertedGraphics = convertedDataBuffer.createGraphics();
        convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        //pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        for (Rectangle blockArea : blockAreas)
        {
          //pixels += blockArea.width * blockArea.height;
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        if (lastColors == 262144 || lastColors == 2097152)
        {
          convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
          recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
          convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
          //pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
          for (Rectangle blockArea : blockAreas)
          {
            //pixels += blockArea.width * blockArea.height;
            imageOutputBuffer.reset();
            pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
            imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          }
        }
        else
        {
          //pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
          for (Rectangle blockArea : blockAreas)
          {
            //pixels += blockArea.width * blockArea.height;
            imageOutputBuffer.reset();
            pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
            imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          }
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
    }
    //long endTime = System.nanoTime();
    //System.out.println("image encoding time: " + (endTime - startTime) / 1000);
  }
  
  public void sendImageDifference() throws IOException
  {
    //long startTime = System.nanoTime();
    //long pixels = 0;
    needRefresh = false;
    List<Rectangle> blockAreas = null;
    if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferByte, previousImageBufferByte, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferUShort, previousImageBufferUShort, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
    {
      blockAreas = VTImageDataUtils.compareBlockArea(lastImageBufferInt, previousImageBufferInt, 0, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea, 64, 64);
    }
    //System.out.println("blocks_before:" + blockAreas.size());
    blockAreas = VTImageDataUtils.mergeNeighbourRectangles(blockAreas);
    //System.out.println("blocks_after:" + blockAreas.size());
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_IMAGE);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG)
    {
      if (lastColors == 16 || lastColors == 8 || lastColors == 4)
      {
        if (convertedDataBuffer == null || convertedDataBuffer.getType() != BufferedImage.TYPE_BYTE_GRAY)
        {
          convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_BYTE_GRAY, 0, recyclableDataBuffer);
          recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        imageDataBuffer = convertedDataBuffer;
        jpgWriterParam.setDestinationType(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY));
      }
      else
      {
        jpgWriterParam.setDestinationType(jpgWriter.getDefaultWriteParam().getDestinationType());
      }
      
      IIOMetadata jpgWriterMetadata = setJpegSubsamplingMode444(jpgWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(imageDataBuffer), jpgWriterParam));
      
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG);
      if (lastColors == 16 || lastColors == 8 || lastColors == 4)
      {
        connection.getGraphicsControlDataOutputStream().writeInt(BufferedImage.TYPE_BYTE_GRAY);
      }
      else
      {
        connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      }
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      for (Rectangle blockArea : blockAreas)
      {
        //pixels += blockArea.width * blockArea.height;
        imageOutputBuffer.reset();
        jpgWriter.setOutput(jpgImageOutputStream);
        BufferedImage subImage = imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height);
        jpgWriter.write(jpgWriterMetadata, new IIOImage(subImage, null, jpgWriterMetadata), jpgWriterParam);
        connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
        connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
        connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
        imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
      }
      connection.getGraphicsDirectImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(blockAreas.size());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
        pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
        for (Rectangle blockArea : blockAreas)
        {
          //pixels += blockArea.width * blockArea.height;
          imageOutputBuffer.reset();
          pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        if (convertedDataBuffer == null || convertedDataBuffer.getType() != BufferedImage.TYPE_INT_RGB)
        {
          convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
          recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
          convertedGraphics = convertedDataBuffer.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
        }
        convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
        pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
        for (Rectangle blockArea : blockAreas)
        {
          //pixels += blockArea.width * blockArea.height;
          imageOutputBuffer.reset();
          pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
          connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
          imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        if (lastColors == 262144 || lastColors == 2097152)
        {
          if (convertedDataBuffer == null || convertedDataBuffer.getType() != BufferedImage.TYPE_INT_RGB)
          {
            convertedDataBuffer = VTImageIO.createImage(0, 0, lastWidth, lastHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
            recyclableDataBuffer = convertedDataBuffer.getRaster().getDataBuffer();
            convertedGraphics = convertedDataBuffer.createGraphics();
            convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
          }
          convertedGraphics.drawImage(imageDataBuffer, 0, 0, null);
          pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
          for (Rectangle blockArea : blockAreas)
          {
            //pixels += blockArea.width * blockArea.height;
            imageOutputBuffer.reset();
            pngEncoder.encode(convertedDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
            imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          }
        }
        else
        {
          pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
          for (Rectangle blockArea : blockAreas)
          {
            //pixels += blockArea.width * blockArea.height;
            imageOutputBuffer.reset();
            pngEncoder.encode(imageDataBuffer.getSubimage(blockArea.x, blockArea.y, blockArea.width, blockArea.height), imageOutputBuffer);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(imageOutputBuffer.size());
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.x);
            connection.getGraphicsDirectImageDataOutputStream().writeInt(blockArea.y);
            imageOutputBuffer.writeTo(connection.getGraphicsDirectImageDataOutputStream());
          }
        }
        connection.getGraphicsDirectImageDataOutputStream().flush();
      }
    }
    //long endTime = System.nanoTime();
    //System.out.println("image encoding time: " + (endTime - startTime) / 1000);
  }
  
  public void sendCustomDifference() throws IOException
  {
    // System.out.println("sendCustomDifference");
    needRefresh = false;
    // long startTime = System.currentTimeMillis();
    // System.out.println("VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM");
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_DIFFERENTIAL_FRAME_CUSTOM);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD);
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame8(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame15(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame24(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsFastImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD);
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame8(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame15(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame24(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsHeavyImageDataOutputStream().flush();
    }
    // long endTime = System.currentTimeMillis();
    // System.out.println("custom encoding time: " + (endTime - startTime));
    // total += endTime - startTime;
    // number++;
    // System.out.println("time:[" + (total / number) + "]");
  }
  
  public void sendCustomRefresh() throws IOException
  {
    //System.out.println("sendCustomRefresh");
    needRefresh = false;
    // long startTime = System.currentTimeMillis();
    //System.out.println("VT_GRAPHICS_MODE_GRAPHICS_INDEPENDENT_FRAME_CUSTOM");
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_FRAME_CUSTOM);
    if (imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD)
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame8(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame15(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame24(connection.getGraphicsFastImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsFastImageDataOutputStream().flush();
    }
    else
    {
      connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getType());
      connection.getGraphicsControlDataOutputStream().writeInt(lastColors);
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getWidth());
      connection.getGraphicsControlDataOutputStream().writeInt(imageDataBuffer.getHeight());
      connection.getGraphicsControlDataOutputStream().flush();
      if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
      {
        vtCustomCodec.encodeFrame8(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferByte, lastImageBufferByte, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
      {
        vtCustomCodec.encodeFrame15(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferUShort, lastImageBufferUShort, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
      {
        vtCustomCodec.encodeFrame24(connection.getGraphicsHeavyImageDataOutputStream(), previousImageBufferInt, lastImageBufferInt, imageDataBuffer.getWidth(), imageDataBuffer.getHeight(), resultArea.x, resultArea.y, resultArea.width, resultArea.height);
      }
      connection.getGraphicsHeavyImageDataOutputStream().flush();
    }
    // long endTime = System.currentTimeMillis();
    // System.out.println("custom encoding time: " + (endTime - startTime));
  }
  
  public void sendRefreshNotNeeded() throws IOException
  {
    needRefresh = false;
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_NOT_NEEDED);
    connection.getGraphicsControlDataOutputStream().flush();
  }
  
  public void sendRefreshInterrupted() throws IOException
  {
    needRefresh = false;
    connection.getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_GRAPHICS_REFRESH_MODE_INTERRUPTED);
    connection.getGraphicsControlDataOutputStream().flush();
  }
  
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
  
//  private IIOMetadata setJpegGrayscale(IIOMetadata metadata)
//  {
//    try
//    {
//      Node rootNode = metadata.getAsTree(metadata.getNativeMetadataFormatName());
//      if (rootNode != null && rootNode.getLastChild() != null)
//      {
//        Node markerNode = rootNode.getLastChild();
//        NodeList markers = markerNode.getChildNodes();
//        for (int i = 0; i < markers.getLength(); i++)
//        {
//          Node node = markers.item(i);
//          Node remove1 = null;
//          Node remove2 = null;
//          if (node.getNodeName().equalsIgnoreCase("sof") && node.hasChildNodes() && node.getChildNodes().getLength() == 3)
//          {
//            NodeList children = node.getChildNodes();
//            remove1 = children.item(1);
//            remove2 = children.item(2);
//          }
//          if (remove1 != null)
//          {
//            node.removeChild(remove1);
//          }
//          if (remove2 != null)
//          {
//            node.removeChild(remove2);
//          }
//        }
//      }
//      if (rootNode != null)
//      {
//        metadata.setFromTree(metadata.getNativeMetadataFormatName(), rootNode);
//      }
//    }
//    catch (Throwable t)
//    {
//      
//    }
//    return metadata;
//  }
  
  private IIOMetadata setJpegSubsamplingMode444(IIOMetadata metadata)
  {
    // Tweaking the image metadata to override default subsampling(4:2:0) with
    // 4:4:4.
    try
    {
      Node rootNode = metadata.getAsTree(metadata.getNativeMetadataFormatName());
      // The top level root node has two children, out of which the second one
      // will
      // contain all the information related to image markers.
      if (rootNode != null && rootNode.getLastChild() != null)
      {
        Node markerNode = rootNode.getLastChild();
        NodeList markers = markerNode.getChildNodes();
        // Search for 'SOF' marker where subsampling information is stored.
        for (int i = 0; i < markers.getLength(); i++)
        {
          Node node = markers.item(i);
          // 'SOF' marker can have
          // 1 child node if the color representation is greyscale,
          // 3 child nodes if the color representation is YCbCr, and
          // 4 child nodes if the color representation is YCMK.
          // This subsampling applies only to YCbCr.
          if (node.getNodeName().equalsIgnoreCase("sof") && node.hasChildNodes() && node.getChildNodes().getLength() == 3)
          {
            // In 'SOF' marker, first child corresponds to the luminance
            // channel, and setting
            // the HsamplingFactor and VsamplingFactor to 1, will imply 4:4:4
            // chroma subsampling.
            NamedNodeMap attrMap = node.getFirstChild().getAttributes();
            int samplingMode = 17;
            // int samplingMode = 33;
            attrMap.getNamedItem("HsamplingFactor").setNodeValue((samplingMode & 0xf) + "");
            attrMap.getNamedItem("VsamplingFactor").setNodeValue(((samplingMode >> 4) & 0xf) + "");
            // attrMap.getNamedItem("HsamplingFactor").setNodeValue(1 + "");
            // attrMap.getNamedItem("VsamplingFactor").setNodeValue(1 + "");
            break;
          }
        }
      }
      if (rootNode != null)
      {
        metadata.setFromTree(metadata.getNativeMetadataFormatName(), rootNode);
      }
    }
    catch (Throwable t)
    {
      
    }
    return metadata;
  }
  
  public void run()
  {
    vtCustomCodec = new VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII();
    
    try
    {
      pngEncoder = new PngEncoder().withCompressionLevel(1);
      //pngEncoder = new PngEncoder(PngEncoder.COLOR_INDEXED, PngEncoder.BEST_SPEED);
      //pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
      
      jpgImageOutputStream = ImageIO.createImageOutputStream(imageOutputBuffer);
      jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
      jpgWriterParam = jpgWriter.getDefaultWriteParam();
      if (jpgWriterParam.canWriteCompressed())
      {
        jpgWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriterParam.setCompressionQuality(0.75f);
      }
      if (jpgWriterParam.canWriteProgressive())
      {
        jpgWriterParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
      }
      if (jpgWriterParam.canWriteTiles())
      {
        jpgWriterParam.setTilingMode(ImageWriteParam.MODE_DISABLED);
      }
    }
    catch (Throwable e1)
    {
      //e1.printStackTrace();
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
          }
          else if (captureArea.x == -2 && captureArea.y == -2)
          {
            // adjusted proportional
            viewProvider.setScaledDimensions(captureArea.width, captureArea.height, true);
          }
          else if (captureArea.x == -3 && captureArea.y == -3)
          {
            // adjusted independent
            viewProvider.setScaledDimensions(captureArea.width, captureArea.height, false);
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
//          if (lastImageCoding != imageCoding)
//          {
//            viewProvider.clearResources();
//            lastWidth = -1;
//            lastHeight = -1;
//            interruptedLastWidth = -1;
//            interruptedLastHeight = -1;
//            lastDepth = -1;
//            lastColors = -1;
//            lastDataType = -1;
//          }
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
                imageDataBuffer = viewProvider.createScreenCapture(captureArea, imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD || imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD ? CODEC_PADDING_SIZE : 0, drawPointer);
                //imageDataBuffer = viewProvider.createScreenCapture(captureArea, CODEC_PADDING_SIZE, drawPointer);
              }
              catch (Throwable t)
              {
                //t.printStackTrace();
                imageDataBuffer = null;
              }
            }
            else
            {
              try
              {
                imageDataBuffer = viewProvider.createScreenCapture(imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD || imageCoding == VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD ? CODEC_PADDING_SIZE : 0, drawPointer);
                //imageDataBuffer = viewProvider.createScreenCapture(CODEC_PADDING_SIZE, drawPointer);
              }
              catch (Throwable t)
              {
                //t.printStackTrace();
                imageDataBuffer = null;
              }
            }
            if (imageDataBuffer != null)
            {
              if (imageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD && imageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD)
              {
                if (imageDataBuffer.getWidth() == lastWidth
                && imageDataBuffer.getHeight() == lastHeight
                && imageDataBuffer.getColorModel().getPixelSize() == lastDepth
                && viewProvider.getColorCount() == lastColors
                && imageDataBuffer.getRaster().getDataBuffer().getDataType() == lastDataType
                && (lastImageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_JPG || lastImageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_PNG))
                //&& imageCoding == lastImageCoding)
                {
                  boolean different = false;
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    different = !VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, 0, lastWidth, lastHeight, null, resultArea);
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
                  lastColors = viewProvider.getColorCount();
                  lastDataType = imageDataBuffer.getRaster().getDataBuffer().getDataType();
                  lastImageCoding = imageCoding;
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
                    VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, 0, lastWidth, lastHeight, null, resultArea);
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
                    VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, 0, lastWidth, lastHeight, null, resultArea);
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
                    VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, 0, lastWidth, lastHeight, null, resultArea);
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
                if (imageDataBuffer.getWidth() + CODEC_PADDING_SIZE == lastWidth
                && imageDataBuffer.getHeight() + CODEC_PADDING_SIZE == lastHeight
                && imageDataBuffer.getColorModel().getPixelSize() == lastDepth
                && viewProvider.getColorCount() == lastColors
                && imageDataBuffer.getRaster().getDataBuffer().getDataType() == lastDataType
                && (lastImageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_ZSD || lastImageCoding != VT.VT_GRAPHICS_MODE_GRAPHICS_IMAGE_CODING_GZD))
                //&& imageCoding == lastImageCoding)
                {
                  // startTime = System.currentTimeMillis();
                  boolean different = false;
                  if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE)
                  {
                    different = !VTImageDataUtils.deltaArea(previousImageBufferByte, lastImageBufferByte, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
                  {
                    different = !VTImageDataUtils.deltaArea(previousImageBufferUShort, lastImageBufferUShort, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  else if (imageDataBuffer.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
                  {
                    different = !VTImageDataUtils.deltaArea(previousImageBufferInt, lastImageBufferInt, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  if (different)
                  {
                    resultArea.x = Math.max(resultArea.x - CODEC_PADDING_SIZE, 0);
                    resultArea.y = Math.max(resultArea.y - CODEC_PADDING_SIZE, 0);
                    sendCustomDifference();
                  }
                  else
                  {
                    resultArea.x = Math.max(resultArea.x - CODEC_PADDING_SIZE, 0);
                    resultArea.y = Math.max(resultArea.y - CODEC_PADDING_SIZE, 0);
                    sendRefreshNotNeeded();
                  }
                }
                else
                {
                  lastWidth = imageDataBuffer.getWidth() + CODEC_PADDING_SIZE;
                  lastHeight = imageDataBuffer.getHeight() + CODEC_PADDING_SIZE;
                  interruptedLastWidth = imageDataBuffer.getWidth();
                  interruptedLastHeight = imageDataBuffer.getHeight();
                  //lastWidth = imageDataBuffer.getWidth();
                  //lastHeight = imageDataBuffer.getHeight();
                  //interruptedLastWidth = lastWidth;
                  //interruptedLastHeight = lastHeight;
                  lastDepth = imageDataBuffer.getColorModel().getPixelSize();
                  lastColors = viewProvider.getColorCount();
                  lastDataType = imageDataBuffer.getRaster().getDataBuffer().getDataType();
                  lastImageCoding = imageCoding;
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
                    VTImageDataUtils.deltaArea(lastImageBufferByte, previousImageBufferByte, 0, lastWidth, lastHeight, null, resultArea);
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
                    VTImageDataUtils.deltaArea(lastImageBufferUShort, previousImageBufferUShort, 0, lastWidth, lastHeight, null, resultArea);
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
                    VTImageDataUtils.deltaArea(lastImageBufferInt, previousImageBufferInt, 0, lastWidth, lastHeight, null, resultArea);
                  }
                  resultArea.x = Math.max(resultArea.x - CODEC_PADDING_SIZE, 0);
                  resultArea.y = Math.max(resultArea.y - CODEC_PADDING_SIZE, 0);
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
          // Manually control the CPU throttle!
          if (screenCaptureInterval > 0)
          {
            synchronized (screenCaptureIntervalSynchronizer)
            {
              screenCaptureIntervalSynchronizer.wait(screenCaptureInterval);
            }
          }
          else
          {
            
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
    // sendSessionEnding();
    synchronized (session)
    {
      session.notify();
    }
  }
}