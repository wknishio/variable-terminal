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
package vate.io.airlift.compress.zstd;

import static vate.io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static vate.io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

import vate.io.airlift.compress.MalformedInputException;
import vate.io.airlift.compress.UnsafeUtils;

public final class ZstdUtil
{
    private ZstdUtil()
    {
    }
    
    public static int maxCompressedLength(int uncompressedSize)
    {
        int result = uncompressedSize + (uncompressedSize >>> 8);

        if (uncompressedSize < MAX_BLOCK_SIZE) {
            result += (MAX_BLOCK_SIZE - uncompressedSize) >>> 11;
        }

        return result;
    }

    public static int highestBit(int value)
    {
        return 31 - Integer.numberOfLeadingZeros(value);
    }

    public static boolean isPowerOf2(int value)
    {
        return (value & (value - 1)) == 0;
    }

    public static int mask(int bits)
    {
        return (1 << bits) - 1;
    }

    public static void verify(boolean condition, long offset, String reason)
    {
        if (!condition) {
            throw new MalformedInputException(offset, reason);
        }
    }

    public static void checkArgument(boolean condition, String reason)
    {
        if (!condition) {
            throw new IllegalArgumentException(reason);
        }
    }

    static void checkPositionIndexes(int start, int end, int size)
    {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
        }
    }

    private static String badPositionIndexes(int start, int end, int size)
    {
        if (start < 0 || start > size) {
            return badPositionIndex(start, size, "start index");
        }
        if (end < 0 || end > size) {
            return badPositionIndex(end, size, "end index");
        }
        // end < start
        return String.format("end index (%s) must not be less than start index (%s)", end, start);
    }

    private static String badPositionIndex(int index, int size, String desc)
    {
        if (index < 0) {
            return String.format("%s (%s) must not be negative", desc, index);
        }
        else if (size < 0) {
            throw new IllegalArgumentException("negative size: " + size);
        }
        else { // index > size
            return String.format("%s (%s) must not be greater than size (%s)", desc, index, size);
        }
    }

    public static void checkState(boolean condition, String reason)
    {
        if (!condition) {
            throw new IllegalStateException(reason);
        }
    }

    public static MalformedInputException fail(long offset, String reason)
    {
        throw new MalformedInputException(offset, reason);
    }

    public static int cycleLog(int hashLog, CompressionParameters.Strategy strategy)
    {
        int cycleLog = hashLog;
        if (strategy == CompressionParameters.Strategy.BTLAZY2 || strategy == CompressionParameters.Strategy.BTOPT || strategy == CompressionParameters.Strategy.BTULTRA) {
            cycleLog = hashLog - 1;
        }
        return cycleLog;
    }

    public static int get24BitLittleEndian(Object inputBase, long inputAddress)
    {
        return (UnsafeUtils.getShort(inputBase, inputAddress) & 0xFFFF)
                | ((UnsafeUtils.getByte(inputBase, inputAddress + SIZE_OF_SHORT) & 0xFF) << Short.SIZE);
    }

    public static void put24BitLittleEndian(Object outputBase, long outputAddress, int value)
    {
      UnsafeUtils.putShort(outputBase, outputAddress, (short) value);
      UnsafeUtils.putByte(outputBase, outputAddress + SIZE_OF_SHORT, (byte) (value >>> Short.SIZE));
    }

    // provides the minimum logSize to safely represent a distribution
    public static int minTableLog(int inputSize, int maxSymbolValue)
    {
        if (inputSize <= 1) {
            throw new IllegalArgumentException("Not supported. RLE should be used instead"); // TODO
        }

        int minBitsSrc = highestBit((inputSize - 1)) + 1;
        int minBitsSymbols = highestBit(maxSymbolValue) + 2;
        return Math.min(minBitsSrc, minBitsSymbols);
    }
}
