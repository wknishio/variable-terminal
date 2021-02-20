package org.vate.client.console.remote.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.VT;
import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTAUDIOLINK extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTAUDIOLINK()
  {
    this.setFullName("*VTAUDIOLINK");
    this.setAbbreviatedName("*VTAL");
    this.setFullSyntax("*VTAUDIOLINK [SIDE/TYPE/MIXER] [.]");
    this.setAbbreviatedSyntax("*VTAL [SD/TP/MX] [.]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (session.isRunningAudio())
    {
      // session.getClient().getAudioSystem().stop();
      session.setRunningAudio(false);
      connection.closeAudioStreams();
      session.getClient().getAudioSystem().stop();
      connection.getCommandWriter().write(command + "\n");
      connection.getCommandWriter().flush();
    }
    else
    {
      // session.getClient().getAudioSystem().stop();
      Mixer.Info inputMixer = null;
      Mixer.Info outputMixer = null;
      if (parsed.length >= 2)
      {
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        for (int i = 1; i < parsed.length; i += 1)
        {
          String[] parameters = parsed[i].split("/");
          if (parameters.length >= 3)
          {
            String side = parameters[0];
            String type = parameters[1];
            String mixer = parameters[2];
            if (side.toUpperCase().startsWith("L"))
            {
              if (type.toUpperCase().startsWith("I"))
              {
                try
                {
                  int index = Integer.parseInt(mixer);
                  inputMixer = info[index];
                }
                catch (Throwable t)
                {
                  VTConsole.print("\nVT>Invalid local input audio mixer number [" + mixer + "]!");
                  // ok = false;
                }
              }
              else if (type.toUpperCase().startsWith("O"))
              {
                try
                {
                  int index = Integer.parseInt(mixer);
                  outputMixer = info[index];
                }
                catch (Throwable t)
                {
                  VTConsole.print("\nVT>Invalid local output audio mixer number [" + mixer + "]!");
                  // ok = false;
                }
              }
              else
              {
                // ok = false;
                // VTConsole.print("\nVT>Invalid
                // command syntax!" +
                // VTHelpManager.getHelpForClientCommand(splitCommand[0]));
                // break;
              }
            }
            else if (side.toUpperCase().startsWith("R"))
            {
              if (type.toUpperCase().startsWith("I"))
              {
                try
                {
                  Integer.parseInt(mixer);
                }
                catch (Throwable t)
                {
                  // VTConsole.print("\nVT>Invalid
                  // remote audio mixer number [" +
                  // mixer + "]!");
                  // ok = false;
                }
              }
              else if (type.toUpperCase().startsWith("O"))
              {
                try
                {
                  Integer.parseInt(mixer);
                }
                catch (Throwable t)
                {
                  // VTConsole.print("\nVT>Invalid
                  // remote audio mixer number [" +
                  // mixer + "]!");
                  // ok = false;
                }
              }
              else
              {
                // ok = false;
                // VTConsole.print("\nVT>Invalid
                // command syntax!" +
                // VTHelpManager.getHelpForClientCommand(splitCommand[0]));
                // break;
              }
            }
            else
            {
              // ok = false;
              // VTConsole.print("\nVT>Invalid command
              // syntax!" +
              // VTHelpManager.getHelpForClientCommand(splitCommand[0]));
              // break;
            }
          }
          else
          {
            // ok = false;
            // VTConsole.print("\nVT>Invalid command
            // syntax!" +
            // VTHelpManager.getHelpForClientCommand(splitCommand[0]));
            // break;
          }
        }
      }

      connection.resetAudioStreams();
      connection.getCommandWriter().write(command + "\n");
      connection.getCommandWriter().flush();
      session.getClient().getAudioSystem().initialize(VT.VT_AUDIO_FORMAT);
      if (connection.getAudioControlInputStream().read() == 1)
      {
        if (session.getClient().getAudioSystem().addAudioPlay(connection.getAudioDataInputStream(), outputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
        {
          connection.getAudioControlOutputStream().write(1);
          connection.getAudioControlOutputStream().flush();
          if (connection.getAudioControlInputStream().read() == 1)
          {
            if (session.getClient().getAudioSystem().addAudioCapture(connection.getAudioDataOutputStream(), inputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
            {
              connection.getAudioControlOutputStream().write(1);
              connection.getAudioControlOutputStream().flush();
              session.setRunningAudio(true);
            }
            else
            {
              connection.getAudioControlOutputStream().write(0);
              connection.getAudioControlOutputStream().flush();
              connection.closeAudioStreams();
              session.setRunningAudio(false);
              session.getClient().getAudioSystem().stop();
              // session.getClient().getAudioSystem().stop();
              VTConsole.print("\nVT>Remote audio link start on client failed!\nVT>");
            }
          }
          else
          {
            connection.closeAudioStreams();
            session.setRunningAudio(false);
            session.getClient().getAudioSystem().stop();
          }
        }
        else
        {
          connection.getAudioControlOutputStream().write(0);
          connection.getAudioControlOutputStream().flush();
          connection.closeAudioStreams();
          session.setRunningAudio(false);
          session.getClient().getAudioSystem().stop();
          // session.getClient().getAudioSystem().stop();
          VTConsole.print("\nVT>Remote audio link start on client failed!\nVT>");
        }
      }
      else
      {
        connection.closeAudioStreams();
        session.setRunningAudio(false);
        session.getClient().getAudioSystem().stop();
        // session.getClient().getAudioSystem().stop();
        // VTConsole.print("\nVT>Remote audio link start on
        // client failed!\nVT>");
      }
    }
  }

  public void close()
  {

  }
}
