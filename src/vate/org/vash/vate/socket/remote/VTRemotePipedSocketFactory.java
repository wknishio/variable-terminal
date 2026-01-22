package org.vash.vate.socket.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public abstract class VTRemotePipedSocketFactory
{  
  public abstract Socket requestPipe(String bind, int type) throws IOException;
  public abstract Socket requestPipe(String bind, int type, OutputStream out) throws IOException;
  public abstract Socket respondPipe(String bind) throws IOException;
  public abstract Socket respondPipe(String bind, OutputStream out) throws IOException;
}