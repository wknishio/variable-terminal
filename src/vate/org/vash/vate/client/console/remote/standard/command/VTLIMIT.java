package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.VT;
import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;

public class VTLIMIT extends VTClientStandardRemoteConsoleCommandProcessor
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
        VTConsole.print("\nVT>Connection upload rate limit: [" + rate + "] bytes per second\nVT>");
      }
      else
      {
        VTConsole.print("\nVT>Connection upload rate limit: [Unlimited] bytes per second\nVT>");
      }
      connection.getCommandWriter().writeLine(command);
      connection.getCommandWriter().flush();
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
        else if (rate < (VT.VT_PACKET_TOTAL_SIZE_BYTES << 0))
        {
          rate = (VT.VT_PACKET_TOTAL_SIZE_BYTES << 0);
        }
        connection.setRateInBytesPerSecond(rate);
        if (rate > 0)
        {
          VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
        }
        else
        {
          VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
        }
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
      }
      catch (NumberFormatException e)
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
        else if (rate < (VT.VT_PACKET_TOTAL_SIZE_BYTES << 0))
        {
          rate = (VT.VT_PACKET_TOTAL_SIZE_BYTES << 0);
        }
        if (parsed[2].toUpperCase().startsWith("D"))
        {
          connection.getCommandWriter().writeLine(command);
          connection.getCommandWriter().flush();
        }
        else
        {
          connection.setRateInBytesPerSecond(rate);
          if (rate > 0)
          {
            VTConsole.print("\nVT>Connection upload rate limit set to: [" + rate + "] bytes per second\nVT>");
          }
          else
          {
            VTConsole.print("\nVT>Connection upload rate limit set to: [Unlimited] bytes per second\nVT>");
          }
          connection.getCommandWriter().writeLine(command);
          connection.getCommandWriter().flush();
        }
      }
      catch (NumberFormatException e)
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
