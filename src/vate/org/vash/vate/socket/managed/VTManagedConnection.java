package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream.VTMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream.VTMultiplexedOutputStream;

public interface VTManagedConnection extends Closeable
{
  public VTMultiplexedInputStream getInputStream(Object link);
  public VTMultiplexedOutputStream getOutputStream(Object link);
  public VTMultiplexedInputStream getInputStream(int type, Object link);
  public VTMultiplexedOutputStream getOutputStream(int type, Object link);
  public int setOutputStream(Object link, OutputStream output, Closeable closeable);
  public int setOutputStream(int type, Object link, OutputStream output, Closeable closeable);
  public InputStream createBufferedInputStream(Object link);
  public OutputStream createBufferedOutputStream(Object link);
  public InputStream createBufferedInputStream(int type, Object link);
  public OutputStream createBufferedOutputStream(int type, Object link);
  public void releaseInputStream(VTMultiplexedInputStream stream);
  public void releaseOutputStream(VTMultiplexedOutputStream stream);
  public VTMultiplexingInputStream getMultiplexedConnectionInputStream();
  public VTMultiplexingOutputStream getMultiplexedConnectionOutputStream();
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