package org.vash.vate.graphics.image;

import java.awt.image.IndexColorModel;

public class VTIndexedColorModel
{
  /*
   * private static int get4LevelValue(int level) { if (level == 0) { return 0;
   * } else if (level == 1) { return 85; } else if (level == 2) { return 170; }
   * else if (level == 3) { return 255; } return 0; }
   */
  
  private static final int DCM_RED_MASK = 0x00ff0000;
  private static final int DCM_GREEN_MASK = 0x0000ff00;
  private static final int DCM_BLUE_MASK = 0x000000ff;
  
  private static final IndexColorModel indexColorModel8 = create8ColorModel();
  private static final IndexColorModel indexColorModel16 = create16ColorModel();
  private static final IndexColorModel indexColorModel27 = create27ColorModel();
  private static final IndexColorModel indexColorModel32 = create32ColorModel();
  private static final IndexColorModel indexColorModel125 = create125ColorModel();
  private static final IndexColorModel indexColorModel216 = create216ColorModel();
  
  private static final int[] rgbiRed = new int[16], rgbiGreen = new int[16], rgbiBlue = new int[16];
  // private static final int[] rgbiValue = new int[16];
  
  static
  {
    int i = 0;
    int r = 0;
    int g = 0;
    int b = 0;
    int c = 0;
    
    for (i = 0; i < 2; i++)
    {
      for (r = 0; r < 2; r++)
      {
        for (g = 0; g < 2; g++)
        {
          for (b = 0; b < 2; b++)
          {
            if (r == g && g == b)
            {
              rgbiRed[c] = ((i * 85) + (r * 170));
              rgbiGreen[c] = ((i * 85) + (g * 170));
              rgbiBlue[c] = ((i * 85) + (b * 170));
            }
            else
            {
              rgbiRed[c] = ((i * r * 85) + (r * 170));
              rgbiGreen[c] = ((i * g * 85) + (g * 170));
              rgbiBlue[c] = ((i * b * 85) + (b * 170));
            }
            c++;
          }
        }
      }
    }
  }
  
  @SuppressWarnings("unused")
  private static int euclidianColorDistance(int red_a, int green_a, int blue_a, int red_b, int green_b, int blue_b)
  {
    // int d_red = Math.abs(red_a - red_b);
    // int d_green = Math.abs(green_a - green_b);
    // int d_blue = Math.abs(blue_a - blue_b);
    // return d_red + d_green + d_blue;
    int d_red = red_a - red_b;
    int d_green = green_a - green_b;
    int d_blue = blue_a - blue_b;
    return (d_red * d_red) + (d_green * d_green) + (d_blue * d_blue);
  }
  
  @SuppressWarnings("unused")
  private static int approximateRGBI(int red, int green, int blue, int i1)
  {
    int threshold = i1 > 0 ? 102 : 153;
    int r = (red >= threshold ? 8 : 0);
    int g = (green >= threshold ? 4 : 0);
    int b = (blue >= threshold ? 2 : 0);
    return (r) | (g) | (b) | i1;
  }
  
  public static synchronized int get16ColorRGBIValueEuclidian(int rgb)
  {
    int red = (rgb & DCM_RED_MASK) >> 16;
    int green = (rgb & DCM_GREEN_MASK) >> 8;
    int blue = (rgb & DCM_BLUE_MASK);
//		
//		int index1 = approximateRGBI(red, green, blue, 0);
//		int index2 = approximateRGBI(red, green, blue, 1);
//		
//		int dist1 = euclidianColorDistance(rgbiRed[index1], rgbiGreen[index1], rgbiBlue[index1], red, green, blue);
//		int dist2 = euclidianColorDistance(rgbiRed[index2], rgbiGreen[index2], rgbiBlue[index2], red, green, blue);
//		
//		if (dist1 <= dist2)
//		{
//			return index1;
//		}
//		else
//		{
//			return index2;
//		}
    
    int min_index = 0;
    int min_dist = Integer.MAX_VALUE;
    int current_dist = 0;
    int d_red = 0;
    int d_green = 0;
    int d_blue = 0;
    
    for (int i = 0; i < 16; i++)
    {
      d_red = red - rgbiRed[i];
      d_green = green - rgbiGreen[i];
      d_blue = blue - rgbiBlue[i];
      
      current_dist = (d_red * d_red) + (d_green * d_green) + (d_blue * d_blue);
      
      if (current_dist < min_dist)
      {
        min_dist = current_dist;
        min_index = i;
      }
    }
    
    return min_index;
  }
  
  public static byte get8ColorRGBValue(int rgb)
  {
    return ((byte[]) indexColorModel8.getDataElements(rgb, null))[0];
  }
  
  public static byte get27Color3LevelRGBValue(int rgb)
  {
    return ((byte[]) indexColorModel27.getDataElements(rgb, null))[0];
  }
  
  public static byte get16ColorRGBIValue(int rgb)
  {
    return ((byte[]) indexColorModel16.getDataElements(rgb, null))[0];
  }
  
  public static byte get32ColorRGBIIValue(int rgb)
  {
    return ((byte[]) indexColorModel32.getDataElements(rgb, null))[0];
  }
  
  public static byte get125Color5LevelRGBValue(int rgb)
  {
    return ((byte[]) indexColorModel125.getDataElements(rgb, null))[0];
  }
  
  public static byte get216Color6LevelRGBValue(int rgb)
  {
    return ((byte[]) indexColorModel216.getDataElements(rgb, null))[0];
  }
  
  private static int get3LevelRGBValue(int level)
  {
    if (level == 0)
    {
      return 0;
    }
    else if (level == 1)
    {
      return 128;
    }
    else if (level == 2)
    {
      return 255;
    }
    return 0;
  }
  
  private static int get5LevelRGBValue(int level)
  {
    if (level == 0)
    {
      return 0;
    }
    else if (level == 1)
    {
      return 64;
    }
    else if (level == 2)
    {
      return 128;
    }
    else if (level == 3)
    {
      return 191;
    }
    else if (level == 4)
    {
      return 255;
    }
    return 0;
  }
  
  public static IndexColorModel create8ColorModel()
  {
    byte[] red = new byte[8];
    byte[] green = new byte[8];
    byte[] blue = new byte[8];
    int i = 0;
    int j = 0;
    int k = 0;
    // int l = 0;
    int p = 0;
    
    for (i = 0; i < 2; i++)
    {
      for (j = 0; j < 2; j++)
      {
        for (k = 0; k < 2; k++)
        {
          red[p] = (byte) ((i * 255) & 0xFF);
          green[p] = (byte) ((j * 255) & 0xFF);
          blue[p] = (byte) ((k * 255) & 0xFF);
          p++;
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
//  public static IndexColorModel createPacked4Bit16ColorModel()
//  {
//    byte[] r = new byte[16];
//    byte[] g = new byte[16];
//    byte[] b = new byte[16];
//    int i = 0;
//    int j = 0;
//    int k = 0;
//    int l = 0;
//    int p = 0;
//
//    for (i = 0; i < 2; i++)
//    {
//      for (j = 0; j < 2; j++)
//      {
//        for (k = 0; k < 2; k++)
//        {
//          for (l = 0; l < 2; l++)
//          {
//            r[p] = (byte) ((((191 * i) + (i * l * 64))) & 0xFF);
//            g[p] = (byte) ((((191 * j) + (j * l * 64))) & 0xFF);
//            b[p] = (byte) ((((191 * k) + (k * l * 64))) & 0xFF);
//            p++;
//          }
//        }
//      }
//    }
//    return new IndexColorModel(4, r.length, r, g, b);
//  }
  
  public static IndexColorModel create16ColorModel()
  {
    byte[] red = new byte[16];
    byte[] green = new byte[16];
    byte[] blue = new byte[16];
    int i = 0;
    int r = 0;
    int g = 0;
    int b = 0;
    int c = 0;
    
    for (i = 0; i < 2; i++)
    {
      for (r = 0; r < 2; r++)
      {
        for (g = 0; g < 2; g++)
        {
          for (b = 0; b < 2; b++)
          {
            if (r == g && g == b)
            {
              red[c] = (byte) ((i * 85) + (r * 170));
              green[c] = (byte) ((i * 85) + (g * 170));
              blue[c] = (byte) ((i * 85) + (b * 170));
            }
            else
            {
              red[c] = (byte) ((i * r * 85) + (r * 170));
              green[c] = (byte) ((i * g * 85) + (g * 170));
              blue[c] = (byte) ((i * b * 85) + (b * 170));
            }
            c++;
          }
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
  public static IndexColorModel create32ColorModel()
  {
    byte[] red = new byte[32];
    byte[] green = new byte[32];
    byte[] blue = new byte[32];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    int m = 0;
    int p = 0;
    
    for (l = 0; l < 2; l++)
    {
      for (m = 0; m < 2; m++)
      {
        for (i = 0; i < 2; i++)
        {
          for (j = 0; j < 2; j++)
          {
            for (k = 0; k < 2; k++)
            {
              if (i == j && j == k)
              {
                red[p] = (byte) ((i * 102) + (l * 102) + (m * 51) & 0xFF);
                green[p] = (byte) ((j * 102) + (l * 102) + (m * 51) & 0xFF);
                blue[p] = (byte) ((k * 102) + (l * 102) + (m * 51) & 0xFF);
              }
              else
              {
                red[p] = (byte) ((i * 102) + (i * l * 102) + (i * m * 51) & 0xFF);
                green[p] = (byte) ((j * 102) + (j * l * 102) + (j * m * 51) & 0xFF);
                blue[p] = (byte) ((k * 102) + (k * l * 102) + (k * m * 51) & 0xFF);
              }
              p++;
            }
          }
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
  public static IndexColorModel create27ColorModel()
  {
    byte[] red = new byte[27];
    byte[] green = new byte[27];
    byte[] blue = new byte[27];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    for (i = 0; i < 3; i++)
    {
      for (j = 0; j < 3; j++)
      {
        for (k = 0; k < 3; k++)
        {
          red[l] = (byte) (get3LevelRGBValue(i) & 0xFF);
          green[l] = (byte) (get3LevelRGBValue(j) & 0xFF);
          blue[l] = (byte) (get3LevelRGBValue(k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
  public static IndexColorModel create64ColorModel()
  {
    byte[] red = new byte[64];
    byte[] green = new byte[64];
    byte[] blue = new byte[64];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    for (i = 0; i < 4; i++)
    {
      for (j = 0; j < 4; j++)
      {
        for (k = 0; k < 4; k++)
        {
          red[l] = (byte) ((85 * i) & 0xFF);
          green[l] = (byte) ((85 * j) & 0xFF);
          blue[l] = (byte) ((85 * k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
  public static IndexColorModel create125ColorModel()
  {
    byte[] red = new byte[125];
    byte[] green = new byte[125];
    byte[] blue = new byte[125];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    for (i = 0; i < 5; i++)
    {
      for (j = 0; j < 5; j++)
      {
        for (k = 0; k < 5; k++)
        {
          red[l] = (byte) (get5LevelRGBValue(i) & 0xFF);
          green[l] = (byte) (get5LevelRGBValue(j) & 0xFF);
          blue[l] = (byte) (get5LevelRGBValue(k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
  
  public static IndexColorModel create216ColorModel()
  {
    byte[] red = new byte[216];
    byte[] green = new byte[216];
    byte[] blue = new byte[216];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    for (i = 0; i < 6; i++)
    {
      for (j = 0; j < 6; j++)
      {
        for (k = 0; k < 6; k++)
        {
          red[l] = (byte) ((51 * i) & 0xFF);
          green[l] = (byte) ((51 * j) & 0xFF);
          blue[l] = (byte) ((51 * k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, red.length, red, green, blue);
  }
}