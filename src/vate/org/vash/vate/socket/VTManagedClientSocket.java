package org.vash.vate.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.client.session.VTClientSessionListener;

public class VTManagedClientSocket
{
  private VTClient vtclient;
  private ArrayBlockingQueue<VTManagedSocket> queue = new ArrayBlockingQueue<VTManagedSocket>(1);
  private volatile Thread interruptible;
  //private int streams;
  
  private class VTCloseableClientConnection implements VTManagedCloseableConnection
  {
    private VTClientConnection connection;
    
    private VTCloseableClientConnection(VTClientConnection connection)
    {
      this.connection = connection;
    }
    
    public void close() throws IOException
    {
      connection.closeSockets();
    }
    
    public boolean isConnected()
    {
      return connection.isConnected();
    }
    
    public Socket getConnectionSocket()
    {
      return connection.getConnectionSocket();
    }
    
    public InputStream getInputStream(int number)
    {
      return connection.getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 13 + number);
    }
    
    public OutputStream getOutputStream(int number)
    {
      return connection.getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 13 + number);
    }
  }
  
  private class VTManagedClientSocketClientSessionListener implements VTClientSessionListener
  {
    public void sessionStarted(VTClientSession session)
    {
      //System.out.println("client.session.started()");
      InputStream input = session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 13);
      OutputStream output = session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 13);
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableClientConnection(session.getConnection()), input, output);
      session.addSessionResource(this.getClass().getSimpleName(), socket);
      queue.offer(socket);
    }
    
    public void sessionFinished(VTClientSession session)
    {
      //System.out.println("client.session.finished()");
    }
  }
  
  public VTManagedClientSocket()
  {
    this.vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
  }
  
  public VTClient getClient()
  {
    return vtclient;
  }
  
  public void start()
  {
    vtclient.startThread();
  }
  
  public void stop()
  {
    vtclient.stop();
  }
  
  public VTManagedSocket connect() throws InterruptedException
  {
    interruptible = Thread.currentThread();
    VTManagedSocket socket = queue.take();
    interruptible = null;
    return socket;
  }
  
  public void interrupt()
  {
    try
    {
      if (interruptible != null)
      {
        interruptible.interrupt();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    interrupt();
    stop();
  }
  
//  public static void main(String[] args)
//  {
//    VTManagedClientSocket managed = new VTManagedClientSocket();
//    managed.start();
//    try
//    {
//      Socket socket = managed.connect();
//      System.out.println("client.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      
//      while (socket.isConnected())
//      {
//        long time = System.currentTimeMillis();
//        writer.write("client.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println(reader.readLine());
//        Thread.sleep(1000);
//      }
//    }
//    catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//    System.out.println("client.socket.disconnected()");
//  }
}
