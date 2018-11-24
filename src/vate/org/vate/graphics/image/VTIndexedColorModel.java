package org.vate.graphics.image;

import java.awt.image.IndexColorModel;

public class VTIndexedColorModel
{
	/* private static int get4LevelValue(int level) { if (level == 0) { return
	 * 0; } else if (level == 1) { return 85; } else if (level == 2) { return
	 * 170; } else if (level == 3) { return 255; } return 0; } */
	
	private static final int DCM_RED_MASK = 0x00ff0000;
	private static final int DCM_GREEN_MASK = 0x0000ff00;
	private static final int DCM_BLUE_MASK = 0x000000ff;
	
	private static final int[] rgbiRed;
	private static final int[] rgbiGreen;
	private static final int[] rgbiBlue;
	private static final int[] rgbiValue;
	
	static
	{
		rgbiRed = new int[16];
		rgbiGreen = new int[16];
		rgbiBlue = new int[16];
		rgbiValue = new int[16];
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int h = 0;
		
		for (i = 0; i < 2; i++)
		{
			for (j = 0; j < 2; j++)
			{
				for (k = 0; k < 2; k++)
				{
					for (l = 0; l < 2; l++)
					{
						rgbiRed[h] = ((((191 * i) + (l * 64))) & 0xFF);
						rgbiGreen[h] = ((((191 * j) + (l * 64))) & 0xFF);
						rgbiBlue[h] = ((((191 * k) + (l * 64))) & 0xFF);
						rgbiValue[h] = (i << 3) | (j << 2) | (k << 1) | (l);
						h++;
					}
				}
			}
		}
	}
	
	public static int get16ColorRGBIValue(int rgb)
	{
		int red =  (rgb & DCM_RED_MASK) >> 16;
		int green =  (rgb & DCM_GREEN_MASK) >> 8;
		int blue =  (rgb & DCM_BLUE_MASK);
		
		int index1 = approximateRGBIIndex(red, green, blue, 0);
		int index2 = approximateRGBIIndex(red, green, blue, 1);
		
		int dist1 = euclidianColorDistance(rgbiRed[index1], rgbiGreen[index1], rgbiBlue[index1], red, green, blue);
		int dist2 = euclidianColorDistance(rgbiRed[index2], rgbiGreen[index2], rgbiBlue[index2], red, green, blue);
		
		if (dist1 <= dist2)
		{
			return rgbiValue[index1];
		}
		else
		{
			return rgbiValue[index2];
		}
		
//		int min = Integer.MAX_VALUE;
//		int dist = min;
//		int best = 0;
//		
//		for (int i = 0;i < 16; i++)
//		{
//			dist = euclidianColorDistance(rgbiReds[i], rgbiGreens[i], rgbiBlues[i], red, green, blue);
//			if (dist < min)
//			{
//				min = dist;
//				best = i;
//			}
//		}
//		return rgbiValues[best];
	}
	
	private static int euclidianColorDistance(int red_a, int green_a, int blue_a, int red_b, int green_b, int blue_b)
	{
		//int d_red = Math.abs(red_a - red_b);
		//int d_green = Math.abs(green_a - green_b);
		//int d_blue = Math.abs(blue_a - blue_b);
		//return d_red + d_green + d_blue;
		int d_red = red_a - red_b;
		int d_green = green_a - green_b;
		int d_blue = blue_a - blue_b;
		return (d_red * d_red) + (d_green * d_green) + (d_blue * d_blue);
	}
	
	private static int approximateRGBIIndex(int red, int green, int blue, int intensity)
	{
		int threshold = intensity > 0 ? 85 : 127;
	    int r = (red >= threshold ? 8 : 0);
	    int g = (green >= threshold ? 4 : 0);
	    int b = (blue >= threshold ? 2 : 0);
	    return (r) | (g) | (b) | intensity;
	}
		
	private static int get3LevelValue(int level)
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
	
	private static int get5LevelValue(int level)
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
	
	public static IndexColorModel create4Bit16ColorModel()
	{
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int h = 0;
		
		for (i = 0; i < 2; i++)
		{
			for (j = 0; j < 2; j++)
			{
				for (k = 0; k < 2; k++)
				{
					for (l = 0; l < 2; l++)
					{
						r[h] = (byte) ((((191 * i) + (l * 64))) & 0xFF);
						g[h] = (byte) ((((191 * j) + (l * 64))) & 0xFF);
						b[h] = (byte) ((((191 * k) + (l * 64))) & 0xFF);
						h++;
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
		int h = 0;
		
		for (i = 0; i < 2; i++)
		{
			for (j = 0; j < 2; j++)
			{
				for (k = 0; k < 2; k++)
				{
					for (l = 0; l < 2; l++)
					{
						r[h] = (byte) ((((191 * i) + (l * 64))) & 0xFF);
						g[h] = (byte) ((((191 * j) + (l * 64))) & 0xFF);
						b[h] = (byte) ((((191 * k) + (l * 64))) & 0xFF);
						h++;
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
					r[l] = (byte) (get3LevelValue(i) & 0xFF);
					g[l] = (byte) (get3LevelValue(j) & 0xFF);
					b[l] = (byte) (get3LevelValue(k) & 0xFF);
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
					r[l] = (byte) (get5LevelValue(i) & 0xFF);
					g[l] = (byte) (get5LevelValue(j) & 0xFF);
					b[l] = (byte) (get5LevelValue(k) & 0xFF);
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