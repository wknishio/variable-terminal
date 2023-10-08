package org.vash.vate.nat.mapping;

//import java.net.InetAddress;

//import net.sbbi.upnp.impls.InternetGatewayDevice;
//import net.sbbi.upnp.messages.ActionResponse;
//import net.tomp2p.natpmp.Gateway;
//import net.tomp2p.natpmp.MapRequestMessage;
//import net.tomp2p.natpmp.NatPmpDevice;
//
public class VTNATSinglePortMappingManager implements Runnable
{
//  private volatile boolean running;
//  private int discoveryTime;
//  private int intervalTime;
//  private Thread manager;
//  private volatile VTNATPortMapping deletedPortMapping;
//  private volatile VTNATPortMapping currentPortMapping;
//  private volatile VTNATPortMapping nextPortMapping;
//  private volatile InternetGatewayDevice[] upnpDevices;
//  private volatile InetAddress[] natpmpGateways;
//
//  public VTNATSinglePortMappingManager(int discoveryTime, int intervalTime)
//  {
//    this.discoveryTime = discoveryTime;
//    this.intervalTime = intervalTime;
//
//    Thread shutdownHook = new Thread(null, new Runnable()
//    {
//      public void run()
//      {
//        if (currentPortMapping != null)
//        {
//          try
//          {
//            if (upnpDevices != null)
//            {
//              for (InternetGatewayDevice device : upnpDevices)
//              {
//                device.deletePortMapping(currentPortMapping.getRemoteHost(), currentPortMapping.getExternalPort(), currentPortMapping.getProtocol());
//              }
//            }
//            if (natpmpGateways != null && natpmpGateways.length > 0)
//            {
//              MapRequestMessage natpmpRequest = new MapRequestMessage(!currentPortMapping.getProtocol().equalsIgnoreCase("UDP"), currentPortMapping.getInternalPort(), currentPortMapping.getExternalPort(), (int) currentPortMapping.getLeaseTime(), null);
//              for (InetAddress natpmpGateway : natpmpGateways)
//              {
//                try
//                {
//                  NatPmpDevice natpmpDevice = new NatPmpDevice(natpmpGateway);
//                  natpmpDevice.enqueueMessage(natpmpRequest);
//                  natpmpDevice.waitUntilQueueEmpty();
//                }
//                catch (Throwable t)
//                {
//                  // t.printStackTrace();
//                }
//              }
//            }
//          }
//          catch (Throwable t)
//          {
//
//          }
//          return;
//        }
//      }
//    }, this.getClass().getSimpleName());
//    Runtime.getRuntime().addShutdownHook(shutdownHook);
//  }
//
//  public void start()
//  {
//    if (this.running)
//    {
//      stop();
//    }
//    manager = new Thread(null, this, this.getClass().getSimpleName());
//    manager.setDaemon(true);
//    synchronized (this)
//    {
//      this.running = true;
//      notify();
//    }
//    manager.start();
//  }
//
//  public void stop()
//  {
//    synchronized (this)
//    {
//      this.running = false;
//      notify();
//    }
//    try
//    {
//      if (manager != null)
//      {
//        manager.join();
//      }
//    }
//    catch (Throwable e)
//    {
//
//    }
//  }
//
//  public int getDiscoveryTime()
//  {
//    return discoveryTime;
//  }
//
//  public int getIntervalTime()
//  {
//    return intervalTime;
//  }
//
//  public boolean checkPortMapping(int internalPort, String remoteHost, int externalPort, int leaseTime, String protocol)
//  {
//    VTNATPortMapping mapping = new VTNATPortMapping(internalPort, remoteHost, externalPort, leaseTime, protocol, "");
//    upnpDevices = discoverUPNPDevices();
//    return checkUPNPPortMapping(upnpDevices, mapping);
//  }
//
//  public void setPortMapping(int internalPort, String remoteHost, int externalPort, int leaseTime, String protocol, String description, VTNATPortMappingResultNotify resultNotify)
//  {
//    VTNATPortMapping mapping = new VTNATPortMapping(internalPort, remoteHost, externalPort, leaseTime, protocol, description);
//    synchronized (this)
//    {
//      nextPortMapping = mapping;
//      notify();
//    }
//  }
//
//  public void deletePortMapping()
//  {
//    synchronized (this)
//    {
//      if (currentPortMapping != null || nextPortMapping != null)
//      {
//        deletedPortMapping = currentPortMapping;
//        currentPortMapping = null;
//        nextPortMapping = null;
//        notify();
//      }
//    }
//  }
//
//  private InternetGatewayDevice[] discoverUPNPDevices()
//  {
//    try
//    {
//      Thread.sleep(250);
//      return InternetGatewayDevice.getDevices(discoveryTime * 1000);
//    }
//    catch (Throwable e)
//    {
//      // e.printStackTrace();
//      return null;
//    }
//  }
//
//  private boolean checkUPNPPortMapping(InternetGatewayDevice[] devices, VTNATPortMapping mapping)
//  {
//    if (mapping == null)
//    {
//      return true;
//    }
//    for (InternetGatewayDevice device : devices)
//    {
//      String remoteHost = mapping.getRemoteHost();
//      int externalPort = mapping.getExternalPort();
//      String protocol = mapping.getProtocol();
//      try
//      {
//        ActionResponse response = device.getSpecificPortMappingEntry(remoteHost, externalPort, protocol);
//        if (response != null)
//        {
//          return true;
//        }
//      }
//      catch (Throwable e)
//      {
//
//      }
//    }
//    return false;
//  }
//
//  private void deleteUPNPPortMapping(InternetGatewayDevice[] devices, VTNATPortMapping mapping)
//  {
//    if (mapping == null)
//    {
//      return;
//    }
//    for (InternetGatewayDevice device : devices)
//    {
//      String remoteHost = mapping.getRemoteHost();
//      int externalPort = mapping.getExternalPort();
//      String protocol = mapping.getProtocol();
//      try
//      {
//        device.deletePortMapping(remoteHost, externalPort, protocol);
//        Thread.sleep(250);
//      }
//      catch (Throwable e)
//      {
//
//      }
//    }
//    return;
//  }
//
//  private void setUPNPPortMapping(InternetGatewayDevice[] devices, VTNATPortMapping mapping)
//  {
//    if (mapping == null)
//    {
//      return;
//    }
//    for (InternetGatewayDevice device : devices)
//    {
//      String localAddress = device.getDiscoveryResponseAddress().getHostAddress();
//      String description = mapping.getDescription();
//      int internalPort = mapping.getInternalPort();
//      String remoteHost = mapping.getRemoteHost();
//      int externalPort = mapping.getExternalPort();
//      String protocol = mapping.getProtocol();
//      try
//      {
//        device.addPortMapping(description, remoteHost, internalPort, externalPort, localAddress, 0, protocol);
//        Thread.sleep(250);
//      }
//      catch (Throwable e)
//      {
//        // e.printStackTrace();
//      }
//    }
//    return;
//  }
//
//  public VTNATPortMapping getPortMapping()
//  {
//    return nextPortMapping;
//  }
//
  public void run()
  {
//    while (running)
//    {
//      if (nextPortMapping != null)
//      {
//        synchronized (this)
//        {
//          if (nextPortMapping != null && (currentPortMapping == null || !currentPortMapping.toString().equals(nextPortMapping.toString())))
//          {
//            deletedPortMapping = currentPortMapping;
//            currentPortMapping = nextPortMapping;
//          }
//        }
//        InternetGatewayDevice[] devices = discoverUPNPDevices();
//        if (devices != null)
//        {
//          upnpDevices = devices;
//        }
//        if (upnpDevices != null && upnpDevices.length > 0)
//        {
//          // Using UPnP
//          deleteUPNPPortMapping(upnpDevices, deletedPortMapping);
//          deletedPortMapping = null;
//          setUPNPPortMapping(upnpDevices, currentPortMapping);
//          // externalIPAddresses =
//          // getExternalIPAddresses(upnpDevices);
//        }
//        else
//        {
//          // Using NAT-PMP
//          try
//          {
//            MapRequestMessage natpmpRequest = new MapRequestMessage(true, currentPortMapping.getInternalPort(), currentPortMapping.getExternalPort(), 3600, null);
//            natpmpGateways = Gateway.getIPs();
//            if (natpmpGateways != null && natpmpGateways.length > 0)
//            {
//              for (InetAddress natpmpGateway : natpmpGateways)
//              {
//                try
//                {
//                  NatPmpDevice natpmpDevice = new NatPmpDevice(natpmpGateway);
//                  natpmpDevice.enqueueMessage(natpmpRequest);
//                  natpmpDevice.waitUntilQueueEmpty();
//                  natpmpDevice.shutdown();
//                }
//                catch (Throwable t)
//                {
//                  // t.printStackTrace();
//                }
//              }
//            }
//            else
//            {
//              // System.out.println("natpmp not found");
//            }
//          }
//          catch (Throwable t)
//          {
//            // t.printStackTrace();
//          }
//        }
//      }
//      synchronized (this)
//      {
//        try
//        {
//          wait(intervalTime * 1000);
//        }
//        catch (InterruptedException e)
//        {
//
//        }
//      }
//    }
  }
}