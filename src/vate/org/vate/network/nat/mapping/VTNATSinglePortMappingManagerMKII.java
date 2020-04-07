package org.vate.network.nat.mapping;

import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	private int discoveryTime;
	// private int leaseTime;
	private int intervalTime;
	private Thread manager;
	// private String[] externalIPAddresses;
	private volatile VTNATPortMapping deletedPortMapping;
	private volatile VTNATPortMapping currentPortMapping;
	private volatile VTNATPortMapping nextPortMapping;
	private volatile VTNATPortMappingResultNotify resultNotify;
	
	//private volatile InternetGatewayDevice[] upnpDevices;
	//private volatile InetAddress[] natpmpGateways;
	
	private Gateway networkGateway;
	private Gateway processGateway;
	private Bus networkBus;
	private Bus processBus;
	
	private Map<PortMapper, MappedPort> currentMappedPorts;
	//private Set<InetAddress> externalNetworkAddresses;

	public VTNATSinglePortMappingManagerMKII(int discoveryTime, int intervalTime)
	{
		this.discoveryTime = discoveryTime;
		// this.leaseTime = leaseTime;
		this.intervalTime = intervalTime;
		this.currentMappedPorts = Collections.synchronizedMap(new LinkedHashMap<PortMapper, MappedPort>());
		//this.externalNetworkAddresses = Collections.synchronizedSet(new LinkedHashSet<InetAddress>());
		networkGateway = NetworkGateway.create();
		processGateway = ProcessGateway.create();
		networkBus = networkGateway.getBus();
		processBus = processGateway.getBus();
		
		Thread shutdownHook = new Thread(null, new Runnable()
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
							//t.printStackTrace();
						}
						return;
					}
				}
			}
		}, this.getClass().getSimpleName());
		Runtime.getRuntime().addShutdownHook(shutdownHook);
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
	
	public void setPortMapping(int internalPort, String remoteHost, int externalPort, String protocol, String description, VTNATPortMappingResultNotify resultNotify)
	{
		this.resultNotify = resultNotify;
		VTNATPortMapping mapping = new VTNATPortMapping(internalPort, remoteHost, externalPort, protocol, description);
		synchronized (currentMappedPorts)
		{
			nextPortMapping = mapping;
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
		}
	}
	
	private List<PortMapper> discoverNATDevices()
	{
		try
		{
			//Thread.sleep(500);
			return PortMapperFactory.discover(networkBus, processBus, new InetAddress[] {});
			//return InternetGatewayDevice.getDevices(discoveryTime * 1000);
		}
		catch (Throwable t)
		{
			//t.printStackTrace();
			return null;
		}
	}
	
	/* public String[] getExternalIPAddresses() { return externalIPAddresses;
	 * } */
	
	/* private String[] getExternalIPAddresses(InternetGatewayDevice[] devices)
	 * { Set<String> externalIPs = new HashSet<String>(); for
	 * (InternetGatewayDevice device : devices) { try { Thread.sleep(500);
	 * externalIPs.add(device.getExternalIPAddress()); } catch (Throwable e) {
	 * } } return externalIPs.toArray(new String[]{}); } */
	
	public VTNATPortMapping getPortMapping()
	{
		return nextPortMapping;
	}
	
	public void run()
	{
		//System.out.println("VTNATSinglePortMappingManagerMKII.run");
		while (running)
		{
			//System.out.println("VTNATSinglePortMappingManagerMKII.running");
			synchronized (currentMappedPorts)
			{
				if (nextPortMapping != null)
				{
					if (nextPortMapping != null && (currentPortMapping == null || !nextPortMapping.equals(currentPortMapping)))
					{
						//detected change in port mappings
						//System.out.println("VTNATSinglePortMappingManagerMKII.detect");
						deletedPortMapping = currentPortMapping;
					}
				}
				
				if (deletedPortMapping != null)
				{
					//delete current mappings
					//System.out.println("VTNATSinglePortMappingManagerMKII.delete");
					for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
					{
						try
						{
							entry.getKey().unmapPort(entry.getValue());
						}
						catch (Throwable t)
						{
							//t.printStackTrace();
						}
					}
					currentMappedPorts.clear();
				}
				
				if (nextPortMapping != null && !nextPortMapping.equals(currentPortMapping))
				{
					//add new mappings
					//System.out.println("VTNATSinglePortMappingManagerMKII.add");
					List<PortMapper> natDevices = discoverNATDevices();
					//System.out.println("natDevices:" + natDevices.size());
					currentMappedPorts.clear();
					for (PortMapper natDevice : natDevices)
					{
						try
						{
							//System.out.println("natDevice:" + natDevice.toString());
							MappedPort natPortMapping = natDevice.mapPort(PortType.TCP, nextPortMapping.getInternalPort(), nextPortMapping.getExternalPort(), 0);
							//VTConsole.println("natPortMapping:" + natPortMapping.toString());
							currentMappedPorts.put(natDevice, natPortMapping);
						}
						catch (Throwable t)
						{
							//t.printStackTrace();
						}
					}
					currentPortMapping = nextPortMapping;
					if (resultNotify != null)
					{
						resultNotify.result(currentMappedPorts);
					}
				}
				else
				{
					if (currentPortMapping != null)
					{
						//System.out.println("VTNATSinglePortMappingManagerMKII.refresh");
						for (Entry<PortMapper, MappedPort> entry : currentMappedPorts.entrySet())
						{
							try
							{
								entry.getKey().refreshPort(entry.getValue(), 0);
							}
							catch (Throwable t)
							{
								//t.printStackTrace();
							}
						}
					}
					else
					{
						//System.out.println("VTNATSinglePortMappingManagerMKII.nothing");
					}
				}
				
				//System.out.println("VTNATSinglePortMappingManagerMKII.wait");
				try
				{
					currentMappedPorts.wait(intervalTime * 1000);
				}
				catch (InterruptedException e)
				{
					
				}
			}
		}
	}
	
	/* public static void main(String[] args) { VTNATSinglePortMappingManager
	 * manager = new VTNATSinglePortMappingManager(5, 10, 5);
	 * System.out.println(Arrays.toString(manager.getExternalIPAddresses()));
	 * } */
}