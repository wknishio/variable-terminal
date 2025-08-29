package org.vash.vate.console.graphical;

import java.awt.Frame;

import org.vash.vate.VTSystem;

public class VTGraphicalConsoleFrame extends Frame
{
  private static final long serialVersionUID = 1L;
  
  public VTGraphicalConsoleFrame(boolean remoteIcon)
  {
    super();
    // IIORegistry.getDefaultInstance().registerServiceProvider(new
    // PngImageReaderSpi());
    // PngImageReader pngReader = new PngImageReader(new PngImageReaderSpi());
    // InputStream stream = null;
    if (remoteIcon)
    {
      try
      {
        this.setIconImage(VTSystem.remoteIcon);
      }
      catch (Throwable t)
      {
        
      }
    }
    else
    {
      try
      {
        this.setIconImage(VTSystem.terminalIcon);
      }
      catch (Throwable t)
      {
        
      }
    }
    
    setIgnoreRepaint(true);
  }
  
  public void pack()
  {
    super.pack();
    // Dimension packed = this.getSize();
    // FontMetrics metrics =
    // VTGraphicalConsole.getTextArea().getFontMetrics(VTGraphicalConsole.getTextArea().getFont());
    // System.out.println("getFontMetrics(VTGraphicalConsole.getTextArea().getFont()).getHeight():
    // " +
    // getFontMetrics(VTGraphicalConsole.getTextArea().getFont()).getHeight());
    // this.setSize(packed.width, packed.height - (metrics.getMaxDescent() *
    // 2));
    // this.setMinimumSize(new Dimension(packed.width, packed.height + 2));
    // this.setMaximumSize(new Dimension(packed.width, Integer.MAX_VALUE));
    // this.setMaximumSize(new Dimension(packed.width, packed.height));
    // this.setPreferredSize(new Dimension(this.getSize().width,
    // this.getSize().height + 2));
    
  }
}