/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.offbynull.portmapper.mappers.pcp.externalmessages;

import org.apache.commons.lang3.Validate;

/**
 * Represents a PCP option. Provides PCP option header parsing/construction functionality. From the RFC:
 * <pre>
 * 
 *    Options use the following Type-Length-Value format:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |  Option Code  |  Reserved     |       Option Length           |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      :                       (optional) Data                         :
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                          Figure 4: Options Header
 * 
 *    The description of the fields is as follows:
 * 
 *    Option Code:  8 bits.  Its most significant bit indicates if this
 *       option is mandatory (0) or optional (1) to process.
 * 
 *    Reserved:  8 bits.  MUST be set to 0 on transmission and MUST be
 *       ignored on reception.
 * 
 *    Option Length:  16 bits.  Indicates the length of the enclosed data,
 *       in octets.  Options with length of 0 are allowed.  Options that
 *       are not a multiple of 4 octets long are followed by one, two, or
 *       three 0 octets to pad their effective length in the packet to be a
 *       multiple of 4 octets.  The Option Length reflects the semantic
 *       length of the option, not including any padding octets.
 * 
 *    Data:  Option data.
 * </pre>
 * @author Kasra Faghihi
 */
public abstract class PcpOption {
    /**
     * PCP option header length.
     */
    protected static final int HEADER_LENGTH = 4;
    /**
     * Maximum amount of padding on a PCP option block. Total size of PCP option block must be multiple of 4.
     */
    protected static final int DATA_PADDING_LIMIT = 4;

    private int code;
    private int dataLength;

    /**
     * Constructs a {@link PcpOption} object by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @param offset offset in {@code buffer} where the PCP option starts
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} is malformed (doesn't contain enough bytes
     * / length is not a multiple of 4 (not enough padding) / data exceeds 65535 bytes)
     */
    public PcpOption(byte[] buffer, int offset) {
        Validate.notNull(buffer);
        Validate.isTrue(offset >= 0);
        final int remainingLength = buffer.length - offset;
        Validate.isTrue(remainingLength >= HEADER_LENGTH);
        
        code = buffer[offset] & 0xFF;
        offset++;
        
        offset++; // skip over reserved
        
        // We check data length to see if its <= 65535 because that's the maximum the length field can be, but a PCP packet can only be
        // 1100 bytes. We're only dealing with the option portion here so we don't care if it exceeds 1100 bytes.
        int dataLength = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        Validate.isTrue(dataLength <= 65535);
        this.dataLength = dataLength;
        offset += 2;

        int expectedPadding = dataLength % DATA_PADDING_LIMIT;
        
        int expectedLength = HEADER_LENGTH + dataLength + expectedPadding;
        Validate.isTrue(remainingLength >= expectedLength);
    }

    /**
     * Constructs a {@link PcpOption} object.
     * @param code option code
     * @param dataLength length of option data (do not include padding)
     * @throws IllegalArgumentException if {@code code < 0 || code > 255}, or if {@code dataLength > 65535}
     */
    public PcpOption(int code, int dataLength) {
        Validate.inclusiveBetween(0, 255, code);
        // We check data length to see if its <= 65535 because that's the maximum the length field can be, but a PCP packet can only be
        // 1100 bytes. We're only dealing with the option portion here so we don't care if it exceeds 1100 bytes.
        Validate.isTrue(dataLength <= 65535);
        
        this.code = code;
        this.dataLength = dataLength;
    }

    /**
     * Get PCP option code.
     * @return PCP option code
     */
    public final int getCode() {
        return code;
    }

    /**
     * Get PCP option data length. Equivalent to {@code getData().length}.
     * @return PCP option data length
     */
    public final int getDataLength() {
        return dataLength;
    }

    /**
     * Get PCP option data.
     * @return PCP option data
     */
    public abstract byte[] getData();

    /**
     * Get the number of bytes this PCP option takes up when dumped out (length of buffer returned by {@link #dump() }).
     * @return length of buffer containing PCP option
     */
    public final int getBufferLength() {
        int padding = dataLength % DATA_PADDING_LIMIT; // padding required on data to get the length to a multiple of 4
        int length = HEADER_LENGTH + dataLength + padding;
        return length;
    }
    
    /**
     * Dump this PCP option in to a buffer.
     * @return buffer containing PCP option
     */
    public final byte[] dump() {
        byte[] data = getData();
        
        int bufferLength = getBufferLength();        
        byte[] buffer = new byte[bufferLength];
        
        buffer[0] = (byte) code; // pcp code
        buffer[1] = (byte) 0; // reserved
        InternalUtils.shortToBytes(buffer, 2, (short) data.length); // length of pcp data
        System.arraycopy(data, 0, buffer, 4, data.length); // pcp data

        return buffer;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    
    public String toString() {
        return "PcpOption{" + "code=" + code + ", dataLength=" + dataLength + '}';
    }

    
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.code;
        hash = 89 * hash + this.dataLength;
        return hash;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PcpOption other = (PcpOption) obj;
        if (this.code != other.code) {
            return false;
        }
        if (this.dataLength != other.dataLength) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
