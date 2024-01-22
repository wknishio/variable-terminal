package org.vash.vate.server.console.remote.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTMIXER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTMIXER()
  {
    this.setFullName("*VTMIXER");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXER [SIDE]");
    this.setAbbreviatedSyntax("*VTMX [SD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        message.setLength(0);
        message.append("\nVT>List of server audio mixers:\nVT>");
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        int number = 0;
        for (Mixer.Info info : mixers)
        {
          message.append("\nVT>Number: [" + number++ + "]");
          message.append("\nVT>Name: [" + info.getName() + "]");
          // message.append("\nVT>Vendor: [" + info.getVendor() + "]");
          message.append("\nVT>Description: [" + info.getDescription() + "]");
          message.append("\nVT>");
        }
        message.append("\nVT>End of server audio mixers list\nVT>");
        connection.getResultWriter().write(message.toString());
        connection.getResultWriter().flush();
      }
      else if (parsed[1].toUpperCase().startsWith("L"))
      {
        
      }
    }
    else
    {
      message.setLength(0);
      message.append("\nVT>List of server audio mixers:\nVT>");
      Mixer.Info[] mixers = AudioSystem.getMixerInfo();
      int number = 0;
      for (Mixer.Info info : mixers)
      {
        message.append("\nVT>Number: [" + number++ + "]");
        message.append("\nVT>Name: [" + info.getName() + "]");
        // message.append("\nVT>Vendor: [" + info.getVendor() + "]");
        message.append("\nVT>Description: [" + info.getDescription() + "]");
        message.append("\nVT>");
      }
      message.append("\nVT>End of server audio mixers list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
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
