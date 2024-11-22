package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public interface VTManagedConnection extends Closeable
{
  public Socket getConnectionSocket();
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number);
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number);
  public Object getConnectionSession();
  public void pingConnection();
  public long ping();
  public long ping(long timeoutNanoSeconds);
  public long getOutputRateBytesPerSecond();
  public void setOutputRateBytesPerSecond(long bytesPerSecond);
}