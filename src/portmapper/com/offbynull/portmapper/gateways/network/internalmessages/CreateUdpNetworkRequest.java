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
package com.offbynull.portmapper.gateways.network.internalmessages;

import com.offbynull.portmapper.gateway.Bus;
import java.net.InetAddress;
import org.apache.commons.lang3.Validate;

/**
 * Create a UDP socket. Possible responses are {@link CreateUdpNetworkResponse} and {@link IdentifiableErrorNetworkResponse}).
 * @author Kasra Faghihi
 */
public final class CreateUdpNetworkRequest extends IdentifiableNetworkRequest {
    private Bus responseBus;
    private InetAddress sourceAddress;

    /**
     * Constructs a {@link CreateUdpNetworkRequest} object.
     * @param id id of socket
     * @param responseBus bus to send responses/notifications to for the created socket 
     * @param sourceAddress source address of the socket to be created
     * @throws NullPointerException if any argument is {@code null}
     */
    public CreateUdpNetworkRequest(int id, Bus responseBus, InetAddress sourceAddress) {
        super(id);
        Validate.notNull(responseBus);
        Validate.notNull(sourceAddress);
        this.responseBus = responseBus;
        this.sourceAddress = sourceAddress;
    }

    /**
     * Bus to send responses/notifications to for the created socket.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }

    /**
     * Source address of the socket to be created.
     * @return source address
     */
    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    
    public String toString() {
        return "CreateUdpNetworkRequest{" + "responseBus=" + responseBus + ", sourceAddress=" + sourceAddress + '}';
    }
}
