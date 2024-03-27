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
  private final ExecutorService executor;
  private final VTMultiplexingControlThread controlThread;
  
  public VTMultiplexingConnection(ExecutorService executor)
  {
    this.executor = executor;
    this.controlThread = new VTMultiplexingControlThread(this, executor);
  }
  
  public void start()
  {
    executor.execute(controlThread);
  }
  
  public void stop()
  {
    // controlThread.stop();
  }
  
  public void close()
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
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, Object link)
  {
    if (link instanceof Integer)
    {
      //int number = (Integer) link;
      //return dataOutputStream.linkOutputStream(type, number);
      return dataOutputStream.linkOutputStream(type, (Integer) link);
    }
    return dataOutputStream.linkOutputStream(type, link);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.linkOutputStream(type, number, link);
    return stream;
  }
  
  public void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      dataOutputStream.releaseOutputStream(stream);
    }
  }
  
  // public synchronized VTLinkableDynamicMultiplexedInputStream
  // getInputStream(short type, int number)
  // {
  // VTLinkableDynamicMultiplexedInputStream stream =
  // dataInputStream.getInputStream(type, number);
  // return stream;
  // }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, Object link)
  {
    if (link instanceof Integer)
    {
      //int number = (Integer) link;
      //return dataInputStream.linkInputStream(type, number);
      return dataInputStream.linkInputStream(type, (Integer) link);
    }
    return dataInputStream.linkInputStream(type, link);
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.linkInputStream(type, number, link);
    return stream;
  }
  
  public void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
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