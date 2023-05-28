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
package com.offbynull.portmapper.gateways.network;

import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.helpers.ByteBufferUtils;
import com.offbynull.portmapper.gateways.network.UdpNetworkEntry.AddressedByteBuffer;
import com.offbynull.portmapper.gateways.network.internalmessages.ConnectedTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateTcpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateUdpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CreateUdpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.CloseNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.ErrorNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.GetLocalIpAddressesNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetLocalIpAddressesNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.GetNextIdNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.IdentifiableErrorNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.IdentifiableErrorNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.KillNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadClosedTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.ReadUdpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteEmptyTcpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteEmptyUdpNetworkNotification;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteTcpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteTcpNetworkResponse;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteUdpNetworkRequest;
import com.offbynull.portmapper.gateways.network.internalmessages.WriteUdpNetworkResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NetworkRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkRunnable.class);
    
    private final Bus bus;
    private final LinkedBlockingQueue<Object> queue;
    private final Selector selector;
    private int nextId = 0;
    private Method isUp = null;
    private Method isLoopback = null;

    NetworkRunnable() {
        try {
            selector = Selector.open();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        queue = new LinkedBlockingQueue<Object>();
        bus = new NetworkBus(selector, queue);
        try
        {
          isUp = NetworkInterface.class.getDeclaredMethod("isUp");
          isLoopback = NetworkInterface.class.getDeclaredMethod("isLoopback");
          //setDefault.invoke(null, unverifiedSSL);
          // SSLContext.setDefault(unverifiedSSL);
        }
        catch (Throwable t)
        {
          
        }
    }
    private Map<Integer, NetworkEntry<?>> idMap = new HashMap<Integer, NetworkEntry<?>>();
    private Map<Channel, NetworkEntry<?>> channelMap = new HashMap<Channel, NetworkEntry<?>>();
    private ByteBuffer buffer = ByteBuffer.allocate(65535);

    public Bus getBus() {
        return bus;
    }

    
    public void run() {
        LOG.debug("Starting gateway");
        try {
            while (true) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    }
                    Channel channel = (Channel) key.channel();
                    NetworkEntry<?> entry = channelMap.get(channel);
                    if (entry == null) {
                        channel.close();
                        continue;
                    }
                    try {
                        if (channel instanceof SocketChannel) {
                            handleSelectForTcpChannel(key, (TcpNetworkEntry) entry);
                        } else if (channel instanceof DatagramChannel) {
                            handleSelectForUdpChannel(key, (UdpNetworkEntry) entry);
                        } else {
                            throw new IllegalStateException(); // should never happen
                        }
                        updateSelectionKey(entry, (AbstractSelectableChannel) channel);
                    } catch (Throwable e) {
                        int id = entry.getId();
                        LOG.debug(id + " Exception encountered", e);
                        entry.getResponseBus().send(new IdentifiableErrorNetworkNotification(id));
                    }
                }
                ArrayList<Object> msgs = new ArrayList<Object>();
                queue.drainTo(msgs);
                for (Object msg : msgs) {
                    processMessage(msg);
                }
            }
        } catch (KillRequestException kre) {
            // do nothing
        } catch (Throwable e) {
            LOG.debug("Encountered unexpected exception", e);
            //throw new RuntimeException(e); // rethrow exception
        } finally {
            LOG.debug("Stopping gateway");
            shutdownResources();
            LOG.debug("Shutdown of resources complete");
        }
    }

    private void handleSelectForTcpChannel(SelectionKey selectionKey, TcpNetworkEntry entry) {
        SocketChannel channel = (SocketChannel) entry.getChannel();
        Bus responseBus = entry.getResponseBus();
        int id = entry.getId();
        if (selectionKey.isConnectable()) {
            LOG.debug("{} TCP connection", id);
            try {
                // This block is sometimes called more than once for each connection -- we still call finishConnect but we also check to
                // see if we're already connected before sending the CreateTcpSocketNetworkResponse msg
                boolean alreadyConnected = channel.isConnected();
                boolean connected = channel.finishConnect();
                if (!alreadyConnected && connected) {
                    entry.setConnecting(false);
                    responseBus.send(new ConnectedTcpNetworkNotification(id));
                }
            } catch (IOException ioe) {
                LOG.debug(id + " Exception encountered", ioe);
                responseBus.send(new IdentifiableErrorNetworkNotification(id));
            }
        }
        if (selectionKey.isReadable()) {
            try {
                buffer.clear();
                int readCount = channel.read(buffer);
                buffer.flip();

                LOG.debug("{} TCP read {} bytes", id, readCount);

                if (readCount == -1) {
                    // read finished, set flag to stop requesting read notifications
                    entry.setReadFinished(true);
                    responseBus.send(new ReadClosedTcpNetworkNotification(id));
                } else if (buffer.remaining() > 0) {
                    byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                    responseBus.send(new ReadTcpNetworkNotification(id, bufferAsArray));
                }
            } catch (IOException ioe) {
                LOG.debug(id + " Exception encountered", ioe);
                responseBus.send(new IdentifiableErrorNetworkNotification(id));
            }
        }
        if (selectionKey.isWritable()) {
            try {
              ArrayList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
                // if OP_WRITE was set, WriteTcpBlockNetworkRequest is pending (we should have at least 1 outgoing buffer)
                int writeCount = 0;
                if (outBuffers.isEmpty() && !entry.isNotifiedOfWritable()) {
                    LOG.debug("{} TCP write empty", id);

                    // if empty but not notified yet
                    entry.setNotifiedOfWritable(true);
                    entry.getResponseBus().send(new WriteEmptyTcpNetworkNotification(id));
                } else {
                    while (!outBuffers.isEmpty()) {
                        ByteBuffer outBuffer = outBuffers.get(0);
                        writeCount += channel.write(outBuffer);

                        LOG.debug("{} TCP wrote {} bytes", id, writeCount);

                        if (outBuffer.remaining() > 0) {
                            // not everything was written, which means we can't send anymore data until we get another OP_WRITE, so leave
                            break;
                        }
                        outBuffers.remove(0);
                        responseBus.send(new WriteTcpNetworkResponse(id, writeCount));
                    }
                }
            } catch (IOException ioe) {
                LOG.debug(id + " Exception encountered", ioe);
                responseBus.send(new IdentifiableErrorNetworkNotification(id));
            }
        }
    }

    private void handleSelectForUdpChannel(SelectionKey selectionKey, UdpNetworkEntry entry) {
        DatagramChannel channel = (DatagramChannel) entry.getChannel();
        Bus responseBus = entry.getResponseBus();
        int id = entry.getId();
        if (selectionKey.isReadable()) {
            try {
                buffer.clear();

                // Would directly call DatagramChannel.getLocalAddress(), but this doesn't look to be available on android. Doing this
                // on Java 7/8 performs the same function -- it probably does the same on Android as well?
                InetSocketAddress localAddress = (InetSocketAddress) channel.socket().getLocalSocketAddress();
                InetSocketAddress remoteAddress = (InetSocketAddress) channel.receive(buffer);

                LOG.debug("{} UDP read {} bytes from {} to {}", id, buffer.position(), remoteAddress, localAddress);

                if (remoteAddress != null) {
                    buffer.flip();
                    byte[] bufferAsArray = ByteBufferUtils.copyContentsToArray(buffer);
                    responseBus.send(new ReadUdpNetworkNotification(id, localAddress, remoteAddress, bufferAsArray));
                }
            } catch (IOException ioe) {
                LOG.debug(id + " Exception encountered", ioe);
                responseBus.send(new IdentifiableErrorNetworkNotification(id));
            }
        }
        if (selectionKey.isWritable()) {
            try {
              ArrayList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                if (!outBuffers.isEmpty()) {
                    // if not empty
                    AddressedByteBuffer outBuffer = outBuffers.remove(0);

                    ByteBuffer outgoingBuffer = outBuffer.getBuffer();
                    
                    // Would directly call DatagramChannel.getLocalAddress(), but this doesn't look to be available on android. Doing this
                    // on Java 7/8 performs the same function -- it probably does the same on Android as well?
                    
                    InetSocketAddress localAddress = (InetSocketAddress) channel.socket().getLocalSocketAddress();
                    InetSocketAddress remoteAddress = outBuffer.getSocketAddress();
                    int totalCount = outgoingBuffer.remaining();

                    LOG.debug("{} UDP write {} bytes from {} to {}", id, totalCount, localAddress, remoteAddress);
                    
                    int writeCount = channel.send(outgoingBuffer, remoteAddress);

                    LOG.debug("{} UDP wrote {} bytes of {} from {} to {}", id, writeCount, totalCount, localAddress, remoteAddress);

                    responseBus.send(new WriteUdpNetworkResponse(id, writeCount));
                } else if (!entry.isNotifiedOfWritable()) {
                    LOG.debug("{} UDP write empty", id);

                    // if empty but not notified yet
                    entry.setNotifiedOfWritable(true);
                    entry.getResponseBus().send(new WriteEmptyUdpNetworkNotification(id));
                }
            } catch (IOException ioe) {
                LOG.debug(id + " Exception encountered", ioe);
                responseBus.send(new IdentifiableErrorNetworkNotification(id));
            }
        }
    }

    private void updateSelectionKey(NetworkEntry<?> entry, AbstractSelectableChannel channel) throws ClosedChannelException {
        int newKey = 0;
        if (entry instanceof TcpNetworkEntry) {
            TcpNetworkEntry tcpNetworkEntry = (TcpNetworkEntry) entry;
            if (tcpNetworkEntry.isConnecting()) {
                // if connecting (tcp-only)
                newKey |= SelectionKey.OP_CONNECT;
            }
            
            if (!tcpNetworkEntry.isReadFinished()) {
                newKey |= SelectionKey.OP_READ;
            }
        } else if (entry instanceof UdpNetworkEntry) {
            newKey |= SelectionKey.OP_READ;
        }
        
        if (!entry.getOutgoingBuffers().isEmpty()) {
            // if not empty
            newKey |= SelectionKey.OP_WRITE;
            entry.setNotifiedOfWritable(false);
        } else if (!entry.isNotifiedOfWritable()) {
            // if is empty but not notified yet
            newKey |= SelectionKey.OP_WRITE;
        }
        if (newKey != entry.getSelectionKey()) {
            entry.setSelectionKey(newKey);
            int id = entry.getId();
            LOG.debug("{} Key updated to {}", id, newKey);
            channel.register(selector, newKey); // register new key if different -- calling register may have performance issues?
        }
    }

    private void processMessage(Object msg) throws IOException {
        LOG.debug("Processing message: {}", msg);

        if (msg instanceof GetNextIdNetworkRequest) {
            int id = nextId++;
            
            GetNextIdNetworkRequest req = (GetNextIdNetworkRequest) msg;
            Bus responseBus = req.getResponseBus();
            responseBus.send(new GetNextIdNetworkResponse(id));
        } else if (msg instanceof CreateUdpNetworkRequest) {
            CreateUdpNetworkRequest req = (CreateUdpNetworkRequest) msg;
            int id = req.getId();
            Bus responseBus = req.getResponseBus();
            
            DatagramChannel channel = null;
            UdpNetworkEntry entry = null;
            try {
                channel = DatagramChannel.open();
                channel.configureBlocking(false);
                
                // Would directly call DatagramChannel.bind(), but this doesn't look to be available on android. Doing this on Java 7/8
                // performs the same function -- it probably does the same on Android as well?
                channel.socket().bind(new InetSocketAddress(req.getSourceAddress(), 0));
                
                entry = new UdpNetworkEntry(id, channel, responseBus);
                updateSelectionKey(entry, channel);
                
                idMap.put(id, entry);
                channelMap.put(channel, entry);
                
                responseBus.send(new CreateUdpNetworkResponse(id));
            } catch (RuntimeException re) {
                if (channel != null) {
                    IOUtils.closeQuietly(channel);
                }

                if (entry != null) {
                    idMap.remove(entry.getId());
                    channelMap.remove(entry.getChannel());
                }
                
                LOG.debug("Unable to create socket", re);

                responseBus.send(new IdentifiableErrorNetworkResponse(id));
            }
        } else if (msg instanceof CreateTcpNetworkRequest) {
            CreateTcpNetworkRequest req = (CreateTcpNetworkRequest) msg;
            int id = req.getId();
            Bus responseBus = req.getResponseBus();
            
            SocketChannel channel = null;
            TcpNetworkEntry entry = null;
            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                // Would directly call SocketChannel.bind(), but this doesn't look to be available on android. Doing this on Java 7/8
                // performs the same function -- it probably does the same on Android as well?
                channel.socket().bind(new InetSocketAddress(req.getSourceAddress(), 0));
                InetSocketAddress dst = new InetSocketAddress(req.getDestinationAddress(), req.getDestinationPort());
                channel.connect(dst);
                
                entry = new TcpNetworkEntry(id, channel, responseBus);
                entry.setConnecting(true);
                updateSelectionKey(entry, channel);
                
                idMap.put(id, entry);
                channelMap.put(channel, entry);

                responseBus.send(new CreateTcpNetworkResponse(id));
            } catch (RuntimeException re) {
                if (channel != null) {
                    IOUtils.closeQuietly(channel);
                }

                if (entry != null) {
                    idMap.remove(entry.getId());
                    channelMap.remove(entry.getChannel());
                }
                
                LOG.debug("Unable to create socket", re);

                responseBus.send(new IdentifiableErrorNetworkResponse(id));
            }
        } else if (msg instanceof CloseNetworkRequest) {
            CloseNetworkRequest req = (CloseNetworkRequest) msg;
            int id = req.getId();
            
            NetworkEntry<?> entry = idMap.get(id);
            if (entry != null) {
                Bus responseBus = entry.getResponseBus();
                Channel channel = entry.getChannel();
                
                idMap.remove(id);
                channelMap.remove(channel);
                
                IOUtils.closeQuietly(channel);
                responseBus.send(new CloseNetworkResponse(id));
            }
        } else if (msg instanceof WriteTcpNetworkRequest) {
            WriteTcpNetworkRequest req = (WriteTcpNetworkRequest) msg;
            Bus responseBus = null;
            int id = req.getId();
            try {
                TcpNetworkEntry entry = (TcpNetworkEntry) idMap.get(id);
                if (entry != null) {
                    responseBus = entry.getResponseBus();
                    ArrayList<ByteBuffer> outBuffers = entry.getOutgoingBuffers();
                    ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                    if (writeBuffer.hasRemaining()) {
                        // only add if it has content -- adding empty is worthless because this is a stream
                        outBuffers.add(writeBuffer);
                    }
                    AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                    updateSelectionKey(entry, channel);
                }
            } catch (RuntimeException re) {
                LOG.debug("Unable to process message", re);
                if (responseBus != null) {
                    responseBus.send(new IdentifiableErrorNetworkResponse(id));
                }
            }
        } else if (msg instanceof WriteUdpNetworkRequest) {
            WriteUdpNetworkRequest req = (WriteUdpNetworkRequest) msg;
            Bus responseBus = null;
            int id = req.getId();
            try {
                UdpNetworkEntry entry = (UdpNetworkEntry) idMap.get(id);
                if (entry != null) {
                    responseBus = entry.getResponseBus();
                    ArrayList<AddressedByteBuffer> outBuffers = entry.getOutgoingBuffers();
                    ByteBuffer writeBuffer = ByteBuffer.wrap(req.getData());
                    InetSocketAddress writeAddress = req.getRemoteAddress();
                    outBuffers.add(new AddressedByteBuffer(writeBuffer, writeAddress));
                    AbstractSelectableChannel channel = (AbstractSelectableChannel) entry.getChannel();
                    updateSelectionKey(entry, channel);
                }
            } catch (RuntimeException re) {
                LOG.debug("Unable to process message", re);
                if (responseBus != null) {
                    responseBus.send(new IdentifiableErrorNetworkResponse(id));
                }
            }
        } else if (msg instanceof GetLocalIpAddressesNetworkRequest) {
            GetLocalIpAddressesNetworkRequest req = (GetLocalIpAddressesNetworkRequest) msg;
            Set<InetAddress> ret = new HashSet<InetAddress>();
            Bus responseBus = req.getResponseBus();
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    try
                    {
                    	//if (!networkInterface.isUp())
                    	if (!networkInterface.getInetAddresses().hasMoreElements())
                    	{
                            LOG.debug("Interface {} not up -- skipping", networkInterface);
                            continue;
                        }
                    	
                    	if (isUp != null)
                    	{
                    	  try
                    	  {
                    	    //networkInterface.isLoopback()
                    	    Boolean result = (Boolean) isUp.invoke(networkInterface);
                    	    if (!result)
                    	    {
                    	      LOG.debug("Interface {} not up -- skipping", networkInterface);
                            continue;
                    	    }
                    	  }
                    	  catch (Throwable t)
                        {
                            
                        }
                    	}
                    	
                    	if (isLoopback != null)
                      {
                        try
                        {
                          //networkInterface.isLoopback()
                          Boolean result = (Boolean) isLoopback.invoke(networkInterface);
                          if (result)
                          {
                            LOG.debug("Interface {} is loopback -- skipping", networkInterface);
                            continue;
                          }
                        }
                        catch (Throwable t)
                        {
                            
                        }
                      }
                    	
                    }
                    catch (Throwable t)
                    {
                        
                    }
                    try {
                        Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                        while (addrs.hasMoreElements()) {
                            InetAddress addr = addrs.nextElement();
                            if (!addr.isLoopbackAddress()) {
                                ret.add(addr);
                            }
                        }
                    } catch (RuntimeException niException) {
                        LOG.warn("Unable to access interface {}", networkInterface, niException);
                    }
                }
                responseBus.send(new GetLocalIpAddressesNetworkResponse(ret));
            } catch (RuntimeException re) {
                LOG.debug("Unable to process message", re);
                if (responseBus != null) {
                    responseBus.send(new ErrorNetworkResponse());
                }
            }
        } else if (msg instanceof KillNetworkRequest) {
            throw new KillRequestException();
        }
    }

    private void shutdownResources() {
        LOG.debug("Shutting down all resources");
        
        for (int id : new HashSet<Integer>(idMap.keySet())) { // shutdownResource removes items from idMap, so create a dupe of set such that you
                                                       // don't encounter issues with making changes to the set while you're iterating
            forcefullyShutdownResource(id);
        }
        
        try {
            selector.close();
        } catch (Throwable e) {
            LOG.debug("Error shutting down selector", e);
        }
        channelMap.clear();
        idMap.clear();
    }

    private void forcefullyShutdownResource(int id) {
        NetworkEntry<?> ne = idMap.remove(id);
        
        LOG.debug("{} Attempting to shutdown", id);
        
        Channel channel = null;
        try {
            channel = ne.getChannel();
            channelMap.remove(channel);
            
            ne.getResponseBus().send(new IdentifiableErrorNetworkNotification(id));
        } catch (Throwable e) {
            LOG.debug(id + " Error shutting down resource", e);
        } finally {
            IOUtils.closeQuietly(channel);
        }
    }
    
    
    private static final class KillRequestException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
    }
}
