package org.vash.vate.client.console.remote.standard.command;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.vash.vate.VTSystem;
import org.vash.vate.audio.VTAudioSystem;
import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;

public class VTAUDIOLINK extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTAUDIOLINK()
  {
    this.setFullName("*VTAUDIOLINK");
    this.setAbbreviatedName("*VTAL");
    this.setFullSyntax("*VTAUDIOLINK [MODE] [SIDE/TYPE/MIXER] [.]");
    this.setAbbreviatedSyntax("*VTAL [MD] [SD/TP/MX] [.]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    try
    {
      VTAudioSystem formatAudioSystem = session.getClient().getAudioSystem();
      if (formatAudioSystem.isRunning())
      {
        connection.closeAudioStreams();
        formatAudioSystem.stop();
        VTMainConsole.print("\rVT>Remote audio link stopped!\nVT>");
      }
      else
      {
        int currentAudioCodec = VTSystem.VT_AUDIO_CODEC_DEFAULT;
        AudioFormat currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_DEFAULT;
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
                    
                    VTMainConsole.print("\rVT>Invalid client input audio mixer number [" + mixer + "]!");
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
                    VTMainConsole.print("\rVT>Invalid client output audio mixer number [" + mixer + "]!");
                    // ok = false;
                  }
                }
                else
                {
                  
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
                    
                  }
                }
                else
                {
                  
                }
              }
              else
              {
                
              }
            }
            else
            {
              // detect narrowband or wideband or superwideband or fullband
              if (parsed[i].toUpperCase().contains("N"))
              {
                currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_8000;
              }
              if (parsed[i].toUpperCase().contains("W"))
              {
                currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_16000;
              }
              if (parsed[i].toUpperCase().contains("H"))
              {
                currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_24000;
              }
              if (parsed[i].toUpperCase().contains("F"))
              {
                currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_48000;
              }
              if (parsed[i].toUpperCase().contains("S"))
              {
                currentAudioCodec = VTSystem.VT_AUDIO_CODEC_SPEEX;
              }
              if (parsed[i].toUpperCase().contains("O"))
              {
                currentAudioCodec = VTSystem.VT_AUDIO_CODEC_OPUS;
              }
            }
          }
        }
        
        if (currentAudioCodec == VTSystem.VT_AUDIO_CODEC_SPEEX)
        {
          if (currentAudioFormat == VTSystem.VT_AUDIO_FORMAT_48000 || currentAudioFormat == VTSystem.VT_AUDIO_FORMAT_24000)
          {
            currentAudioFormat = VTSystem.VT_AUDIO_FORMAT_32000;
          }
        }
        
        boolean ok = formatAudioSystem.initialize(currentAudioFormat);
        if (!ok)
        {
          connection.closeAudioStreams();
          formatAudioSystem.stop();
          VTMainConsole.print("\rVT>Remote audio link start on client failed!\nVT>");
          return;
        }
        
        TargetDataLine target = null;
        SourceDataLine source = null;
        ok = (target = formatAudioSystem.searchTargetDataLine(currentAudioFormat, inputMixer, VTSystem.VT_AUDIO_LINE_CAPTURE_BUFFER_MILLISECONDS)) != null && (source = formatAudioSystem.searchSourceDataLine(currentAudioFormat, outputMixer, VTSystem.VT_AUDIO_LINE_PLAYBACK_BUFFER_MILLISECONDS)) != null && formatAudioSystem.addAudioCapture(connection.getAudioDataOutputStream(), inputMixer, target, currentAudioCodec, VTSystem.VT_AUDIO_CODEC_FRAME_MILLISECONDS) && formatAudioSystem.addAudioPlay(connection.getAudioDataInputStream(), outputMixer, source, currentAudioCodec, VTSystem.VT_AUDIO_CODEC_FRAME_MILLISECONDS);
        
        if (!ok)
        {
          if (target != null)
          {
            target.close();
          }
          if (source != null)
          {
            source.close();
          }
          connection.closeAudioStreams();
          formatAudioSystem.stop();
          VTMainConsole.print("\rVT>Remote audio link start on client failed!\nVT>");
          return;
        }
        
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
        connection.resetAudioStreams();
        if (connection.getAudioDataInputStream().read() == 1)
        {
          formatAudioSystem.start();
        }
        else
        {
          connection.closeAudioStreams();
          formatAudioSystem.stop();
        }
      }
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
      throw new Exception(t);
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
