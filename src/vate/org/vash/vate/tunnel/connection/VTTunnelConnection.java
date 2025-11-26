package org.vash.vate.tunnel.connection;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VTSystem;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelBindSocketListener;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketBuilder;
import org.vash.vate.tunnel.channel.VTTunnelRemoteSocketFactory;

public class VTTunnelConnection
{
  private VTLinkableDynamicMultiplexingInputStream dataInputStream;
  private VTLinkableDynamicMultiplexingOutputStream dataOutputStream;
  private VTLittleEndianInputStream controlInputStream;
  private VTLittleEndianOutputStream controlOutputStream;
  // private VTLittleEndianInputStream relayInputStream;
  // private VTLittleEndianOutputStream relayOutputStream;
  private VTTunnelChannel responseChannelDirect;
  private VTTunnelChannel responseChannelQuick;
  private VTTunnelChannel responseChannelHeavy;
//  private VTTunnelChannelRemoteSocketBuilder remoteSocketBuilder;
  private Collection<VTTunnelChannelBindSocketListener> bindListeners;
  // private int tunnelType;
  private ExecutorService executorService;
  private Collection<Closeable> closeables;
  private final Collection<String> nonces = new LinkedHashSet<String>();
  //private final Random random = new VTSplitMix64Random(new VTBlake3SecureRandom().nextLong());
  private volatile boolean closed = false;
  
  public VTTunnelConnection(ExecutorService executorService, Collection<Closeable> closeables)
  {
    this.responseChannelDirect = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT, this);
    this.responseChannelQuick = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_QUICK, this);
    this.responseChannelHeavy = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_HEAVY, this);
    this.bindListeners = new ConcurrentLinkedQueue<VTTunnelChannelBindSocketListener>();
    // this.tunnelType = tunnelType;
    this.executorService = executorService;
    this.closeables = closeables;
    //this.remoteSocketBuilder = createRemoteSocketBuilder();
  }
  
  // public int getTunnelType()
  // {
  // return tunnelType;
  // }
  
  public Collection<String> getNonces()
  {
    return nonces;
  }
  
//  public Random getRandom()
//  {
//    return random;
//  }
  
  public ExecutorService getExecutorService()
  {
    return executorService;
  }
  
  public Collection<Closeable> getCloseables()
  {
    return closeables;
  }
  
  public boolean bindFTPListener(int channelType, int connectTimeout, int dataTimeout, String bindHost, int bindPort, VTProxy proxy)
  {
    String remoteBind = "";
    String bindHostValue = bindHost;
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      remoteBind = bindHost.substring(0, idx);
      bindHostValue = bindHost.substring(idx + 1);
    }
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHostValue, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_FTP)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setConnectTimeout(connectTimeout);
        listener.getChannel().setDataTimeout(dataTimeout);
        listener.getChannel().setRemoteBind(remoteBind);
        listener.getChannel().setTunnelUsername(null);
        listener.getChannel().setTunnelPassword(null);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, true, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel);
    if (listener.bind())
    {
      bindListeners.add(listener);
      executorService.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindFTPListener(int channelType, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String username, String password, VTProxy proxy)
  {
    String remoteBind = "";
    String bindHostValue = bindHost;
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      remoteBind = bindHost.substring(0, idx);
      bindHostValue = bindHost.substring(idx + 1);
    }
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHostValue, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_FTP)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setConnectTimeout(connectTimeout);
        listener.getChannel().setDataTimeout(dataTimeout);
        listener.getChannel().setRemoteBind(remoteBind);
        listener.getChannel().setTunnelUsername(username);
        listener.getChannel().setTunnelPassword(password);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, username, password, true, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel);
    if (listener.bind())
    {
      bindListeners.add(listener);
      executorService.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindSOCKSListener(int channelType, int connectTimeout, int dataTimeout, String bindHost, int bindPort, VTProxy proxy)
  {
    String remoteBind = "";
    String bindHostValue = bindHost;
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      remoteBind = bindHost.substring(0, idx);
      bindHostValue = bindHost.substring(idx + 1);
    }
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHostValue, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setConnectTimeout(connectTimeout);
        listener.getChannel().setDataTimeout(dataTimeout);
        listener.getChannel().setRemoteBind(remoteBind);
        listener.getChannel().setTunnelUsername(null);
        listener.getChannel().setTunnelPassword(null);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, false, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel);
    if (listener.bind())
    {
      bindListeners.add(listener);
      executorService.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindSOCKSListener(int channelType, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String username, String password, VTProxy proxy)
  {
    String remoteBind = "";
    String bindHostValue = bindHost;
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      remoteBind = bindHost.substring(0, idx);
      bindHostValue = bindHost.substring(idx + 1);
    }
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHostValue, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setConnectTimeout(connectTimeout);
        listener.getChannel().setDataTimeout(dataTimeout);
        listener.getChannel().setRemoteBind(remoteBind);
        listener.getChannel().setTunnelUsername(username);
        listener.getChannel().setTunnelPassword(password);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, username, password, false, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel);
    if (listener.bind())
    {
      bindListeners.add(listener);
      executorService.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  public boolean bindTCPRedirectListener(int channelType, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String redirectHost, int redirectPort, VTProxy proxy)
  {
    String remoteBind = "";
    String bindHostValue = bindHost;
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      remoteBind = bindHost.substring(0, idx);
      bindHostValue = bindHost.substring(idx + 1);
    }
    VTTunnelChannelBindSocketListener listener = getBindListener(bindHostValue, bindPort);
    if (listener != null)
    {
      if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
      {
        listener.getChannel().setChannelType(channelType);
        listener.getChannel().setConnectTimeout(connectTimeout);
        listener.getChannel().setDataTimeout(dataTimeout);
        listener.getChannel().setRemoteBind(remoteBind);
        listener.getChannel().setRedirectAddress(redirectHost, redirectPort);
        listener.getChannel().setProxy(proxy);
        return true;
      }
      else
      {
        return false;
      }
    }
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, redirectHost, redirectPort, proxy);
    listener = new VTTunnelChannelBindSocketListener(channel);
    if (listener.bind())
    {
      bindListeners.add(listener);
      executorService.execute(listener);
      return true;
    }
    else
    {
      return false;
    }
  }
  
  private VTTunnelChannelRemoteSocketBuilder createRemoteSocketBuilder(VTTunnelChannel channel)
  {
    return new VTTunnelChannelRemoteSocketBuilder(channel); 
  }
  
  public VTTunnelRemoteSocketFactory createRemoteSocketFactory(VTTunnelChannel channel)
  {
    return new VTTunnelRemoteSocketFactory(createRemoteSocketBuilder(channel));
  }
  
//  public VTTunnelRemoteSocketFactory createRemoteSocketFactory()
//  {
//    return createRemoteSocketFactory(responseChannel);
//  }
  
  public Collection<VTTunnelChannelBindSocketListener> getBindListeners()
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
      int idx = bindHost.indexOf(';');
      if (idx >= 0)
      {
        //remoteBind = bindHost.substring(0, idx);
        bindHost = bindHost.substring(idx + 1);
      }
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
  
  public VTTunnelChannel getResponseChannel(int channelType)
  {
    if ((channelType & responseChannelHeavy.getChannelType()) == responseChannelHeavy.getChannelType())
    {
      return responseChannelHeavy;
    }
    if ((channelType & responseChannelQuick.getChannelType()) == responseChannelQuick.getChannelType())
    {
      return responseChannelQuick;
    }
    return responseChannelDirect;
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
    responseChannelDirect.close();
    responseChannelQuick.close();
    responseChannelHeavy.close();
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
      return dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int channelType, int number, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
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
      return dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int channelType, int number, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
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