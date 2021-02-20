package org.vate.tunnel.session;

import org.vate.stream.pipe.VTStreamRedirector;
import org.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelSessionHandler implements Runnable
{
  private VTTunnelChannel channel;
  private VTTunnelSession session;

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

  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      VTStreamRedirector redirector = new VTStreamRedirector(session.getSocketInputStream(), session.getTunnelOutputStream());
      redirector.run();
      session.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    finally
    {
      if (channel != null)
      {
        channel.removeSession(this);
      }
    }
  }
}