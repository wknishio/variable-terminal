package org.vash.vate.graphics.codec;

import java.util.HashMap;
import java.util.Map;

public class VTFrameDifferencePalette
{
  private int count;
  private byte[] bytePaletteArray;
  private short[] shortPaletteArray;
  private int[] intPaletteArray;
  private long[] longPaletteArray;
  private Map<Byte, Integer> bytePalleteMap;
  private Map<Short, Integer> shortPalleteMap;
  private Map<Integer, Integer> intPalleteMap;
  private Map<Long, Integer> longPalleteMap;
  
  public VTFrameDifferencePalette(int size)
  {
    this.bytePaletteArray = new byte[size];
    this.shortPaletteArray = new short[size];
    this.intPaletteArray = new int[size];
    this.longPaletteArray = new long[size];
    this.bytePalleteMap = new HashMap<Byte, Integer>();
    this.shortPalleteMap = new HashMap<Short, Integer>();
    this.intPalleteMap = new HashMap<Integer, Integer>();
    this.longPalleteMap = new HashMap<Long, Integer>();
  }
  
  public void reset()
  {
    count = 0;
  }
  
  public int size()
  {
    return bytePaletteArray.length;
  }
  
  public int count()
  {
    return count;
  }
  
  public int addByteColor(byte color)
  {
    for (int i = 0; i < count; i++)
    {
      if (bytePaletteArray[i] == color)
      {
        return i;
      }
    }
    bytePaletteArray[count++] = color;
    bytePalleteMap.put(color, count - 1);
    return count - 1;
  }
  
  public int addShortColor(short color)
  {
    for (int i = 0; i < count; i++)
    {
      if (shortPaletteArray[i] == color)
      {
        return i;
      }
    }
    shortPaletteArray[count++] = color;
    shortPalleteMap.put(color, count - 1);
    return count - 1;
  }
  
  public int addIntColor(int color)
  {
    for (int i = 0; i < count; i++)
    {
      if (intPaletteArray[i] == color)
      {
        return i;
      }
    }
    intPaletteArray[count++] = color;
    intPalleteMap.put(color, count - 1);
    return count - 1;
  }
  
  public int addLongColor(long color)
  {
    for (int i = 0; i < count; i++)
    {
      if (longPaletteArray[i] == color)
      {
        return i;
      }
    }
    longPaletteArray[count++] = color;
    longPalleteMap.put(color, count - 1);
    return count - 1;
  }
  
  public byte getByteValue(int index)
  {
    return bytePaletteArray[index];
  }
  
  public short getShortValue(int index)
  {
    return shortPaletteArray[index];
  }
  
  public int getIntValue(int index)
  {
    return intPaletteArray[index];
  }
  
  public long getLongValue(int index)
  {
    return longPaletteArray[index];
  }
  
  public int getByteIndex(byte value)
  {
    return bytePalleteMap.get(value);
  }
  
  public int getShortIndex(short value)
  {
    return shortPalleteMap.get(value);
  }
  
  public int getIntIndex(int value)
  {
    return intPalleteMap.get(value);
  }
  
  public int getLongIndex(long value)
  {
    return longPalleteMap.get(value);
  }
}