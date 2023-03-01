package org.vash.vate.socket;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.server.session.VTServerSession;

public interface VTManagedConnection extends Closeable
{
  //public Socket getConnectionSocket();
  public boolean isConnected();
  public InputStream getInputStream(int number);
  public OutputStream getOutputStream(int number);
  public VTClientSession getClientSession();
  public VTServerSession getServerSession();
}