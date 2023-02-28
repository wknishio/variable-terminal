package org.vash.vate.graphics.image;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public final class VTIconDisplay extends Canvas
{
  private static final long serialVersionUID = 1L;
  
  private BufferedImage image;
  
  public void setImage(BufferedImage image, int width, int height)
  {
    this.image = image;
    // this.setSize(getMinimumSize());
    // this.setMaximumSize(getMinimumSize());
    this.setPreferredSize(new Dimension(width, height));
  }
  
  public void update(Graphics g)
  {
    if (image != null)
    {
      paint(g);
    }
    else
    {
      super.update(g);
    }
  }
  
  public void paint(Graphics g)
  {
    if (image != null)
    {
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      Dimension componentDimension = this.getSize();
      double adjustedScaleFactorX = (imageWidth / componentDimension.getWidth());
      double adjustedScaleFactorY = (imageHeight / componentDimension.getHeight());
      if (adjustedScaleFactorX > adjustedScaleFactorY)
      {
        adjustedScaleFactorY = adjustedScaleFactorX;
      }
      else
      {
        adjustedScaleFactorX = adjustedScaleFactorY;
      }
      int scaledWidth = (int) Math.round(imageWidth / adjustedScaleFactorX);
      int scaledHeight = (int) Math.round(imageHeight / adjustedScaleFactorY);
      int x = (componentDimension.width - scaledWidth) / 2;
      int y = (componentDimension.height - scaledHeight) / 2;
      g.drawImage(image, x, y, scaledWidth, scaledHeight, this);
    }
    else
    {
      super.paint(g);
    }
  }
}
