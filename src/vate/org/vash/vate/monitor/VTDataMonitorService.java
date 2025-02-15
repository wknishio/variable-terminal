package org.vash.vate.monitor;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.task.VTTask;

public class VTDataMonitorService extends VTTask
{
  private volatile long currentInput;
  private volatile long currentOutput;
  private volatile long transferredInput;
  private volatile long transferredOutput;
  private volatile long lastInput;
  private volatile long lastOutput;
  private volatile long differenceInput;
  private volatile long differenceOutput;
  private final ConcurrentLinkedQueue<VTDataMonitorConnection> connections = new ConcurrentLinkedQueue<VTDataMonitorConnection>();
  private final ConcurrentLinkedQueue<VTDataMonitorPanel> panels = new ConcurrentLinkedQueue<VTDataMonitorPanel>();
      
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
  
  public void addMonitorPanel(VTDataMonitorPanel panel)
  {
    panels.add(panel);
  }
  
  public void removeMonitorPanel(VTDataMonitorPanel panel)
  {
    panels.remove(panel);
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
      if (panels.size() <= 0)
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
      differenceInput = currentInput - lastInput;
      differenceOutput = currentOutput - lastOutput;
      String message = "Tx: " + humanReadableByteCount(differenceOutput) + "/s Rx: " + humanReadableByteCount(differenceInput) + "/s";
      for (VTDataMonitorPanel panel : panels)
      {
        try
        {
          panel.setMessage(message);
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
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
    CharacterIterator ci = new StringCharacterIterator(" KMGTPE");
    while (bytes >= 1000)
    {
      bytes /= 1000;
      ci.next();
    }
    return String.format(Locale.US, "%07.3f %cB", bytes, ci.current());
  }
}
