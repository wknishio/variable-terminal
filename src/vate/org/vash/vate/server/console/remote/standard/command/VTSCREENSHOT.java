package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSCREENSHOT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSCREENSHOT()
  {
    this.setFullName("*VTSCREENSHOT");
    this.setAbbreviatedName("*VTSS");
    this.setFullSyntax("*VTSCREENSHOT [MODE] [DISPLAY]");
    this.setAbbreviatedSyntax("*VTSS [MD] [DP]");
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
          session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216);
          session.getScreenshotTask().setUseJPG(false);
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
          session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216);
          session.getScreenshotTask().setUseJPG(false);
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
            if (parsed[i].toUpperCase().contains("C"))
            {
              session.getScreenshotTask().setDrawPointer(true);
            }
            if (parsed[i].toUpperCase().contains("O"))
            {
              session.getScreenshotTask().setDrawPointer(false);
            }
            if (parsed[i].toUpperCase().contains("P"))
            {
              session.getScreenshotTask().setUseJPG(false);
            }
            if (parsed[i].toUpperCase().contains("J"))
            {
              session.getScreenshotTask().setUseJPG(true);
            }
            if (parsed[i].toUpperCase().contains("T"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16777216);
            }
            if (parsed[i].toUpperCase().contains("V"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_2097152);
            }
            if (parsed[i].toUpperCase().contains("A"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_262144);
            }
            if (parsed[i].toUpperCase().contains("H"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_32768);
            }
            if (parsed[i].toUpperCase().contains("E"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4096);
            }
            if (parsed[i].toUpperCase().contains("N"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_512);
            }
            if (parsed[i].toUpperCase().contains("M"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_216);
            }
            if (parsed[i].toUpperCase().contains("S"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_125);
            }
            if (parsed[i].toUpperCase().contains("F"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_64);
            }
            if (parsed[i].toUpperCase().contains("L"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_27);
            }
            if (parsed[i].toUpperCase().contains("G"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_16);
            }
            if (parsed[i].toUpperCase().contains("D"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_8);
            }
            if (parsed[i].toUpperCase().contains("W"))
            {
              session.getScreenshotTask().setColorQuality(VTAWTScreenCaptureProvider.VT_COLOR_QUALITY_4);
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
  
  public boolean remote()
  {
    return false;
  }
}
