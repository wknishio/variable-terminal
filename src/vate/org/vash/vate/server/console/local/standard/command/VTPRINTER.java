package org.vash.vate.server.console.local.standard.command;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTPRINTER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTPRINTER()
  {
    this.setFullName("*VTPRINTER");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTER");
    this.setAbbreviatedSyntax("*VTPR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    if (printServices.length > 0)
    {
      int i = 0;
      message.append("\rVT>List of server print services:\nVT>");
      for (PrintService printService : printServices)
      {
        // message.append("\nVT>Name: [" +
        // printService.getName() + "]");
        message.append("\nVT>Number: [" + i++ + "]" + (defaultPrintService.getName().equals(printService.getName()) ? " (Default)" : "") + "\nVT>Name: [" + printService.getName() + "]");
        /*
         * for (DocFlavor flavor : printService.getSupportedDocFlavors()) {
         * message.append("\nVT>Flavor: " + flavor.getMimeType()); }
         */
        message.append("\nVT>");
      }
      message.append("\nVT>End of server print services list\nVT>");
      
      VTConsole.print(message.toString());
    }
    else
    {
      VTConsole.print("\rVT>No print services found on server!\nVT>");
    }
  }
  
  public void close()
  {
    
  }
}
