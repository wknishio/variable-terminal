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
package com.offbynull.portmapper.mappers.natpmp.externalmessages;

import static com.offbynull.portmapper.mappers.natpmp.externalmessages.InternalUtils.NAT_PMP_VERSION;
import org.apache.commons.lang3.Validate;

/**
 * Abstract NAT-PMP mapping request. From the RFC:
 * <pre>
 * 3.3.  Requesting a Mapping
 * 
 *    To create a mapping, the client sends a UDP packet to port 5351 of
 *    the gateway's internal IP address with the following format:
 * 
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Vers = 0      | OP = x        | Reserved                      |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Internal Port                 | Suggested External Port       |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    | Requested Port Mapping Lifetime in Seconds                    |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * 
 *    Opcodes supported:
 *    1 - Map UDP
 *    2 - Map TCP
 * 
 *    The Reserved field MUST be set to zero on transmission and MUST be
 *    ignored on reception.
 * 
 *    The Ports and Lifetime are transmitted in the traditional network
 *    byte order (i.e., most significant byte first).
 * 
 *    The Internal Port is set to the local port on which the client is
 *    listening.
 * 
 *    If the client would prefer to have a high-numbered "anonymous"
 *    external port assigned, then it should set the Suggested External
 *    Port to zero, which indicates to the gateway that it should allocate
 *    a high-numbered port of its choosing.  If the client would prefer
 *    instead to have the mapped external port be the same as its local
 *    internal port if possible (e.g., a web server listening on port 80
 *    that would ideally like to have external port 80), then it should set
 *    the Suggested External Port to the desired value.  However, the
 *    gateway is not obliged to assign the port suggested, and may choose
 *    not to, either for policy reasons (e.g., port 80 is reserved and
 *    clients may not request it) or because that port has already been
 *    assigned to some other client.  Because of this, some product
 *    developers have questioned the value of having the Suggested External
 *    Port field at all.  The reason is for failure recovery.  Most low-
 *    cost home NAT gateways do not record temporary port mappings in
 *    persistent storage, so if the gateway crashes or is rebooted, all the
 *    mappings are lost.  A renewal packet is formatted identically to an
 *    initial mapping request packet, except that for renewals the client
 *    sets the Suggested External Port field to the port the gateway
 *    actually assigned, rather than the port the client originally wanted.
 * 
 *    When a freshly rebooted NAT gateway receives a renewal packet from a
 *    client, it appears to the gateway just like an ordinary initial
 *    request for a port mapping, except that in this case the Suggested
 *    External Port is likely to be one that the NAT gateway *is* willing
 *    to allocate (it allocated it to this client right before the reboot,
 *    so it should presumably be willing to allocate it again).  This
 *    improves the stability of external ports across NAT gateway restarts.
 * 
 *    The RECOMMENDED Port Mapping Lifetime is 7200 seconds (two hours).
 * 
 * ...
 * 
 * 3.4.  Destroying a Mapping
 * 
 *    A mapping may be destroyed in a variety of ways.  If a client fails
 *    to renew a mapping, then at the time its lifetime expires, the
 *    mapping MUST be automatically deleted.  In the common case where the
 *    gateway device is a combined DHCP server and NAT gateway, when a
 *    client's DHCP address lease expires, the gateway device MAY
 *    automatically delete any mappings belonging to that client.
 *    Otherwise, a new client being assigned the same IP address could
 *    receive unexpected inbound UDP packets or inbound TCP connection
 *    requests that it did not ask for and does not want.
 * 
 *    A client MAY also send an explicit packet to request deletion of a
 *    mapping that is no longer needed.  A client requests explicit
 *    deletion of a mapping by sending a message to the NAT gateway
 *    requesting the mapping, with the Requested Lifetime in Seconds set to
 *    zero.  The Suggested External Port MUST be set to zero by the client
 *    on sending, and MUST be ignored by the gateway on reception.
 * 
 * ...
 * 
 *    A client can request the explicit deletion of all its UDP or TCP
 *    mappings by sending the same deletion request to the NAT gateway with
 *    the external port, internal port, and lifetime set to zero.  A client
 *    MAY choose to do this when it first acquires a new IP address in
 *    order to protect itself from port mappings that were performed by a
 *    previous owner of the IP address.  After receiving such a deletion
 *    request, the gateway MUST delete all its UDP or TCP port mappings
 *    (depending on the opcode).  The gateway responds to such a deletion
 *    request with a response as described above, with the internal port
 *    set to zero.  If the gateway is unable to delete a port mapping, for
 *    example, because the mapping was manually configured by the
 *    administrator, the gateway MUST still delete as many port mappings as
 *    possible, but respond with a non-zero result code.  The exact result
 *    code to return depends on the cause of the failure.  If the gateway
 *    is able to successfully delete all port mappings as requested, it
 *    MUST respond with a result code of zero.
 * </pre>
 * @author Kasra Faghihi
 */
public abstract class MappingNatPmpRequest extends NatPmpRequest {
    private static final int LENGTH = 12;

    private final int internalPort;
    private final int suggestedExternalPort;
    private final long lifetime;
    
    /**
     * Construct a {@link MappingNatPmpRequest} object.
     * @param expectedOp expected op code
     * @param buffer buffer containing NAT-PMP request data
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code buffer} isn't the right size or is malformed ({@code op != expectedOp || version != 0 ||
     * 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 || 0L > lifetime > 0xFFFFFFFFL})
     */
    MappingNatPmpRequest(int expectedOp, byte[] buffer) {
        super(expectedOp);

        Validate.notNull(buffer);
        Validate.isTrue(buffer.length == LENGTH);

        Validate.isTrue(this.getOp() == expectedOp);

        this.internalPort = InternalUtils.bytesToShort(buffer, 4) & 0xFFFF;
        this.suggestedExternalPort = InternalUtils.bytesToShort(buffer, 6) & 0xFFFF;
        this.lifetime = InternalUtils.bytesToInt(buffer, 8) & 0xFFFFFFFFL;

        validateState();
    }

    /**
     * Construct a {@link MappingNatPmpRequest} object.
     * @param op op code
     * @param internalPort internal port
     * @param suggestedExternalPort suggested external port ({@code 0} for no preference)
     * @param lifetime desired lifetime of mapping ({@code 0} to destroy mapping)
     * @throws IllegalArgumentException if {@code 1 > internalPort > 65535 || 0 > suggestedExternalPort > 65535 ||
     * 0L > lifetime > 0xFFFFFFFFL}
     */
    MappingNatPmpRequest(int op, int internalPort, int suggestedExternalPort, long lifetime) {
        super(op);
        this.internalPort = internalPort;
        this.suggestedExternalPort = suggestedExternalPort;
        this.lifetime = lifetime;
        
        validateState();
    }

    private void validateState() {
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(0, 65535, suggestedExternalPort);
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, lifetime);
    }

    
    public final byte[] dump() {
        byte[] data = new byte[LENGTH];

        data[0] = NAT_PMP_VERSION;
        data[1] = (byte) getOp();
        data[2] = 0;
        data[3] = 0;
        InternalUtils.shortToBytes(data, 4, (short) internalPort);
        InternalUtils.shortToBytes(data, 6, (short) suggestedExternalPort);
        InternalUtils.intToBytes(data, 8, (int) lifetime);

        return data;
    }

    /**
     * Get internal port.
     * @return internal port
     */
    public final int getInternalPort() {
        return internalPort;
    }

    /**
     * Get suggested external port.
     * @return suggested external port
     */
    public final int getSuggestedExternalPort() {
        return suggestedExternalPort;
    }

    /**
     * Get lifetime.
     * @return lifetime
     */
    public final long getLifetime() {
        return lifetime;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    
    public String toString() {
        return "MappingNatPmpRequest{super=" + super.toString() + "internalPort=" + internalPort + ", suggestedExternalPort="
                + suggestedExternalPort + ", lifetime=" + lifetime + '}';
    }
    
    
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + this.internalPort;
        hash = 97 * hash + this.suggestedExternalPort;
        hash = 97 * hash + (int) (this.lifetime ^ (this.lifetime >>> 32));
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
        final MappingNatPmpRequest other = (MappingNatPmpRequest) obj;
        if (this.internalPort != other.internalPort) {
            return false;
        }
        if (this.suggestedExternalPort != other.suggestedExternalPort) {
            return false;
        }
        if (this.lifetime != other.lifetime) {
            return false;
        }
        return true;
    }
    // CHECKSTYLE:ON:DesignForExtension
}
