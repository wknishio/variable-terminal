package org.vash.vate.socket.allocated;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream.VTMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream.VTMultiplexedOutputStream;

public class VTAllocatedSocketManager
{
  private VTMultiplexingInputStream dataInputStream;
  private VTMultiplexingOutputStream dataOutputStream;
  private VTLittleEndianInputStream controlInputStream;
  private VTLittleEndianOutputStream controlOutputStream;
  private final ExecutorService executorService;
  
  public VTAllocatedSocketManager(ExecutorService executorService)
  {
    this.executorService = executorService;
  }
  
  public ExecutorService getExecutorService()
  {
    return executorService;
  }
  
  public void start()
  {
    //executorService.execute(controlThread);
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
  
  public VTMultiplexedOutputStream linkOutputStream(int type, Object link)
  {
    if (link instanceof Integer)
    {
      return dataOutputStream.linkOutputStream(type, (Integer) link);
    }
    return dataOutputStream.linkOutputStream(type, link);
  }
  
  public void releaseOutputStream(VTMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      dataOutputStream.releaseOutputStream(stream);
    }
  }
  
  public VTMultiplexedInputStream linkInputStream(int type, Object link)
  {
    if (link instanceof Integer)
    {
      return dataInputStream.linkInputStream(type, (Integer) link);
    }
    return dataInputStream.linkInputStream(type, link);
  }
  
  public void releaseInputStream(VTMultiplexedInputStream stream)
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
  
  public void setDataInputStream(VTMultiplexingInputStream in)
  {
    this.dataInputStream = in;
  }
  
  public void setControlInputStream(InputStream in)
  {
    controlInputStream = new VTLittleEndianInputStream(in);
  }
  
  public void setDataOutputStream(VTMultiplexingOutputStream out)
  {
    this.dataOutputStream = out;
  }
  
  public void setControlOutputStream(OutputStream out)
  {
    controlOutputStream = new VTLittleEndianOutputStream(out);
  }
}