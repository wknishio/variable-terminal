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
package com.offbynull.portmapper.mappers.upnpigd;

import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.helpers.NetworkUtils;
import com.offbynull.portmapper.mapper.MapperIoUtils.BytesToResponseTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.RequestToBytesTransformer;
import com.offbynull.portmapper.mapper.MapperIoUtils.TcpRequest;
import com.offbynull.portmapper.mapper.MapperIoUtils.UdpRequest;
import static com.offbynull.portmapper.mapper.MapperIoUtils.getLocalIpAddresses;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performBatchedTcpRequests;
import static com.offbynull.portmapper.mapper.MapperIoUtils.performUdpRequests;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.RootUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.RootUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.RootUpnpIgdResponse.ServiceReference;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.IdentifiedService;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdRequest;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdRequest.ProbeDeviceType;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDiscoveryUpnpIgdResponse;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType;
//import static com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.FIREWALL;
//import static com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.OLD_PORT_MAPPER;
//import static com.offbynull.portmapper.mappers.upnpigd.externalmessages.ServiceDescriptionUpnpIgdResponse.ServiceType.NEW_PORT_MAPPER;
import com.offbynull.portmapper.mappers.upnpigd.externalmessages.UpnpIgdHttpRequest;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UPNP-IGD {@link PortMapper} implementation.
 *
 * @author Kasra Faghihi
 */
public abstract class UpnpIgdPortMapper implements PortMapper {
    private static final Logger LOG = LoggerFactory.getLogger(UpnpIgdPortMapper.class);

    private final Bus networkBus;
    private final InetAddress internalAddress;
    private final URL controlUrl;
    private final String serverName;
    private final String serviceType;
    private final Range<Long> externalPortRange;
    private final Range<Long> leaseDurationRange;

    /**
     * Constructs a {@link UpnpIgdPortMapper} object.
     * @param networkBus network bus
     * @param internalAddress source address (address to communicate with gateway from)
     * @param controlUrl control URL
     * @param serverName sever name (may be {@code null})
     * @param serviceType service type
     * @param externalPortRange external port range
     * @param leaseDurationRange lease duration range
     * @throws NullPointerException if any argument other than {@code serverName} is {@code null}
     * @throws IllegalArgumentException if {@code 0L > externalPortRange > 65535L || 0L > leaseDurationRange > 0xFFFFFFFFL}, or if
     * {@code controlUrl} scheme is not {@code "http"}
     * 
     */
    protected UpnpIgdPortMapper(Bus networkBus, InetAddress internalAddress, URL controlUrl, String serverName, String serviceType,
            Range<Long> externalPortRange, Range<Long> leaseDurationRange) {
        Validate.notNull(networkBus);
        Validate.notNull(internalAddress);
        Validate.notNull(controlUrl);
//        Validate.notNull(serverName); // can be null
        Validate.notNull(serviceType);
        Validate.notNull(externalPortRange);
        Validate.notNull(leaseDurationRange);
        Validate.isTrue(leaseDurationRange.getMinimum() >= 0L);
        Validate.isTrue(leaseDurationRange.getMaximum() <= 0xFFFFFFFFL);
        Validate.isTrue(externalPortRange.getMinimum() >= 0L);
        Validate.isTrue(externalPortRange.getMaximum() <= 0xFFFFL);
        Validate.isTrue("http".equalsIgnoreCase(controlUrl.getProtocol()));
        this.networkBus = networkBus;
        this.internalAddress = internalAddress;
        this.controlUrl = controlUrl;
        this.serverName = serverName;
        this.serviceType = serviceType;
        this.externalPortRange = externalPortRange;
        this.leaseDurationRange = leaseDurationRange;
    }

    /**
     * Get network bus.
     * @return network bus
     */
    protected final Bus getNetworkBus() {
        return networkBus;
    }

    /**
     * Get local address used to interface with the UPnP-IGD device.
     * @return local address
     */
    protected final InetAddress getInternalAddress() {
        return internalAddress;
    }

    /**
     * Get control URL.
     * @return control URL
     */
    protected final URL getControlUrl() {
        return controlUrl;
    }

    /**
     * Get server name.
     * @return server name (may be {@code null})
     */
    protected final String getServerName() {
        return serverName;
    }

    /**
     * Get service type.
     * @return service type
     */
    protected final String getServiceType() {
        return serviceType;
    }

    /**
     * Get external port mapping range.
     * @return external port mapping range
     */
    protected final Range<Long> getExternalPortRange() {
        return externalPortRange;
    }

    /**
     * Get lease duration range.
     * @return lease duration range
     */
    protected final Range<Long> getLeaseDurationRange() {
        return leaseDurationRange;
    }

    // CHECKSTYLE:OFF:DesignForExtension
    
    public String toString() {
        return "UpnpIgdPortMapper{" + "internalAddress=" + internalAddress + ", controlUrl=" + controlUrl + ", serverName=" + serverName
                + ", serviceType=" + serviceType + ", externalPortRange=" + externalPortRange + ", leaseDurationRange=" + leaseDurationRange
                + '}';
    }
    // CHECKSTYLE:ON:DesignForExtension

    /**
     * Identify UPnP-IGD devices on all interfaces.
     * @param networkBus network bus
     * @return set of found UPnP-IGD devices
     * @throws NullPointerException if any argument is {@code null}
     * @throws InterruptedException if interrupted
     */
    public static List<UpnpIgdPortMapper> identify(Bus networkBus) throws InterruptedException {
        LOG.debug("Attempting to identify devices");
        String ST = "upnp:rootdevice";
        //String ST = "ssdp:all";
        Validate.notNull(networkBus);

        // Probe for devices -- for each device found, query the device
        Set<InetAddress> sourceAddresses = getLocalIpAddresses(networkBus);
        Collection<UdpRequest> discoveryRequests = new ArrayList<UdpRequest>();
        for (InetAddress sourceAddress : sourceAddresses) {
            if (sourceAddress instanceof Inet4Address) {
                UdpRequest req = new UdpRequest(
                        sourceAddress,
                        ProbeDeviceType.IPV4.getMulticastSocketAddress(),
                        new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV4, null, 3, ST),
                        new BasicRequestTransformer(),
                        new ServiceDiscoveryResponseTransformer());
                discoveryRequests.add(req);
            } else if (sourceAddress instanceof Inet6Address) {
                UdpRequest v6LocalReq = new UdpRequest(
                        sourceAddress,
                        ProbeDeviceType.IPV6_LINK_LOCAL.getMulticastSocketAddress(),
                        new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_LINK_LOCAL, null, 3, ST),
                        new BasicRequestTransformer(),
                        new ServiceDiscoveryResponseTransformer());
                discoveryRequests.add(v6LocalReq);

                UdpRequest v6SiteReq = new UdpRequest(
                        sourceAddress,
                        ProbeDeviceType.IPV6_SITE_LOCAL.getMulticastSocketAddress(),
                        new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_SITE_LOCAL, null, 3, ST),
                        new BasicRequestTransformer(),
                        new ServiceDiscoveryResponseTransformer());
                discoveryRequests.add(v6SiteReq);

                UdpRequest v6OrgReq = new UdpRequest(
                        sourceAddress,
                        ProbeDeviceType.IPV6_ORGANIZATION_LOCAL.getMulticastSocketAddress(),
                        new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_ORGANIZATION_LOCAL, null, 3, ST),
                        new BasicRequestTransformer(),
                        new ServiceDiscoveryResponseTransformer());
                discoveryRequests.add(v6OrgReq);

                UdpRequest v6GlobalReq = new UdpRequest(
                        sourceAddress,
                        ProbeDeviceType.IPV6_GLOBAL.getMulticastSocketAddress(),
                        new ServiceDiscoveryUpnpIgdRequest(ProbeDeviceType.IPV6_GLOBAL, null, 3, ST),
                        new BasicRequestTransformer(),
                        new ServiceDiscoveryResponseTransformer());
                discoveryRequests.add(v6GlobalReq);
            } else {
                throw new IllegalStateException();
            }
        }
        performUdpRequests(networkBus, discoveryRequests, true, 1000L, 1000L, 1000L, 1000L, 1000L);

        // Get root XMLs
        Collection<TcpRequest> rootRequests = new ArrayList<TcpRequest>(discoveryRequests.size());
        Set<URL> processedLocations = new HashSet<URL>();
        for (UdpRequest discoveryReq : discoveryRequests) {
            LOG.debug("Processing discovery {}", discoveryReq);
            for (Object response : discoveryReq.getResponses()) {
                try {
                    ServiceDiscoveryUpnpIgdResponse discoveryResp = (ServiceDiscoveryUpnpIgdResponse) response;
                    if (!processedLocations.add(discoveryResp.getLocation())) {
                        LOG.debug("Found duplicate discovery location -- skipping");
                        continue;
                    }

                    ProbeResult other = new ProbeResult();
                    other.source = discoveryReq.getSourceAddress();
                    other.location = discoveryResp.getLocation();
                    other.serverName = discoveryResp.getServer();
                    
                    TcpRequest req = new TcpRequest(
                            other.source,
                            getAddressFromUrl(other.location),
                            new RootUpnpIgdRequest(other.location.getAuthority(), other.location.getFile()),
                            new BasicRequestTransformer(),
                            new RootUpnpIgdBytesToResponseTransformer(other.location));
                    req.setOther(other);

                    rootRequests.add(req);
                } catch (RuntimeException iae) {
                    LOG.debug("Encountered error", iae);
                } catch (Throwable iae) {
                    LOG.debug("Encountered error", iae);
                }
            }
        }
        performBatchedTcpRequests(networkBus, rootRequests, 3, 5000L, 5000L, 5000L);

        // Extract service locations from root XMLs + get service descriptions
        Collection<TcpRequest> serviceDescRequests = new ArrayList<TcpRequest>(rootRequests.size());
        for (TcpRequest rootRequest : rootRequests) {
            LOG.debug("Processing root {}", rootRequest);
            try {
                RootUpnpIgdResponse rootResp = (RootUpnpIgdResponse) rootRequest.getResponse();

                for (ServiceReference serviceReference : rootResp.getServices()) {
                    URL scpdUrl = serviceReference.getScpdUrl();

                    RootRequestResult other = new RootRequestResult();
                    other.probeResult = (ProbeResult) rootRequest.getOther();
                    other.serviceReference = serviceReference;

                    TcpRequest req = new TcpRequest(
                            rootRequest.getSourceAddress(),
                            getAddressFromUrl(scpdUrl),
                            new ServiceDescriptionUpnpIgdRequest(scpdUrl.getAuthority(), scpdUrl.getFile()),
                            new BasicRequestTransformer(),
                            new ServiceDescriptionUpnpIgdBytesToResponseTransformer());
                    req.setOther(other);

                    serviceDescRequests.add(req);
                }
            } catch (RuntimeException iae) {
                LOG.debug("Encountered error", iae);
            } catch (Throwable iae) {
                LOG.debug("Encountered error", iae);
            }
        }
        performBatchedTcpRequests(networkBus, serviceDescRequests, 3, 5000L, 5000L, 5000L);

        // Get service descriptions
        List<UpnpIgdPortMapper> ret = new ArrayList<UpnpIgdPortMapper>();
        for (TcpRequest serviceDescRequest : serviceDescRequests) {
            LOG.debug("Processing description {}", serviceDescRequest);
            try {
                ServiceDescriptionUpnpIgdResponse serviceDescResp = (ServiceDescriptionUpnpIgdResponse) serviceDescRequest.getResponse();
                //System.out.println("serviceDescRequest = " + serviceDescRequest.toString());
                RootRequestResult rootReqRes = (RootRequestResult) serviceDescRequest.getOther();
                if (serviceDescResp == null || serviceDescResp.getIdentifiedServices() == null)
                {
                	//System.out.println("serviceDescResp = null");
                	continue;
                }
                for (Entry<ServiceType, IdentifiedService> e : serviceDescResp.getIdentifiedServices().entrySet()) {
                    ServiceType serviceType = e.getKey();
                    IdentifiedService identifiedService = e.getValue();

                    UpnpIgdPortMapper upnpIgdPortMapper;
                    switch (serviceType) {
                        case OLD_PORT_MAPPER:
                            upnpIgdPortMapper = new PortMapperUpnpIgdPortMapper(
                                    networkBus, serviceDescRequest.getSourceAddress(),
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange(),
                                    false);
                            break;
                        case NEW_PORT_MAPPER:
                            upnpIgdPortMapper = new PortMapperUpnpIgdPortMapper(
                                    networkBus, serviceDescRequest.getSourceAddress(),
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange(),
                                    true);
                            break;
                        case FIREWALL:
                            upnpIgdPortMapper = new FirewallUpnpIgdPortMapper(
                                    networkBus, serviceDescRequest.getSourceAddress(),
                                    rootReqRes.serviceReference.getControlUrl(),
                                    rootReqRes.probeResult.serverName,
                                    rootReqRes.serviceReference.getServiceType(),
                                    identifiedService.getExternalPortRange(),
                                    identifiedService.getLeaseDurationRange());
                            break;
                        default:
                            throw new IllegalStateException(); // should never happen
                    }

                    ret.add(upnpIgdPortMapper);
                }
            } catch (RuntimeException iae) {
                LOG.debug("Encountered error", iae);
            } catch (Throwable iae) {
                LOG.debug("Encountered error", iae);
            }
        }

        return ret;
    }


    
    public final InetAddress getSourceAddress() {
        return internalAddress;
    }
    
    /**
     * Extracts the host and port from a URL.
     * @param url url to extract host and port from
     * @return socket address pointing to {@code url}'s address
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if port could not be determined (because protocol not recognized), or if protocol was anything other
     * than HTTP.
     */
    protected static final InetSocketAddress getAddressFromUrl(URL url) {
        Validate.notNull(url);
        Validate.isTrue(url.getProtocol().equalsIgnoreCase("http"));
        
        String host = url.getHost();
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        
        Validate.isTrue(port != -1);
        
        return NetworkUtils.toSocketAddress(host, port);
    }

    private static final class ProbeResult {

        private InetAddress source;
        private URL location;
        private String serverName;
    }

    private static final class RootRequestResult {

        private ProbeResult probeResult;
        private ServiceReference serviceReference;
    }

    /**
     * Dumps any {@link UpnpIgdHttpRequest} to byte array.
     */
    protected static final class BasicRequestTransformer implements RequestToBytesTransformer {

        
        public byte[] create(Object request) {
            return ((UpnpIgdHttpRequest) request).dump();
        }
    }

    private static final class ServiceDiscoveryResponseTransformer implements BytesToResponseTransformer {

        
        public Object create(byte[] buffer) {
            return new ServiceDiscoveryUpnpIgdResponse(buffer);
        }
    }

    private static final class RootUpnpIgdBytesToResponseTransformer implements BytesToResponseTransformer {

        private URL baseUrl;

        private RootUpnpIgdBytesToResponseTransformer(URL baseUrl) {
            this.baseUrl = baseUrl;
        }

        
        public Object create(byte[] buffer) {
            return new RootUpnpIgdResponse(baseUrl, buffer);
        }
    }

    private static final class ServiceDescriptionUpnpIgdBytesToResponseTransformer implements BytesToResponseTransformer {

        
        public Object create(byte[] buffer) {
            return new ServiceDescriptionUpnpIgdResponse(buffer);
        }
    }
}
