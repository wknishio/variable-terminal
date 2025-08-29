package org.vash.vate.client.graphicslink;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.vash.vate.VTSystem;

public class VTGraphicsLinkClientWriterFrame extends Frame
{
  private static final long serialVersionUID = 1L;
  private int lastWidth = -1;
  private int lastHeight = -1;
  
  private class VTGraphicsLinkClientWriterFrameComponentListener implements ComponentListener
  {
    // private boolean hidden = false;
    
    public void componentHidden(ComponentEvent e)
    {
      // this.hidden = true;
    }
    
    public void componentMoved(ComponentEvent e)
    {
      
    }
    
    public void componentResized(ComponentEvent e)
    {
      int width = getWidth();
      int height = getHeight();
      if (width != lastWidth || height != lastHeight)
      {
        revalidate();
      }
      else
      {
        
      }
      lastWidth = width;
      lastHeight = height;
      // System.out.println("componentResized");
    }
    
    public void componentShown(ComponentEvent e)
    {
      revalidate();
    }
  }
  
  public VTGraphicsLinkClientWriterFrame(GraphicsConfiguration configuration)
  {
    super(configuration);
    this.setBackground(new Color(0x00555555));
    try
    {
      this.setIconImage(VTSystem.desktopIcon);
    }
    catch (Throwable t)
    {
      
    }
    this.addComponentListener(new VTGraphicsLinkClientWriterFrameComponentListener());
  }
  
  public VTGraphicsLinkClientWriterFrame()
  {
    super();
    this.setBackground(new Color(0x00555555));
    try
    {
      this.setIconImage(VTSystem.desktopIcon);
    }
    catch (Throwable t)
    {
      
    }
    this.addComponentListener(new VTGraphicsLinkClientWriterFrameComponentListener());
  }
  
  public void pack()
  {
    super.pack();
    super.validate();
  }
  
  public void invalidate()
  {
    
  }
  
  public void validate()
  {
    
  }
  
  /*
   * public void validate() { int width = this.getWidth(); int height =
   * this.getHeight(); if (width != lastWidth || height != lastHeight) {
   * super.validate(); } lastWidth = width; lastHeight = height; }
   */
  
  public void revalidate()
  {
    super.invalidate();
    super.validate();
  }
  
  public void forcedValidate()
  {
    super.validate();
  }
  
  /*
   * public void update(Graphics g) { paint(g); } public void paint(Graphics g)
   * { } public void paintAll(Graphics g) { }
   */
}