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
package io.airlift.compress.zstd;

import io.airlift.compress.Compressor;

import java.nio.ByteBuffer;

import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;
import static java.lang.String.format;
import static org.vash.vate.compatibility.VTObjects.requireNonNull;


public class ZstdCompressor
        implements Compressor
{
  
    private CompressionParameters parameters;
    private CompressionContext context;
    
    public ZstdCompressor()
    {
      parameters = CompressionParameters.compute(CompressionParameters.DEFAULT_COMPRESSION_LEVEL, MAX_BLOCK_SIZE);
      context = new CompressionContext(parameters, 0, MAX_BLOCK_SIZE);
    }

    
    public int maxCompressedLength(int uncompressedSize)
    {
        int result = uncompressedSize + (uncompressedSize >>> 8);

        if (uncompressedSize < MAX_BLOCK_SIZE) {
            result += (MAX_BLOCK_SIZE - uncompressedSize) >>> 11;
        }

        return result;
    }

    
    public int compress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset, int maxOutputLength)
    {
        verifyRange(input, inputOffset, inputLength);
        verifyRange(output, outputOffset, maxOutputLength);

        long inputAddress = 0 + inputOffset;
        long outputAddress = 0 + outputOffset;
        
        return ZstdFrameCompressor.compress(input, inputAddress, inputAddress + inputLength, output, outputAddress, outputAddress + maxOutputLength, CompressionParameters.DEFAULT_COMPRESSION_LEVEL + 1, parameters, context);

        //return ZstdFrameCompressor.compress(input, inputAddress, inputAddress + inputLength, output, outputAddress, outputAddress + maxOutputLength, CompressionParameters.DEFAULT_COMPRESSION_LEVEL);
    }

    
    public void compress(ByteBuffer inputBuffer, ByteBuffer outputBuffer)
    {
      
    }

    private static void verifyRange(byte[] data, int offset, int length)
    {
        requireNonNull(data, "data is null");
        if (offset < 0 || length < 0 || offset + length > data.length) {
            throw new IllegalArgumentException(format("Invalid offset or length (%s, %s) in array of length %s", offset, length, data.length));
        }
    }
}
