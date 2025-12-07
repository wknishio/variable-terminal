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
package vate.com.offbynull.portmapper.mappers.pcp;

import java.net.InetAddress;
import java.util.Arrays;

import org.vash.vate.compatibility.VTArrays;

import vate.com.offbynull.portmapper.mapper.MappedPort;
import vate.com.offbynull.portmapper.mapper.PortType;
import vate.org.apache.commons.lang3.Validate;

final class PcpMappedPort implements MappedPort {
    
    private byte[] nonce;
    private int internalPort;
    private int externalPort;
    private InetAddress externalAddress;
    private PortType portType;
    private long lifetime;

    PcpMappedPort(byte[] nonce, int internalPort, int externalPort, InetAddress externalAddress, PortType portType, long duration) {
        Validate.notNull(nonce);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(externalAddress);
        Validate.notNull(portType);
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, duration);
        Validate.isTrue(nonce.length == 12);
        this.nonce = VTArrays.copyOf(nonce, nonce.length);
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.externalAddress = externalAddress;
        this.portType = portType;
        this.lifetime = duration;
    }

    byte[] getNonce() {
        return VTArrays.copyOf(nonce, nonce.length);
    }

    
    public int getInternalPort() {
        return internalPort;
    }

    
    public int getExternalPort() {
        return externalPort;
    }

    
    public InetAddress getExternalAddress() {
        return externalAddress;
    }

    
    public PortType getPortType() {
        return portType;
    }

    
    public long getLifetime() {
        return lifetime;
    }

    
    public String toString() {
        return "PcpMappedPort{" + "nonce=" + Arrays.toString(nonce) + ", internalPort=" + internalPort + ", externalPort=" + externalPort
                + ", externalAddress=" + externalAddress + ", portType=" + portType + ", lifetime=" + lifetime + '}';
    }
    
}
