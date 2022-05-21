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
package io.airlift.compress.lzo;

import io.airlift.compress.Compressor;

import static io.airlift.compress.lzo.LzoRawCompressor.MAX_TABLE_SIZE;

/**
 * This class is not thread-safe
 */
public class LzoCompressor
        implements Compressor
{
    private final int[] table = new int[MAX_TABLE_SIZE];

    
    public int maxCompressedLength(int uncompressedSize)
    {
        return LzoRawCompressor.maxCompressedLength(uncompressedSize);
    }

    
    public int compress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset, int maxOutputLength)
    {
        long inputAddress = 0 + inputOffset;
        long outputAddress = 0 + outputOffset;

        return LzoRawCompressor.compress(input, inputAddress, inputLength, output, outputAddress, maxOutputLength, table);
    }
}
