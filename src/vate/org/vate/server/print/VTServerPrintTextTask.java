package org.vate.server.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
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

import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerPrintTextTask extends VTTask
{
	private volatile boolean finished;
	private String text;
	private Integer printServiceNumber;
	private DocPrintJob docPrintJob;
	private VTServerSession session;
	
	public VTServerPrintTextTask(VTServerSession session)
	{
		this.session = session;
		this.finished = true;
	}
	
	public void setText(String text)
	{
		this.text = text;
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
	
	public void run()
	{
		try
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
				}
				
				if (printService != null)
				{
					/* for (DocFlavor flavor :
					 * printService.getSupportedDocFlavors()) {
					 * System.out.println(flavor.getMimeType()); } */
					DocFlavor docFlavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
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
									session.getConnection().getResultWriter().write("\nVT>Print job of text [" + text + "] canceled!" + "\nVT>");
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
									session.getConnection().getResultWriter().write("\nVT>Print job of text [" + text + "] completed!" + "\nVT>");
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
									session.getConnection().getResultWriter().write("\nVT>Print job of text [" + text + "] failed!" + "\nVT>");
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
									session.getConnection().getResultWriter().write("\nVT>Print job of text [" + text + "] completed!" + "\nVT>");
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
					// Doc doc = new SimpleDoc(new FileInputStream(text),
					// docFlavor, docAttributes);
					Printable printable = new Printable()
					{
						public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
						{
							if (pageIndex > 0)
							{
								return NO_SUCH_PAGE;
							}
							Graphics2D g2d = (Graphics2D) graphics;
							g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
							g2d.setPaint(Color.black);
							Font font = new Font("Monospaced", Font.BOLD, 24);
							g2d.setFont(font);
							FontMetrics fontMetrics = g2d.getFontMetrics();
							float x = 0;
							float y = 0;
							int z = 0;
							String[] words = text.split("\\s+");
							
							for (String word : words)
							{
								x = (float) ((pageFormat.getImageableWidth() / 2) - (fontMetrics.stringWidth(word) / 2));
								y = (++z) * fontMetrics.getHeight();
								g2d.drawString(word, x, y);
							}
							
							return PAGE_EXISTS;
						}
					};
					Doc doc = new SimpleDoc(printable, docFlavor, docAttributes);
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
			}
			catch (PrintException e)
			{
				/* synchronized (this) {
				 * session.getConnection().getResultWriter().
				 * write("\nVT>Print job failed!" + "\nVT>");
				 * session.getConnection().getResultWriter().flush(); finished =
				 * true; } */
			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		
	}
	
	/* public static void main(String[] args) { VTServerPrintTextTask task =
	 * new VTServerPrintTextTask(null); task.setText("BUILD.XML"); task.run();
	 * } */
}