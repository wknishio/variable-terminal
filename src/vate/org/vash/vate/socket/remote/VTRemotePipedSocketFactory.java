package org.vash.vate.socket.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public abstract class VTRemotePipedSocketFactory
{  
  public abstract Socket pipeSocket(String bind, int type, boolean originator) throws IOException;
  public abstract Socket pipeSocket(String bind, int type, boolean originator, OutputStream out) throws IOException;
}