package org.vash.vate.nat.mapping;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.offbynull.portmapper.PortMapperFactory;
import com.offbynull.portmapper.gateway.Bus;
import com.offbynull.portmapper.gateway.Gateway;
import com.offbynull.portmapper.gateways.network.NetworkGateway;
import com.offbynull.portmapper.gateways.process.ProcessGateway;
import com.offbynull.portmapper.mapper.MappedPort;
import com.offbynull.portmapper.mapper.PortMapper;
import com.offbynull.portmapper.mapper.PortType;

public class VTNATSinglePortMappingManagerMKII implements Runnable
{
  private volatile boolean running;
  private volatile boolean hooked;
  private int discoveryTime;
  private int intervalTime;
  private Thread manager;
  // private String[] externalIPAddresses;
  private volatile VTNATPortMapping deletedPortMapping;
  private volatile VTNATPortMapping currentPortMapping;
  private volatile VTNATPortMapping nextPortMapping;
  private volatile VTNATPortMappingResultNotify resultNotify;
  
  // private volatile InternetGatewayDevice[] upnpDevices;
  // private volatile InetAddress[] natpmpGateways;
  
  private Gateway networkGateway;
  private Gateway processGateway;
  private Bus networkBus;
  private Bus processBus;
  
  private Map<PortMapper, MappedPort> currentMappedPorts;
  private Thread shutdownHook;
  // private Set<InetAddress> externalNetworkAddresses;
  
  public VTNATSinglePortMappingManagerMKII(int discoveryTime, int intervalTime)
  {
    this.discoveryTime = discoveryTime;
    this.intervalTime = intervalTime;
    this.currentMappedPorts = new LinkedHashMap<PortMapper, MappedPort>();
    // this.externalNetworkAddresses = Collections.synchronizedSet(new
    // LinkedHashSet<InetAddress>());
    networkGateway = NetworkGateway.create();
    processGateway = ProcessGateway.create();
    networkBus = networkGateway.getBus();
    processBus = processGateway.getBus();
    
    shutdownHook = new Thread(null, new Runnable()
    {
      public void run()
      {
        synchronized (currentMappedPorts)
        {
          if (currentMappedPorts.size() > 0)
          {
            try
            {
              for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
              {
                entry.getKey().unmapPort(entry.getValue());
              }
            }
            catch (Throwable t)
            {
              // t.printStackTrace();
            }
            return;
          }
        }
      }
    }, this.getClass().getSimpleName());
  }
  
  public void start()
  {
    // System.out.println("started nat mapper");
    if (this.running)
    {
      stop();
    }
    manager = new Thread(null, this, this.getClass().getSimpleName());
    manager.setDaemon(true);
    synchronized (currentMappedPorts)
    {
      this.running = true;
      currentMappedPorts.notify();
    }
    manager.start();
  }
  
  public void stop()
  {
    synchronized (currentMappedPorts)
    {
      this.running = false;
      currentMappedPorts.notify();
    }
    try
    {
      if (manager != null)
      {
        manager.join();
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public int getDiscoveryTime()
  {
    return discoveryTime;
  }
  
  public int getIntervalTime()
  {
    return intervalTime;
  }
  
  public void setPortMapping(int internalPort, String remoteHost, int externalPort, long leaseTime, String protocol, String description, VTNATPortMappingResultNotify resultNotify)
  {
    if (!hooked)
    {
      Runtime.getRuntime().addShutdownHook(shutdownHook);
      hooked = true;
    }
    this.resultNotify = resultNotify;
    VTNATPortMapping nextMapping = new VTNATPortMapping(internalPort, remoteHost, externalPort, leaseTime, protocol, description);
    synchronized (currentMappedPorts)
    {
      nextPortMapping = nextMapping;
      if (currentPortMapping != null && !nextPortMapping.equals(currentPortMapping))
      {
        deletedPortMapping = currentPortMapping;
      }
      else
      {
        deletedPortMapping = null;
      }
      currentMappedPorts.notify();
    }
  }
  
  public void deletePortMapping()
  {
    synchronized (currentMappedPorts)
    {
      if (currentPortMapping != null || nextPortMapping != null)
      {
        deletedPortMapping = currentPortMapping != null ? currentPortMapping : nextPortMapping;
        currentPortMapping = null;
        nextPortMapping = null;
        currentMappedPorts.notify();
      }
      else
      {
        deletedPortMapping = null;
      }
    }
  }
  
  private List<PortMapper> discoverNATDevices()
  {
    try
    {
      return PortMapperFactory.discover(networkBus, processBus, new InetAddress[] {});
      // return InternetGatewayDevice.getDevices(discoveryTime * 1000);
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
      return null;
    }
  }
  
  /*
   * public String[] getExternalIPAddresses() { return externalIPAddresses; }
   */
  
  /*
   * private String[] getExternalIPAddresses(InternetGatewayDevice[] devices) {
   * Set<String> externalIPs = new HashSet<String>(); for (InternetGatewayDevice
   * device : devices) { try { Thread.sleep(500);
   * externalIPs.add(device.getExternalIPAddress()); } catch (Throwable e) { } }
   * return externalIPs.toArray(new String[]{}); }
   */
  
  public VTNATPortMapping getPortMapping()
  {
    return nextPortMapping;
  }
  
  public void run()
  {
    boolean delete = false;
    boolean discover = false;
    boolean clear = false;
    int nextInternalPort = 0;
    int nextExternalPort = 0;
    long nextLeaseTime = 0;
    
    Map<PortMapper, MappedPort> nextMappedPorts = new LinkedHashMap<PortMapper, MappedPort>();
    
    while (running)
    {
      delete = false;
      discover = false;
      clear = false;
      
      // check
      synchronized (currentMappedPorts)
      {
        if (deletedPortMapping != null)
        {
          delete = true;
          deletedPortMapping = null;
        }
        if (nextPortMapping != null && !nextPortMapping.equals(currentPortMapping))
        {
          discover = true;
          nextInternalPort = nextPortMapping.getInternalPort();
          nextExternalPort = nextPortMapping.getExternalPort();
          nextLeaseTime = nextPortMapping.getLeaseTime();
        }
      }
      
      // nat
      if (delete)
      {
        // delete
        clear = true;
        for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
        {
          try
          {
            entry.getKey().unmapPort(entry.getValue());
          }
          catch (Throwable t)
          {
            // t.printStackTrace();
          }
        }
      }
      
      if (discover)
      {
        // discover
        clear = true;
        List<PortMapper> natDevices = discoverNATDevices();
        nextMappedPorts.clear();
        for (PortMapper natDevice : natDevices)
        {
          try
          {
            MappedPort natPortMapping = natDevice.mapPort(PortType.TCP, nextInternalPort, nextExternalPort, nextLeaseTime);
            nextMappedPorts.put(natDevice, natPortMapping);
          }
          catch (Throwable t)
          {
            // t.printStackTrace();
          }
        }
      }
      else
      {
        // refresh
        for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
        {
          try
          {
            entry.getKey().refreshPort(entry.getValue(), 600);
          }
          catch (Throwable t)
          {
            // t.printStackTrace();
          }
        }
      }
      
      // update
      synchronized (currentMappedPorts)
      {
        if (delete)
        {
          
        }
        if (clear)
        {
          currentMappedPorts.clear();
        }
        if (discover)
        {
          currentPortMapping = nextPortMapping;
          currentMappedPorts.putAll(nextMappedPorts);
          nextMappedPorts.clear();
          if (resultNotify != null)
          {
            List<String> externalHosts = new ArrayList<String>();
            for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
            {
              InetAddress address = entry.getValue().getExternalAddress();
              if (address != null)
              {
                // int port = entry.getValue().getExternalPort();
                externalHosts.add(address.getHostAddress());
              }
            }
            resultNotify.result(externalHosts);
          }
        }
        try
        {
          currentMappedPorts.wait(intervalTime * 1000);
        }
        catch (Throwable e)
        {
          
        }
      }
    }
  }
  
  /*
   * public static void main(String[] args) { VTNATSinglePortMappingManager
   * manager = new VTNATSinglePortMappingManager(5, 10, 5);
   * System.out.println(Arrays.toString(manager.getExternalIPAddresses())); }
   */
}