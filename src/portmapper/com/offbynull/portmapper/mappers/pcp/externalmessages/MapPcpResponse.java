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
import java.nio.BufferUnderflowException; // NOPMD Javadoc not recognized (fixed in latest PMD but maven plugin has to catch up)
import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.vash.vate.compatibility.VTArrays;
import org.vash.vate.compatibility.VTObjects;

/**
 * Represents a MAP PCP response. From the RFC:
 * <pre>
 *    The following diagram shows the format of Opcode-specific information
 *    in a response packet for the MAP Opcode:
 * 
 *       0                   1                   2                   3
 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |                 Mapping Nonce (96 bits)                       |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |   Protocol    |          Reserved (24 bits)                   |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |        Internal Port          |    Assigned External Port     |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *      |                                                               |
 *      |            Assigned External IP Address (128 bits)            |
 *      |                                                               |
 *      |                                                               |
 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *                       Figure 10: MAP Opcode Response
 * 
 *    These fields are described below:
 * 
 *    Lifetime (in common header):  On an error response, this indicates
 *       how long clients should assume they'll get the same error response
 *       from the PCP server if they repeat the same request.  On a success
 *       response, this indicates the lifetime for this mapping, in
 *       seconds.
 * 
 *    Mapping Nonce:  Copied from the request.
 * 
 *    Protocol:  Copied from the request.
 * 
 *    Reserved:  24 reserved bits, MUST be sent as 0 and MUST be ignored
 *       when received.
 * 
 *    Internal Port:  Copied from the request.
 * 
 *    Assigned External Port:  On a success response, this is the assigned
 *       external port for the mapping.  On an error response, the
 *       suggested external port is copied from the request.
 * 
 *    Assigned External IP Address:  On a success response, this is the
 *       assigned external IPv4 or IPv6 address for the mapping.  An IPv4
 *       address is encoded using IPv4-mapped IPv6 address.  On an error
 *       response, the suggested external IP address is copied from the
 *       request.
 * </pre>
 * @author Kasra Faghihi
 */
public final class MapPcpResponse extends PcpResponse {
    private static final int OPCODE = 1;
    private static final int DATA_LENGTH = 36;
    private static final int NONCE_LENGTH = 12;

    private byte[] mappingNonce;
    private int protocol;
    private int internalPort;
    private int assignedExternalPort;
    private InetAddress assignedExternalIpAddress;

    /**
     * Constructs a {@link MapPcpResponse} object.
     * @param mappingNonce random value used to map requests to responses
     * @param protocol IANA protocol number
     * @param internalPort internal port
     * @param assignedExternalPort assigned external port
     * @param assignedExternalIpAddress assigned external IP address
     * @param lifetime lifetime in seconds
     * @param epochTime server's epoch time in seconds
     * @param resultCode result code
     * @param options PCP options to use
     * @throws NullPointerException if any argument is {@code null} or contains {@code null}
     * @throws IllegalArgumentException if {@code 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 0 > protocol > 255
     * || 0 > internalPort > 65535 || (resultCode == 0 ? 1 > assignedExternalPort > 65535 : 0 > assignedExternalPort > 65535)}
     */
    public MapPcpResponse(byte[] mappingNonce, int protocol, int internalPort, int assignedExternalPort,
            InetAddress assignedExternalIpAddress, int resultCode, long lifetime, long epochTime, PcpOption ... options) {
        super(OPCODE, resultCode, lifetime, epochTime, DATA_LENGTH, options);
        
        Validate.notNull(mappingNonce);
        Validate.notNull(assignedExternalIpAddress);

        this.mappingNonce = VTArrays.copyOf(mappingNonce, mappingNonce.length);
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.assignedExternalPort = assignedExternalPort;
        this.assignedExternalIpAddress = assignedExternalIpAddress; // for any ipv4 must be ::ffff:0:0, for any ipv6 must be ::
        
        validateState();
    }

    /**
     * Constructs a {@link MapPcpResponse} object by parsing a buffer.
     * @param buffer buffer containing PCP response data
     * @throws NullPointerException if any argument is {@code null}
     * @throws BufferUnderflowException if not enough data is available in {@code buffer}
     * @throws IllegalArgumentException if there's not enough or too much data remaining in the buffer, or if the version doesn't match the
     * expected version (must always be {@code 2}), or if the r-flag isn't set, or if there's an unsuccessful/unrecognized result code,
     * or if the op code doesn't match the MAP opcode, or if the response has a {@code 0} for its {@code internalPort} or
     * {@code assignedExternalPort} field, or if there were problems parsing options
     * @throws IllegalArgumentException if any numeric argument is negative, or if {@code buffer} isn't the right size (max of 1100 bytes)
     * or is malformed ({@code r-flag != 1 || op != 1 || 0L > lifetime > 0xFFFFFFFFL || mappingNonce.length != 12 || 0 > protocol > 255
     * || 0 > internalPort > 65535  || (resultCode == 0 && lifetime != 0 ? 1 > assignedExternalPort > 65535 : 0 > assignedExternalPort >
     * 65535)}) or contains an unparseable options region.
     */
    public MapPcpResponse(byte[] buffer) {
        super(buffer, DATA_LENGTH);
        
        Validate.isTrue(super.getOp() == OPCODE);

        int offset = HEADER_LENGTH;
        
        mappingNonce = new byte[NONCE_LENGTH];
        System.arraycopy(buffer, offset, mappingNonce, 0, mappingNonce.length);
        offset += mappingNonce.length;

        protocol = buffer[offset] & 0xFF;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        internalPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;
        
        assignedExternalPort = InternalUtils.bytesToShort(buffer, offset) & 0xFFFF;
        offset += 2;
        
        assignedExternalIpAddress = NetworkUtils.convertBytesToAddress(buffer, offset, 16);
        offset += 16;
        
        validateState();
    }
    
    private void validateState() {
        Validate.notNull(mappingNonce);
        Validate.isTrue(mappingNonce.length == NONCE_LENGTH);
        Validate.inclusiveBetween(0, 255, protocol); // copied from the request, see javadoc for request to see what 0 means
        Validate.inclusiveBetween(0, 65535, internalPort); // copied from the request, see javadoc for request to see what 0 means...
                                                           // 0 is valid in certain cases, but those cases can't be checked here.
        if (getResultCode() == 0 && getLifetime() != 0L) { // lifetime of 0 meeans delete
            Validate.inclusiveBetween(1, 65535, assignedExternalPort); // on success, this is the assigned external port for the mapping
                                                                       // ... which must be between 1 and 65535 (unless its a delete)
        } else {
            Validate.inclusiveBetween(0, 65535, assignedExternalPort); // on error, 'suggested external port' copied from request (can be 0)
        }

        Validate.notNull(assignedExternalIpAddress);
    }

    
    public byte[] getData() {
        byte[] data = new byte[DATA_LENGTH];
        
        int offset = 0;
        
        System.arraycopy(mappingNonce, 0, data, offset, mappingNonce.length);
        offset += mappingNonce.length;
        
        data[offset] = (byte) protocol;
        offset++;
        
        offset += 3; // 3 reserved bytes
        
        InternalUtils.shortToBytes(data, offset, (short) internalPort);
        offset += 2;
        
        InternalUtils.shortToBytes(data, offset, (short) assignedExternalPort);
        offset += 2;

        byte[] ipv6Array = NetworkUtils.convertAddressToIpv6Bytes(assignedExternalIpAddress);
        System.arraycopy(ipv6Array, 0, data, offset, ipv6Array.length);
        offset += ipv6Array.length;
        
        return data;
    }

    /**
     * Get nonce.
     * @return nonce
     */
    public byte[] getMappingNonce() {
        return VTArrays.copyOf(mappingNonce, mappingNonce.length);
    }

    /**
     * Get IANA protocol number.
     * @return IANA protocol number
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Get internal port number.
     * @return internal port number
     */
    public int getInternalPort() {
        return internalPort;
    }

    /**
     * Get assigned external port number.
     * @return assigned external port number
     */
    public int getAssignedExternalPort() {
        return assignedExternalPort;
    }

    /**
     * Get assigned external IP address.
     * @return assigned external IP address
     */
    public InetAddress getAssignedExternalIpAddress() {
        return assignedExternalIpAddress;
    }

    
    public String toString() {
        return "MapPcpResponse{super=" + super.toString() + "mappingNonce=" + Arrays.toString(mappingNonce) + ", protocol=" + protocol
                + ", internalPort=" + internalPort + ", assignedExternalPort=" + assignedExternalPort + ", assignedExternalIpAddress="
                + assignedExternalIpAddress + '}';
    }

    
    public int hashCode() {
        int hash = super.hashCode();
        hash = 79 * hash + Arrays.hashCode(this.mappingNonce);
        hash = 79 * hash + this.protocol;
        hash = 79 * hash + this.internalPort;
        hash = 79 * hash + this.assignedExternalPort;
        hash = 79 * hash + VTObjects.hashCode(this.assignedExternalIpAddress);
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
        final MapPcpResponse other = (MapPcpResponse) obj;
        if (this.protocol != other.protocol) {
            return false;
        }
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.assignedExternalPort != other.assignedExternalPort) {
            return false;
        }
        if (!Arrays.equals(this.mappingNonce, other.mappingNonce)) {
            return false;
        }
        if (!VTObjects.equals(this.assignedExternalIpAddress, other.assignedExternalIpAddress)) {
            return false;
        }
        return true;
    }

}
