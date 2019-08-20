package org.vate.server.console.remote.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.VT;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTAUDIOLINK extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTAUDIOLINK()
	{
		this.setFullName("*VTAUDIOLINK");
		this.setAbbreviatedName("*VTAL");
		this.setFullSyntax("*VTAUDIOLINK [SIDE/TYPE/MIXER] [...]");
		this.setAbbreviatedSyntax("*VTAL [SD/TP/MX] [.]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (!session.isRunningAudio())
		{
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
								
							}
							else if (type.toUpperCase().startsWith("O"))
							{
								
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
									int index = Integer.parseInt(mixer);
									inputMixer = info[index];
								}
								catch (Throwable t)
								{
									connection.getResultWriter().write("\nVT>Invalid remote input audio mixer number [" + mixer + "]!");
									connection.getResultWriter().flush();
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
									connection.getResultWriter().write("\nVT>Invalid remote output audio mixer number [" + mixer + "]!");
									connection.getResultWriter().flush();
									// ok = false;
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
						
					}
				}
			}
			
			connection.resetAudioStreams();
			if (!session.getServer().getAudioSystem().isRunning())
			{
				session.getServer().getAudioSystem().initialize(VT.VT_AUDIO_FORMAT);
			}
			if (session.getServer().getAudioSystem().addAudioPlay(connection.getAudioDataInputStream(), outputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
			{
				connection.getAudioControlOutputStream().write(1);
				connection.getAudioControlOutputStream().flush();
				if (connection.getAudioControlInputStream().read() == 1)
				{
					if (session.getServer().getAudioSystem().addAudioCapture(connection.getAudioDataOutputStream(), inputMixer, VT.VT_AUDIO_LINE_BUFFER_MILLISECONDS, VT.VT_AUDIO_CODEC_DEFAULT, VT.VT_AUDIO_CODEC_FRAME_MILLISECONDS))
					{
						connection.getAudioControlOutputStream().write(1);
						connection.getAudioControlOutputStream().flush();
						if (connection.getAudioControlInputStream().read() == 1)
						{
							session.setRunningAudio(true);
							connection.getResultWriter().write("\nVT>Remote audio link started!\nVT>");
							connection.getResultWriter().flush();
						}
					}
					else
					{
						connection.getAudioControlOutputStream().write(0);
						connection.getAudioControlOutputStream().flush();
						connection.closeAudioStreams();
						session.setRunningAudio(false);
						connection.getResultWriter().write("\nVT>Remote audio link start on server failed!\nVT>");
						connection.getResultWriter().flush();
					}
				}
				else
				{
					connection.closeAudioStreams();
					session.setRunningAudio(false);
				}
			}
			else
			{
				connection.getAudioControlOutputStream().write(0);
				connection.getAudioControlOutputStream().flush();
				connection.closeAudioStreams();
				session.setRunningAudio(false);
				connection.getResultWriter().write("\nVT>Remote audio link start on server failed!\nVT>");
				connection.getResultWriter().flush();
			}
		}
		else
		{
			connection.closeAudioStreams();
			session.setRunningAudio(false);
			connection.getResultWriter().write("\nVT>Remote audio link stopped!\nVT>");
			connection.getResultWriter().flush();
		}
	}

	public void close()
	{
		
	}
}
