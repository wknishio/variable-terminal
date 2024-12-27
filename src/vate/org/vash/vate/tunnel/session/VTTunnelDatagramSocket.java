package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.vash.vate.stream.endian.VTLittleEndianByteArrayInputOutputStream;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public class VTTunnelDatagramSocket extends DatagramSocket implements Closeable, Runnable
{
  private Socket tunnelSocket;
  private VTLittleEndianInputStream input;
  private VTLittleEndianOutputStream output;
  private VTLittleEndianInputStream packetInputStream;
  private VTLittleEndianOutputStream packetOutputStream;
  private VTLittleEndianByteArrayInputOutputStream packetInputData = new VTLittleEndianByteArrayInputOutputStream(65535);
  private VTLittleEndianByteArrayInputOutputStream packetOutputData = new VTLittleEndianByteArrayInputOutputStream(65535);
  private DatagramPacket inputPacket = new DatagramPacket(new byte[65535], 65535);
  private DatagramPacket outputPacket = new DatagramPacket(new byte[65535], 65535);
  private ExecutorService executorService;
  private String localAddress;
  private int localPort;
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService) throws IOException
  {
    super();
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, String host, int port) throws IOException
  {
    super(new InetSocketAddress(host, port));
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
  }
  
  public VTTunnelDatagramSocket(Socket tunnelSocket, ExecutorService executorService, InetAddress address, int port) throws IOException
  {
    super(port, address);
    this.tunnelSocket = tunnelSocket;
    this.executorService = executorService;
  }
  
  public void setInputStream(InputStream inputStream)
  {
    this.input = new VTLittleEndianInputStream(inputStream);
    this.packetInputData = new VTLittleEndianByteArrayInputOutputStream(65535);
    this.packetInputStream = new VTLittleEndianInputStream(packetInputData.getInputStream());
  }
  
  public void setOutputStream(OutputStream outputStream)
  {
    this.output = new VTLittleEndianOutputStream(outputStream);
    this.packetOutputData = new VTLittleEndianByteArrayInputOutputStream(65535);
    this.packetOutputStream = new VTLittleEndianOutputStream(packetOutputData.getOutputStream());
  }
  
  public void receive(DatagramPacket packet) throws IOException
  {
    packetInputData.reset();
    byte[] data = input.readData();
    packetInputData.setBuffer(data);
    int length = packetInputStream.readUnsignedShort();
    int port = packetInputStream.readUnsignedShort();
    byte[] byteAddress = new byte[data.length - 4 - length];
    packetInputStream.readFully(byteAddress);
    packet.setPort(port);
    packet.setAddress(InetAddress.getByAddress(byteAddress));
    packet.setData(data, packetInputData.getInputPos(), length);
  }
  
  public void send(DatagramPacket packet) throws IOException
  {
    packetOutputData.reset();
    packetOutputStream.writeUnsignedShort(packet.getLength());
    packetOutputStream.writeUnsignedShort(packet.getPort());
    byte[] byteAddress = packet.getAddress().getAddress();
    packetOutputStream.write(byteAddress);
    packetOutputStream.write(packet.getData(), packet.getOffset(), packet.getLength());
    output.writeData(packetOutputData.getBuffer(), 0, packetOutputData.getOutputCount());
    output.flush();
  }
  
  public void close()
  {
    try
    {
      super.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      tunnelSocket.close();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  private boolean processInputPacket() throws IOException
  {
    //receive from local and send to tunnel
    super.receive(inputPacket);
    send(inputPacket);
    //System.out.println("processInputPacket()");
    return true;
  }
  
  private boolean processOutputPacket() throws IOException
  {
    //receive from tunnel and send to local
    receive(outputPacket);
    super.send(outputPacket);
    //System.out.println("processOutputPacket()");
    return true;
  }
  
  private Runnable getInputPacketRunnable()
  {
    return new Runnable()
    {
      public void run()
      {
        try
        {
          while (processInputPacket())
          {
            
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
          while (processOutputPacket())
          {
            
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
  
  public void setLocalAddress(String localAddress)
  {
    this.localAddress = localAddress;
  }
  
  public int getLocalPort()
  {
    if (localPort != 0)
    {
      return localPort;
    }
    return super.getLocalPort();
  }
  
  public void setLocalPort(int localPort)
  {
    this.localPort = localPort;
  }
}