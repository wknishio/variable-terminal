package org.vate.server.print;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerPrintServiceResolver extends VTTask
{
  private volatile boolean finished;
  private int order = -1;
  private VTServerSession session;
  private Set<String> mimeSet = new LinkedHashSet<String>();

  public VTServerPrintServiceResolver(VTServerSession session)
  {
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

  public void setOrder(int order)
  {
    this.order = order;
  }

  public void run()
  {
    try
    {
      StringBuilder message = new StringBuilder();
      PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
      PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

      if (order < 0)
      {
        if (printServices.length > 0)
        {
          int i = 0;
          message.append("\nVT>List of print services on server:\nVT>");
          for (PrintService printService : printServices)
          {
            message.append("\nVT>Number: [" + i++ + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
            message.append("\nVT>");
          }
          message.append("\nVT>End of print services list\nVT>");
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
            session.getConnection().getResultWriter().write("\nVT>No print services found on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      else
      {
        if (printServices.length > 0)
        {
          message.append("\nVT>Print service details on server:\nVT>");
          PrintService printService = printServices[order];
          mimeSet.clear();
          message.append("\nVT>Number: [" + order + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
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
            session.getConnection().getResultWriter().write("\nVT>No print services found on server!\nVT>");
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
          session.getConnection().getResultWriter().write("\nVT>Print service [" + order + "] not found on server!\nVT>");
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

    }
    finished = true;
  }
}
