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
package com.offbynull.portmappervt.mappers.upnpigd;

import java.net.InetAddress;

import org.apache.commons.lang3vt.Validate;

import com.offbynull.portmappervt.mapper.MappedPort;
import com.offbynull.portmappervt.mapper.PortType;

final class FirewallMappedPort implements MappedPort {
    
    private String key;
    private int internalPort;
    private int externalPort;
    private PortType portType;
    private long lifetime;

    FirewallMappedPort(String key, int internalPort, int externalPort, PortType portType, long duration) {
        Validate.notNull(key);
        Validate.inclusiveBetween(1, 65535, internalPort);
        Validate.inclusiveBetween(1, 65535, externalPort);
        Validate.notNull(portType);
        Validate.inclusiveBetween(0L, Long.MAX_VALUE, duration);
        this.key = key;
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        this.portType = portType;
        this.lifetime = duration;
    }

    String getKey() {
        return key;
    }

    
    public int getInternalPort() {
        return internalPort;
    }

    
    public int getExternalPort() {
        return externalPort;
    }

    
    public InetAddress getExternalAddress() {
        return null;
    }

    
    public PortType getPortType() {
        return portType;
    }

    
    public long getLifetime() {
        return lifetime;
    }

    
    public String toString() {
        return "FirewallMappedPort{" + "key=" + key + ", internalPort=" + internalPort + ", externalPort=" + externalPort
                + ", portType=" + portType + ", lifetime=" + lifetime + '}';
    }
}
