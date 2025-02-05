package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public class VTTunnelDatagramSocket extends DatagramSocket implements Closeable, Runnable
{
  private VTLittleEndianInputStream tunnelInputStream;
  private VTLittleEndianOutputStream tunnelOutputStream;
  private VTByteArrayInputStream tunnelInputPacket = new VTByteArrayInputStream(new byte[65536]);
  private VTByteArrayOutputStream tunnelOutputPacket = new VTByteArrayOutputStream(new byte[65536]);
  private VTLittleEndianInputStream tunnelPacketInputStream = new VTLittleEndianInputStream(tunnelInputPacket);
  private VTLittleEndianOutputStream tunnelPacketOutputStream = new VTLittleEndianOutputStream(tunnelOutputPacket);
  private DatagramPacket datagramInputPacket = new DatagramPacket(new byte[65536], 65536);
  private DatagramPacket datagramOutputPacket = new DatagramPacket(new byte[65536], 65536);
  private final Socket tunnelSocket;
  private final ExecutorService executorService;
  private final DatagramSocket remoteSocket;
  private final String localAddress;
  private final int localPort;
  private final boolean client;
  private final byte[] byteAddressIPv4 = new byte[4];
  private final byte[] byteAddressIPv6 = new byte[16];
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, String address, int port) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = null;
    this.remoteSocket = null;
    this.localAddress = address;
    this.localPort = port;
    this.client = true;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
    this.remoteSocket = new DatagramSocket((SocketAddress)null);
    this.remoteSocket.bind(null);
    this.localAddress = null;
    this.localPort = 0;
    this.client = false;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, int port) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
    this.remoteSocket = new DatagramSocket((SocketAddress)null);
    this.remoteSocket.bind(new InetSocketAddress(port));
    this.localAddress = null;
    this.localPort = 0;
    this.client = false;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, String address, int port) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
    this.remoteSocket = new DatagramSocket((SocketAddress)null);
    this.remoteSocket.bind(new InetSocketAddress(address, port));
    this.localAddress = null;
    this.localPort = 0;
    this.client = false;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, InetAddress address, int port) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
    this.remoteSocket = new DatagramSocket((SocketAddress)null);
    this.remoteSocket.bind(new InetSocketAddress(address, port));
    this.localAddress = null;
    this.localPort = 0;
    this.client = false;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, DatagramSocket remoteSocket) throws IOException
  {
    super((SocketAddress)null);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
    if (remoteSocket != null)
    {
      this.remoteSocket = remoteSocket;
    }
    else
    {
      this.remoteSocket = new DatagramSocket((SocketAddress)null);
      this.remoteSocket.bind(null);
    }
    this.localAddress = null;
    this.localPort = 0;
    this.client = false;
  }
  
  public void setTunnelInputStream(InputStream inputStream)
  {
    this.tunnelInputStream = new VTLittleEndianInputStream(inputStream);
  }
  
  public void setTunnelOutputStream(OutputStream outputStream)
  {
    this.tunnelOutputStream = new VTLittleEndianOutputStream(outputStream);
  }
  
  public void receive(DatagramPacket packet) throws IOException
  {
    tunnelInputPacket.reset();
    int frameLength = tunnelInputStream.readData(tunnelInputPacket.buf());
    int packetLength = tunnelPacketInputStream.readUnsignedShort();
    int packetPort = tunnelPacketInputStream.readUnsignedShort();
    byte[] byteAddress = null;
    if (frameLength - 4 - packetLength == 4)
    {
      byteAddress = byteAddressIPv4;
    }
    else if (frameLength - 4 - packetLength == 16)
    {
      byteAddress = byteAddressIPv6;
    }
    else
    {
      byteAddress = new byte[frameLength - 4 - packetLength];
    }
    tunnelPacketInputStream.readFully(byteAddress);
    byte[] packetData = packet.getData();
    tunnelPacketInputStream.readFully(packetData, 0, packetLength);
    packet.setPort(packetPort);
    packet.setAddress(InetAddress.getByAddress(byteAddress));
    packet.setData(packetData, 0, packetLength);
  }
  
  public void send(DatagramPacket packet) throws IOException
  {
    tunnelOutputPacket.reset();
    tunnelPacketOutputStream.writeUnsignedShort(packet.getLength());
    tunnelPacketOutputStream.writeUnsignedShort(packet.getPort());
    tunnelPacketOutputStream.write(packet.getAddress().getAddress());
    tunnelPacketOutputStream.write(packet.getData(), packet.getOffset(), packet.getLength());
    tunnelOutputStream.writeData(tunnelOutputPacket.buf(), 0, tunnelOutputPacket.count());
    tunnelOutputStream.flush();
  }
  
  public void close()
  {
    if (remoteSocket != null)
    {
      try
      {
        remoteSocket.close();
      }
      catch (Throwable t)
      {
        
      }
    }
    try
    {
      tunnelSocket.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      super.close();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  private Runnable getInputPacketRunnable()
  {
    return new Runnable()
    {
      public void run()
      {
        try
        {
          while (true)
          {
            remoteSocket.receive(datagramInputPacket);
            send(datagramInputPacket);
          }
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        close();
      }
    };
  }
  
  private Runnable getOutputPacketRunnable()
  {
    return new Runnable()
    {
      public void run()
      {
        try
        {
          while (true)
          {
            receive(datagramOutputPacket);
            remoteSocket.send(datagramOutputPacket);
          }
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        close();
      }
    };
  }
  
  private void execute()
  {
    if (executorService == null)
    {
      return;
    }
    
    Future<?> inputFuture = executorService.submit(getInputPacketRunnable());
    Future<?> outputFuture = executorService.submit(getOutputPacketRunnable());
    
    try
    {
      inputFuture.get();
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    
    try
    {
      outputFuture.get();
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
  }
  
  public void run()
  {
    execute();
  }
  
  public InetAddress getLocalAddress()
  {
    if (remoteSocket != null)
    {
      return remoteSocket.getLocalAddress();
    }
    if (localAddress != null)
    {
      try
      {
        return InetAddress.getByName(localAddress);
      }
      catch (Throwable t)
      {
        
      }
    }
    return super.getLocalAddress();
  }
  
  public int getLocalPort()
  {
    if (remoteSocket != null)
    {
      return remoteSocket.getLocalPort();
    }
    if (localPort != 0)
    {
      return localPort;
    }
    return super.getLocalPort();
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setSoTimeout(timeout);
      return;
    }
    super.setSoTimeout(timeout);
  }
  
  public int getSoTimeout() throws SocketException
  {
    if (client)
    {
      return 0;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getSoTimeout();
    }
    return super.getSoTimeout();
  }
  
  public void setSendBufferSize(int size) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setSendBufferSize(size);
      return;
    }
    super.setSendBufferSize(size);
  }
  
  public int getSendBufferSize() throws SocketException
  {
    if (client)
    {
      return 0;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getSendBufferSize();
    }
    return super.getSendBufferSize();
  }
  
  public void setReceiveBufferSize(int size) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setReceiveBufferSize(size);
      return;
    }
    super.setReceiveBufferSize(size);
  }
  
  public int getReceiveBufferSize() throws SocketException
  {
    if (client)
    {
      return 0;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getReceiveBufferSize();
    }
    return super.getReceiveBufferSize();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setReuseAddress(on);
      return;
    }
    super.setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    if (client)
    {
      return false;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getReuseAddress();
    }
    return super.getReuseAddress();
  }
  
  public void setBroadcast(boolean on) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setBroadcast(on);
      return;
    }
    super.setBroadcast(on);
  }
  
  public boolean getBroadcast() throws SocketException
  {
    if (client)
    {
      return false;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getBroadcast();
    }
    return super.getBroadcast();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    if (client)
    {
      return;
    }
    if (remoteSocket != null)
    {
      remoteSocket.setTrafficClass(tc);
      return;
    }
    super.setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    if (client)
    {
      return 0;
    }
    if (remoteSocket != null)
    {
      return remoteSocket.getTrafficClass();
    }
    return super.getTrafficClass();
  }
}