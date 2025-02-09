package org.vash.vate.monitoring;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
        this.wait(1000);
      }
      currentInput = 0;
      currentOutput = 0;
      for (VTDataMonitorConnection connection : connections)
      {
        transferredInput = connection.getInput().getTransferredBytes();
        transferredOutput = connection.getOutput().getTransferredBytes();
        if (transferredInput < 0)
        {
          connection.getInput().resetTransferredBytes();
          transferredInput = 0;
        }
        if (transferredOutput < 0)
        {
          connection.getOutput().resetTransferredBytes();
          transferredOutput = 0;
        }
        currentInput += transferredInput;
        currentOutput += transferredOutput;
        if (currentInput < 0 || currentOutput < 0)
        {
          lastInput = 0;
          lastOutput = 0;
          for (VTDataMonitorConnection reset : connections)
          {
            reset.getInput().resetTransferredBytes();
            reset.getOutput().resetTransferredBytes();
          }
          continue;
        }
      }
      differenceInput = currentInput - lastInput;
      differenceOutput = currentOutput - lastOutput;
      String message = "Tx:" + humanReadableByteCountSI(differenceOutput) + "/s Rx:" + humanReadableByteCountSI(differenceInput) + "/s";
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
      if (currentInput > lastInput)
      {
        lastInput = currentInput;
      }
      if (currentOutput > lastOutput)
      {
        lastOutput = currentOutput;
      }
      if (currentInput < lastInput || currentOutput < lastOutput)
      {
        lastInput = 0;
        lastOutput = 0;
        for (VTDataMonitorConnection reset : connections)
        {
          reset.getInput().resetTransferredBytes();
          reset.getOutput().resetTransferredBytes();
        }
      }
    }
  }
  
  public static String humanReadableByteCountSI(double bytes)
  {
    if (bytes < 0)
    {
      return "0000.00 B";
    }
    if (bytes < 1024)
    {
      return String.format("%07.2f B", bytes);
    }
    CharacterIterator ci = new StringCharacterIterator(" KMGTPE");
    while (bytes >= 1024)
    {
      bytes /= 1024;
      ci.next();
    }
    return String.format("%07.2f%cB", bytes, ci.current());
  }
}
