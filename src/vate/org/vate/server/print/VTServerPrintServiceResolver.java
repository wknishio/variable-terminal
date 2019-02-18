package org.vate.server.print;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerPrintServiceResolver extends VTTask
{
	private volatile boolean finished;
	private VTServerSession session;
	
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
	
	public void run()
	{
		try
		{
			StringBuilder message = new StringBuilder();
			PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
			PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
			
			if (printServices.length > 0)
			{
				int i = 0;
				message.append("\nVT>List of print services on server:\nVT>");
				for (PrintService printService : printServices)
				{
					message.append("\nVT>Number: [" + i++ + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
//					for (DocFlavor flavor : printService.getSupportedDocFlavors())
//					{
//						message.append("\nVT>MIME:[" + flavor.getMimeType() + "]");
//						message.append("\nVT>MediaType:[" + flavor.getMediaType() + "]");
//						message.append("\nVT>MediaSubtype:[" + flavor.getMediaSubtype() + "]");
//						message.append("\nVT>Class:[" + flavor.getRepresentationClassName() + "]");
//					}
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
		catch (Throwable e)
		{
			
		}
		finished = true;
	}
}
