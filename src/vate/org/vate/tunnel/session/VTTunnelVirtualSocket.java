package org.vate.tunnel.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.vate.stream.pipe.VTPipedInputStream;
import org.vate.stream.pipe.VTPipedOutputStream;

public class VTTunnelVirtualSocket extends Socket
{
  private InputStream in;
  private OutputStream out;
  private OutputStream pipe;

  public VTTunnelVirtualSocket(OutputStream output) throws IOException
  {
    this.out = output;
    VTPipedInputStream pipeSink = new VTPipedInputStream();
    VTPipedOutputStream pipeSource = new VTPipedOutputStream();
    pipeSink.connect(pipeSource);
    this.in = pipeSink;
    this.pipe = pipeSource;
  }

  public InputStream getInputStream()
  {
    return in;
  }

  public OutputStream getOutputStream()
  {
    return out;
  }

  public OutputStream getInputStreamSource()
  {
    return pipe;
  }

  public void shutdownOutput() throws IOException
  {
    if (out != null)
    {
      try
      {
        out.close();
      }
      catch (Throwable e)
      {

      }
    }
  }

  public void shutdownInput() throws IOException
  {
    if (in != null)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {

      }
    }
    if (pipe != null)
    {
      try
      {
        pipe.close();
      }
      catch (Throwable e)
      {

      }
    }
  }

  public void close() throws IOException
  {
    if (in != null)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {

      }
    }
    if (out != null)
    {
      try
      {
        out.close();
      }
      catch (Throwable e)
      {

      }
    }
    if (pipe != null)
    {
      try
      {
        pipe.close();
      }
      catch (Throwable e)
      {

      }
    }
  }
}