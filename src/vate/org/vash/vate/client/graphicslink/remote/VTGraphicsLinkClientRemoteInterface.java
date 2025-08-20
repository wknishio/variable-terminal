package org.vash.vate.client.graphicslink.remote;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;

public class VTGraphicsLinkClientRemoteInterface extends Canvas
{
  private static final long serialVersionUID = 1L;
  private volatile boolean updating;
  //private boolean synchronousRefresh = false;
  private BufferedImage sourceImageDataBuffer;
  private ScrollPane scrolled;
  private VTGraphicsLinkClientRemoteInterfaceAsynchronousRepainter repainter;
  
  public VTGraphicsLinkClientRemoteInterface(ExecutorService executorService)
  {
    this.setBackground(Color.BLACK);
    this.repainter = new VTGraphicsLinkClientRemoteInterfaceAsynchronousRepainter(this, executorService);
  }
  
  public void setScrollPane(ScrollPane scrolled)
  {
    this.scrolled = scrolled;
  }
  
  public void dispose()
  {
    
  }
  
  public void setImage(BufferedImage image)
  {
    if (image != null)
    {
      Dimension nextSize = new Dimension(image.getWidth(), image.getHeight());
      if (sourceImageDataBuffer == null || sourceImageDataBuffer.getType() != image.getType() || sourceImageDataBuffer.getWidth() != image.getWidth() || sourceImageDataBuffer.getHeight() != image.getHeight() || sourceImageDataBuffer != image || !nextSize.equals(getSize()))
      {
        try
        {
          sourceImageDataBuffer = image;
          setSize(nextSize);
          setMaximumSize(nextSize);
          setMinimumSize(nextSize);
          setPreferredSize(nextSize);
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    else
    {
      sourceImageDataBuffer = null;
    }
  }
  
  public void clearImage()
  {
    setImage(null);
    // super.update(g);
  }
  
  public void update(Graphics g)
  {
    // this.graphics = g;
    if (sourceImageDataBuffer != null)
    {
      this.paint(g);
    }
    else
    {
      // this.paint(g);
      super.paint(g);
    }
    // super.update(g);
  }
  
  public void paint(Graphics g)
  {
    if (sourceImageDataBuffer != null)
    {
      Point local = scrolled.getScrollPosition();
      Dimension size = scrolled.getViewportSize();
      Rectangle area = new Rectangle(local.x, local.y, size.width, size.height);
      try
      {
        g.drawImage(sourceImageDataBuffer, area.x, area.y, area.x + area.width, area.y + area.height, area.x, area.y, area.x + area.width, area.y + area.height, null);
      }
      catch (Throwable t)
      {
        
      }
    }
    else
    {
      super.paint(g);
    }
  }
  
  public boolean isUpdating()
  {
    return updating;
  }
  
  public void setUpdating(boolean updating)
  {
    this.updating = updating;
  }
  
  public void startAsynchronousRepainter()
  {
    repainter.start();
  }
  
  public void stopAsynchronousRepainter()
  {
    repainter.stop();
  }
  
  public void interruptAsynchronousRepainter()
  {
    repainter.interrupt();
  }
  
  public void resumeAsynchronousRepainter()
  {
    repainter.resume();
  }
  
  public boolean isAsynchronousRepainterRunning()
  {
    return repainter.isRunning();
  }
  
  public boolean isAsynchronousRepainterInterrupted()
  {
    return repainter.isInterrupted();
  }
}