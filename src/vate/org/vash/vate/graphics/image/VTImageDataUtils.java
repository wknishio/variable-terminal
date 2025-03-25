package org.vash.vate.graphics.image;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
  
  public static final List<VTRectangle> splitBlockArea(final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> blockAreas = new LinkedList<VTRectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        VTRectangle blockArea = new VTRectangle(captureArea.x + j, captureArea.y + i, Math.min(blockWidth, captureArea.width - j), Math.min(blockHeight, captureArea.height - i));
        blockAreas.add(blockArea);
      }
    }
    return blockAreas;
  }
  
  public static final List<VTRectangle> mergeNeighbourVTRectangles(final List<VTRectangle> rectangles)
  {
    //Collections.sort(rectangles, rectangleComparator);
    boolean found = false;
    ListIterator<VTRectangle> iterator;
    VTRectangle current = null;
    VTRectangle next = null;
    do
    {
      found = false;
      iterator = rectangles.listIterator();
      current = null;
      next = null;
      if (iterator.hasNext())
      {
        current = iterator.next();
      }
      while (iterator.hasNext())
      {
        next = iterator.next();
        // neighbour test
        if (((current.y == next.y) && (current.height == next.height) && (current.x + current.width == next.x))
        || ((current.x == next.x) && (current.width == next.width) && (current.y + current.height == next.y)))
        {
          found = true;
          // union = true;
          current = current.union(next);
          iterator.remove();
          iterator.previous();
          iterator.set(current);
        }
        else
        {
          current = next;
        }
      }
    }
    while (found);
    return rectangles;
  }
  
  public static final void copyArea(final byte[] source, final byte[] destination, final int offset, final int width, final int height, final VTRectangle transferArea)
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
  
  public static final void copyArea(final short[] source, final short[] destination, final int offset, final int width, final int height, final VTRectangle transferArea)
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
  
  public static final void copyArea(final int[] source, final int[] destination, final int offset, final int width, final int height, final VTRectangle transferArea)
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
  
  public static final void copyArea(final long[] source, final long[] destination, final int offset, final int width, final int height, final VTRectangle transferArea)
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
  
  public static final boolean compareArea(final byte[] array1, final byte[] array2, int offset, final int width, final int height, final VTRectangle captureArea)
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
  
  public static final boolean compareArea(final short[] array1, final short[] array2, int offset, final int width, final int height, final VTRectangle captureArea)
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
  
  public static final boolean compareArea(final int[] array1, final int[] array2, int offset, final int width, final int height, final VTRectangle captureArea)
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
  
  public static final boolean compareArea(final long[] array1, final long[] array2, int offset, final int width, final int height, final VTRectangle captureArea)
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
  
  public static final boolean compareArea(final byte[] array1, final byte[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final BitSet pixelBits)
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
  
  public static final boolean compareArea(final short[] array1, final short[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final BitSet pixelBits)
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
  
  public static final boolean compareArea(final int[] array1, final int[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final BitSet pixelBits)
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
  
  public static final boolean compareArea(final long[] array1, final long[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final BitSet pixelBits)
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
  public static final List<VTRectangle> compareBlockArea(final byte[] array1, final byte[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> blockAreas = new LinkedList<VTRectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final List<VTRectangle> compareBlockArea(final short[] array1, final short[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> blockAreas = new LinkedList<VTRectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final List<VTRectangle> compareBlockArea(final int[] array1, final int[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> blockAreas = new LinkedList<VTRectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final List<VTRectangle> compareBlockArea(final long[] array1, final long[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> blockAreas = new LinkedList<VTRectangle>();
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final byte[] array1, final byte[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final short[] array1, final short[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final int[] array1, final int[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final long[] array1, final long[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final byte[] array1, final byte[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits, final BitSet pixelBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final short[] array1, final short[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits, final BitSet pixelBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final int[] array1, final int[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits, final BitSet pixelBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final void compareBlockArea(final long[] array1, final long[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight, final BitSet blockAreaBits, final BitSet pixelBits)
  {
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
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
  
  public static final boolean deltaArea(final byte[] array1, final byte[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final VTRectangle resultArea)
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
  
  public static final boolean deltaArea(final short[] array1, final short[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final VTRectangle resultArea)
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
  
  public static final boolean deltaArea(final int[] array1, final int[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final VTRectangle resultArea)
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
  
  public static final boolean deltaArea(final long[] array1, final long[] array2, int offset, final int width, final int height, final VTRectangle captureArea, final VTRectangle resultArea)
  {
    if (width * height == 0)
    {
      resultArea.x = 0;
      resultArea.y = 0;
      resultArea.width = 0;
      resultArea.height = 0;
      return true;
    }
    
    long delta = 0;
    long bits = 0;
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
  
  public static final List<VTRectangle> deltaBlockArea(final byte[] array1, final byte[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> deltaAreas = new LinkedList<VTRectangle>();
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        VTRectangle deltaArea = new VTRectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  public static final List<VTRectangle> deltaBlockArea(final short[] array1, final short[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> deltaAreas = new LinkedList<VTRectangle>();
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        VTRectangle deltaArea = new VTRectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  public static final List<VTRectangle> deltaBlockArea(final int[] array1, final int[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> deltaAreas = new LinkedList<VTRectangle>();
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        VTRectangle deltaArea = new VTRectangle(0, 0, 1, 1);
        if (!deltaArea(array1, array2, offset, width, height, blockArea, deltaArea) && deltaArea.width != 0 && deltaArea.height != 0)
        {
          deltaAreas.add(deltaArea);
        }
      }
    }
    return deltaAreas;
  }
  
  public static final List<VTRectangle> deltaBlockArea(final long[] array1, final long[] array2, final int offset, final int width, final int height, final VTRectangle captureArea, final int blockWidth, final int blockHeight)
  {
    List<VTRectangle> deltaAreas = new LinkedList<VTRectangle>();
    VTRectangle blockArea = new VTRectangle(0, 0, 1, 1);
    int i, j;
    for (i = 0; i < captureArea.height; i += blockHeight)
    {
      for (j = 0; j < captureArea.width; j += blockWidth)
      {
        blockArea.x = captureArea.x + j;
        blockArea.y = captureArea.y + i;
        blockArea.width = Math.min(blockWidth, captureArea.width - j);
        blockArea.height = Math.min(blockHeight, captureArea.height - i);
        VTRectangle deltaArea = new VTRectangle(0, 0, 1, 1);
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