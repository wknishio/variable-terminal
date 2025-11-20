package org.vash.vate.client.graphicslink;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.vash.vate.VTSystem;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTGraphicsLinkClientSession
{
  private boolean finished;
  private Future<?> readerThread;
  private Future<?>  writerThread;
  // private VTGraphicsLinkRemoteGraphics remote;
  private VTClientSession session;
  private VTGraphicsLinkClientReader reader;
  private VTGraphicsLinkClientWriter writer;
  private ExecutorService executorService;
  
  public VTGraphicsLinkClientSession(VTClientSession session)
  {
    this.session = session;
    this.executorService = session.getExecutorService();
    this.reader = new VTGraphicsLinkClientReader(this);
    this.writer = new VTGraphicsLinkClientWriter(this);
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
      headless = VTReflectionUtils.isAWTHeadless();
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      if (headless)
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_SESSION_UNSTARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        VTMainConsole.print("\rVT>Remote graphics link start on client failed!\nVT>");
        if (session.getConnection().getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_SESSION_UNSTARTED)
        {
          //VTConsole.print("\nVT>Remote graphics link start on server failed!\nVT>");
        }
      }
      else
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VTSystem.VT_GRAPHICS_LINK_SESSION_STARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        if (session.getConnection().getGraphicsControlDataInputStream().read() == VTSystem.VT_GRAPHICS_LINK_SESSION_STARTED)
        {
          return true;
        }
        else
        {
          VTMainConsole.print("\rVT>Remote graphics link start on server failed!\nVT>");
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
    //writerThread = new Thread(null, writer, writer.getClass().getSimpleName());
    //writerThread.setDaemon(true);
    //readerThread = new Thread(null, reader, reader.getClass().getSimpleName());
    //readerThread.setDaemon(true);
    if (writer.isReadOnly())
    {
      //VTConsole.print("\nVT>Starting remote graphics link in view mode...\nVT>");
    }
    else
    {
      //VTConsole.print("\nVT>Starting remote graphics link in control mode...\nVT>");
    }
    //writerThread.start();
    //readerThread.start();
    writerThread = executorService.submit(writer);
    readerThread = executorService.submit(reader);
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
      readerThread.get();
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      writerThread.get();
    }
    catch (Throwable e)
    {
      
    }
    reader.dispose();
    writer.dispose();
  }
  
  public void endSession()
  {
    try
    {
      //session.getConnection().getGraphicsDirectImageDataInputStream().close();
      //session.getConnection().getGraphicsSnappedImageDataInputStream().close();
      //session.getConnection().getGraphicsDeflatedImageDataInputStream().close();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    if (session.getConnection().isConnected())
    {
      VTMainConsole.print("\nVT>Remote graphics link stopped!\nVT>");
    }
    finished = true;
  }
}