package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;
import org.vash.vate.tunnel.connection.VTTunnelConnection;

public interface VTManagedConnection extends Closeable
{
  public VTMultiplexingInputStream getMultiplexedConnectionInputStream();
  public VTMultiplexingOutputStream getMultiplexedConnectionOutputStream();
  public VTRemotePipedSocketFactory createRemotePipedSocketFactory(int type);
  public VTRemoteSocketFactory createRemoteSocketFactory(int type);
  public VTTunnelConnection getTunnelConnection();
  public Socket getSocket();
  public void close() throws IOException;
  public Class<?> getSessionClass();
  public <T> T getSession(Class<T> clazz);
  public void requestPing();
  public long checkPing();
  public long checkPing(long timeoutNanoSeconds);
  public long getOutputRateBytesPerSecond();
  public void setOutputRateBytesPerSecond(long bytesPerSecond);
}