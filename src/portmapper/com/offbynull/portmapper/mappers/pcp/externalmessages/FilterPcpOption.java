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

import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;

import org.apache.commons.lang3.Validate;
import org.vash.vate.compatibility.VTObjects;

/**
 * Represents a FILTER PCP option. From the RFC:
 * <pre>
 *    The FILTER option is formatted as follows:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      | Option Code=3 |  Reserved     |   Option Length=20            |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |    Reserved   | Prefix Length |      Remote Peer Port         |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |         Remote Peer IP remotePeerIpAddress (128 bits)         |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                       Figure 15: FILTER Option Layout
 * 
 *    These fields are described below:
 * 
 *    Reserved:  8 reserved bits, MUST be sent as 0 and MUST be ignored
 *       when received.
 * 
 *    Prefix Length:  indicates how many bits of the IPv4 or IPv6 remotePeerIpAddress
 *       are relevant for this filter.  The value 0 indicates "no filter",
 *       and will remove all previous filters.  See below for detail.
 * 
 *    Remote Peer Port:  the port number of the remote peer.  The value 0
 *       indicates "all ports".
 * 
 *    Remote Peer IP remotePeerIpAddress:  The IP remotePeerIpAddress of the remote peer.
 * 
 *       Option Name: FILTER
 *       Number: 3
 *       Purpose: specifies a filter for incoming packets
 *       Valid for Opcodes: MAP
 *       Length: 20 octets
 *       May appear in: request.  May appear in response only if it
 *       appeared in the associated request.
 *       Maximum occurrences: as many as fit within maximum PCP message
 *       size
 * </pre>
 * @author Kasra Faghihi
 */
public final class FilterPcpOption extends PcpOption {
    private static final int OP_CODE = 3;
    private static final int DATA_LENGTH = 20;
    
    private int prefixLength;
    private int remotePeerPort;
    private InetAddress remotePeerIpAddress;
    
    /**
     * Constructs a {@link FilterPcpOption} by parsing a buffer.
     * @param buffer buffer containing PCP option data
     * @param offset offset in {@code buffer} where the PCP option starts
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} is malformed (doesn't contain enough bytes
     * / length is not a multiple of 4 (not enough padding) / length exceeds 65535 / data does not contain enough bytes / code is not 3 /
     * prefix length is more than 128)
     */
    public FilterPcpOption(byte[] buffer, int offset) {
        super(buffer, offset);
        
        Validate.isTrue(super.getCode() == OP_CODE);
        Validate.isTrue(super.getDataLength() == DATA_LENGTH);
        
        offset += HEADER_LENGTH;
        
        offset++; // reserved
        
        prefixLength = buffer[offset];
        offset++;
        Validate.inclusiveBetween(0, 128, prefixLength); // 0 indicates 'no filter'
        
        remotePeerPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;
        Validate.inclusiveBetween(0, 65535, remotePeerPort); // 0 indicates 'all ports', should never trigger
        
        remotePeerIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
    }
    
    /**
     * Constructs a {@link FilterPcpOption}.
     * @param prefixLength prefix length ({@code 0} = no filter}
     * @param remotePeerPort remote peer port ({@code 0} = all ports)
     * @param remotePeerIpAddress remote peer IP remotePeerIpAddress
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code prefixLength < 0 || > 128}, or if {@code remotePeerPort < 0 || > 65535} 
     */
    public FilterPcpOption(int prefixLength, int remotePeerPort, InetAddress remotePeerIpAddress) {
        super(OP_CODE, DATA_LENGTH);

        Validate.inclusiveBetween(0, 128, prefixLength); // 0 indicates 'no filter'
        Validate.inclusiveBetween(0, 65535, remotePeerPort); // 0 indicates 'all ports'
        Validate.notNull(remotePeerIpAddress);
        
        this.prefixLength = prefixLength;
        this.remotePeerPort = remotePeerPort;
        this.remotePeerIpAddress = remotePeerIpAddress;
    }

    /**
     * Get the prefix length.
     * @return prefix length
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * Get the remote peer port.
     * @return remote peer port
     */
    public int getRemotePeerPort() {
        return remotePeerPort;
    }

    /**
     * Get the remote IP address.
     * @return remote IP address
     */
    public InetAddress getRemotePeerIpAddress() {
        return remotePeerIpAddress;
    }

    
    public byte[] getData() {
        byte[] data = new byte[DATA_LENGTH];

        // write reserved
        data[0] = 0; // not required but leave it in anwyays
        // write prefix len
        data[1] = (byte) prefixLength;
        // write port
        InternalUtils.shortToBytes(data, 2, (short) remotePeerPort);
        // write ip
        byte[] ipv6AsBytes = NetworkUtils.convertAddressToIpv6Bytes(remotePeerIpAddress);
        Validate.validState(ipv6AsBytes.length == 16); // sanity check, should never throw exception
        System.arraycopy(ipv6AsBytes, 0, data, 4, ipv6AsBytes.length);
        
        return data;
    }

    
    public String toString() {
        return "FilterPcpOption{super=" + super.toString() + "prefixLength=" + prefixLength + ", remotePeerPort=" + remotePeerPort
                + ", remotePeerIpAddress=" + remotePeerIpAddress + '}';
    }

    
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + this.prefixLength;
        hash = 97 * hash + this.remotePeerPort;
        hash = 97 * hash + VTObjects.hashCode(this.remotePeerIpAddress);
        return hash;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilterPcpOption other = (FilterPcpOption) obj;
        if (this.prefixLength != other.prefixLength) {
            return false;
        }
        if (this.remotePeerPort != other.remotePeerPort) {
            return false;
        }
        if (!VTObjects.equals(this.remotePeerIpAddress, other.remotePeerIpAddress)) {
            return false;
        }
        return true;
    }
}
