package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public interface VTManagedConnection extends Closeable
{
  public VTLinkableDynamicMultiplexedInputStream getInputStream(Object link);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(Object link);
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, Object link);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, Object link);
  public int setInputStreamOutputStream(Object link, OutputStream outputStream, Closeable closeable);
  public int setInputStreamOutputStream(int type, Object link, OutputStream outputStream, Closeable closeable);
  public InputStream createBufferedInputStream(Object link);
  public OutputStream createBufferedOutputStream(Object link);
  public InputStream createBufferedInputStream(int type, Object link);
  public OutputStream createBufferedOutputStream(int type, Object link);
  public void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream);
  public void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream);
  public VTLinkableDynamicMultiplexingInputStream getMultiplexedConnectionInputStream();
  public VTLinkableDynamicMultiplexingOutputStream getMultiplexedConnectionOutputStream();
  public int getInputStreamIndexStart();
  public int getOutputStreamIndexStart();
  public void close() throws IOException;
  public Socket getSocket();
  public Class<?> getSessionClass();
  public <T> T getSession(Class<T> clazz);
  public void requestPing();
  public long checkPing();
  public long checkPing(long timeoutNanoSeconds);
  public long getOutputRateBytesPerSecond();
  public void setOutputRateBytesPerSecond(long bytesPerSecond);
}