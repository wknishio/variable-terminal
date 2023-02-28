package org.vash.vate.network.nat.mapping;

public class VTNATPortMapping
{
  private int internalPort;
  private String remoteHost;
  private int externalPort;
  private long leaseTime;
  private String protocol;
  private String description;
  
  public VTNATPortMapping(int internalPort, String remoteHost, int externalPort, long leaseTime, String protocol, String description)
  {
    this.internalPort = internalPort;
    this.remoteHost = remoteHost;
    this.externalPort = externalPort;
    this.leaseTime = leaseTime;
    this.protocol = protocol;
    this.description = description;
  }
  
  public int getInternalPort()
  {
    return internalPort;
  }
  
  public String getRemoteHost()
  {
    return remoteHost;
  }
  
  public int getExternalPort()
  {
    return externalPort;
  }
  
  public String getProtocol()
  {
    return protocol;
  }
  
  public String getDescription()
  {
    return description;
  }
  
  public long getLeaseTime()
  {
    return leaseTime;
  }
  
  public String toString()
  {
    return protocol + ";" + internalPort + ";" + remoteHost + ";" + externalPort + ";" + leaseTime + ";" + description;
  }
  
  public boolean equals(Object other)
  {
    if (other == null)
    {
      return false;
    }
    return toString().equals(other.toString());
  }
  
}
