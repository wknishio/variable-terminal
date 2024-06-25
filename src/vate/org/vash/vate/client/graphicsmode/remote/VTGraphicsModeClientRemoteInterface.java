package org.vash.vate.client.graphicsmode.remote;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;

@SuppressWarnings("unused")
public class VTGraphicsModeClientRemoteInterface extends Canvas
{
  private static final long serialVersionUID = 1L;
  private boolean updating;
  @SuppressWarnings("unused")
  private boolean synchronousRefresh = false;
  private BufferedImage sourceImageDataBuffer;
  // private BufferedImage displayImageDataBuffer;
  // private Graphics2D displayImageGraphics;
  // private DataBuffer recyclableDataBuffer;
  private ScrollPane scrolled;
  // private Rectangle refreshArea = new Rectangle(0, 0, 0, 0);
  private VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter repainter;
  
  public VTGraphicsModeClientRemoteInterface(ExecutorService executorService)
  {
    // this.setBackground(new Color(0x00999999));
    // this.setBackground(new Color(0x00808080));
    //this.setBackground(new Color(0x00555555));
    this.setBackground(Color.BLACK);
    this.repainter = new VTGraphicsModeClientRemoteInterfaceAsynchronousRepainter(this, executorService);
    // this.graphics
    // this.resizer = new VTGraphicsModeClientViewPortChangeListener(this);
  }
  
  public void setScrollPane(ScrollPane scrolled)
  {
    this.scrolled = scrolled;
  }
  
  public void dispose()
  {
//    if (displayImageDataBuffer != null)
//    {
//      displayImageDataBuffer.flush();
//      displayImageDataBuffer = null;
//    }
//    if (displayImageGraphics != null)
//    {
//      displayImageGraphics.dispose();
//      displayImageGraphics = null;
//    }
//    if (recyclableDataBuffer != null)
//    {
//      recyclableDataBuffer = null;
//    }
  }
  
  public void prepareDisplay(BufferedImage image)
  {
//    if (displayImageDataBuffer != null)
//    {
//      displayImageDataBuffer.flush();
//    }
//    if (displayImageGraphics != null)
//    {
//      displayImageGraphics.dispose();
//    }
//    ColorModel colorModel = image.getColorModel();
//    int colors = 0;
//    if (colorModel instanceof IndexColorModel)
//    {
//      colors = ((IndexColorModel) colorModel).getMapSize();
//    }
//    if (colorModel instanceof DirectColorModel)
//    {
//      colors = 1 << ((DirectColorModel) colorModel).getPixelSize();
//    }
//    if (colors <= 256)
//    {
//      displayImageDataBuffer = VTImageIO.newImage(0, 0, image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB, 0, recyclableDataBuffer);
//      recyclableDataBuffer = displayImageDataBuffer.getData().getDataBuffer();
//      displayImageGraphics = displayImageDataBuffer.createGraphics();
//      displayImageGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
//    }
//    else
//    {
//      displayImageDataBuffer = null;
//      displayImageGraphics = null;
//    }
  }
  
  /*
   * public void setNotResizing(boolean notResizing) { if (notResizing) {
   * Dimension nextSize = new Dimension(0, 0); this.setSize(nextSize);
   * this.setMaximumSize(nextSize); this.setMinimumSize(nextSize);
   * this.setPreferredSize(nextSize); } this.notResizing = notResizing; }
   */
  
  /*
   * public void resizeToViewPort() { Dimension size =
   * ((ScrollPane)this.getParent()).getViewportSize(); this.setSize(size);
   * this.setMaximumSize(size); this.setMinimumSize(size);
   * this.setPreferredSize(size); }
   */
  
  /*
   * public void clear() { Dimension nextSize = new Dimension(0, 0);
   * this.setSize(nextSize); this.setMaximumSize(nextSize);
   * this.setMinimumSize(nextSize); this.setPreferredSize(nextSize); }
   */
  public void setRefreshArea(Rectangle area)
  {
    // this.refreshArea = area;
  }
  
  public void refreshImage()
  {
//    if (displayImageDataBuffer != null)
//    {
//      Point local = scrolled.getScrollPosition();
//      Dimension size = scrolled.getViewportSize();
//      Rectangle area = new Rectangle(local.x, local.y, size.width, size.height);
//      try
//      {
//        displayImageGraphics.drawImage(sourceImageDataBuffer, area.x, area.y, area.x + area.width, area.y + area.height, area.x, area.y, area.x + area.width, area.y + area.height, null);
//      }
//      catch (Throwable t)
//      {
//
//      }
//    }
  }
  
//  public Dimension getPreferredSize()
//  {
//    if (sourceImageDataBuffer != null)
//    {
//      Dimension nextSize = new Dimension(sourceImageDataBuffer.getWidth(), sourceImageDataBuffer.getHeight());
//      return nextSize;
//    }
//    return super.getPreferredSize();
//  }
  
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
          prepareDisplay(sourceImageDataBuffer);
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    else
    {
      // displayImageDataBuffer = null;
      sourceImageDataBuffer = null;
    }
  }
  
  public void clearImage()
  {
    // VTImageIO.clearImage(image);
    setImage(null);
    // super.update(g);
  }
  
  /*
   * public synchronized Dimension getImageSize() { if (image != null) { return
   * new Dimension(image.getWidth(), image.getHeight()); } return
   * this.getSize(); }
   */
  
  /*
   * public void validate() { } public void invalidate() { } public void
   * repaint() { }
   */
  
  public void update(Graphics g)
  {
    // System.out.println("called update");
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
    // System.out.println("called paint");
    // super.paint(g);
    // BufferedImage sourceImage = sourceImageDataBuffer;
    // BufferedImage displayImage = displayImageDataBuffer;
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
      //g.drawImage(sourceImageDataBuffer, 0, 0, null);
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
  
  public void setSynchronousRefresh(boolean synchronousRefresh)
  {
    this.synchronousRefresh = synchronousRefresh;
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