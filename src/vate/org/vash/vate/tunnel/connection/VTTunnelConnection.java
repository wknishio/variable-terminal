package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
import org.vash.vate.socket.VTProxy;
import org.vash.vate.socket.VTTunnelRemoteSocketFactory;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelBindSocketListener;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketBuilder;

public class VTTunnelConnection
{
  // public static final int TUNNEL_TYPE_TCP = 0;
  // public static final int TUNNEL_TYPE_SOCKS = 1;
  // public static final int TUNNEL_TYPE_STREAM = 2;
  
  private VTLinkableDynamicMultiplexingInputStream dataInputStream;
  private VTLinkableDynamicMultiplexingOutputStream dataOutputStream;
  private VTLittleEndianInputStream controlInputStream;
  private VTLittleEndianOutputStream controlOutputStream;
  // private VTLittleEndianInputStream relayInputStream;
  // private VTLittleEndianOutputStream relayOutputStream;
  private VTTunnelChannel remoteRedirectChannel;
  private Set<VTTunnelChannelBindSocketListener> bindListeners;
  // private int tunnelType;
  private ExecutorService threads;
  private volatile boolean closed = false;
  
  public VTTunnelConnection(ExecutorService threads)
  {
    this.remoteRedirectChannel = new VTTunnelChannel(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT, this);
    this.bindListeners = new LinkedHashSet<VTTunnelChannelBindSocketListener>();
    // this.tunnelType = tunnelType;
    this.threads = threads;
  }
  
  // public int getTunnelType()
  // {
  // return tunnelType;
  // }
  
  public boolean bindSOCKSListener(int channelType, String bindHost, int bindPort, VTProxy proxy)
  {
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHost, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setSocksUsername(null);
        listener.getChannel().setSocksPassword(null);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, bindHost, bindPort, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel, threads);
    if (listener.bind())
    {
      bindListeners.add(listener);
      threads.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindSOCKSListener(int channelType, String bindHost, int bindPort, String socksUsername, String socksPassword, VTProxy proxy)
  {
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHost, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setSocksUsername(socksUsername);
        listener.getChannel().setSocksPassword(socksPassword);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, bindHost, bindPort, socksUsername, socksPassword, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel, threads);
    if (listener.bind())
    {
      bindListeners.add(listener);
      threads.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindTCPRedirectListener(int channelType, String bindHost, int bindPort, String redirectHost, int redirectPort, VTProxy proxy)
  {
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHost, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setRedirectAddress(redirectHost, redirectPort);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, bindHost, bindPort, redirectHost, redirectPort, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel, threads);
    if (listener.bind())
    {
      bindListeners.add(listener);
      threads.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public VTTunnelRemoteSocketFactory createRemoteSocketFactory()
  {
    return new VTTunnelRemoteSocketFactory(createRemoteSocketBuilder());
  }
  
  private VTTunnelChannelRemoteSocketBuilder createRemoteSocketBuilder()
  {
    return new VTTunnelChannelRemoteSocketBuilder(remoteRedirectChannel); 
  }
  
  public Set<VTTunnelChannelBindSocketListener> getBindListeners()
  {
    return bindListeners;
  }
  
  public VTTunnelChannelBindSocketListener getBindListener(String bindHost, int bindPort)
  {
    if (bindHost == null || bindHost.length() == 0)
    {
      for (VTTunnelChannelBindSocketListener channel : bindListeners)
      {
        if (channel.getChannel().getBindPort() == bindPort)
        {
          InetAddress bindAddress = channel.getChannel().getBindAddress().getAddress();
          if (bindAddress != null && bindAddress.isAnyLocalAddress())
          {
            return channel;
          }
        }
      }
    }
    else
    {
      for (VTTunnelChannelBindSocketListener channel : bindListeners)
      {
        if (channel.getChannel().getBindPort() == bindPort)
        {
          InetAddress bindAddress = channel.getChannel().getBindAddress().getAddress();
          if (bindAddress != null)
          {
            if (bindHost.equals(bindAddress.getHostAddress()) || bindHost.equals(bindAddress.getHostName()))
            {
              return channel;
            }
          }
        }
      }
    }
    return null;
  }
  
  public boolean removeBindListener(VTTunnelChannelBindSocketListener listener)
  {
    return bindListeners.remove(listener);
  }
  
  /* public OutputStream getDataOutputStream() { return dataOutputStream; } */
  
  public void start()
  {
    // dataInputStream.startPacketReader();
  }
  
  public void stop()
  {
    try
    {
      // dataInputStream.stopPacketReader();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  public void close()
  {
    // stop();
    if (closed)
    {
      return;
    }
    closed = true;
    //System.out.println("VTTunnelConnection.close()");
    remoteRedirectChannel.close();
    for (VTTunnelChannelBindSocketListener listener : bindListeners)
    {
      try
      {
        listener.close();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
    bindListeners.clear();
    try
    {
      controlInputStream.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      controlOutputStream.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int channelType, Object link)
  {
    if (link instanceof Integer)
    {
      return dataOutputStream.linkOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataOutputStream.linkOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int channelType, int number, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.linkOutputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
    return stream;
  }
  
  public void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      dataOutputStream.releaseOutputStream(stream);
    }
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int channelType, Object link)
  {
    if (link instanceof Integer)
    {
      return dataInputStream.linkInputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataInputStream.linkInputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int channelType, int number, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.linkInputStream((VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
    return stream;
  }
  
  public void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      dataInputStream.releaseInputStream(stream);
    }
  }
  
  public VTLittleEndianInputStream getControlInputStream()
  {
    return controlInputStream;
  }
  
  public VTLittleEndianOutputStream getControlOutputStream()
  {
    return controlOutputStream;
  }
  
  public void setDataInputStream(VTLinkableDynamicMultiplexingInputStream in)
  {
    this.dataInputStream = in;
  }
  
  public void setControlInputStream(InputStream in)
  {
    controlInputStream = new VTLittleEndianInputStream(in);
  }
  
  public void setDataOutputStream(VTLinkableDynamicMultiplexingOutputStream out)
  {
    this.dataOutputStream = out;
  }
  
  public void setControlOutputStream(OutputStream out)
  {
    controlOutputStream = new VTLittleEndianOutputStream(out);
  }
}