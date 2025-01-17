package org.vash.vate.tunnel.session;

import org.vash.vate.stream.pipe.VTStreamRedirector;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelSessionHandler implements Runnable
{
  private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  
  public VTTunnelSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  {
    this.session = session;
    this.channel = channel;
    if (channel != null)
    {
      channel.addSession(this);
    }
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  public void close()
  {
    try
    {
      session.close();
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    if (channel != null)
    {
      channel.removeSession(this);
    }
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      if (session.getSocket() != null)
      {
        VTStreamRedirector redirector = new VTStreamRedirector(session.getSocketInputStream(), session.getTunnelOutputStream());
        redirector.run();
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    finally
    {
      close();
    }
  }
}