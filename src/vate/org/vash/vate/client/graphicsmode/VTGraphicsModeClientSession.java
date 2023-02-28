package org.vash.vate.client.graphicsmode;

import java.awt.GraphicsEnvironment;

import org.vash.vate.VT;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.VTConsole;

public class VTGraphicsModeClientSession
{
  private volatile boolean finished;
  private Thread readerThread;
  private Thread writerThread;
  // private VTGraphicsLinkRemoteGraphics remote;
  private VTClientSession session;
  private VTGraphicsModeClientReader reader;
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientSession(VTClientSession session)
  {
    this.session = session;
    this.reader = new VTGraphicsModeClientReader(this);
    this.writer = new VTGraphicsModeClientWriter(this);
    this.reader.setWriter(writer);
    this.writer.setReader(reader);
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
  
  public VTClientSession getSession()
  {
    return session;
  }
  
  public boolean verifySession()
  {
    boolean headless = true;
    try
    {
      headless = GraphicsEnvironment.isHeadless();
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      if (headless)
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_SESSION_UNSTARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        VTConsole.print("\nVT>Remote graphics link start on client failed!\nVT>");
        if (session.getConnection().getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_SESSION_UNSTARTED)
        {
          VTConsole.print("\nVT>Remote graphics link start on server failed!\nVT>");
        }
      }
      else
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_SESSION_STARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        if (session.getConnection().getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_SESSION_STARTED)
        {
          return true;
        }
        else
        {
          VTConsole.print("\nVT>Remote graphics link start on server failed!\nVT>");
        }
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public void receiveInitialScreenSize()
  {
    try
    {
      int width = session.getConnection().getGraphicsControlDataInputStream().readInt();
      int height = session.getConnection().getGraphicsControlDataInputStream().readInt();
      writer.setInitialScreenSize(width, height);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void startSession()
  {
    writer.setStopped(false);
    reader.setStopped(false);
    writerThread = new Thread(null, writer, writer.getClass().getSimpleName());
    writerThread.setDaemon(true);
    readerThread = new Thread(null, reader, reader.getClass().getSimpleName());
    readerThread.setDaemon(true);
    if (writer.isReadOnly())
    {
      VTConsole.print("\nVT>Starting remote graphics link in view mode...\nVT>");
    }
    else
    {
      VTConsole.print("\nVT>Starting remote graphics link in control mode...\nVT>");
    }
    writerThread.start();
    readerThread.start();
  }
  
  public boolean isStopped()
  {
    return reader.isStopped() || writer.isStopped();
  }
  
  public void setStopped(boolean stopped)
  {
    writer.setStopped(stopped);
  }
  
  public boolean isReadOnly()
  {
    return writer.isReadOnly();
  }
  
  public void setReadOnly(boolean readOnly)
  {
    writer.setReadOnly(readOnly);
  }
  
  /*
   * public void setHighQuality(boolean highQuality) {
   * writer.setHighQuality(highQuality); }
   */
  
  public void waitSession()
  {
    /*
     * while(!isStopped()) { try { Thread.sleep(1); } catch
     * (InterruptedException e) { } }
     */
    synchronized (this)
    {
      while (!isStopped())
      {
        try
        {
          wait();
        }
        catch (InterruptedException e)
        {
          
        }
      }
    }
  }
  
  public void tryStopThreads()
  {
    setStopped(true);
    if (session.getClipboardTransferTask().aliveThread())
    {
      session.getClipboardTransferTask().interruptThread();
      // session.getClipboardTransferThread().stop();
    }
  }
  
  public void waitThreads()
  {
    session.getClipboardTransferTask().joinThread();
    try
    {
      readerThread.join();
    }
    catch (InterruptedException e)
    {
      
    }
    try
    {
      writerThread.join();
    }
    catch (InterruptedException e)
    {
      
    }
    reader.dispose();
    writer.dispose();
  }
  
  public void endSession()
  {
    try
    {
      VTConsole.print("\nVT>Stopping remote graphics link...\nVT>");
      // session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_SESSION_ENDED);
      // session.getConnection().getGraphicsControlDataOutputStream().flush();
      // session.getConnection().getGraphicsControlDataInputStream().read();
      // session.getConnection().getGraphicsControlDataOutputStream().close();
      // session.getConnection().getGraphicsControlDataInputStream().close();
      // session.getConnection().getGraphicsControlDataInputStream().read();
      synchronized (session.getGraphicsClient())
      {
        VTConsole.print("\nVT>Remote graphics link stopped!\nVT>");
        finished = true;
      }
    }
    catch (Throwable e)
    {
      
    }
    finished = true;
  }
}