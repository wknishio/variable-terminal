package org.vash.vate.socket;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public interface VTManagedCloseableConnection extends Closeable
{
  public Socket getConnectionSocket();
  public boolean isConnected();
  public InputStream getInputStream(int number);
  public OutputStream getOutputStream(int number);
}