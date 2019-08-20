package org.vate.client.console.remote.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;
import org.vate.help.VTHelpManager;

public class VTAUDIOMIXERS extends VTClientStandardRemoteConsoleCommandProcessor
{
	public VTAUDIOMIXERS()
	{
		this.setFullName("*VTAUDIOMIXERS");
		this.setAbbreviatedName("*VTAM");
		this.setFullSyntax("*VTAUDIOMIXERS [SIDE]");
		this.setAbbreviatedSyntax("*VTAM [SD]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		if (parsed.length >= 2)
		{
			if (parsed[1].toUpperCase().startsWith("R"))
			{
				connection.getCommandWriter().write(command + "\n");
				connection.getCommandWriter().flush();
			}
			else if (parsed[1].toUpperCase().startsWith("L"))
			{
				message.setLength(0);
				message.append("\nVT>List of local audio mixers:\nVT>");
				Mixer.Info[] mixers = AudioSystem.getMixerInfo();
				int number = 0;
				for (Mixer.Info info : mixers)
				{
					message.append("\nVT>Number: [" + number++ + "]");
					message.append("\nVT>Name: [" + info.getName() + "]");
					message.append("\nVT>Description: [" + info.getDescription() + "]");
					message.append("\nVT>");
				}
				message.append("\nVT>End of local audio mixers list\nVT>");
				VTConsole.print(message.toString());
			}
			else
			{
				VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
			}
		}
		else
		{
			message.setLength(0);
			message.append("\nVT>List of local audio mixers:\nVT>");
			Mixer.Info[] mixers = AudioSystem.getMixerInfo();
			int number = 0;
			for (Mixer.Info info : mixers)
			{
				message.append("\nVT>Number: [" + number++ + "]");
				message.append("\nVT>Name: [" + info.getName() + "]");
				message.append("\nVT>Description: [" + info.getDescription() + "]");
				message.append("\nVT>");
			}
			message.append("\nVT>End of local audio mixers list\nVT>");
			VTConsole.print(message.toString());
			connection.getCommandWriter().write(command + "\n");
			connection.getCommandWriter().flush();
		}
	}
	
	public void close()
	{
		
	}
}
