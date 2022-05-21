package org.vash.vate.server.authentication;

import java.io.IOException;

import org.vash.vate.VT;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3Digest;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;

public class VTServerAuthenticator
{
  private static byte[] VT_AUTHENTICATION_ACCEPTED_STRING = new byte[16];
  private static byte[] VT_AUTHENTICATION_REJECTED_STRING = new byte[16];

  static
  {
    try
    {
      VT_AUTHENTICATION_ACCEPTED_STRING = ("VT/ACCEPT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION).getBytes("UTF-8");
      VT_AUTHENTICATION_REJECTED_STRING = ("VT/REJECT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION).getBytes("UTF-8");
    }
    catch (Throwable e)
    {

    }
  }

  private volatile boolean accepted = false;
  private String user;
  private String password;
  private byte[] digestedUser = new byte[64];
  private byte[] digestedPassword = new byte[64];
  private byte[] randomData = new byte[64];
  //private byte[] paddingData = new byte[64];
  private byte[] receivedCredential;
  private byte[] localNonce;
  private byte[] remoteNonce;
  private VTBlake3Digest blake3Digester = new VTBlake3Digest();
  //private MessageDigest sha256Digester;
  private VTServer server;
  private VTServerConnection connection;
  private VTServerAuthenticatorTimeoutTask timeoutTask = new VTServerAuthenticatorTimeoutTask();

  private class VTServerAuthenticatorTimeoutTask implements Runnable
  {
    // private volatile boolean finished = true;
    // private Thread timeoutThread;

    public void start()
    {
      accepted = false;
      // finished = false;
      // timeoutThread = new Thread(this, getClass().getSimpleName());
      // timeoutThread.start();
      server.getServerThreads().execute(this);
    }

    public void run()
    {
      try
      {
        // Thread.currentThread().setName(getClass().getSimpleName());
        // timeoutThread = Thread.currentThread();
        // Thread.sleep(VT.VT_AUTHENTICATION_TIMEOUT_MILLISECONDS);
        if (!accepted)
        {
          synchronized (this)
          {
            wait(VT.VT_AUTHENTICATION_TIMEOUT_MILLISECONDS);
          }
        }
        // VTConsole.print("\nVT>AuthenticationTimeout");
      }
      catch (Throwable e)
      {
        // VTConsole.print("\nVT>InterruptedTimeout");
      }
      try
      {
        if (!accepted && !connection.getConnectionSocket().isClosed())
        {
          connection.closeSockets();
        }
      }
      catch (Throwable e)
      {

      }
      // finished = true;
    }

    public void stop()
    {
      try
      {
//				if (timeoutThread != null)
//				{
//					timeoutThread.interrupt();
//				}
        synchronized (this)
        {
          notifyAll();
        }
      }
      catch (Throwable t)
      {

      }
    }
  }

  public VTServerAuthenticator(VTServer server, VTServerConnection connection)
  {
    this.server = server;
    this.connection = connection;
    // this.localNonce = connection.getLocalNonce();
    // this.remoteNonce = connection.getRemoteNonce();
    //try
    //{
      //this.sha256Digester = MessageDigest.getInstance("SHA-256");
    //}
    //catch (NoSuchAlgorithmException e)
    //{

    //}
  }

  public void startTimeoutThread()
  {
    accepted = false;
    timeoutTask.start();
    // server.getServerThreads().execute(timeoutTask);
  }

  public void stopTimeoutThread()
  {
    timeoutTask.stop();
  }

  public String getUser()
  {
    return user;
  }

  public String getPassword()
  {
    return password;
  }

  public byte[] getDigestedUser()
  {
    return digestedUser;
  }

  public byte[] getDigestedPassword()
  {
    return digestedPassword;
  }

  public boolean tryAuthentication() throws IOException
  {
    accepted = false;
    
    //connection.getSecureRandom().nextBytes(paddingData);
    //connection.getAuthenticationWriter().write(paddingData);
    //connection.getAuthenticationWriter().flush();
    //connection.getAuthenticationReader().readFully(paddingData);
    
    connection.exchangeNonces(true);
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    
    //connection.getSecureRandom().nextBytes(randomData);
    //connection.getAuthenticationWriter().write(randomData);
    //connection.getAuthenticationWriter().flush();
    //connection.getAuthenticationReader().readFully(randomData);
    
    connection.getSecureRandom().nextBytes(randomData);
    connection.getAuthenticationWriter().write(randomData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(digestedUser);
    
    connection.getSecureRandom().nextBytes(randomData);
    connection.getAuthenticationWriter().write(randomData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(digestedPassword);
    
    receivedCredential = new byte[128];
    byte[] digestedCredential = new byte[128];
    System.arraycopy(digestedUser, 0, receivedCredential, 0, 64);
    System.arraycopy(digestedPassword, 0, receivedCredential, 64, 64);
    if (server.getUserCredentials().size() > 0)
    {
      for (byte[] storedCredential : server.getUserCredentials().keySet())
      {
        System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
        blake3Digester.update(digestedCredential, 0, 64);
        blake3Digester.update(localNonce);
        System.arraycopy(blake3Digester.digest(remoteNonce), 0, digestedCredential, 0, 64);
        blake3Digester.update(digestedCredential, 64, 64);
        blake3Digester.update(localNonce);
        System.arraycopy(blake3Digester.digest(remoteNonce), 0, digestedCredential, 64, 64);
        if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
        {
          blake3Digester.update(remoteNonce);
          blake3Digester.update(localNonce);
          // connection.getConnectionSocket().setSoTimeout(0);
          connection.getAuthenticationWriter().write(blake3Digester.digest(VT_AUTHENTICATION_ACCEPTED_STRING));
          connection.getAuthenticationWriter().flush();
          connection.getAuthenticationReader().readFully(randomData);
          // VTConsole.print("\rVT>Authentication
          // successful!\nVT>");
          user = server.getUserCredentials().get(storedCredential).user;
          password = server.getUserCredentials().get(storedCredential).password;
          accepted = true;
          stopTimeoutThread();
          return true;
        }
      }
    }
    else
    {
      byte[] storedCredential = new byte[128];
      System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
      blake3Digester.update(digestedCredential, 0, 64);
      blake3Digester.update(localNonce);
      System.arraycopy(blake3Digester.digest(remoteNonce), 0, digestedCredential, 0, 64);
      blake3Digester.update(digestedCredential, 64, 64);
      blake3Digester.update(localNonce);
      System.arraycopy(blake3Digester.digest(remoteNonce), 0, digestedCredential, 64, 64);
      if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
      {
        blake3Digester.update(remoteNonce);
        blake3Digester.update(localNonce);
        // connection.getConnectionSocket().setSoTimeout(0);
        connection.getAuthenticationWriter().write(blake3Digester.digest(VT_AUTHENTICATION_ACCEPTED_STRING));
        connection.getAuthenticationWriter().flush();
        connection.getAuthenticationReader().readFully(randomData);
        // VTConsole.print("\rVT>Authentication
        // successful!\nVT>");
        user = server.getUserCredentials().get(storedCredential).user;
        password = server.getUserCredentials().get(storedCredential).password;
        accepted = true;
        stopTimeoutThread();
        return true;
      }
    }
    // VTConsole.print("\rVT>Authentication failed!\nVT>");
    blake3Digester.update(remoteNonce);
    blake3Digester.update(localNonce);
    // connection.getConnectionSocket().setSoTimeout(0);
    connection.getAuthenticationWriter().write(blake3Digester.digest(VT_AUTHENTICATION_REJECTED_STRING));
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(randomData);
    stopTimeoutThread();
    return false;
  }
}