package org.vash.vate.server.screenshot;

import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import org.vash.vate.VT;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.objectplanet.image.PngEncoder;

// import org.eclipse.swt.SWT;

public class VTServerScreenshotTask extends VTTask
{
  private static final int fileScreenshotBufferSize = VT.VT_FILE_BUFFER_SIZE_BYTES;
  private boolean finished;
  private boolean drawPointer;
  private boolean useJPG;
  private Integer deviceNumber;
  private DateFormat firstFormat;
  private DateFormat secondFormat;
  private OutputStream photoOutputStream;
  private File screenshotFile;
  private GregorianCalendar clock;
  private VTServerConnection connection;
  // private VTServerSession session;
  private VTAWTScreenCaptureProvider screenshotProvider;
  private PngEncoder pngEncoder;
  private BufferedImage convertedImage = null;
  private Graphics2D convertedGraphics = null;
  private DataBuffer recyclableDataBuffer;
  private ImageWriter jpgWriter;
  private ImageWriteParam jpgWriterParam;
  private ImageOutputStream jpgImageOutputStream;
  // private VTServerGraphicsResource graphicsResource;
  
  public VTServerScreenshotTask(VTServerSession session)
  {
    // this.session = session;
    this.connection = session.getConnection();
    this.screenshotProvider = session.getScreenshotProvider();
    this.drawPointer = false;
    this.clock = new GregorianCalendar();
    this.firstFormat = new SimpleDateFormat("G", Locale.ENGLISH);
    this.secondFormat = new SimpleDateFormat("MM-dd-'T'-HH-mm-ss-SSS-z");
    this.finished = true;
    this.deviceNumber = null;
    this.useJPG = false;
    this.pngEncoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
    this.pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
    try
    {
      this.jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
      this.jpgWriterParam = jpgWriter.getDefaultWriteParam();
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
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    // this.screenshotProvider.setGraphicsDevice(null);
    // this.provider = new
    // VTSWTScreenshotProvider(session.getServer().getGraphics());
  }
  
  public void dispose()
  {
    pngEncoder = null;
    if (convertedImage != null)
    {
      convertedImage.flush();
      convertedImage = null;
    }
    if (convertedGraphics != null)
    {
      convertedGraphics.dispose();
      convertedGraphics = null;
    }
    if (recyclableDataBuffer != null)
    {
      recyclableDataBuffer = null;
    }
    // if (screenshotProvider != null)
    // {
    // screenshotProvider.dispose();
    // }
    // screenshotProvider = null;
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public void setColorQuality(int colorQuality)
  {
    this.screenshotProvider.setColorQuality(colorQuality);
  }
  
  public void setDrawPointer(boolean drawPointer)
  {
    this.drawPointer = drawPointer;
  }
  
  public void setDeviceNumber(Integer deviceNumber)
  {
    this.deviceNumber = deviceNumber;
  }
  
  public void setUseJPG(boolean jpg)
  {
    this.useJPG = jpg;
  }
  
  @SuppressWarnings("all")
  public void task()
  {
    try
    {
      if (deviceNumber != null)
      {
        // screenshotProvider.setGraphicsDevice(graphicsDevice)
        if (deviceNumber > -1)
        {
          GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
          if (devices != null)
          {
            deviceNumber = Math.min(deviceNumber, devices.length - 1);
            if (deviceNumber >= 0)
            {
              screenshotProvider.setGraphicsDevice(devices[deviceNumber]);
            }
            else
            {
              screenshotProvider.setGraphicsDevice(null);
            }
          }
          else
          {
            screenshotProvider.setGraphicsDevice(null);
          }
        }
        else
        {
          screenshotProvider.setGraphicsDevice(null);
        }
      }
      else
      {
        screenshotProvider.setGraphicsDevice(null);
      }
      if (!screenshotProvider.isScreenCaptureInitialized(0) && !screenshotProvider.initializeScreenCapture(0))
      {
        synchronized (this)
        {
          try
          {
            connection.getResultWriter().write("\nVT>Screen capture initialization failed!\nVT>");
            connection.getResultWriter().flush();
          }
          catch (Throwable e)
          {
            
          }
          finished = true;
          return;
        }
      }
      //connection.getResultWriter().write("\nVT>Screen capture starting...\nVT>");
      //connection.getResultWriter().flush();
      // provider.initialize();
      // clock.setTimeInMillis(System.currentTimeMillis());
      clock.setTime(Calendar.getInstance().getTime());
      if (useJPG)
      {
        screenshotFile = new File(firstFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondFormat.format(clock.getTime()) + ".jpg");
      }
      else
      {
        screenshotFile = new File(firstFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondFormat.format(clock.getTime()) + ".png");
      }
      
      photoOutputStream = new BufferedOutputStream(Channels.newOutputStream(new FileOutputStream(screenshotFile).getChannel()), fileScreenshotBufferSize);
      // screenshotProvider.writeHighQualityScreenshot(photoOutputStream,
      // SWT.IMAGE_BMP);
      BufferedImage screenCapture = screenshotProvider.createScreenCapture(0, drawPointer);
      connection.getResultWriter().write("\nVT>Screen capture data obtained, image will be saved in:\nVT>[" + screenshotFile.getAbsolutePath() + "]\nVT>");
      connection.getResultWriter().flush();
      
      //int lastColors = screenshotProvider.getColorCount();
      
      if (useJPG)
      {
        if (screenshotProvider.getColorCount() == 16 || screenshotProvider.getColorCount() == 8 || screenshotProvider.getColorCount() == 4)
        {
          jpgWriterParam.setDestinationType(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_BYTE_GRAY));
          IIOMetadata jpgWriterMetadata = setJpegSubsamplingMode444(jpgWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(screenCapture), jpgWriterParam));
          convertedImage = VTImageIO.createImage(0, 0, screenCapture.getWidth(), screenCapture.getHeight(), BufferedImage.TYPE_BYTE_GRAY, 0, recyclableDataBuffer);
          recyclableDataBuffer = convertedImage.getRaster().getDataBuffer();
          convertedGraphics = convertedImage.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
          convertedGraphics.drawImage(screenCapture, 0, 0, null);
          jpgImageOutputStream = ImageIO.createImageOutputStream(photoOutputStream);
          jpgWriter.setOutput(jpgImageOutputStream);
          jpgWriter.write(jpgWriterMetadata, new IIOImage(convertedImage, null, jpgWriterMetadata), jpgWriterParam);
          jpgImageOutputStream.flush();
        }
        else
        {
          jpgWriterParam.setDestinationType(jpgWriter.getDefaultWriteParam().getDestinationType());
          IIOMetadata jpgWriterMetadata = setJpegSubsamplingMode444(jpgWriter.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(screenCapture), jpgWriterParam));
          jpgImageOutputStream = ImageIO.createImageOutputStream(photoOutputStream);
          jpgWriter.setOutput(jpgImageOutputStream);
          jpgWriter.write(jpgWriterMetadata, new IIOImage(screenCapture, null, jpgWriterMetadata), jpgWriterParam);
          jpgImageOutputStream.flush();
        }
      }
      else
      {
        if (screenCapture.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE && (screenCapture.getType() == BufferedImage.TYPE_BYTE_INDEXED || screenCapture.getType() == BufferedImage.TYPE_BYTE_GRAY))
        {
          pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
          pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
          pngEncoder.encode(screenCapture, photoOutputStream);
        }
        else if (screenCapture.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT)
        {
          convertedImage = VTImageIO.createImage(0, 0, screenCapture.getWidth(), screenCapture.getHeight(), BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
          recyclableDataBuffer = convertedImage.getRaster().getDataBuffer();
          convertedGraphics = convertedImage.createGraphics();
          convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
          convertedGraphics.drawImage(screenCapture, 0, 0, null);
          pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
          pngEncoder.encode(convertedImage, photoOutputStream);
        }
        else if (screenCapture.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT)
        {
          if (screenshotProvider.getColorCount() == 262144 || screenshotProvider.getColorCount() == 2097152)
          {
            convertedImage = VTImageIO.createImage(0, 0, screenCapture.getWidth(), screenCapture.getHeight(), BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
            recyclableDataBuffer = convertedImage.getRaster().getDataBuffer();
            convertedGraphics = convertedImage.createGraphics();
            convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
            convertedGraphics.drawImage(screenCapture, 0, 0, null);
            pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
            pngEncoder.encode(convertedImage, photoOutputStream);
          }
          else
          {
            pngEncoder.setColorType(PngEncoder.COLOR_TRUECOLOR);
            pngEncoder.encode(screenCapture, photoOutputStream);
          }
        }
      }
      
      photoOutputStream.flush();
      // photoOutputStream.close();
      // provider.dispose();
      synchronized (this)
      {
        connection.getResultWriter().write("\nVT>Screen capture completed successfully!\nVT>");
        connection.getResultWriter().flush();
        finished = true;
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      synchronized (this)
      {
        try
        {
          connection.getResultWriter().write("\nVT>Screen capture failed!\nVT>");
          connection.getResultWriter().flush();
        }
        catch (Throwable e1)
        {
          
        }
        finished = true;
      }
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
    if (photoOutputStream != null)
    {
      try
      {
        photoOutputStream.close();
      }
      catch (IOException e)
      {
        
      }
    }
    if (convertedImage != null)
    {
      convertedImage.flush();
      convertedImage = null;
    }
    if (convertedGraphics != null)
    {
      convertedGraphics.dispose();
      convertedGraphics = null;
    }
    System.runFinalization();
    System.gc();
    finished = true;
  }
  
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
}