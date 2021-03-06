package org.vate.graphics.image;

import java.awt.image.IndexColorModel;

public class VTIndexedColorModel
{
  /*
   * private static int get4LevelValue(int level) { if (level == 0) { return 0; }
   * else if (level == 1) { return 85; } else if (level == 2) { return 170; } else
   * if (level == 3) { return 255; } return 0; }
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
    // byte[] rgbiRed = new byte[16];
    // byte[] rgbiGreen = new byte[16];
    // byte[] rgbiBlue = new byte[16];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    int p = 0;

    for (i = 0; i < 2; i++)
    {
      for (j = 0; j < 2; j++)
      {
        for (k = 0; k < 2; k++)
        {
          for (l = 0; l < 2; l++)
          {
            if (i == 0 && j == 0 && k == 0)
            {
              rgbiRed[p] = ((l * 97) & 0xFF);
              rgbiGreen[p] = ((l * 97) & 0xFF);
              rgbiBlue[p] = ((l * 97) & 0xFF);
              // rgbiValue[p] = (rgbiRed[p] << 16) | (rgbiGreen[p] << 8) | (rgbiBlue[p]);
            }
            else
            {
              rgbiRed[p] = ((i * 158) + (l * i * 97) & 0xFF);
              rgbiGreen[p] = ((j * 158) + (l * j * 97) & 0xFF);
              rgbiBlue[p] = ((k * 158) + (l * k * 97) & 0xFF);
              // rgbiValue[p] = (rgbiRed[p] << 16) | (rgbiGreen[p] << 8) | (rgbiBlue[p]);
            }
            p++;
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

  public static synchronized int get16ColorRGBIValueSlow(int rgb)
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
    byte[] r = new byte[8];
    byte[] g = new byte[8];
    byte[] b = new byte[8];
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
          r[p] = (byte) ((i * 255) & 0xFF);
          g[p] = (byte) ((j * 255) & 0xFF);
          b[p] = (byte) ((k * 255) & 0xFF);
          p++;
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel createPacked4Bit16ColorModel()
  {
    byte[] r = new byte[16];
    byte[] g = new byte[16];
    byte[] b = new byte[16];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    int p = 0;

    for (i = 0; i < 2; i++)
    {
      for (j = 0; j < 2; j++)
      {
        for (k = 0; k < 2; k++)
        {
          for (l = 0; l < 2; l++)
          {
            r[p] = (byte) ((((191 * i) + (i * l * 64))) & 0xFF);
            g[p] = (byte) ((((191 * j) + (j * l * 64))) & 0xFF);
            b[p] = (byte) ((((191 * k) + (k * l * 64))) & 0xFF);
            p++;
          }
        }
      }
    }
    return new IndexColorModel(4, r.length, r, g, b);
  }

  public static IndexColorModel create16ColorModel()
  {
    byte[] r = new byte[16];
    byte[] g = new byte[16];
    byte[] b = new byte[16];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    int p = 0;

    for (i = 0; i < 2; i++)
    {
      for (j = 0; j < 2; j++)
      {
        for (k = 0; k < 2; k++)
        {
          for (l = 0; l < 2; l++)
          {
            if (i == j && j == k)
            {
              r[p] = (byte) ((i * 158) + (l * 97) & 0xFF);
              g[p] = (byte) ((j * 158) + (l * 97) & 0xFF);
              b[p] = (byte) ((k * 158) + (l * 97) & 0xFF);
            }
            else
            {
              r[p] = (byte) ((i * 128) + (l * i * 127) & 0xFF);
              g[p] = (byte) ((j * 128) + (l * j * 127) & 0xFF);
              b[p] = (byte) ((k * 128) + (l * k * 127) & 0xFF);
            }
            p++;
          }
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel create32ColorModel()
  {
    byte[] r = new byte[32];
    byte[] g = new byte[32];
    byte[] b = new byte[32];
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;
    int m = 0;
    int p = 0;

    for (i = 0; i < 2; i++)
    {
      for (j = 0; j < 2; j++)
      {
        for (k = 0; k < 2; k++)
        {
          for (l = 0; l < 2; l++)
          {
            for (m = 0; m < 2; m++)
            {
              if (i == 0 && j == 0 && k == 0)
              {
                r[p] = (byte) ((l * 102) + (m * 51) & 0xFF);
                g[p] = (byte) ((l * 102) + (m * 51) & 0xFF);
                b[p] = (byte) ((l * 102) + (m * 51) & 0xFF);
              }
              else
              {
                r[p] = (byte) ((i * 102) + (i * l * 102) + (i * m * 51) & 0xFF);
                g[p] = (byte) ((j * 102) + (j * l * 102) + (j * m * 51) & 0xFF);
                b[p] = (byte) ((k * 102) + (k * l * 102) + (k * m * 51) & 0xFF);
              }
              p++;
            }
          }
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel create27ColorModel()
  {
    byte[] r = new byte[27];
    byte[] g = new byte[27];
    byte[] b = new byte[27];
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
          r[l] = (byte) (get3LevelRGBValue(i) & 0xFF);
          g[l] = (byte) (get3LevelRGBValue(j) & 0xFF);
          b[l] = (byte) (get3LevelRGBValue(k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel create64ColorModel()
  {
    byte[] r = new byte[64];
    byte[] g = new byte[64];
    byte[] b = new byte[64];
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
          r[l] = (byte) ((85 * i) & 0xFF);
          g[l] = (byte) ((85 * j) & 0xFF);
          b[l] = (byte) ((85 * k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel create125ColorModel()
  {
    byte[] r = new byte[125];
    byte[] g = new byte[125];
    byte[] b = new byte[125];
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
          r[l] = (byte) (get5LevelRGBValue(i) & 0xFF);
          g[l] = (byte) (get5LevelRGBValue(j) & 0xFF);
          b[l] = (byte) (get5LevelRGBValue(k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }

  public static IndexColorModel create216ColorModel()
  {
    byte[] r = new byte[216];
    byte[] g = new byte[216];
    byte[] b = new byte[216];
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
          r[l] = (byte) ((51 * i) & 0xFF);
          g[l] = (byte) ((51 * j) & 0xFF);
          b[l] = (byte) ((51 * k) & 0xFF);
          l++;
        }
      }
    }
    return new IndexColorModel(8, r.length, r, g, b);
  }
}