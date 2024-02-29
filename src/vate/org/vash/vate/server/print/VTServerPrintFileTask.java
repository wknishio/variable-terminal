package org.vash.vate.server.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.print.CancelablePrintJob;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerPrintFileTask extends VTTask
{
  private boolean finished;
  private String file;
  private Integer printServiceNumber;
  private DocPrintJob docPrintJob;
  private VTServerSession session;
  
  public VTServerPrintFileTask(VTServerSession session)
  {
    this.session = session;
    this.finished = true;
  }
  
  public void setFile(String file)
  {
    this.file = file;
  }
  
  public void setStopped(boolean stopped)
  {
    if (stopped && !finished)
    {
      if (docPrintJob != null)
      {
        if (docPrintJob instanceof CancelablePrintJob)
        {
          CancelablePrintJob cancelable = (CancelablePrintJob) docPrintJob;
          try
          {
            cancelable.cancel();
          }
          catch (PrintException e)
          {
            
          }
        }
      }
    }
  }
  
  public void setPrintServiceNumber(Integer printServiceNumber)
  {
    this.printServiceNumber = printServiceNumber;
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
      File source = new File(file);
      if (!source.isAbsolute())
      {
        source = new File(file);
      }
      
      final FileInputStream stream = new FileInputStream(source);
      
      try
      {
        PrintService printService = null;
        
        if (printServiceNumber != null)
        {
          PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
          printService = printServices[printServiceNumber];
        }
        else
        {
          printService = PrintServiceLookup.lookupDefaultPrintService();
        }
        
        if (printService != null)
        {
          DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
          docPrintJob = printService.createPrintJob();
          docPrintJob.addPrintJobListener(new PrintJobListener()
          {
            public void printDataTransferCompleted(PrintJobEvent pje)
            {
              // System.out.println("printDataTransferCompleted");
              if (stream != null)
              {
                try
                {
                  stream.close();
                }
                catch (IOException e)
                {
                  
                }
              }
            }
            
            public void printJobCanceled(PrintJobEvent pje)
            {
              if (stream != null)
              {
                try
                {
                  stream.close();
                }
                catch (IOException e)
                {
                  
                }
              }
              try
              {
                synchronized (this)
                {
                  if (finished)
                  {
                    return;
                  }
                  finished = true;
                  session.getConnection().getResultWriter().write("\nVT>Print job of file [" + file + "] canceled!" + "\nVT>");
                  session.getConnection().getResultWriter().flush();
                }
              }
              catch (IOException e)
              {
                
              }
            }
            
            public void printJobCompleted(PrintJobEvent pje)
            {
              if (stream != null)
              {
                try
                {
                  stream.close();
                }
                catch (IOException e)
                {
                  
                }
              }
              try
              {
                synchronized (this)
                {
                  if (finished)
                  {
                    return;
                  }
                  finished = true;
                  session.getConnection().getResultWriter().write("\nVT>Print job of file [" + file + "] completed!" + "\nVT>");
                  session.getConnection().getResultWriter().flush();
                }
              }
              catch (IOException e)
              {
                
              }
            }
            
            public void printJobFailed(PrintJobEvent pje)
            {
              if (stream != null)
              {
                try
                {
                  stream.close();
                }
                catch (IOException e)
                {
                  
                }
              }
              try
              {
                synchronized (this)
                {
                  if (finished)
                  {
                    return;
                  }
                  finished = true;
                  session.getConnection().getResultWriter().write("\nVT>Print job of file [" + file + "] failed!" + "\nVT>");
                  session.getConnection().getResultWriter().flush();
                }
              }
              catch (IOException e)
              {
                
              }
            }
            
            public void printJobNoMoreEvents(PrintJobEvent pje)
            {
              // System.out.println("printJobNoMoreEvents");
              if (stream != null)
              {
                try
                {
                  stream.close();
                }
                catch (IOException e)
                {
                  
                }
              }
              try
              {
                synchronized (this)
                {
                  if (finished)
                  {
                    return;
                  }
                  finished = true;
                  session.getConnection().getResultWriter().write("\nVT>Print job of file [" + file + "] completed!" + "\nVT>");
                  session.getConnection().getResultWriter().flush();
                }
              }
              catch (IOException e)
              {
                
              }
            }
            
            public void printJobRequiresAttention(PrintJobEvent pje)
            {
              DocPrintJob job = pje.getPrintJob();
              if (job instanceof CancelablePrintJob)
              {
                CancelablePrintJob cancelable = (CancelablePrintJob) job;
                try
                {
                  cancelable.cancel();
                }
                catch (PrintException e)
                {
                  
                }
              }
            }
          });
          DocAttributeSet docAttributes = new HashDocAttributeSet();
          PrintRequestAttributeSet printRequestAttributes = new HashPrintRequestAttributeSet();
          printRequestAttributes.add(new Copies(1));
          // printRequestAttributes.add(MediaSize.ISO.A4);
          
          Doc doc = new SimpleDoc(stream, docFlavor, docAttributes);
          
          // Doc doc = new SimpleDoc(printable, docFlavor,
          // docAttributes);
          docPrintJob.print(doc, printRequestAttributes);
          // Thread.sleep(30000);
        }
        else
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Print service number [" + printServiceNumber + "] not found!" + "\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
        synchronized (this)
        {
          session.getConnection().getResultWriter().write("\nVT>Print service number [" + printServiceNumber + "] not found!" + "\nVT>");
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
        if (stream != null)
        {
          stream.close();
        }
      }
      /*
       * catch (PrintException e) { synchronized (this) {
       * session.getConnection().getResultWriter().
       * write("\nVT>Print job failed!" + "\nVT>");
       * session.getConnection().getResultWriter().flush(); finished = true; }
       * if (stream != null) { stream.close(); } }
       */
    }
    catch (FileNotFoundException e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>File for print [" + file + "] not found!" + "\nVT>");
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
        catch (Throwable e1)
        {
          // e.printStackTrace();
        }
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  /*
   * public static void main(String[] args) { VTServerPrintFileTask task = new
   * VTServerPrintFileTask(null); task.setFile("build.xml"); task.run(); }
   */
}