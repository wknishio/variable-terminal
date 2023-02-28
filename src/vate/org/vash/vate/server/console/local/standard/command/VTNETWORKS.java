package org.vash.vate.server.console.local.standard.command;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTNETWORKS extends VTServerStandardLocalConsoleCommandProcessor
{
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
  
  public VTNETWORKS()
  {
    this.setFullName("*VTNETWORKS");
    this.setAbbreviatedName("*VTNTS");
    this.setFullSyntax("*VTNETWORKS");
    this.setAbbreviatedSyntax("*VTNTS");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    Enumeration<NetworkInterface> networkInterfaces;
    networkInterfaces = NetworkInterface.getNetworkInterfaces();
    message.append("\rVT>List of network interfaces on server:\nVT>");
    if (networkInterfaces != null && networkInterfaces.hasMoreElements())
    {
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
          if (getHardwareAddress != null)
          {
            byte[] hardwareAddress = (byte[]) getHardwareAddress.invoke(networkInterface);
            // byte[] hardwareAddress = networkInterface.getHardwareAddress();
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
      message.append("\nVT>End of network interfaces list\nVT>");
    }
    else
    {
      message.append("\rVT>No network interfaces found on server!\nVT>");
    }
    VTConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
