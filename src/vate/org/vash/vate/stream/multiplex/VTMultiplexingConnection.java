package org.vash.vate.stream.multiplex;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTMultiplexingConnection
{
  private VTLinkableDynamicMultiplexingInputStream dataInputStream;
  private VTLinkableDynamicMultiplexingOutputStream dataOutputStream;
  private VTLittleEndianInputStream controlInputStream;
  private VTLittleEndianOutputStream controlOutputStream;
  private final ExecutorService threads;
  private final VTMultiplexingControlThread controlThread;

  public VTMultiplexingConnection(ExecutorService threads)
  {
    this.threads = threads;
    this.controlThread = new VTMultiplexingControlThread(this, threads);
  }

  public synchronized void start()
  {
    threads.execute(controlThread);
  }

  public synchronized void stop()
  {
    // controlThread.stop();
  }

  public synchronized void close()
  {
    stop();
    try
    {
      controlInputStream.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      controlOutputStream.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }

  public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(short type, Object link)
  {
    if (link instanceof Integer)
    {
      return dataOutputStream.linkOutputStream(type, (Integer) link);
    }
    return dataOutputStream.linkOutputStream(type, link);
  }

  public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(short type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.linkOutputStream(type, number);
    stream.setLink(link);
    return stream;
  }

  public synchronized void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      dataOutputStream.releaseOutputStream(stream);
    }
  }
  
  //public synchronized VTLinkableDynamicMultiplexedInputStream getInputStream(short type, int number)
  //{
    //VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.getInputStream(type, number);
    //return stream;
  //}

  
  public synchronized VTLinkableDynamicMultiplexedInputStream getInputStream(short type, Object link)
  {
    if (link instanceof Integer)
    {
      return dataInputStream.linkInputStream(type, (Integer) link);
    }
    return dataInputStream.linkInputStream(type, link);
  }
  
  public synchronized VTLinkableDynamicMultiplexedInputStream getInputStream(short type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.linkInputStream(type, number);
    stream.setLink(link);
    return stream;
  }
  
  public synchronized void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      dataInputStream.releaseInputStream(stream);
    }
  }

  public VTLittleEndianInputStream getControlInputStream()
  {
    return controlInputStream;
  }

  public VTLittleEndianOutputStream getControlOutputStream()
  {
    return controlOutputStream;
  }

  public void setDataInputStream(VTLinkableDynamicMultiplexingInputStream in)
  {
    this.dataInputStream = in;
  }

  public void setControlInputStream(InputStream in)
  {
    controlInputStream = new VTLittleEndianInputStream(in);
  }

  public void setDataOutputStream(VTLinkableDynamicMultiplexingOutputStream out)
  {
    this.dataOutputStream = out;
  }

  public void setControlOutputStream(OutputStream out)
  {
    controlOutputStream = new VTLittleEndianOutputStream(out);
  }
}