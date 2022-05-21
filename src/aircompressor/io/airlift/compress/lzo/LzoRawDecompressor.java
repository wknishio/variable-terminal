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

import io.airlift.compress.MalformedInputException;
import io.airlift.compress.UnsafeUtils;
import static io.airlift.compress.lzo.LzoConstants.SIZE_OF_INT;
import static io.airlift.compress.lzo.LzoConstants.SIZE_OF_LONG;
import static io.airlift.compress.lzo.LzoConstants.SIZE_OF_SHORT;
import static java.lang.Integer.toBinaryString;

public final class LzoRawDecompressor
{
    private static final int[] DEC_32_TABLE = {4, 1, 2, 1, 4, 4, 4, 4};
    private static final int[] DEC_64_TABLE = {0, 0, 0, -1, 0, 1, 2, 3};

    private LzoRawDecompressor() {}

    @SuppressWarnings("InnerAssignment")
    public static int decompress(
            final byte[] inputBase,
            final long inputAddress,
            final long inputLimit,
            final byte[] outputBase,
            final long outputAddress,
            final long outputLimit)
            throws MalformedInputException
    {
        // nothing compresses to nothing
        if (inputAddress == inputLimit) {
            return 0;
        }

        // maximum offset in buffers to which it's safe to write long-at-a-time
        final long fastOutputLimit = outputLimit - SIZE_OF_LONG;

        // LZO can concat multiple blocks together so, decode until all input data is consumed
        long input = inputAddress;
        long output = outputAddress;
        while (input < inputLimit) {
            boolean firstCommand = true;
            int lastLiteralLength = 0;
            while (true) {
                if (input >= inputLimit) {
                    throw new MalformedInputException(input - inputAddress);
                }
                int command = UnsafeUtils.getByte(inputBase, input++) & 0xFF;
                // Commands are described using a bit pattern notation:
                // 0: bit is not set
                // 1: bit is set
                // L: part of literal length
                // H: high bits of match offset position
                // D: low bits of match offset position
                // M: part of match length
                // ?: see documentation in command decoder

                int matchLength;
                int matchOffset;
                int literalLength;
                if ((command & 0xF0) == 0) {
                    if (lastLiteralLength == 0) {
                        // 0b0000_LLLL (0bLLLL_LLLL)*
                        // copy 4 or more literals only

                        // copy length :: fixed
                        //   0
                        matchOffset = 0;

                        // copy offset :: fixed
                        //   0
                        matchLength = 0;

                        // literal length - 3 :: variable bits :: valid range [4..]
                        //   3 + variableLength(command bits [0..3], 4)
                        literalLength = command & 0xF;
                        if (literalLength == 0) {
                            literalLength = 0xF;

                            int nextByte = 0;
                            while (input < inputLimit && (nextByte = UnsafeUtils.getByte(inputBase, input++) & 0xFF) == 0) {
                                literalLength += 0xFF;
                            }
                            literalLength += nextByte;
                        }
                        literalLength += 3;
                    }
                    else if (lastLiteralLength <= 3) {
                        // 0b0000_DDLL 0bHHHH_HHHH
                        // copy of a 2-byte block from the dictionary within a 1kB distance

                        // copy length: fixed
                        //   2
                        matchLength = 2;

                        // copy offset :: valid range [1..1024]
                        //   DD from command [2..3]
                        //   HH from trailer [0..7]
                        // offset = (HH << 2) + DD + 1
                        if (input >= inputLimit) {
                            throw new MalformedInputException(input - inputAddress);
                        }
                        matchOffset = (command & 0xC) >>> 2;
                        matchOffset |= (UnsafeUtils.getByte(inputBase, input++) & 0xFF) << 2;

                        // literal length :: 2 bits :: valid range [0..3]
                        //   [0..1] from command [0..1]
                        literalLength = (command & 0x3);
                    }
                    else {
                        // 0b0000_DDLL 0bHHHH_HHHH

                        // copy length :: fixed
                        //   3
                        matchLength = 3;

                        // copy offset :: 10 bits :: valid range [2049..3072]
                        //   DD from command [2..3]
                        //   HH from trailer [0..7]
                        // offset = (H << 2) + D + 2049
                        if (input >= inputLimit) {
                            throw new MalformedInputException(input - inputAddress);
                        }
                        matchOffset = (command & 0xC) >>> 2;
                        matchOffset |= (UnsafeUtils.getByte(inputBase, input++) & 0xFF) << 2;
                        matchOffset |= 0x800;

                        // literal length :: 2 bits :: valid range [0..3]
                        //   [0..1] from command [0..1]
                        literalLength = (command & 0x3);
                    }
                }
                else if (firstCommand) {
                    // first command has special handling when high nibble is set
                    matchLength = 0;
                    matchOffset = 0;
                    literalLength = command - 17;
                }
                else if ((command & 0xF0) == 0x10) {
                    // 0b0001_HMMM (0bMMMM_MMMM)* 0bDDDD_DDDD_DDDD_DDLL

                    // copy length - 2 :: variable bits :: valid range [3..]
                    //   2 + variableLength(command bits [0..2], 3)
                    matchLength = command & 0x7;
                    if (matchLength == 0) {
                        matchLength = 0x7;

                        int nextByte = 0;
                        while (input < inputLimit && (nextByte = UnsafeUtils.getByte(inputBase, input++) & 0xFF) == 0) {
                            matchLength += 0xFF;
                        }
                        matchLength += nextByte;
                    }
                    matchLength += 2;

                    // read trailer
                    if (input + SIZE_OF_SHORT > inputLimit) {
                        throw new MalformedInputException(input - inputAddress);
                    }
                    int trailer = UnsafeUtils.getShort(inputBase, input) & 0xFFFF;
                    input += SIZE_OF_SHORT;

                    // copy offset :: 16 bits :: valid range [16383..49151]
                    //   [0..13] from trailer [2..15]
                    //   [14] if command bit [3] set
                    //   plus fixed offset 0b11_1111_1111_1111
                    matchOffset = (command & 0x8) << 11;
                    matchOffset += trailer >> 2;
                    if (matchOffset == 0) {
                        // match offset of zero, means that this is the last command in the sequence
                        break;
                    }
                    matchOffset += 0x3FFF;

                    // literal length :: 2 bits :: valid range [0..3]
                    //   [0..1] from trailer [0..1]
                    literalLength = trailer & 0x3;
                }
                else if ((command & 0xE0) == 0x20) {
                    // command in [32, 63]
                    // 0b001M_MMMM (0bMMMM_MMMM)* 0bDDDD_DDDD_DDDD_DDLL

                    // copy length - 2 :: variable bits :: valid range [3..]
                    //   2 + variableLength(command bits [0..4], 5)
                    matchLength = command & 0x1F;
                    if (matchLength == 0) {
                        matchLength = 0x1F;

                        int nextByte = 0;
                        while (input < inputLimit && (nextByte = UnsafeUtils.getByte(inputBase, input++) & 0xFF) == 0) {
                            matchLength += 0xFF;
                        }
                        matchLength += nextByte;
                    }
                    matchLength += 2;

                    // read trailer
                    if (input + SIZE_OF_SHORT > inputLimit) {
                        throw new MalformedInputException(input - inputAddress);
                    }
                    int trailer = UnsafeUtils.getShort(inputBase, input) & 0xFFFF;
                    input += SIZE_OF_SHORT;

                    // copy offset :: 14 bits :: valid range [0..16383]
                    //  [0..13] from trailer [2..15]
                    matchOffset = trailer >>> 2;

                    // literal length :: 2 bits :: valid range [0..3]
                    //   [0..1] from trailer [0..1]
                    literalLength = trailer & 0x3;
                }
                else if ((command & 0xC0) != 0) {
                    // 0bMMMD_DDLL 0bHHHH_HHHH

                    // copy length - 1 :: 3 bits :: valid range [1..8]
                    //   [0..2] from command [5..7]
                    //   add 1
                    matchLength = (command & 0xE0) >>> 5;
                    matchLength += 1;

                    // copy offset :: 11 bits :: valid range [0..4095]
                    //   [0..2] from command [2..4]
                    //   [3..10] from trailer [0..7]
                    if (input >= inputLimit) {
                        throw new MalformedInputException(input - inputAddress);
                    }
                    matchOffset = (command & 0x1C) >>> 2;
                    matchOffset |= (UnsafeUtils.getByte(inputBase, input++) & 0xFF) << 3;

                    // literal length :: 2 bits :: valid range [0..3]
                    //   [0..1] from command [0..1]
                    literalLength = (command & 0x3);
                }
                else {
                    String binary = toBinary(command);
                    throw new MalformedInputException(input - 1, "Invalid LZO command " + binary);
                }
                firstCommand = false;

                // copy match
                if (matchLength != 0) {
                    // lzo encodes match offset minus one
                    matchOffset++;

                    long matchAddress = output - matchOffset;
                    if (matchAddress < outputAddress || output + matchLength > outputLimit) {
                        throw new MalformedInputException(input - inputAddress);
                    }
                    long matchOutputLimit = output + matchLength;

                    if (output > fastOutputLimit) {
                        // slow match copy
                        while (output < matchOutputLimit) {
                            UnsafeUtils.putByte(outputBase, output++, UnsafeUtils.getByte(outputBase, matchAddress++));
                        }
                    }
                    else {
                        // copy repeated sequence
                        if (matchOffset < SIZE_OF_LONG) {
                            // 8 bytes apart so that we can copy long-at-a-time below
                            int increment32 = DEC_32_TABLE[matchOffset];
                            int decrement64 = DEC_64_TABLE[matchOffset];

                            UnsafeUtils.putByte(outputBase, output, UnsafeUtils.getByte(outputBase, matchAddress));
                            UnsafeUtils.putByte(outputBase, output + 1, UnsafeUtils.getByte(outputBase, matchAddress + 1));
                            UnsafeUtils.putByte(outputBase, output + 2, UnsafeUtils.getByte(outputBase, matchAddress + 2));
                            UnsafeUtils.putByte(outputBase, output + 3, UnsafeUtils.getByte(outputBase, matchAddress + 3));
                            output += SIZE_OF_INT;
                            matchAddress += increment32;

                            UnsafeUtils.putInt(outputBase, output, UnsafeUtils.getInt(outputBase, matchAddress));
                            output += SIZE_OF_INT;
                            matchAddress -= decrement64;
                        }
                        else {
                            UnsafeUtils.putLong(outputBase, output, UnsafeUtils.getLong(outputBase, matchAddress));
                            matchAddress += SIZE_OF_LONG;
                            output += SIZE_OF_LONG;
                        }

                        if (matchOutputLimit >= fastOutputLimit) {
                            if (matchOutputLimit > outputLimit) {
                                throw new MalformedInputException(input - inputAddress);
                            }

                            while (output < fastOutputLimit) {
                                UnsafeUtils.putLong(outputBase, output, UnsafeUtils.getLong(outputBase, matchAddress));
                                matchAddress += SIZE_OF_LONG;
                                output += SIZE_OF_LONG;
                            }

                            while (output < matchOutputLimit) {
                                UnsafeUtils.putByte(outputBase, output++, UnsafeUtils.getByte(outputBase, matchAddress++));
                            }
                        }
                        else {
                            while (output < matchOutputLimit) {
                                UnsafeUtils.putLong(outputBase, output, UnsafeUtils.getLong(outputBase, matchAddress));
                                matchAddress += SIZE_OF_LONG;
                                output += SIZE_OF_LONG;
                            }
                        }
                    }
                    output = matchOutputLimit; // correction in case we over-copied
                }

                // copy literal
                long literalOutputLimit = output + literalLength;
                if (literalOutputLimit > fastOutputLimit || input + literalLength > inputLimit - SIZE_OF_LONG) {
                    if (literalOutputLimit > outputLimit) {
                        throw new MalformedInputException(input - inputAddress);
                    }

                    // slow, precise copy
                    UnsafeUtils.copyMemory(inputBase, input, outputBase, output, literalLength);
                    input += literalLength;
                    output += literalLength;
                }
                else {
                    // fast copy. We may over-copy but there's enough room in input and output to not overrun them
                    do {
                        UnsafeUtils.putLong(outputBase, output, UnsafeUtils.getLong(inputBase, input));
                        input += SIZE_OF_LONG;
                        output += SIZE_OF_LONG;
                    }
                    while (output < literalOutputLimit);
                    input -= (output - literalOutputLimit); // adjust index if we over-copied
                    output = literalOutputLimit;
                }
                lastLiteralLength = literalLength;
            }
        }
        return (int) (output - outputAddress);
    }

    private static String toBinary(int command)
    {
        String binaryString = String.format("%8s", toBinaryString(command)).replace(' ', '0');
        return "0b" + binaryString.substring(0, 4) + "_" + binaryString.substring(4);
    }
}
