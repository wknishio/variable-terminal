package org.vash.vate.shell.adapter;

import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class VTSilenceJavaUtilLogging
{
  @SuppressWarnings("all")
  public VTSilenceJavaUtilLogging()
  {
    try
    {
      System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
      LogManager logManager = java.util.logging.LogManager.getLogManager();
      Enumeration<String> loggerNames = logManager.getLoggerNames();
      java.util.logging.Logger.global.setLevel(Level.OFF);
      while (loggerNames.hasMoreElements())
      {
        String loggerName = loggerNames.nextElement();
        logManager.getLogger(loggerName).setLevel(Level.OFF);
      }
    }
    catch (Throwable t)
    {
      
    }
  }
}
