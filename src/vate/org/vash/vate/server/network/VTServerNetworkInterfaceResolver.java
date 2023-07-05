package org.vash.vate.server.network;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerNetworkInterfaceResolver extends VTTask
{
  private volatile boolean finished;
  private StringBuilder message;
  private VTServerSession session;
  private static Method getHardwareAddress;
  
  static
  {
    try
    {
      getHardwareAddress = NetworkInterface.class.getMethod("getHardwareAddress");
      // getHardwareAddress.setAccessible(true);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTServerNetworkInterfaceResolver(VTServerSession session)
  {
    this.session = session;
    this.message = new StringBuilder();
    this.finished = true;
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public void run()
  {
    try
    {
      message.setLength(0);
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      if (networkInterfaces != null && networkInterfaces.hasMoreElements())
      {
        message.append("\nVT>List of server network interfaces:\nVT>");
        while (networkInterfaces.hasMoreElements())
        {
          NetworkInterface networkInterface = networkInterfaces.nextElement();
          Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
          if (!addresses.hasMoreElements())
          {
            continue;
          }
          message.append("\nVT>Name: [" + networkInterface.getName() + "]" + "\nVT>Display name: [" + networkInterface.getDisplayName() + "]");
          
          try
          {
            // Method getHardwareAddress =
            // networkInterface.getClass().getMethod("getHardwareAddress");
            if (getHardwareAddress != null)
            {
              byte[] hardwareAddress = (byte[]) getHardwareAddress.invoke(networkInterface);
              if (hardwareAddress != null && hardwareAddress.length > 0)
              {
                message.append("\nVT>Hardware address: [");
                for (int i = 0; i < hardwareAddress.length; i++)
                {
                  message.append(String.format("%02X%s", hardwareAddress[i], (i < hardwareAddress.length - 1) ? "-" : ""));
                }
                message.append("]");
              }
            }
          }
          catch (Throwable t)
          {
            // Hardware address available in 1.6 and beyond but we support 1.5
          }
          
          while (addresses.hasMoreElements())
          {
            InetAddress address = addresses.nextElement();
            message.append("\nVT>Host address: [" + address.getHostAddress() + "]");
            // "\nVT>Host name: [" + address.getCanonicalHostName() + "]\nVT>");
            // "]\nVT>Canonical host name: [" +
            // address.getCanonicalHostName() + "]");
          }
          message.append("\nVT>");
        }
        message.append("\nVT>End of server network interfaces list\nVT>");
        synchronized (this)
        {
          session.getConnection().getResultWriter().write(message.toString());
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
      }
      else
      {
        message.append("\nVT>No network interfaces found on server!\nVT>");
        synchronized (this)
        {
          session.getConnection().getResultWriter().write(message.toString());
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
      }
    }
    catch (Throwable e)
    {
      
    }
    finished = true;
  }
}