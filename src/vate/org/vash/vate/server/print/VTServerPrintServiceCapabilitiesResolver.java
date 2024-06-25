package org.vash.vate.server.print;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerPrintServiceCapabilitiesResolver extends VTTask
{
  private boolean finished;
  private int number;
  private VTServerSession session;
  
  public VTServerPrintServiceCapabilitiesResolver(VTServerSession session)
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
      
      if (printServices.length > 0)
      {
        message.append("\nVT>List of server print service capabilites:\nVT>");
        PrintService printService = printServices[number];
        message.append("\nVT>Number: [" + number + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]\nVT>");
//				for (DocFlavor flavor : printService.getSupportedDocFlavors())
//				{
//					message.append("\nVT>MIME: [" + flavor.getMimeType() + "]");
//					message.append("\nVT>MediaType: [" + flavor.getMediaType() + "]");
//					message.append("\nVT>MediaSubtype: [" + flavor.getMediaSubtype() + "]");
//					message.append("\nVT>Class: [" + flavor.getRepresentationClassName() + "]\nVT>");
//				}
        message.append("\nVT>");
        message.append("\nVT>End of server print services capabilites list\nVT>");
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
    catch (Throwable e)
    {
      
    }
    finished = true;
  }
}
