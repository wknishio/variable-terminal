package org.vash.vate.server.print;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerPrintServiceResolver extends VTTask
{
  private boolean finished;
  private int number = -1;
  private VTServerSession session;
  private Set<String> mimeSet = new LinkedHashSet<String>();
  
  public VTServerPrintServiceResolver(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.finished = true;
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public void setNumber(int number)
  {
    this.number = number;
  }
  
  public void task()
  {
    try
    {
      StringBuilder message = new StringBuilder();
      PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
      PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
      
      if (number < 0)
      {
        if (printServices.length > 0)
        {
          int i = 0;
          message.append("\rVT>List of server print services:\nVT>");
          for (PrintService printService : printServices)
          {
            message.append("\nVT>Number: [" + i++ + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
            message.append("\nVT>");
          }
          message.append("\nVT>End of server print services list\nVT>");
          synchronized (this)
          {
            session.getConnection().getResultWriter().write(message.toString());
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        else
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>No print services found on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      else
      {
        if (printServices.length > 0)
        {
          message.append("\rVT>List of server print service details:\nVT>");
          PrintService printService = printServices[number];
          mimeSet.clear();
          message.append("\nVT>Number: [" + number + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
          for (DocFlavor flavor : printService.getSupportedDocFlavors())
          {
            mimeSet.add(flavor.getMimeType());
          }
          if (mimeSet.size() > 0)
          {
            message.append("\nVT>Supported MIME types:");
          }
          for (String mime : mimeSet)
          {
            message.append("\nVT>MIME: [" + mime + "]");
          }
          message.append("\nVT>\nVT>End of print service details\nVT>");
          synchronized (this)
          {
            session.getConnection().getResultWriter().write(message.toString());
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        else
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>No print services found on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        
      }
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\rVT>Print service [" + number + "] not found on server!\nVT>");
          session.getConnection().getResultWriter().flush();
        }
        catch (Throwable t)
        {
          
        }
        finished = true;
      }
    }
    catch (Throwable e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\rVT>No print services found on server!\nVT>");
          session.getConnection().getResultWriter().flush();
        }
        catch (Throwable t)
        {
          
        }
        finished = true;
      }
    }
    finished = true;
  }
}
