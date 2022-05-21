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
package com.offbynull.portmapper.mappers.upnpigd.externalmessages;

import com.offbynull.portmapper.mapper.PortType;
import com.offbynull.portmapper.helpers.NetworkUtils;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.Validate;

/**
 * Represents a UPnP AddPinhole request.
 * <p>
 * For a more thorough description of arguments, see docs at http://upnp.org/specs/gw/igd1 and http://upnp.org/specs/gw/igd2.
 * @author Kasra Faghihi
 */
public final class AddPinholeUpnpIgdRequest extends UpnpIgdSoapRequest {
    
    /**
     * Constructs a {@link AddPinholeUpnpIgdRequest} object.
     * @param host device host
     * @param controlLocation control location
     * @param serviceType service type
     * @param remoteHost remote address ({@code null} means wildcard) -- <b>should</b> be IPv6
     * @param remotePort external port ({@code 0} means wildcard)
     * @param internalClient internal address -- <b>should</b> be IPv4
     * @param internalPort internal port ({@code 0} means wildcard)
     * @param protocol protocol to target for port mapping (TCP/UDP) -- ({@code null} means wildcard) 
     * @param leaseDuration lease duration
     * @throws NullPointerException if any argument other than {@code remoteHost} is {@code null}
     * @throws IllegalArgumentException if {@code 0 > externalPort > 65535 || 0 > internalPort > 65535 || 1L > leaseDuration > 0xFFFFFFFFL}
     */
    public AddPinholeUpnpIgdRequest(String host, String controlLocation, String serviceType,
            InetAddress remoteHost,
            int remotePort,
            InetAddress internalClient,
            int internalPort,
            PortType protocol,
            long leaseDuration) {
        super(host, controlLocation, serviceType, "AddPinhole",
                generateArguments(remoteHost, remotePort, protocol, internalPort, internalClient, leaseDuration));
    }
    
    private static Map<String, String> generateArguments(
            InetAddress remoteHost, // must be IPv6 address (don't bother checking) -- null means wildcard ("")
            int externalPort, // 0 to 65535 -- 0 means wildcard 
            PortType protocol, // must be either "TCP" or "UDP" -- null means wildcard
            int internalPort, //  0 to 65535 -- 0 means wildcard
            InetAddress internalClient, // must be IPv6 address of interface accessing server (don't bother checking)
            long leaseDuration) { // 1 to max 0xFFFFFFFF
        
        Map<String, String> ret = new LinkedHashMap<String, String>();
        
        if (remoteHost == null) {
            ret.put("RemoteHost", "");
        } else {
            ret.put("RemoteHost", NetworkUtils.toIpv6AddressString(remoteHost));
        }
        
        Validate.inclusiveBetween(0, 65535, externalPort);
        ret.put("RemotePort", "" + externalPort);
        
        if (internalClient == null) {
            ret.put("InternalClient", "");
        } else {
            ret.put("InternalClient", NetworkUtils.toIpv6AddressString(internalClient));
        }

        Validate.inclusiveBetween(0, 65535, internalPort);
        ret.put("InternalPort", "" + internalPort);
        
        ret.put("Protocol", (protocol == null ? "65535" : "" + protocol.getProtocolNumber())); // 65535 is wildcard
        
        Validate.inclusiveBetween(0L, 0xFFFFFFFFL, leaseDuration);
        ret.put("LeaseTime", "" + leaseDuration);
        
        return ret;
    }

    
    public String toString() {
        return "AddPinholeUpnpIgdRequest{super=" + super.toString() + '}';
    }
    
    // NO NON-STATIC FIELDS, so parent's equals/hashCode should work
}
