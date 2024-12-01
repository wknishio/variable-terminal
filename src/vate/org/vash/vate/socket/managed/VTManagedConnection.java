package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public interface VTManagedConnection extends Closeable
{
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number);
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number);
  public Socket getSocket();
  public Class<?> getSessionClass();
  public <T> T getSession(Class<T> clazz);
  public void requestPing();
  public long checkPing();
  public long checkPing(long timeoutNanoSeconds);
  public long getOutputRateBytesPerSecond();
  public void setOutputRateBytesPerSecond(long bytesPerSecond);
}