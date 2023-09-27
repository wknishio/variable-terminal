package org.vash.vate.graphics.image;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class VTImageDataUtils
{
//  private static final RectangleComparator rectangleComparator = new RectangleComparator();
//  
//  private static class RectangleComparator implements Comparator<Rectangle>
//  {
//    private RectangleComparator()
//    {
//      
//    }
//    
//    public int compare(Rectangle o1, Rectangle o2)
//    {
//      long sum1 = o1.x + o1.y;
//      long sum2 = o2.x + o2.y;
//      if (sum1 < sum2)
//      {
//        return -1;
//      }
//      if (sum1 > sum2)
//      {
//        return 1;
//      }
//      if (o1.y < o2.y)
//      {
//        return -1;
//      }
//      if (o1.x < o2.x)
//      {
//        return -1;
//      }
//      if (o1.y > o2.y)
//      {
//        return 1;
//      }
//      if (o1.x > o2.x)
//      {
//        return 1;
//      }
//      return 0;
//    }
//  }
  
  public static final List<Rectangle> splitBlockArea(int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> blockAreas = new ArrayList<Rectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        Rectangle blockArea = new Rectangle(captureArea.x + j, captureArea.y + i, Math.min(blockWidth, captureArea.width - j), Math.min(blockHeight, captureArea.height - i));
        blockAreas.add(blockArea);
      }
    }
    return blockAreas;
  }
  
  public static final List<Rectangle> mergeNeighbourRectangles(List<Rectangle> rectangles)
  {
    //Collections.sort(rectangles, rectangleComparator);
    boolean found = false;
    //for (Rectangle rectangle : rectangles)
    //{
      //System.out.print(rectangle + ";");
      //System.out.print("block:[width:[" + rectangle.width + "],height:[" + rectangle.height + "],area:[" + rectangle.width * rectangle.height + "]];");
    //}
    //System.out.println();
    // boolean union = false;
    // boolean proceed = false;
    do
    {
      found = false;
      for (int i = 0; i < rectangles.size(); i++)
      {
        Rectangle current = rectangles.get(i);
        // union = false;
        for (int j = i + 1; j < rectangles.size(); j++)
        {
          Rectangle next = rectangles.get(j);
          // neighbour test
          if (((current.y == next.y) && (current.height == next.height) && (current.x + current.width == next.x))
          || ((current.x == next.x) && (current.width == next.width) && (current.y + current.height == next.y)))
          {
            found = true;
            // union = true;
            current = current.union(next);
            rectangles.remove(j--);
            rectangles.set(i, current);
          }
        }
      }
    }
    while (found);
    //System.out.println("merged_blocks:[" + rectangles.size() + "]");
    //for (Rectangle rectangle : rectangles)
    //{
      //System.out.print(rectangle + ";");
      //System.out.print("block:[width:[" + rectangle.width + "],height:[" + rectangle.height + "],area:[" + rectangle.width * rectangle.height + "]];");
    //}
    //System.out.println();
    return rectangles;
  }
  
  public static final void copyArea(byte[] source, byte[] destination, int offset, int width, int height, Rectangle transferArea)
  {
    int length = width * height;
    if (length == 0)
    {
      return;
    }
    
    int index = 0;
    int i = 0;
    // int j = 0;
    int x = Math.min(transferArea.x, width - 1);
    int y = Math.min(transferArea.y, height - 1);
    int m = Math.min(transferArea.width, width - x);
    int n = Math.min(transferArea.height, height - y);
    
    // if (x >= 0 && y >= 0 && m != width && n != height)
    if (x == 0 && y == 0 && m == width && n == height)
    {
      System.arraycopy(source, offset, destination, offset, length);
    }
    else
    {
      index = offset + x + (y * width);
      for (i = 0; i < n; i++)
      {
        System.arraycopy(source, index, destination, index, m);
        index += width;
      }
    }
  }
  
  public static final void copyArea(short[] source, short[] destination, int offset, int width, int height, Rectangle transferArea)
  {
    int length = width * height;
    if (length == 0)
    {
      return;
    }
    
    int index = 0;
    int i = 0;
    // int j = 0;
    int x = Math.min(transferArea.x, width - 1);
    int y = Math.min(transferArea.y, height - 1);
    int m = Math.min(transferArea.width, width - x);
    int n = Math.min(transferArea.height, height - y);
    
    // if (x >= 0 && y >= 0 && m != width && n != height)
    if (x == 0 && y == 0 && m == width && n == height)
    {
      System.arraycopy(source, offset, destination, offset, length);
    }
    else
    {
      index = offset + x + (y * width);
      for (i = 0; i < n; i++)
      {
        System.arraycopy(source, index, destination, index, m);
        index += width;
      }
    }
  }
  
  public static final void copyArea(int[] source, int[] destination, int offset, int width, int height, Rectangle transferArea)
  {
    int length = width * height;
    if (length == 0)
    {
      return;
    }
    
    int index = 0;
    int i = 0;
    // int j = 0;
    int x = Math.min(transferArea.x, width - 1);
    int y = Math.min(transferArea.y, height - 1);
    int m = Math.min(transferArea.width, width - x);
    int n = Math.min(transferArea.height, height - y);
    
    // if (x >= 0 && y >= 0 && m != width && n != height)
    if (x == 0 && y == 0 && m == width && n == height)
    {
      System.arraycopy(source, offset, destination, offset, length);
    }
    else
    {
      index = offset + x + (y * width);
      for (i = 0; i < n; i++)
      {
        System.arraycopy(source, index, destination, index, m);
        index += width;
      }
    }
  }
  
  public static final void copyArea(long[] source, long[] destination, int offset, int width, int height, Rectangle transferArea)
  {
    int length = width * height;
    if (length == 0)
    {
      return;
    }
    
    int index = 0;
    int i = 0;
    // int j = 0;
    int x = Math.min(transferArea.x, width - 1);
    int y = Math.min(transferArea.y, height - 1);
    int m = Math.min(transferArea.width, width - x);
    int n = Math.min(transferArea.height, height - y);
    
    // if (x >= 0 && y >= 0 && m != width && n != height)
    if (x == 0 && y == 0 && m == width && n == height)
    {
      System.arraycopy(source, offset, destination, offset, length);
    }
    else
    {
      index = offset + x + (y * width);
      for (i = 0; i < n; i++)
      {
        System.arraycopy(source, index, destination, index, m);
        index += width;
      }
    }
  }
  
  public static final List<Rectangle> compareBlockArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> blockAreas = new ArrayList<Rectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        Rectangle blockArea = new Rectangle(0, 0, 1, 1);
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        if (!compareArea(array1, array2, offset, width, height, blockArea))
        {
          blockAreas.add(blockArea);
        }
      }
    }
    return blockAreas;
  }
  
  public static final List<Rectangle> compareBlockArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> blockAreas = new ArrayList<Rectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        Rectangle blockArea = new Rectangle(0, 0, 1, 1);
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        if (!compareArea(array1, array2, offset, width, height, blockArea))
        {
          blockAreas.add(blockArea);
        }
      }
    }
    return blockAreas;
  }
  
  public static final List<Rectangle> compareBlockArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> blockAreas = new ArrayList<Rectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        Rectangle blockArea = new Rectangle(0, 0, 1, 1);
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        if (!compareArea(array1, array2, offset, width, height, blockArea))
        {
          blockAreas.add(blockArea);
        }
      }
    }
    return blockAreas;
  }
  
  public static final List<Rectangle> compareBlockArea(long[] array1, long[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> blockAreas = new ArrayList<Rectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        Rectangle blockArea = new Rectangle(0, 0, 1, 1);
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        if (!compareArea(array1, array2, offset, width, height, blockArea))
        {
          blockAreas.add(blockArea);
        }
      }
    }
    return blockAreas;
  }
  
  public static final boolean compareArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        bits |= array1[index] ^ array2[index];
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        bits |= array1[index] ^ array2[index];
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  
  
  public static final boolean compareArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        bits |= array1[index] ^ array2[index];
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(long[] array1, long[] array2, int offset, int width, int height, Rectangle captureArea)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    long bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        bits |= array1[index] ^ array2[index];
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, BitSet pixelBits)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int diff = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        diff = array1[index] ^ array2[index];
        bits |= diff;
        pixelBits.set(index, diff != 0);
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, BitSet pixelBits)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int diff = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        diff = array1[index] ^ array2[index];
        bits |= diff;
        pixelBits.set(index, diff != 0);
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, BitSet pixelBits)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    int bits = 0;
    int diff = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        diff = array1[index] ^ array2[index];
        bits |= diff;
        pixelBits.set(index, diff != 0);
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final boolean compareArea(long[] array1, long[] array2, int offset, int width, int height, Rectangle captureArea, BitSet pixelBits)
  {
    if (width * height == 0)
    {
      return true;
    }
    
    long bits = 0;
    long diff = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        diff = array1[index] ^ array2[index];
        bits |= diff;
        pixelBits.set(index, diff != 0);
        index++;
      }
      offset += width;
    }
    return bits == 0;
  }
  
  public static final void compareBlockArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea));
      }
    }
  }
  
  public static final void compareBlockArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea));
      }
    }
  }
  
  public static final void compareBlockArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea));
      }
    }
  }
  
  public static final void compareBlockArea(long[] array1, long[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea));
      }
    }
  }
  
  public static final void compareBlockArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits, BitSet pixelBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea, pixelBits));
      }
    }
  }
  
  public static final void compareBlockArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits, BitSet pixelBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea, pixelBits));
      }
    }
  }
  
  public static final void compareBlockArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits, BitSet pixelBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea, pixelBits));
      }
    }
  }
  
  public static final void compareBlockArea(long[] array1, long[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight, BitSet blockAreaBits, BitSet pixelBits)
  {
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j, k;
    k = 0;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        blockAreaBits.set(k++, !compareArea(array1, array2, offset, width, height, blockArea, pixelBits));
      }
    }
  }
  
  public static final boolean deltaArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, Rectangle resultArea)
  {
    if (width * height == 0)
    {
      resultArea.x = 0;
      resultArea.y = 0;
      resultArea.width = 0;
      resultArea.height = 0;
      return true;
    }
    
    int delta = 0;
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    int cx;
    int cy;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    int mnx = x + m;
    int mny = y + n;
    int mxx = -1;
    int mxy = -1;
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        delta = array1[index] ^ array2[index];
        if (delta != 0)
        {
          cx = x + j;
          cy = y + i;
          if (mnx > cx)
          {
            mnx = cx;
          }
          if (mny > cy)
          {
            mny = cy;
          }
          if (mxx < cx)
          {
            mxx = cx;
          }
          if (mxy < cy)
          {
            mxy = cy;
          }
        }
        bits |= delta;
        index++;
      }
      offset += width;
    }
    resultArea.x = mnx;
    resultArea.y = mny;
    resultArea.width = Math.max(1 + mxx - mnx, 0);
    resultArea.height = Math.max(1 + mxy - mny, 0);
    return bits == 0;
  }
  
  public static final boolean deltaArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, Rectangle resultArea)
  {
    if (width * height == 0)
    {
      resultArea.x = 0;
      resultArea.y = 0;
      resultArea.width = 0;
      resultArea.height = 0;
      return true;
    }
    
    int delta = 0;
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    int cx;
    int cy;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    int mnx = x + m;
    int mny = y + n;
    int mxx = -1;
    int mxy = -1;
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        delta = array1[index] ^ array2[index];
        if (delta != 0)
        {
          cx = x + j;
          cy = y + i;
          if (mnx > cx)
          {
            mnx = cx;
          }
          if (mny > cy)
          {
            mny = cy;
          }
          if (mxx < cx)
          {
            mxx = cx;
          }
          if (mxy < cy)
          {
            mxy = cy;
          }
        }
        bits |= delta;
        index++;
      }
      offset += width;
    }
    resultArea.x = mnx;
    resultArea.y = mny;
    resultArea.width = Math.max(1 + mxx - mnx, 0);
    resultArea.height = Math.max(1 + mxy - mny, 0);
    return bits == 0;
  }
  
  public static final boolean deltaArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, Rectangle resultArea)
  {
    if (width * height == 0)
    {
      resultArea.x = 0;
      resultArea.y = 0;
      resultArea.width = 0;
      resultArea.height = 0;
      return true;
    }
    
    int delta = 0;
    int bits = 0;
    int index = 0;
    //int offset = 0;
    int i = 0;
    int j = 0;
    int x;
    int y;
    int m;
    int n;
    int cx;
    int cy;
    
    if (captureArea != null && captureArea.x >= 0 && captureArea.y >= 0)
    {
      x = Math.min(captureArea.x, width - 1);
      y = Math.min(captureArea.y, height - 1);
      m = Math.min(captureArea.width, width - x);
      n = Math.min(captureArea.height, height - y);
    }
    else
    {
      x = 0;
      y = 0;
      m = width;
      n = height;
    }
    
    int mnx = x + m;
    int mny = y + n;
    int mxx = -1;
    int mxy = -1;
    
    offset += x + (y * width);
    for (i = 0; i < n; i++)
    {
      index = offset;
      for (j = 0; j < m; j++)
      {
        delta = array1[index] ^ array2[index];
        if (delta != 0)
        {
          cx = x + j;
          cy = y + i;
          if (mnx > cx)
          {
            mnx = cx;
          }
          if (mny > cy)
          {
            mny = cy;
          }
          if (mxx < cx)
          {
            mxx = cx;
          }
          if (mxy < cy)
          {
            mxy = cy;
          }
        }
        bits |= delta;
        index++;
      }
      offset += width;
    }
    resultArea.x = mnx;
    resultArea.y = mny;
    resultArea.width = Math.max(1 + mxx - mnx, 0);
    resultArea.height = Math.max(1 + mxy - mny, 0);
    return bits == 0;
  }
  
  public static final List<Rectangle> deltaBlockArea(byte[] array1, byte[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> deltaAreas = new ArrayList<Rectangle>();
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        Rectangle deltaArea = new Rectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  public static final List<Rectangle> deltaBlockArea(short[] array1, short[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> deltaAreas = new ArrayList<Rectangle>();
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        Rectangle deltaArea = new Rectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  public static final List<Rectangle> deltaBlockArea(int[] array1, int[] array2, int offset, int width, int height, Rectangle captureArea, int blockWidth, int blockHeight)
  {
    List<Rectangle> deltaAreas = new ArrayList<Rectangle>();
    Rectangle blockArea = new Rectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        Rectangle deltaArea = new Rectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  // private static final int DCM_RED_MASK = 0x00ff0000;
  // private static final int DCM_GREEN_MASK = 0x0000ff00;
  // private static final int DCM_BLUE_MASK = 0x000000ff;
  // private static final int DCM_ALPHA_MASK = 0xff000000;
}