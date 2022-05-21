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

import io.airlift.compress.Decompressor;
import io.airlift.compress.MalformedInputException;

public class LzoDecompressor
        implements Decompressor
{
    
    public int decompress(byte[] input, int inputOffset, int inputLength, byte[] output, int outputOffset, int maxOutputLength)
            throws MalformedInputException
    {
        long inputAddress = 0 + inputOffset;
        long inputLimit = inputAddress + inputLength;
        long outputAddress = 0 + outputOffset;
        long outputLimit = outputAddress + maxOutputLength;

        return LzoRawDecompressor.decompress(input, inputAddress, inputLimit, output, outputAddress, outputLimit);
    }

}
