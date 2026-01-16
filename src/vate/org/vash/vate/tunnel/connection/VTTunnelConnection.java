package org.vash.vate.tunnel.connection;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VTSystem;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream.VTMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream.VTMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelBindSocketListener;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketBuilder;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketFactory;

public class VTTunnelConnection
{
  private VTMultiplexingInputStream dataInputStream;
  private VTMultiplexingOutputStream dataOutputStream;
  private VTLittleEndianInputStream controlInputStream;
  private VTLittleEndianOutputStream controlOutputStream;
  // private VTLittleEndianInputStream relayInputStream;
  // private VTLittleEndianOutputStream relayOutputStream;
  private VTTunnelChannel responseChannelDirect;
  private VTTunnelChannel responseChannelQuick;
  private VTTunnelChannel responseChannelHeavy;
  private VTTunnelChannel pipedChannelBuffered;
//  private VTTunnelChannelRemoteSocketBuilder remoteSocketBuilder;
  private Collection<VTTunnelChannelBindSocketListener> bindListeners;
  // private int tunnelType;
  private ExecutorService executorService;
  private Collection<Closeable> closeables;
  private final Collection<String> nonces = new LinkedHashSet<String>();
  //private final Random random = new VTSplitMix64Random(new VTBlake3SecureRandom().nextLong())
  private final VTTunnelChannelRemoteSocketFactory remotePipedSocketFactory;
  private final Random random;
  private volatile boolean closed = false;
  
  public VTTunnelConnection(ExecutorService executorService, Collection<Closeable> closeables, Random random)
  {
    this.random = random;
    this.responseChannelDirect = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT, this, random);
    this.responseChannelQuick = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_QUICK, this, random);
    this.responseChannelHeavy = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_HEAVY, this, random);
    this.pipedChannelBuffered = new VTTunnelChannel(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, this);
    this.bindListeners = new ConcurrentLinkedQueue<VTTunnelChannelBindSocketListener>();
    // this.tunnelType = tunnelType;
    this.executorService = executorService;
    this.closeables = closeables;
    this.remotePipedSocketFactory = new VTTunnelChannelRemoteSocketFactory(createRemoteSocketBuilder(pipedChannelBuffered));
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
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, true, proxy, random);
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
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, username, password, true, proxy, random);
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
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, false, proxy, random);
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
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, username, password, false, proxy, random);
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
    VTTunnelChannel channel = new VTTunnelChannel(channelType, this, connectTimeout, dataTimeout, bindHost, bindPort, redirectHost, redirectPort, proxy, random);
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
  
  public VTTunnelChannelRemoteSocketFactory createRemoteSocketFactory(VTTunnelChannel channel)
  {
    return new VTTunnelChannelRemoteSocketFactory(createRemoteSocketBuilder(channel));
  }
  
  public VTTunnelChannelRemoteSocketFactory getRemotePipedSocketFactory()
  {
    return remotePipedSocketFactory;
  }
  
//  public VTTunnelChannelRemoteSocketFactory createRemoteSocketFactory()
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
  
  public VTTunnelChannel getPipedChannel()
  {
    return pipedChannelBuffered;
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
    pipedChannelBuffered.close();
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
  
  public VTMultiplexedOutputStream getOutputStream(int channelType, Object link)
  {
    if (link instanceof Integer)
    {
      return dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTMultiplexedOutputStream getOutputStream(int channelType, int number, Object link)
  {
    VTMultiplexedOutputStream stream = dataOutputStream.linkOutputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
    return stream;
  }
  
  public void releaseOutputStream(VTMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      dataOutputStream.releaseOutputStream(stream);
    }
  }
  
  public VTMultiplexedInputStream getInputStream(int channelType, Object link)
  {
    if (link instanceof Integer)
    {
      return dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), (Integer) link);
    }
    return dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), link);
  }
  
  public VTMultiplexedInputStream getInputStream(int channelType, int number, Object link)
  {
    VTMultiplexedInputStream stream = dataInputStream.linkInputStream((VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT | channelType), number, link);
    return stream;
  }
  
  public void releaseInputStream(VTMultiplexedInputStream stream)
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
  
  public void setDataInputStream(VTMultiplexingInputStream in)
  {
    this.dataInputStream = in;
  }
  
  public void setControlInputStream(InputStream in)
  {
    controlInputStream = new VTLittleEndianInputStream(in);
  }
  
  public void setDataOutputStream(VTMultiplexingOutputStream out)
  {
    this.dataOutputStream = out;
  }
  
  public void setControlOutputStream(OutputStream out)
  {
    controlOutputStream = new VTLittleEndianOutputStream(out);
  }
}