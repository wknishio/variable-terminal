/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.airlift.compress;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import static net.jpountz.util.Utils.NATIVE_BYTE_ORDER;

//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.nio.ByteOrder;

//import sun.misc.Unsafe;

public final class UnsafeUtils {

//  private static final Unsafe UNSAFE;
//  private static final long BYTE_ARRAY_OFFSET;
//  private static final int BYTE_ARRAY_SCALE;
//  private static final long INT_ARRAY_OFFSET;
//  private static final int INT_ARRAY_SCALE;
//  private static final long SHORT_ARRAY_OFFSET;
//  private static final int SHORT_ARRAY_SCALE;
//  
//  static {
//    try {
//    	Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
//    	unsafeConstructor.setAccessible(true);
//    	UNSAFE = unsafeConstructor.newInstance();
//      BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
//      BYTE_ARRAY_SCALE = UNSAFE.arrayIndexScale(byte[].class);
//      INT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(int[].class);
//      INT_ARRAY_SCALE = UNSAFE.arrayIndexScale(int[].class);
//      SHORT_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(short[].class);
//      SHORT_ARRAY_SCALE = UNSAFE.arrayIndexScale(short[].class);
//    } catch (Exception e) {
//      throw new ExceptionInInitializerError("Cannot access Unsafe");
//    }
//  }

  public static void checkRange(byte[] buf, int off) {
    SafeUtils.checkRange(buf, off);
  }

  public static void checkRange(byte[] buf, int off, int len) {
    SafeUtils.checkRange(buf, off, len);
  }

  public static void checkLength(int len) {
    SafeUtils.checkLength(len);
  }

  public static byte readByte(byte[] src, int srcOff) {
    //return UNSAFE.getByte(src, BYTE_ARRAY_OFFSET + BYTE_ARRAY_SCALE * srcOff);
    return SafeUtils.readByte(src, srcOff);
  }

  public static void writeByte(byte[] src, int srcOff, byte value) {
    //UNSAFE.putByte(src, BYTE_ARRAY_OFFSET + BYTE_ARRAY_SCALE * srcOff, (byte) value);
    SafeUtils.writeByte(src, srcOff, value);
  }

  public static void writeByte(byte[] src, int srcOff, int value) {
    writeByte(src, srcOff, (byte) value);
  }

  public static long readLong(byte[] src, int srcOff) {
    //return UNSAFE.getLong(src, BYTE_ARRAY_OFFSET + srcOff);
    return SafeUtils.readLong(src, srcOff);
  }

  public static long readLongLE(byte[] src, int srcOff) {
    long i = readLong(src, srcOff);
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
      //i = Long.reverseBytes(i);
    //}
    return i;
  }

  public static void writeLong(byte[] dest, int destOff, long value) {
    //UNSAFE.putLong(dest, BYTE_ARRAY_OFFSET + destOff, value);
    SafeUtils.writeLongLE(dest, destOff, value);
  }
    
  public static void writeLongLE(byte[] dest, int destOff, long value) {
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
    //{
      //writeIntLE(dest, destOff, (int) value);
      //writeIntLE(dest, destOff + 4, (int) (value >>> 32));
    //}
    //else
    //{
      //writeLong(dest, destOff, value);
    //}
    writeLong(dest, destOff, value);
  }

  public static int readInt(byte[] src, int srcOff) {
    //return UNSAFE.getInt(src, BYTE_ARRAY_OFFSET + srcOff);
    return SafeUtils.readInt(src, srcOff);
  }

  public static int readIntLE(byte[] src, int srcOff) {
    int i = readInt(src, srcOff);
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
      //i = Integer.reverseBytes(i);
    //}
    return i;
  }

  public static void writeInt(byte[] dest, int destOff, int value) {
    //UNSAFE.putInt(dest, BYTE_ARRAY_OFFSET + destOff, value);
    SafeUtils.writeInt(dest, destOff, value);
  }
  
  public static void writeIntLE(byte[] dest, int destOff, int value) {
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
    //{
      //writeShortLE(dest, destOff, (short) value);
      //writeShortLE(dest, destOff + 2, (short) (value >>> 16));
    //}
    //else
    //{
      //writeInt(dest, destOff, value);
    //}
    writeInt(dest, destOff, value);
  }

  public static int readShort(byte[] src, int srcOff) {
    return SafeUtils.readShort(src, srcOff);
    //return UNSAFE.getShort(src, BYTE_ARRAY_OFFSET + srcOff);
  }

  public static int readShortLE(byte[] src, int srcOff) {
    int s = readShort(src, srcOff);
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
      //s = Integer.reverseBytes(s);
    //}
    return s & 0xFFFF;
  }

  public static void writeShort(byte[] dest, int destOff, int value) {
    //UNSAFE.putShort(dest, BYTE_ARRAY_OFFSET + destOff, (short)value);
    SafeUtils.writeShort(dest, destOff, value);
  }

  public static void writeShortLE(byte[] buf, int off, int v) {
    //if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
    //{
      //writeByte(buf, off, (byte) v);
      //writeByte(buf, off + 1, (byte) (v >>> 8));
    //}
    //else
    //{
      //writeShort(buf, off, v);
    //}
    writeShort(buf, off, v);
  }

  public static int readInt(int[] src, int srcOff) {
    return SafeUtils.readInt(src, srcOff);
    //return UNSAFE.getInt(src, INT_ARRAY_OFFSET + INT_ARRAY_SCALE * srcOff);
  }

  public static void writeInt(int[] dest, int destOff, int value) {
    SafeUtils.writeInt(dest, destOff, value);
    //UNSAFE.putInt(dest, INT_ARRAY_OFFSET + INT_ARRAY_SCALE * destOff, value);
  }

  public static int readShort(short[] src, int srcOff) {
    return SafeUtils.readShort(src, srcOff);
    //return UNSAFE.getShort(src, SHORT_ARRAY_OFFSET + SHORT_ARRAY_SCALE * srcOff) & 0xFFFF;
  }

  public static void writeShort(short[] dest, int destOff, int value) {
    SafeUtils.writeShort(dest, destOff, value);
    //UNSAFE.putShort(dest, SHORT_ARRAY_OFFSET + SHORT_ARRAY_SCALE * destOff, (short) value);
  }
  
  public static byte getByte(byte[] src, long srcOff)
  {
    return readByte(src, (int)srcOff);
  }
  
  public static void putByte(byte[] src, long srcOff, int value)
  {
    writeByte(src, (int)srcOff, value);
  }
  
  public static int getShort(byte[] src, long srcOff)
  {
    return readShortLE(src, (int)srcOff);
  }
  
  public static void putShort(byte[] src, long srcOff, int value)
  {
    writeShortLE(src, (int)srcOff, value);
  }
  
  public static int getInt(byte[] src, long srcOff)
  {
    return readIntLE(src, (int)srcOff);
  }
  
  public static void putInt(byte[] src, long srcOff, int value)
  {
    writeIntLE(src, (int)srcOff, value);
  }
  
  public static long getLong(byte[] src, long srcOff)
  {
    return readLongLE(src, (int)srcOff);
  }
  
  public static void putLong(byte[] src, long srcOff, long value)
  {
    writeLongLE(src, (int)srcOff, value);
  }
  
  public static void copyMemory(byte[] src, int soff, byte[] dst, int doff, int len)
  {
    System.arraycopy(src, soff, dst, doff, len);
    //UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + soff, dst, BYTE_ARRAY_OFFSET + doff, len);
  }
  
  public static void copyMemory(byte[] src, long soff, byte[] dst, long doff, int len)
  {
    System.arraycopy(src, (int)soff, dst, (int)doff, len);
    //UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + soff, dst, BYTE_ARRAY_OFFSET + doff, len);
  }
  
  public static void copyMemory(byte[] src, long soff, byte[] dst, long doff, long len)
  {
    System.arraycopy(src, (int)soff, dst, (int)doff, (int)len);
    //UNSAFE.copyMemory(src, BYTE_ARRAY_OFFSET + soff, dst, BYTE_ARRAY_OFFSET + doff, len);
  }
  
  public static byte readByte(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readByte((byte[]) src, srcOff);
//    }
//    return UNSAFE.getByte(src, (long)srcOff);
    return readByte((byte[]) src, srcOff);
  }

  public static void writeByte(Object src, int srcOff, byte value) {
//    if (src instanceof byte[])
//    {
//      writeByte((byte[]) src, srcOff, value);
//      return;
//    }
//    UNSAFE.putByte(src, (long)srcOff, (byte) value);
    writeByte((byte[]) src, srcOff, value);
  }

  public static void writeByte(Object src, int srcOff, int value) {
//    if (src instanceof byte[])
//    {
//      writeByte((byte[]) src, srcOff, value);
//      return;
//    }
//    writeByte(src, srcOff, (byte) value);
    writeByte((byte[]) src, srcOff, value);
  }

  public static long readLong(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readLong((byte[]) src, srcOff);
//    }
//    return UNSAFE.getLong(src, (long)srcOff);
    return readLong((byte[]) src, srcOff);
  }

  public static long readLongLE(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readLongLE((byte[]) src, srcOff);
//    }
//    long i = readLong(src, srcOff);
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
//      i = Long.reverseBytes(i);
//    }
//    return i;
    return readLongLE((byte[]) src, srcOff);
  }

  public static void writeLong(Object dest, int destOff, long value) {
//    if (dest instanceof byte[])
//    {
//      writeLong((byte[]) dest, destOff, value);
//      return;
//    }
//    UNSAFE.putLong(dest, (long)destOff, value);
    writeLong((byte[]) dest, destOff, value);
  }
    
  public static void writeLongLE(Object dest, int destOff, long value) {
//    if (dest instanceof byte[])
//    {
//      writeLongLE((byte[]) dest, destOff, value);
//      return;
//    }
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
//    {
//      writeIntLE(dest, destOff, (int) value);
//      writeIntLE(dest, destOff + 4, (int) (value >>> 32));
//    }
//    else
//    {
//      writeLong(dest, destOff, value);
//    }
    writeLongLE((byte[]) dest, destOff, value);
  }

  public static int readInt(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readInt((byte[]) src, srcOff);
//    }
//    return UNSAFE.getInt(src, (long)srcOff);
    return readInt((byte[]) src, srcOff);
  }

  public static int readIntLE(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readIntLE((byte[]) src, srcOff);
//    }
//    int i = readInt(src, srcOff);
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
//      i = Integer.reverseBytes(i);
//    }
//    return i;
    return readIntLE((byte[]) src, srcOff);
  }

  public static void writeInt(Object dest, int destOff, int value) {
//    if (dest instanceof byte[])
//    {
//      writeInt((byte[]) dest, destOff, value);
//      return;
//    }
//    UNSAFE.putInt(dest, (long)destOff, value);
    writeInt((byte[]) dest, destOff, value);
  }
  
  public static void writeIntLE(Object dest, int destOff, int value) {
//    if (dest instanceof byte[])
//    {
//      writeIntLE((byte[]) dest, destOff, value);
//      return;
//    }
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
//    {
//      writeShortLE(dest, destOff, (short) value);
//      writeShortLE(dest, destOff + 2, (short) (value >>> 16));
//    }
//    else
//    {
//      writeInt(dest, destOff, value);
//    }
    writeIntLE((byte[]) dest, destOff, value);
  }

  public static int readShort(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readShort((byte[]) src, srcOff);
//    }
//    return UNSAFE.getShort(src, (long)srcOff);
    return readShort((byte[]) src, srcOff);
  }

  public static int readShortLE(Object src, int srcOff) {
//    if (src instanceof byte[])
//    {
//      return readShortLE((byte[]) src, srcOff);
//    }
//    int s = readShort(src, srcOff);
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN) {
//      s = Integer.reverseBytes(s);
//    }
//    return s & 0xFFFF;
    return readShortLE((byte[]) src, srcOff);
  }

  public static void writeShort(Object dest, int destOff, int value) {
//    if (dest instanceof byte[])
//    {
//      writeShort((byte[]) dest, destOff, value);
//      return;
//    }
//    UNSAFE.putShort(dest, (long)destOff, (short)value);
    writeShort((byte[]) dest, destOff, value);
  }

  public static void writeShortLE(Object dest, int destOff, int value) {
//    if (dest instanceof byte[])
//    {
//      writeShortLE((byte[]) dest, destOff, value);
//      return;
//    }
//    if (NATIVE_BYTE_ORDER == ByteOrder.BIG_ENDIAN)
//    {
//      writeByte(dest, destOff, (byte) value);
//      writeByte(dest, destOff + 1, (byte) (value >>> 8));
//    }
//    else
//    {
//      writeShort(dest, destOff, value);
//    }
    writeShortLE((byte[]) dest, destOff, value);
  }
  
  public static byte getByte(Object src, long srcOff)
  {
//    if (src instanceof byte[])
//    {
//      return getByte((byte[]) src, srcOff);
//    }
//    return readByte(src, (int)srcOff);
    return getByte((byte[]) src, srcOff);
  }
  
  public static void putByte(Object src, long srcOff, int value)
  {
//    if (src instanceof byte[])
//    {
//      writeByte((byte[]) src, (int)srcOff, value);
//      return;
//    }
//    writeByte(src, (int)srcOff, value);
    writeByte((byte[]) src, (int)srcOff, value);
  }
  
  public static int getShort(Object src, long srcOff)
  {
//    if (src instanceof byte[])
//    {
//      return getShort((byte[]) src, (int)srcOff); 
//    }
//    return readShortLE(src, (int)srcOff);
    return getShort((byte[]) src, (int)srcOff); 
  }
  
  public static void putShort(Object src, long srcOff, int value)
  {
//    if (src instanceof byte[])
//    {
//      putShort((byte[]) src, srcOff, value);
//      return;
//    }
//    writeShortLE(src, (int)srcOff, value);
    putShort((byte[]) src, srcOff, value);
  }
  
  public static int getInt(Object src, long srcOff)
  {
//    if (src instanceof byte[])
//    {
//      return getInt((byte[]) src, srcOff);  
//    }
//    return readIntLE(src, (int)srcOff);
    return getInt((byte[]) src, srcOff);  
  }
  
  public static void putInt(Object src, long srcOff, int value)
  {
//    if (src instanceof byte[])
//    {
//      putInt((byte[]) src, srcOff, value);
//      return;
//    }
//    writeIntLE(src, (int)srcOff, value);
    putInt((byte[]) src, srcOff, value);
  }
  
  public static long getLong(Object src, long srcOff)
  {
//    if (src instanceof byte[])
//    {
//      return getLong((byte[]) src, srcOff);
//    }
//    return readLongLE(src, (int)srcOff);
    return getLong((byte[]) src, srcOff);
  }
  
  public static void putLong(Object src, long srcOff, long value)
  {
//    if (src instanceof byte[])
//    {
//      putLong((byte[]) src, srcOff, value);
//      return;
//    }
//    writeLongLE(src, (int)srcOff, value);
    putLong((byte[]) src, srcOff, value);
  }
  
  public static void copyMemory(Object src, int soff, Object dst, int doff, int len)
  {
//    if (src instanceof byte[])
//    {
//      System.arraycopy(src, soff, dst, doff, len);
//    }
    System.arraycopy(src, soff, dst, doff, len);
  }
  
  public static void copyMemory(Object src, long soff, Object dst, long doff, int len)
  {
//    if (src instanceof byte[])
//    {
//      System.arraycopy(src, (int)soff, dst, (int)doff, len);
//    }
    System.arraycopy(src, (int)soff, dst, (int)doff, len);
  }
  
  public static void copyMemory(Object src, long soff, Object dst, long doff, long len)
  {
//    if (src instanceof byte[])
//    {
//      System.arraycopy(src, (int)soff, dst, (int)doff, (int)len);
//    }
    System.arraycopy(src, (int)soff, dst, (int)doff, (int)len);
  }
  
}
