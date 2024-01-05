package com.googlecode.lanterna.terminal.swing;

import java.awt.Adjustable;
import java.awt.ScrollPaneAdjustable;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.Field;

public class AWTLanternaScrollPaneAdjustable implements Adjustable
{
  private ScrollPaneAdjustable adjustable;
  private Field reflection_maximum;
  private Field reflection_visibleAmount;

  public AWTLanternaScrollPaneAdjustable(ScrollPaneAdjustable adjustable)
  {
    try
    {
      this.adjustable = adjustable;
      this.reflection_maximum = adjustable.getClass().getDeclaredField("maximum");
      this.reflection_visibleAmount = adjustable.getClass().getDeclaredField("visibleAmount");
      reflection_maximum.setAccessible(true);
      reflection_visibleAmount.setAccessible(true);
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }
  
  public void setSpan(int min, int max, int visible)
  {
    
  }
  
  public void setMaximum(int maximum)
  {
    if (reflection_maximum != null)
    {
      try
      {
        reflection_maximum.set(adjustable, maximum);
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
  }
  
  public void setVisibleAmount(int visible)
  {
    if (reflection_visibleAmount != null)
    {
      try
      {
        reflection_visibleAmount.set(adjustable, visible);
      }
      catch (Throwable t)
      {
        t.printStackTrace();
      }
    }
  }

  public int getOrientation()
  {
    return adjustable.getOrientation();
  }

  public void setMinimum(int min)
  {
    
  }

  public int getMinimum()
  {
    return adjustable.getMinimum();
  }

  public int getMaximum()
  {
    return adjustable.getMaximum();
  }

  public void setUnitIncrement(int u)
  {
    adjustable.setUnitIncrement(u);
  }

  public int getUnitIncrement()
  {
    return adjustable.getUnitIncrement();
  }

  public void setBlockIncrement(int b)
  {
    adjustable.setBlockIncrement(b);
  }

  public int getBlockIncrement()
  {
    return adjustable.getBlockIncrement();
  }

  public int getVisibleAmount()
  {
    return adjustable.getVisibleAmount();
  }

  public void setValue(int v)
  {
    adjustable.setValue(v);
  }

  public int getValue()
  {
    return adjustable.getValue();
  }

  public void addAdjustmentListener(AdjustmentListener l)
  {
    adjustable.addAdjustmentListener(l);
  }

  public void removeAdjustmentListener(AdjustmentListener l)
  {
    adjustable.removeAdjustmentListener(l);
  }
}
