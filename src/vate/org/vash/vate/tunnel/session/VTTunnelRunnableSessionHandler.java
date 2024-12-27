package org.vash.vate.tunnel.session;

import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelRunnableSessionHandler extends VTTunnelSessionHandler
{
  private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  private final Runnable runnable;
  
  public VTTunnelRunnableSessionHandler(VTTunnelSession session, VTTunnelChannel channel, Runnable runnable)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.runnable = runnable;
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
      runnable.run();
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    finally
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
  }
}