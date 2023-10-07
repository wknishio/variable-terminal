package org.vash.vate.server.graphicsmode;

import java.awt.Dimension;

import org.vash.vate.VT;
import org.vash.vate.graphics.codec.VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII;
import org.vash.vate.server.session.VTServerSession;

public class VTGraphicsModeServerSession
{
  private Thread readerThread;
  private Thread writerThread;
  private Dimension initialScreenSize;
  private VTServerSession session;
  private VTGraphicsModeServerReader reader;
  private VTGraphicsModeServerWriter writer;
  
  public VTGraphicsModeServerSession(VTServerSession session)
  {
    this.session = session;
    this.reader = new VTGraphicsModeServerReader(this);
    this.writer = new VTGraphicsModeServerWriter(this);
    this.reader.setWriter(writer);
    // this.writer.setReader(reader);
  }
  
  public VTServerSession getSession()
  {
    return session;
  }
  
  public boolean verifySession()
  {
    boolean viewProviderInitialized = false;
    boolean controlProviderInitialized = false;
    try
    {
      viewProviderInitialized = session.getViewProvider().isScreenCaptureInitialized(VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII.PADDING_SIZE) || session.getViewProvider().initializeScreenCapture(VTQuadrupleOctalTreeBlockFrameDeltaCodecMKII.PADDING_SIZE);
      controlProviderInitialized = session.getControlProvider().isInputControlInitialized() || session.getControlProvider().initializeInputControl();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    try
    {
      if (viewProviderInitialized && controlProviderInitialized)
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_SESSION_STARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        if (session.getConnection().getGraphicsControlDataInputStream().read() == VT.VT_GRAPHICS_MODE_SESSION_STARTED)
        {
          initialScreenSize = session.getViewProvider().getCurrentScaledSize();
          return true;
        }
      }
      else
      {
        session.getConnection().getGraphicsControlDataOutputStream().write(VT.VT_GRAPHICS_MODE_SESSION_UNSTARTED);
        session.getConnection().getGraphicsControlDataOutputStream().flush();
        session.getConnection().getGraphicsControlDataInputStream().read();
        //System.out.println("return false");
        return false;
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    return false;
  }
  
  public void sendInitialScreenSize()
  {
    try
    {
      session.getConnection().getGraphicsControlDataOutputStream().writeInt((int) initialScreenSize.getWidth());
      session.getConnection().getGraphicsControlDataOutputStream().writeInt((int) initialScreenSize.getHeight());
      session.getConnection().getGraphicsControlDataOutputStream().flush();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void startSession()
  {
    reader.setStopped(false);
    writer.setStopped(false);
    readerThread = new Thread(null, reader, reader.getClass().getSimpleName());
    readerThread.setDaemon(true);
    writerThread = new Thread(null, writer, writer.getClass().getSimpleName());
    writerThread.setDaemon(true);
    /*
     * try { if (reader.isReadOnly()) {
     * session.getConnection().getResultWriter().
     * write("\nVT>Starting graphics mode in view mode...\nVT>");
     * session.getConnection().getResultWriter().flush(); } else {
     * session.getConnection().getResultWriter().
     * write("\nVT>Starting graphics mode in control mode...\nVT>");
     * session.getConnection().getResultWriter().flush(); } } catch (IOException
     * e) { }
     */
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
    return reader.isReadOnly();
  }
  
  public void setReadOnly(boolean readOnly)
  {
    reader.setReadOnly(readOnly);
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
    }
  }
  
  public void waitThreads()
  {
    /*
     * while(readerThread.isAlive() || writerThread.isAlive()) { try {
     * Thread.sleep(1); } catch (InterruptedException e) { } }
     */
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
      //session.getConnection().getGraphicsDirectImageDataOutputStream().close();
      //session.getConnection().getGraphicsDeflatedImageDataOutputStream().close();
      //session.getConnection().getGraphicsSnappedImageDataOutputStream().close();
      //session.getConnection().getGraphicsControlDataOutputStream().close();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
  }
}