package org.vash.vate.server.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

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
//import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerPrintDataTask extends VTTask
{
  public static int MODE_TEXT = 1;
  public static int MODE_FILE = 2;
  
  public static int FILE_ENCODING_AUTOSENSE = 1;
  public static int FILE_ENCODING_TEXT_NATIVE = 2;
  public static int FILE_ENCODING_TEXT_UTF8 = 3;
  
  private boolean finished;
  private int mode = MODE_TEXT;
  private int encoding = FILE_ENCODING_AUTOSENSE;
  private String data;
  private Integer printServiceNumber;
  private DocPrintJob docPrintJob;
  private VTServerSession session;
  
  public VTServerPrintDataTask(VTServerSession session)
  {
    this.session = session;
    this.finished = true;
  }
  
  public void setMode(int mode)
  {
    this.mode = mode;
  }
  
  public void setFileEncoding(String encoding)
  {
    if (encoding.toUpperCase().startsWith("U"))
    {
      this.encoding = FILE_ENCODING_TEXT_UTF8;
    }
    else if (encoding.toUpperCase().startsWith("N"))
    {
      this.encoding = FILE_ENCODING_TEXT_NATIVE;
    }
    else
    {
      this.encoding = FILE_ENCODING_AUTOSENSE;
    }
  }
  
  public void setData(String data)
  {
    this.data = data;
  }
  
  public void setPrintServiceNumber(Integer printServiceNumber)
  {
    this.printServiceNumber = printServiceNumber;
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
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  private static PageFormat getMinimumMarginPageFormat(PrinterJob printJob)
  {
    PageFormat pf0 = printJob.defaultPage();
    PageFormat pf1 = (PageFormat) pf0.clone();
    Paper p = pf0.getPaper();
    p.setImageableArea(0, 0, pf0.getWidth(), pf0.getHeight());
    pf1.setPaper(p);
    PageFormat pf2 = printJob.validatePage(pf1);
    return pf2;
  }
  
  public void run()
  {
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
        if (printService == null)
        {
          session.getConnection().getResultWriter().write("\nVT>No print services found on server!\nVT>");
          session.getConnection().getResultWriter().flush();
          finished = true;
          return;
        }
      }
      
      PrinterJob printerJob = PrinterJob.getPrinterJob();
      printerJob.setPrintService(printService);
      final PageFormat minimum = getMinimumMarginPageFormat(printerJob);
      printerJob.cancel();
      
      DocAttributeSet docAttributes = new HashDocAttributeSet();
      PrintRequestAttributeSet printRequestAttributes = new HashPrintRequestAttributeSet();
      // printRequestAttributes.add(new Copies(1));
      DocFlavor docFlavor = null;
      
      if (printService != null)
      {
        docPrintJob = printService.createPrintJob();
        docPrintJob.addPrintJobListener(new PrintJobListener()
        {
          public void printDataTransferCompleted(PrintJobEvent pje)
          {
            
          }
          
          public void printJobCanceled(PrintJobEvent pje)
          {
            try
            {
              synchronized (this)
              {
                if (finished)
                {
                  return;
                }
                finished = true;
                session.getConnection().getResultWriter().write("\nVT>Print job of data [" + data + "] canceled!" + "\nVT>");
                session.getConnection().getResultWriter().flush();
              }
            }
            catch (IOException e)
            {
              
            }
          }
          
          public void printJobCompleted(PrintJobEvent pje)
          {
            try
            {
              synchronized (this)
              {
                if (finished)
                {
                  return;
                }
                finished = true;
                session.getConnection().getResultWriter().write("\nVT>Print job of data [" + data + "] completed!" + "\nVT>");
                session.getConnection().getResultWriter().flush();
              }
            }
            catch (IOException e)
            {
              
            }
          }
          
          public void printJobFailed(PrintJobEvent pje)
          {
            // System.out.println("Print job failed");
            try
            {
              synchronized (this)
              {
                if (finished)
                {
                  return;
                }
                finished = true;
                session.getConnection().getResultWriter().write("\nVT>Print job of data [" + data + "] failed!" + "\nVT>");
                session.getConnection().getResultWriter().flush();
              }
            }
            catch (IOException e)
            {
              
            }
          }
          
          public void printJobNoMoreEvents(PrintJobEvent pje)
          {
            try
            {
              synchronized (this)
              {
                if (finished)
                {
                  return;
                }
                finished = true;
                session.getConnection().getResultWriter().write("\nVT>Print job of data [" + data + "] completed!" + "\nVT>");
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
      }
      else
      {
        synchronized (this)
        {
          session.getConnection().getResultWriter().write("\nVT>Print service number [" + printServiceNumber + "] not found!" + "\nVT>");
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
        return;
      }
      
      if (mode == MODE_TEXT)
      {
        /*
         * for (DocFlavor flavor : printService.getSupportedDocFlavors()) {
         * System.out.println(flavor.getMimeType()); }
         */
        docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
//      docFlavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8;
        // printRequestAttributes.add(OrientationRequested.PORTRAIT);
        
        String[] words = data.split("\\s+");
        ArrayList<String> printLines = new ArrayList<String>();
        StringBuilder lineBuilder = new StringBuilder();
        int limit = 80;
        
        for (String word : words)
        {
          if (lineBuilder.length() + word.length() > limit)
          {
            printLines.add(lineBuilder.toString());
            lineBuilder.setLength(0);
            lineBuilder.append(word);
          }
          else
          {
            if (lineBuilder.length() > 0)
            {
              lineBuilder.append(" ");
            }
            lineBuilder.append(word);
          }
        }
        
        if (lineBuilder.length() > 0)
        {
          printLines.add(lineBuilder.toString());
          lineBuilder.setLength(0);
        }
        
        // final Iterator<String> printLineIterator1 = printLines.iterator();
        // final Iterator<String> printLineIterator2 = printLines.iterator();
        final String[] printLinesArray = printLines.toArray(new String[printLines.size()]);
        
        Printable printable = new Printable()
        {
          private int printLinesIndex = 0;
          
          public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
          {
            // System.out.println("pageIndex=" + pageIndex);
            if (pageIndex > 0)
            {
              if (printLinesIndex >= printLinesArray.length)
              {
                // System.out.println("NO_SUCH_PAGE");
                return NO_SUCH_PAGE;
              }
            }
            else
            {
              printLinesIndex = 0;
            }
            // minimum margin code
            // Paper paper = pageFormat.getPaper();
            // paper.setImageableArea(minimum.getImageableX(),
            // minimum.getImageableY(),
            // minimum.getImageableWidth(), minimum.getImageableHeight());
            // pageFormat.setPaper(paper);
            
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.setPaint(Color.black);
            Font font = new Font("Monospaced", Font.BOLD, 10);
            g2d.setFont(font);
            FontMetrics fontMetrics = g2d.getFontMetrics();
            float x = 0;
            float y = 0;
            int z = 0;
            int limit = 80;
            
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < limit; i++)
            {
              lineBuilder.append("X");
            }
            String full = lineBuilder.toString();
            lineBuilder.setLength(0);
            
            if (fontMetrics.stringWidth(full) >= pageFormat.getImageableWidth())
            {
              // page cannot handle 80 characters, will use 40
              limit = 40;
              for (int i = 0; i < limit; i++)
              {
                lineBuilder.append("X");
              }
              full = lineBuilder.toString();
              lineBuilder.setLength(0);
            }
            
            y = fontMetrics.getHeight();
            z = 1;
            while ((y + fontMetrics.getHeight() < pageFormat.getImageableHeight()) && printLinesIndex < printLinesArray.length)
            {
              String line = printLinesArray[printLinesIndex++];
              // System.out.println("line:[" + line + "]");
              if (line.length() <= 0)
              {
                continue;
              }
              int size = line.length();
              int start = 0;
              int end = start + Math.min(size - start, limit);
              for (; start < size && (y + fontMetrics.getHeight() < pageFormat.getImageableHeight()); start += Math.min(size - start, limit))
              {
                end = start + Math.min(size - start, limit);
                String part = line.substring(start, end);
                x = (float) ((pageFormat.getImageableWidth() / 2) - (fontMetrics.stringWidth(full) / 2));
                y = (++z) * fontMetrics.getHeight();
                g2d.drawString(part, x, y);
              }
            }
            
            // System.out.println("PAGE_EXISTS");
            return PAGE_EXISTS;
          }
        };
        
        Book book = new Book();
        book.append(printable, minimum);
        Doc doc = new SimpleDoc(book, docFlavor, docAttributes);
        docPrintJob.print(doc, printRequestAttributes);
      }
      else if (mode == MODE_FILE)
      {
        File source = new File(data);
        
        if (encoding != FILE_ENCODING_AUTOSENSE)
        {
          final FileInputStream stream = new FileInputStream(source);
          
          docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
          // printRequestAttributes.add(OrientationRequested.PORTRAIT);
          
          final ArrayList<String> fileLines = new ArrayList<String>();
          
          BufferedReader fileReader = null;
          try
          {
            if (encoding == FILE_ENCODING_TEXT_UTF8)
            {
              fileReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            }
            else
            {
              fileReader = new BufferedReader(new InputStreamReader(stream));
            }
            String fileLine = null;
            
            while ((fileLine = fileReader.readLine()) != null)
            {
              fileLines.add(fileLine);
            }
          }
          finally
          {
            try
            {
              fileReader.close();
            }
            catch (Throwable t)
            {
              
            }
          }
          
          final ArrayList<String> printLines = new ArrayList<String>();
          final StringBuilder lineBuilder = new StringBuilder();
          int limit = 80;
          
          for (String fileLine : fileLines)
          {
            String[] words = fileLine.split("\\s+");
            
            for (String word : words)
            {
              if (lineBuilder.length() + word.length() > limit)
              {
                printLines.add(lineBuilder.toString());
                lineBuilder.setLength(0);
                lineBuilder.append(word);
              }
              else
              {
                if (lineBuilder.length() > 0)
                {
                  lineBuilder.append(" ");
                }
                lineBuilder.append(word);
              }
            }
            
            if (lineBuilder.length() > 0)
            {
              printLines.add(lineBuilder.toString());
              lineBuilder.setLength(0);
            }
          }
          
          final String[] printLinesArray = printLines.toArray(new String[printLines.size()]);
          
          Printable printable = new Printable()
          {
            private int printLinesIndex = 0;
            
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
            {
              if (pageIndex > 0)
              {
                if (printLinesIndex >= printLinesArray.length)
                {
                  // System.out.println("NO_SUCH_PAGE");
                  return NO_SUCH_PAGE;
                }
              }
              else
              {
                printLinesIndex = 0;
              }
              
              // Paper paper = pageFormat.getPaper();
              // paper.setImageableArea(minimum.getImageableX(),
              // minimum.getImageableY(),
              // minimum.getImageableWidth(), minimum.getImageableHeight());
              // pageFormat.setPaper(paper);
              
              Graphics2D g2d = (Graphics2D) graphics;
              g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
              g2d.setPaint(Color.black);
              Font font = new Font("Monospaced", Font.BOLD, 10);
              g2d.setFont(font);
              FontMetrics fontMetrics = g2d.getFontMetrics();
              float x = 0;
              float y = 0;
              int z = 0;
              int limit = 80;
              
              for (int i = 0; i < limit; i++)
              {
                lineBuilder.append("X");
              }
              String full = lineBuilder.toString();
              lineBuilder.setLength(0);
              
              if (fontMetrics.stringWidth(full) >= pageFormat.getImageableWidth())
              {
                // page cannot handle 80 characters, will use 40
                limit = 40;
                for (int i = 0; i < limit; i++)
                {
                  lineBuilder.append("X");
                }
                full = lineBuilder.toString();
                lineBuilder.setLength(0);
              }
              
              y = fontMetrics.getHeight();
              z = 1;
              while ((y + fontMetrics.getHeight() < pageFormat.getImageableHeight()) && printLinesIndex < printLinesArray.length)
              {
                String printLine = printLinesArray[printLinesIndex++];
                if (printLine.length() <= 0)
                {
                  continue;
                }
                int size = printLine.length();
                int start = 0;
                int end = start + Math.min(size - start, limit);
                for (; start < size && (y + fontMetrics.getHeight() < pageFormat.getImageableHeight()); start += Math.min(size - start, limit))
                {
                  end = start + Math.min(size - start, limit);
                  String part = printLine.substring(start, end);
                  x = (float) ((pageFormat.getImageableWidth() / 2) - (fontMetrics.stringWidth(full) / 2));
                  y = (++z) * fontMetrics.getHeight();
                  g2d.drawString(part, x, y);
                }
              }
              
              return PAGE_EXISTS;
            }
          };
          Book book = new Book();
          book.append(printable, minimum);
          Doc doc = new SimpleDoc(book, docFlavor, docAttributes);
          docPrintJob.print(doc, printRequestAttributes);
        }
        else
        {
          if (isValidURL(data))
          {
            URL url = new URL(data);
            docFlavor = DocFlavor.URL.AUTOSENSE;
            Doc doc = new SimpleDoc(url, docFlavor, docAttributes);
            docPrintJob.print(doc, printRequestAttributes);
          }
          else
          {
            if (source.exists())
            {
              final FileInputStream stream = new FileInputStream(source);
              docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
              Doc doc = new SimpleDoc(stream, docFlavor, docAttributes);
              docPrintJob.print(doc, printRequestAttributes);
            }
            else
            {
              session.getConnection().getResultWriter().write("\nVT>File for printing [" + data + "] not found!" + "\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
              return;
            }
          }
        }
        // printRequestAttributes.add(MediaSize.ISO.A4);
        // Thread.sleep(30000);
      }
    }
    catch (FileNotFoundException e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>File for printing [" + data + "] not found!" + "\nVT>");
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
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>Print job of data [" + data + "] failed!" + "\nVT>");
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
        catch (Throwable e1)
        {
          // e.printStackTrace();
        }
      }
    }
  }
  
  public static boolean isValidURL(String url)
  {
    try
    {
      new URL(url).toURI();
      return true;
    }
    catch (Exception e)
    {
      return false;
    }
  }
  
  /*
   * public static void main(String[] args) { VTServerPrintTextTask task = new
   * VTServerPrintTextTask(null); task.setText("BUILD.XML"); task.run(); }
   */
}