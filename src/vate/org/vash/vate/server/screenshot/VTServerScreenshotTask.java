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

import org.vash.vate.VT;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

import com.objectplanet.image.PngEncoder;

// import org.eclipse.swt.SWT;

public class VTServerScreenshotTask extends VTTask
{
  private static final int fileScreenshotBufferSize = VT.VT_FILE_BUFFER_SIZE_BYTES;
  private volatile boolean finished;
  private boolean drawPointer;
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
  // private VTServerGraphicsResource graphicsResource;
  
  public VTServerScreenshotTask(VTServerSession session)
  {
    // this.session = session;
    this.connection = session.getConnection();
    this.screenshotProvider = session.getScreenshotProvider();
    this.drawPointer = false;
    this.clock = new GregorianCalendar();
    this.firstFormat = new SimpleDateFormat("G", Locale.ENGLISH);
    this.secondFormat = new SimpleDateFormat("MM-dd--HH-mm-ss-SSS-z");
    this.finished = true;
    this.deviceNumber = null;
    this.pngEncoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR, PngEncoder.BEST_SPEED);
    this.pngEncoder.setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL);
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
  
  @SuppressWarnings("all")
  public void run()
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
      screenshotFile = new File(firstFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondFormat.format(clock.getTime()) + ".png");
      photoOutputStream = new BufferedOutputStream(Channels.newOutputStream(new FileOutputStream(screenshotFile).getChannel()), fileScreenshotBufferSize);
      // screenshotProvider.writeHighQualityScreenshot(photoOutputStream,
      // SWT.IMAGE_BMP);
      BufferedImage screenCapture = screenshotProvider.createScreenCapture(0, drawPointer);
      connection.getResultWriter().write("\nVT>Screen capture data obtained, image will be saved in:\nVT>[" + screenshotFile.getAbsolutePath() + "]\nVT>");
      connection.getResultWriter().flush();
      if (screenCapture.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE && screenCapture.getType() == BufferedImage.TYPE_BYTE_INDEXED)
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
      // else if (screenCapture.getType() == BufferedImage.TYPE_BYTE_BINARY)
      // {
      // convertedImage = VTImageIO.newImage(screenCapture.getWidth(),
      // screenCapture.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, 16,
      // recyclableDataBuffer);
      // recyclableDataBuffer = convertedImage.getRaster().getDataBuffer();
      // convertedGraphics = convertedImage.createGraphics();
      // convertedGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
      // convertedGraphics.drawImage(screenCapture, 0, 0, null);
      // pngEncoder.setColorType(PngEncoder.COLOR_INDEXED);
      // pngEncoder.encode(convertedImage, photoOutputStream);
      // }
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
      // ImageIO.write(screenCapture, "png", photoOutputStream);
      // provider.writeHighQualityScreenshot(photoOutputStream,
      // SWT.IMAGE_BMP);
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
}