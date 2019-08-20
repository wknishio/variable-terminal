package org.vate.server.console.local.standard.command;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.vate.console.VTConsole;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTAUDIOMIXERS extends VTServerStandardLocalConsoleCommandProcessor
{
	public VTAUDIOMIXERS()
	{
		this.setFullName("*VTAUDIOMIXERS");
		this.setAbbreviatedName("*VTAM");
		this.setFullSyntax("*VTAUDIOMIXERS");
		this.setAbbreviatedSyntax("*VTAM");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		message.setLength(0);
		message.append("\nVT>List of local audio mixers:\nVT>");
		Mixer.Info[] mixers = AudioSystem.getMixerInfo();
		int number = 0;
		for (Mixer.Info info : mixers)
		{
			// Mixer mixer = AudioSystem.getMixer(info);
			message.append("\nVT>Number: [" + number++ + "]");
			message.append("\nVT>Name: [" + info.getName() + "]");
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
		message.append("\nVT>End of local audio mixers list\nVT>");
		VTConsole.print(message.toString());
	}

	public void close()
	{
		
	}
}
