package org.vash.vate.server.console.remote.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import org.vash.vate.audio.VTAudioBeeper;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBEEP extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTBEEP()
  {
    this.setFullName("*VTBEEP");
    this.setAbbreviatedName("*VTBP");
    this.setFullSyntax("*VTBEEP [HERTZ TIME] [MIXER]");
    this.setAbbreviatedSyntax("*VTBP [HZ TM] [MX]");
  }
  
  private static final int SAMPLE_RATE_HERTZ = 16000;
  private static final int SAMPLE_SIZE_BITS = 8;
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      VTMainConsole.bell();
      connection.getResultWriter().write("\nVT>Beep played on server!\nVT>");
      connection.getResultWriter().flush();
    }
    else if (parsed.length >= 3)
    {
      SourceDataLine sdl = null;
      if (parsed.length >= 4)
      {
        try
        {
          int mixerIndex = Integer.parseInt(parsed[3]);
          sdl = VTAudioBeeper.openSourceDataLine(SAMPLE_RATE_HERTZ, SAMPLE_SIZE_BITS, AudioSystem.getMixerInfo()[mixerIndex]);
        }
        catch (Throwable t)
        {
          
        }
      }
      
      try
      {
        if (sdl == null)
        {
          if (VTMainNativeUtils.beep(Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]), false, session.getExecutorService()))
          {
            connection.getResultWriter().write("\nVT>Beep is playing on server!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Beep is not playing on server!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          if (VTAudioBeeper.beep(SAMPLE_RATE_HERTZ, SAMPLE_SIZE_BITS, Integer.parseInt(parsed[1]), Integer.parseInt(parsed[2]), false, sdl, session.getExecutorService()))
          {
            connection.getResultWriter().write("\nVT>Beep is playing on server!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Beep is not playing on server!\nVT>");
            connection.getResultWriter().flush();
          }
        }
      }
      catch (NumberFormatException e)
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
      catch (Throwable e)
      {
        connection.getResultWriter().write("\nVT>Beep is not playing on server!\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else
    {
      connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  public boolean remote()
  {
    return false;
  }
}
