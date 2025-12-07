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
// Auto-generated: DO NOT EDIT

package net.jpountz.xxhashvt;

import static java.lang.Long.rotateLeft;
import static net.jpountz.xxhashvt.XXHashConstants.*;

import java.nio.ByteBuffer;

import net.jpountz.utilvt.ByteBufferUtils;
import net.jpountz.utilvt.SafeUtils;

/**
 * {@link XXHash64} implementation.
 */
final class XXHash64JavaSafe extends XXHash64 {

  public static final XXHash64 INSTANCE = new XXHash64JavaSafe();

  
  public long hash(byte[] buf, int off, int len, long seed) {

    SafeUtils.checkRange(buf, off, len);

    final int end = off + len;
    long h64;

    if (len >= 32) {
      final int limit = end - 32;
      long v1 = seed + PRIME64_1 + PRIME64_2;
      long v2 = seed + PRIME64_2;
      long v3 = seed + 0;
      long v4 = seed - PRIME64_1;
      do {
        v1 += SafeUtils.readLongLE(buf, off) * PRIME64_2;
        v1 = rotateLeft(v1, 31);
        v1 *= PRIME64_1;
        off += 8;

        v2 += SafeUtils.readLongLE(buf, off) * PRIME64_2;
        v2 = rotateLeft(v2, 31);
        v2 *= PRIME64_1;
        off += 8;

        v3 += SafeUtils.readLongLE(buf, off) * PRIME64_2;
        v3 = rotateLeft(v3, 31);
        v3 *= PRIME64_1;
        off += 8;

        v4 += SafeUtils.readLongLE(buf, off) * PRIME64_2;
        v4 = rotateLeft(v4, 31);
        v4 *= PRIME64_1;
        off += 8;
      } while (off <= limit);

      h64 = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);

      v1 *= PRIME64_2; v1 = rotateLeft(v1, 31); v1 *= PRIME64_1; h64 ^= v1;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v2 *= PRIME64_2; v2 = rotateLeft(v2, 31); v2 *= PRIME64_1; h64 ^= v2;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v3 *= PRIME64_2; v3 = rotateLeft(v3, 31); v3 *= PRIME64_1; h64 ^= v3;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v4 *= PRIME64_2; v4 = rotateLeft(v4, 31); v4 *= PRIME64_1; h64 ^= v4;
      h64 = h64 * PRIME64_1 + PRIME64_4;
    } else {
      h64 = seed + PRIME64_5;
    }

    h64 += len;

    while (off <= end - 8) {
      long k1 = SafeUtils.readLongLE(buf, off);
      k1 *= PRIME64_2; k1 = rotateLeft(k1, 31); k1 *= PRIME64_1; h64 ^= k1;
      h64 = rotateLeft(h64, 27) * PRIME64_1 + PRIME64_4;
      off += 8;
    }

    if (off <= end - 4) {
      h64 ^= (SafeUtils.readIntLE(buf, off) & 0xFFFFFFFFL) * PRIME64_1;
      h64 = rotateLeft(h64, 23) * PRIME64_2 + PRIME64_3;
      off += 4;
    }

    while (off < end) {
      h64 ^= (SafeUtils.readByte(buf, off) & 0xFF) * PRIME64_5;
      h64 = rotateLeft(h64, 11) * PRIME64_1;
      ++off;
    }

    h64 ^= h64 >>> 33;
    h64 *= PRIME64_2;
    h64 ^= h64 >>> 29;
    h64 *= PRIME64_3;
    h64 ^= h64 >>> 32;

    return h64;
  }

  
  public long hash(ByteBuffer buf, int off, int len, long seed) {

    if (buf.hasArray()) {
      return hash(buf.array(), off + buf.arrayOffset(), len, seed);
    }
    ByteBufferUtils.checkRange(buf, off, len);
    buf = ByteBufferUtils.inLittleEndianOrder(buf);

    final int end = off + len;
    long h64;

    if (len >= 32) {
      final int limit = end - 32;
      long v1 = seed + PRIME64_1 + PRIME64_2;
      long v2 = seed + PRIME64_2;
      long v3 = seed + 0;
      long v4 = seed - PRIME64_1;
      do {
        v1 += ByteBufferUtils.readLongLE(buf, off) * PRIME64_2;
        v1 = rotateLeft(v1, 31);
        v1 *= PRIME64_1;
        off += 8;

        v2 += ByteBufferUtils.readLongLE(buf, off) * PRIME64_2;
        v2 = rotateLeft(v2, 31);
        v2 *= PRIME64_1;
        off += 8;

        v3 += ByteBufferUtils.readLongLE(buf, off) * PRIME64_2;
        v3 = rotateLeft(v3, 31);
        v3 *= PRIME64_1;
        off += 8;

        v4 += ByteBufferUtils.readLongLE(buf, off) * PRIME64_2;
        v4 = rotateLeft(v4, 31);
        v4 *= PRIME64_1;
        off += 8;
      } while (off <= limit);

      h64 = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);

      v1 *= PRIME64_2; v1 = rotateLeft(v1, 31); v1 *= PRIME64_1; h64 ^= v1;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v2 *= PRIME64_2; v2 = rotateLeft(v2, 31); v2 *= PRIME64_1; h64 ^= v2;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v3 *= PRIME64_2; v3 = rotateLeft(v3, 31); v3 *= PRIME64_1; h64 ^= v3;
      h64 = h64 * PRIME64_1 + PRIME64_4;

      v4 *= PRIME64_2; v4 = rotateLeft(v4, 31); v4 *= PRIME64_1; h64 ^= v4;
      h64 = h64 * PRIME64_1 + PRIME64_4;
    } else {
      h64 = seed + PRIME64_5;
    }

    h64 += len;

    while (off <= end - 8) {
      long k1 = ByteBufferUtils.readLongLE(buf, off);
      k1 *= PRIME64_2; k1 = rotateLeft(k1, 31); k1 *= PRIME64_1; h64 ^= k1;
      h64 = rotateLeft(h64, 27) * PRIME64_1 + PRIME64_4;
      off += 8;
    }

    if (off <= end - 4) {
      h64 ^= (ByteBufferUtils.readIntLE(buf, off) & 0xFFFFFFFFL) * PRIME64_1;
      h64 = rotateLeft(h64, 23) * PRIME64_2 + PRIME64_3;
      off += 4;
    }

    while (off < end) {
      h64 ^= (ByteBufferUtils.readByte(buf, off) & 0xFF) * PRIME64_5;
      h64 = rotateLeft(h64, 11) * PRIME64_1;
      ++off;
    }

    h64 ^= h64 >>> 33;
    h64 *= PRIME64_2;
    h64 ^= h64 >>> 29;
    h64 *= PRIME64_3;
    h64 ^= h64 >>> 32;

    return h64;
  }


}

