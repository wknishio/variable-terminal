package org.vash.vate.tunnel.session;

import java.util.UUID;

import org.vash.vate.stream.pipe.VTStreamRedirector;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelSessionHandler implements Runnable
{
  private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  private final String key;
  
  public VTTunnelSessionHandler(VTTunnelChannel channel, VTTunnelSession session)
  {
    this(channel, session, UUID.randomUUID().toString());
  }
  
  public VTTunnelSessionHandler(VTTunnelChannel channel, VTTunnelSession session, String key)
  {
    this.channel = channel;
    this.session = session;
    this.key = key;
  }
  
  public VTTunnelChannel getChannel()
  {
    return channel;
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  public String getKey()
  {
    return key;
  }
  
  public void open()
  {
    if (channel != null)
    {
      channel.addSessionHandler(this);
    }
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
      channel.removeSessionHandler(key);
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