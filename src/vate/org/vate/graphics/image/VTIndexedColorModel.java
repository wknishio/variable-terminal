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
		int p = 0;
		
		for (i = 0; i < 2; i++)
		{
			for (j = 0; j < 2; j++)
			{
				for (k = 0; k < 2; k++)
				{
					for (l = 0; l < 2; l++)
					{
						rgbiRed[p] = ((((191 * i) + (l * 64))) & 0xFF);
						rgbiGreen[p] = ((((191 * j) + (l * 64))) & 0xFF);
						rgbiBlue[p] = ((((191 * k) + (l * 64))) & 0xFF);
						rgbiValue[p] = (i << 3) | (j << 2) | (k << 1) | (l);
						p++;
					}
				}
			}
		}
	}
	
	private static final int[] rgbiiRed;
	private static final int[] rgbiiGreen;
	private static final int[] rgbiiBlue;
	private static final int[] rgbiiValue;
	
	static
	{
		rgbiiRed = new int[32];
		rgbiiGreen = new int[32];
		rgbiiBlue = new int[32];
		rgbiiValue = new int[32];
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
							rgbiiRed[p] = ((((153 * i) + (l * 68) + (m * 34))) & 0xFF);
							rgbiiGreen[p] = ((((153 * j) + (l * 68) + (m * 34))) & 0xFF);
							rgbiiBlue[p] = ((((153 * k) + (l * 68) + (m * 34))) & 0xFF);
							rgbiiValue[p] = (i << 4) | (j << 3) | (k << 2) | (l << 1) | m;
							p++;
						}
					}
				}
			}
		}
	}
	
	private static final byte[] rgb3lRed = new byte[27];
	private static final byte[] rgb3lGreen = new byte[27];
	private static final byte[] rgb3lBlue = new byte[27];
	private static final byte[] rgb3lValue = new byte[27];
	
	static
	{
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
					rgb3lRed[l] = (byte) (get3LevelRGBValue(i) & 0xFF);
					rgb3lGreen[l] = (byte) (get3LevelRGBValue(j) & 0xFF);
					rgb3lBlue[l] = (byte) (get3LevelRGBValue(k) & 0xFF);
					rgb3lValue[l] = (byte) l;
					l++;
				}
			}
		}
	}
	
	private static final IndexColorModel indexColorModel27 = create27ColorModel();
	
	public static int get16ColorRGBIValue(int rgb)
	{
		int red =  (rgb & DCM_RED_MASK) >> 16;
		int green =  (rgb & DCM_GREEN_MASK) >> 8;
		int blue =  (rgb & DCM_BLUE_MASK);
		
		int index1 = approximateRGBI(red, green, blue, 0);
		int index2 = approximateRGBI(red, green, blue, 1);
		
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
	}
	
	public static byte get27Color3LevelRGBValue(int rgb)
	{
		return ((byte[])indexColorModel27.getDataElements(rgb, null))[0];
		//byte red_idx = red > ;
	}
	
	public static int get32ColorRGBIIValue(int rgb)
	{
		int red =  (rgb & DCM_RED_MASK) >> 16;
		int green =  (rgb & DCM_GREEN_MASK) >> 8;
		int blue =  (rgb & DCM_BLUE_MASK);
		
		int index1 = approximateRGBII(red, green, blue, 0, 0);
		int index2 = approximateRGBII(red, green, blue, 0, 1);
		int index3 = approximateRGBII(red, green, blue, 2, 0);
		int index4 = approximateRGBII(red, green, blue, 2, 1);
		
		int dist1 = euclidianColorDistance(rgbiiRed[index1], rgbiiGreen[index1], rgbiiBlue[index1], red, green, blue);
		int dist2 = euclidianColorDistance(rgbiiRed[index2], rgbiiGreen[index2], rgbiiBlue[index2], red, green, blue);
		int dist3 = euclidianColorDistance(rgbiiRed[index3], rgbiiGreen[index3], rgbiiBlue[index3], red, green, blue);
		int dist4 = euclidianColorDistance(rgbiiRed[index4], rgbiiGreen[index4], rgbiiBlue[index4], red, green, blue);
		
		int mdist = Math.min(dist1, Math.min(dist2, Math.min(dist3, dist4)));
		
		if (mdist == dist1)
		{
			return rgbiiValue[index1];
		}
		else if (mdist == dist2)
		{
			return rgbiiValue[index2];
		}
		else if (mdist == dist3)
		{
			return rgbiiValue[index3];
		}
		else
		{
			return rgbiiValue[index4];
		}
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
	
	private static int approximateRGBI(int red, int green, int blue, int i1)
	{
		int threshold = i1 > 0 ? 85 : 127;
	    int r = (red >= threshold ? 8 : 0);
	    int g = (green >= threshold ? 4 : 0);
	    int b = (blue >= threshold ? 2 : 0);
	    return (r) | (g) | (b) | i1;
	}
	
	private static int approximateRGBII(int red, int green, int blue, int i1, int i2)
	{
		int threshold = 0;
		if (i1 > 1)
		{
			threshold += 102;
		}
		if (i2 > 0)
		{
			threshold += 25;
		}
	    int r = (red >= threshold ? 16 : 0);
	    int g = (green >= threshold ? 8 : 0);
	    int b = (blue >= threshold ? 4 : 0);
	    return (r) | (g) | (b) | i1 | i2;
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
	
	public static IndexColorModel createPacked4Bit16ColorModel()
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
		int p = 0;
		
		for (i = 0; i < 2; i++)
		{
			for (j = 0; j < 2; j++)
			{
				for (k = 0; k < 2; k++)
				{
					for (l = 0; l < 2; l++)
					{
						r[p] = (byte) ((((191 * i) + (l * 64))) & 0xFF);
						g[p] = (byte) ((((191 * j) + (l * 64))) & 0xFF);
						b[p] = (byte) ((((191 * k) + (l * 64))) & 0xFF);
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
							r[p] = (byte) ((((153 * i) + (l * 68) + (m * 34))) & 0xFF);
							g[p] = (byte) ((((153 * j) + (l * 68) + (m * 34))) & 0xFF);
							b[p] = (byte) ((((153 * k) + (l * 68) + (m * 34))) & 0xFF);
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