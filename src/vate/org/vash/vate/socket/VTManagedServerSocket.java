package org.vash.vate.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import org.vash.vate.VT;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.server.session.VTServerSessionListener;

public class VTManagedServerSocket
{
  private VTServer vtserver;
  private ArrayBlockingQueue<VTManagedSocket> queue = new ArrayBlockingQueue<VTManagedSocket>(1);
  private volatile Thread interruptible;
  //private int streams;
  
  private class VTCloseableServerConnection implements VTManagedCloseableConnection
  {
    private VTServerConnection connection;
    
    private VTCloseableServerConnection(VTServerConnection connection)
    {
      this.connection = connection;
    }
    
    public void close() throws IOException
    {
      connection.closeSockets();
    }
    
    public Socket getConnectionSocket()
    {
      return connection.getConnectionSocket();
    }
    
    public boolean isConnected()
    {
      return connection.isConnected();
    }
    
    public InputStream getInputStream(int number)
    {
      return connection.getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12 + number);
    }
    
    public OutputStream getOutputStream(int number)
    {
      return connection.getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12 + number);
    }
  }
  
  private class VTManagedServerSocketServerSessionListener implements VTServerSessionListener
  {
    public void sessionStarted(VTServerSession session)
    {
      //System.out.println("server.session.started()");
      InputStream input = session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12);
      OutputStream output = session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12);
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableServerConnection(session.getConnection()), input, output);
      session.addSessionResource(this.getClass().getSimpleName(), socket);
      queue.offer(socket);
    }
    
    public void sessionFinished(VTServerSession session)
    {
      //System.out.println("server.session.finished()");
    }
  }
  
  public VTManagedServerSocket()
  {
    this.vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
  }
  
  public VTServer getServer()
  {
    return vtserver;
  }
  
  public void start()
  {
    vtserver.startThread();
  }
  
  public void stop()
  {
    vtserver.stop();
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
//    VTManagedServerSocket managed = new VTManagedServerSocket();
//    managed.start();
//    try
//    {
//      Socket socket = managed.accept();
//      System.out.println("server.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      int i = 5;
//      while (socket.isConnected())
//      {
//        long time = System.currentTimeMillis();
//        writer.write("server.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println(reader.readLine());
//        Thread.sleep(1000);
//        if (i > 0)
//        {
//          i--;
//        }
//        else
//        {
//          socket.close();
//        }
//      }
//    }
//    catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//    System.out.println("server.socket.disconnected()");
//  }
}
