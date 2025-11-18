package org.vash.vate.server.console.local.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTMIXER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTMIXER()
  {
    this.setFullName("*VTMIXER");
    this.setAbbreviatedName("*VTMX");
    this.setFullSyntax("*VTMIXER");
    this.setAbbreviatedSyntax("*VTMX");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    message.append("\rVT>List of server audio mixers:\nVT>");
    Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    int number = 0;
    for (Mixer.Info info : mixers)
    {
      // Mixer mixer = AudioSystem.getMixer(info);
      message.append("\nVT>Number: [" + number++ + "]");
      message.append("\nVT>Name: [" + info.getName() + "]");
      // message.append("\nVT>Vendor: [" + info.getVendor() + "]");
      message.append("\nVT>Description: [" + info.getDescription() + "]");
      // for (Line.Info line : mixer.getSourceLineInfo())
      // {
      // message.append("\nVT>Line: [" + line.toString() + "]");
      // }
      // for (Line.Info line : mixer.getTargetLineInfo())
      // {
      // message.append("\nVT>Line: [" + line.toString() + "]");
      // }
      message.append("\nVT>");
    }
    message.append("\nVT>End of server audio mixers list\nVT>");
    VTMainConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
