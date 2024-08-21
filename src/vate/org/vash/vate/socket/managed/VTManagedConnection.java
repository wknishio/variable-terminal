package org.vash.vate.socket.managed;

import java.io.Closeable;

import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public interface VTManagedConnection extends Closeable
{
  //public Socket getConnectionSocket();
  public boolean isBound();
  public boolean isClosed();
  public boolean isConnected();
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number);
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number, int type);
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number, int type);
  public VTClientSession getClientSession();
  public VTServerSession getServerSession();
  public void ping();
  public boolean ping(long timeout);
}