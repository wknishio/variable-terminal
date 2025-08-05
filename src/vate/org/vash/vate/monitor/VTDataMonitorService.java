package org.vash.vate.monitor;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.task.VTTask;

public class VTDataMonitorService extends VTTask implements VTNanoPingListener
{
  private volatile long currentInput;
  private volatile long currentOutput;
  private volatile long transferredInput;
  private volatile long transferredOutput;
  private volatile long lastInput;
  private volatile long lastOutput;
  private volatile long differenceInput;
  private volatile long differenceOutput;
  private volatile long monitorNanoDelay = -1;
  @SuppressWarnings("unused")
  private volatile long monitorMilliDelay = -1;
  private final ConcurrentLinkedQueue<VTDataMonitorConnection> connections = new ConcurrentLinkedQueue<VTDataMonitorConnection>();
  private final ConcurrentLinkedQueue<VTDataMonitorPanel> uploadMonitorPanels = new ConcurrentLinkedQueue<VTDataMonitorPanel>();
  private final ConcurrentLinkedQueue<VTDataMonitorPanel> downloadMonitorPanels = new ConcurrentLinkedQueue<VTDataMonitorPanel>();
  
  public VTDataMonitorService(ExecutorService executorService)
  {
    super(executorService);
  }
  
  public void ping()
  {
    try
    {
      synchronized (this)
      {
        this.notify();
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
  }
  
  public void addDataConnection(VTDataMonitorConnection connection)
  {
    connections.add(connection);
  }
  
  public void removeDataConnection(VTDataMonitorConnection connection)
  {
    connections.remove(connection);
  }
  
  public void addUploadMonitorPanel(VTDataMonitorPanel panel)
  {
    uploadMonitorPanels.add(panel);
  }
  
  public void removeUploadMonitorPanel(VTDataMonitorPanel panel)
  {
    uploadMonitorPanels.remove(panel);
  }
  
  public void addDownloadMonitorPanel(VTDataMonitorPanel panel)
  {
    downloadMonitorPanels.add(panel);
  }
  
  public void removeDownloadMonitorPanel(VTDataMonitorPanel panel)
  {
    downloadMonitorPanels.remove(panel);
  }
  
  public void task()
  {
    try
    {
      cycle();
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
  }
  
  public void cycle() throws InterruptedException
  {
    while (!isStopped())
    {
      synchronized (this)
      {
        wait(1000);
      }
      if (uploadMonitorPanels.size() <= 0 || downloadMonitorPanels.size() <= 0 || isStopped())
      {
        continue;
      }
      currentInput = 0;
      currentOutput = 0;
      for (VTDataMonitorConnection connection : connections)
      {
        transferredInput = connection.getInput().getTransferredBytes();
        transferredOutput = connection.getOutput().getTransferredBytes();
        if (transferredInput < 0 || transferredOutput < 0)
        {
          resetTransferredBytes();
          continue;
        }
        currentInput += transferredInput;
        currentOutput += transferredOutput;
        if (currentInput < 0 || currentOutput < 0)
        {
          resetTransferredBytes();
          continue;
        }
      }
      differenceInput = (currentInput - lastInput);
      differenceOutput = (currentOutput - lastOutput);
      
      if (uploadMonitorPanels.size() > 0)
      {
        String uploadMessage = "Tx: " + humanReadableByteCount(differenceOutput) + "/s";
        try
        {
          for (VTDataMonitorPanel uploadMonitorPanel : uploadMonitorPanels)
          {
            uploadMonitorPanel.setMessage(uploadMessage);
          }
        }
        catch (Throwable t)
        {
          
        }
      }
      
      if (downloadMonitorPanels != null)
      {
        String downloadMessage = "Rx: " + humanReadableByteCount(differenceInput) + "/s";
        try
        {
          for (VTDataMonitorPanel downloadMonitorPanel : downloadMonitorPanels)
          {
            downloadMonitorPanel.setMessage(downloadMessage);
          }
        }
        catch (Throwable t)
        {
          
        }
      }
      
      if (differenceInput < 0 || differenceOutput < 0)
      {
        resetTransferredBytes();
        continue;
      }
      lastInput = currentInput;
      lastOutput = currentOutput;
    }
  }
  
  public void close() throws IOException
  {
    setStopped(true);
    ping();
  }
  
  private void resetTransferredBytes()
  {
    lastInput = 0;
    lastOutput = 0;
    for (VTDataMonitorConnection reset : connections)
    {
      reset.getInput().resetTransferredBytes();
      reset.getOutput().resetTransferredBytes();
    }
  }
  
  public static String humanReadableByteCount(double bytes)
  {
    if (bytes < 0)
    {
      return "000.000 KB";
    }
    if (bytes < 1000)
    {
      return String.format(Locale.US, "%07.3f KB", bytes / 1000);
    }
    CharacterIterator ci = new StringCharacterIterator(" KMGTPEZYRQ");
    while (bytes >= 1000)
    {
      bytes /= 1000;
      ci.next();
    }
    return String.format(Locale.US, "%07.3f %cB", bytes, ci.current());
  }
  
  public void pingObtained(long nanoDelay)
  {
    monitorNanoDelay = nanoDelay;
    monitorMilliDelay = monitorNanoDelay / 1000000;
  }
}