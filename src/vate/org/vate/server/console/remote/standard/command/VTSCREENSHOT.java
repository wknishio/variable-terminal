package org.vate.server.console.remote.standard.command;

import org.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENSHOT extends VTServerStandardRemoteConsoleCommandProcessor
{
	public VTSCREENSHOT()
	{
		this.setFullName("*VTSCREENSHOT");
		this.setAbbreviatedName("*VTSCS");
		this.setFullSyntax("*VTSCREENSHOT [MODE] [DISPLAY]");
		this.setAbbreviatedSyntax("*VTSCS [MD] [DP]");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		synchronized (session.getScreenshotTask())
		{
			// connection.getResultWriter().write(command);
			// connection.getResultWriter().flush();
			if (parsed.length == 1)
			{
				if (session.getScreenshotTask().isFinished())
				{
					session.getScreenshotTask().joinThread();
				}
				if (!session.getScreenshotTask().aliveThread())
				{
					session.getScreenshotTask().setFinished(false);
					session.getScreenshotTask().setDrawPointer(false);
					session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
					session.getScreenshotTask().setDeviceNumber(-1);
					session.getScreenshotTask().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another screen capture is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
			else if (parsed.length >= 2)
			{
				if (session.getScreenshotTask().isFinished())
				{
					session.getScreenshotTask().joinThread();
				}
				if (!session.getScreenshotTask().aliveThread())
				{
					session.getScreenshotTask().setFinished(false);
					session.getScreenshotTask().setDrawPointer(false);
					session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
					session.getScreenshotTask().setDeviceNumber(-1);
					for (int i = 1; i < parsed.length; i++)
					{
						try
						{
							session.getScreenshotTask().setDeviceNumber(Integer.parseInt(parsed[i]));
						}
						catch (Throwable t)
						{
							
						}
						if (parsed[i].toUpperCase().contains("S"))
						{
							session.getScreenshotTask().setDrawPointer(true);
						}
						if (parsed[i].toUpperCase().contains("C"))
						{
							session.getScreenshotTask().setDrawPointer(false);
						}
						if (parsed[i].toUpperCase().contains("B"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_BEST);
						}
						if (parsed[i].toUpperCase().contains("H"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_HIGH);
						}
						if (parsed[i].toUpperCase().contains("G"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_GOOD);
						}
						if (parsed[i].toUpperCase().contains("M"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_MEDIUM);
						}
						if (parsed[i].toUpperCase().contains("N"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_LOW);
						}
						if (parsed[i].toUpperCase().contains("L"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_SMALL);
						}
						if (parsed[i].toUpperCase().contains("W"))
						{
							session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_WORST);
						}
					}
					session.getScreenshotTask().startThread();
				}
				else
				{
					connection.getResultWriter().write("\nVT>Another screen capture is still running!\nVT>");
					connection.getResultWriter().flush();
				}
			}
		}
	}

	public void close()
	{
		
	}
}
