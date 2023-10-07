package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.VT;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTLIMIT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTLIMIT()
  {
    this.setFullName("*VTLIMIT");
    this.setAbbreviatedName("*VTLM");
    this.setFullSyntax("*VTLIMIT [RATE] [SIDE]");
    this.setAbbreviatedSyntax("*VTLM [RT] [SI]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      long rate = connection.getRateInBytesPerSecond();
      if (rate > 0)
      {
        connection.getResultWriter().write("\nVT>Connection download rate limit: [" + rate + "] bytes per second\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Connection download rate limit: [Unlimited] bytes per second\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length == 2)
    {
      long rate = 0;
      try
      {
        rate = Long.parseLong(parsed[1]);
        if (rate <= 0)
        {
          rate = 0;
        }
        else if (rate < (VT.VT_PACKET_TOTAL_SIZE_BYTES * 1))
        {
          rate = (VT.VT_PACKET_TOTAL_SIZE_BYTES * 1);
        }
        connection.setRateInBytesPerSecond(rate);
        if (rate > 0)
        {
          connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
          connection.getResultWriter().flush();
        }
      }
      catch (NumberFormatException e)
      {
        
      }
    }
    else if (parsed.length >= 3)
    {
      long rate = 0;
      try
      {
        rate = Long.parseLong(parsed[1]);
        if (rate <= 0)
        {
          rate = 0;
        }
        else if (rate < (VT.VT_PACKET_TOTAL_SIZE_BYTES * 1))
        {
          rate = (VT.VT_PACKET_TOTAL_SIZE_BYTES * 1);
        }
        if (!parsed[2].toUpperCase().startsWith("U"))
        {
          connection.setRateInBytesPerSecond(rate);
          if (rate > 0)
          {
            connection.getResultWriter().write("\nVT>Connection download rate limit set to: [" + rate + "] bytes per second\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Connection download rate limit set to: [Unlimited] bytes per second\nVT>");
            connection.getResultWriter().flush();
          }
        }
      }
      catch (NumberFormatException e)
      {
        
      }
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
