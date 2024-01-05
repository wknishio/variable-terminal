package com.googlecode.lanterna.terminal.swing;

import java.awt.Adjustable;
import java.awt.ScrollPane;
import java.awt.ScrollPaneAdjustable;

public class AWTLanternaScrollPane extends ScrollPane
{
  private static final long serialVersionUID = 1L;
  private AWTLanternaScrollPaneAdjustable reflection_vAdjustable;
  private AWTLanternaScrollPaneAdjustable reflection_hAdjustable;
  
  public AWTLanternaScrollPane() throws Throwable
  {
    this(SCROLLBARS_AS_NEEDED);
  }
  
  public AWTLanternaScrollPane(int scrollbarDisplayPolicy)
  {
    super(scrollbarDisplayPolicy);
    try
    {
      reflection_vAdjustable = new AWTLanternaScrollPaneAdjustable((ScrollPaneAdjustable) super.getVAdjustable());
      reflection_hAdjustable = new AWTLanternaScrollPaneAdjustable((ScrollPaneAdjustable) super.getHAdjustable());
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }
  
  public Adjustable getVAdjustable()
  {
    return reflection_vAdjustable;
  }
  
  public Adjustable getHAdjustable()
  {
    return reflection_hAdjustable;
  }

}
