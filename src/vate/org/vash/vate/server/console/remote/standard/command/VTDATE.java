package org.vash.vate.server.console.remote.standard.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDATE extends VTServerStandardRemoteConsoleCommandProcessor
{
  private DateFormat firstDateTimeFormat = new SimpleDateFormat("G", Locale.ENGLISH);
  private DateFormat secondDateTimeFormat = new SimpleDateFormat("MM-dd][HH:mm:ss:SSS-z]");
  private GregorianCalendar clock = new GregorianCalendar();
  
  public VTDATE()
  {
    this.setFullName("*VTDATE");
    this.setAbbreviatedName("*VTDA");
    this.setFullSyntax("*VTDATE");
    this.setAbbreviatedSyntax("*VTDA");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    clock.setTime(Calendar.getInstance().getTime());
    connection.getResultWriter().write("\rVT>Current server date/time ([ER-Y-MM-DD][HH:MM:SS:MS-TZ]):\nVT>[" + firstDateTimeFormat.format(clock.getTime()) + "-" + clock.get(GregorianCalendar.YEAR) + "-" + secondDateTimeFormat.format(clock.getTime()) + "\nVT>");
    connection.getResultWriter().flush();
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
