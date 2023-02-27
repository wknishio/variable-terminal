package org.vash.vate.client.authentication;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3MessageDigest;

public class VTClientAuthenticator
{
  private static final String MAJOR_MINOR_VERSION = VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION;
  private static byte[] VT_AUTHENTICATION_ACCEPTED_STRING = new byte[16];

  static
  {
    try
    {
      VT_AUTHENTICATION_ACCEPTED_STRING = (StringUtils.reverse("VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).toLowerCase() + 
      "VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
    }
    catch (Throwable e)
    {

    }
  }

  private volatile boolean accepted = false;
  //private int credentialCounter;
  private byte[] localNonce;
  private byte[] remoteNonce;
  private byte[] digestedUser = new byte[64];
  private byte[] digestedPassword = new byte[64];
  private byte[] authResult = new byte[64];
  private byte[] randomData = new byte[64];
  //private byte[] paddingData = new byte[1024];
  private String user;
  private String password;
  private VTBlake3MessageDigest blake3Digest = new VTBlake3MessageDigest();
  private VTClient client;
  private VTClientConnection connection;
  private VTClientAuthenticatorTimeoutTask timeoutTask = new VTClientAuthenticatorTimeoutTask();

  private class VTClientAuthenticatorTimeoutTask implements Runnable
  {
    // private volatile boolean finished = true;
    // private Thread timeoutThread;

    public void start()
    {
      accepted = false;
      // finished = false;
      // timeoutThread = new Thread(this, getClass().getSimpleName());
      // timeoutThread.start();
      client.getClientThreads().execute(this);
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

  public VTClientAuthenticator(VTClient client, VTClientConnection connection)
  {
    this.client = client;
    this.connection = connection;
    // this.localNonce = connection.getLocalNonce();
    // this.remoteNonce = connection.getRemoteNonce();
    //this.credentialCounter = 0;
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
    timeoutTask.start();
    // client.getClientThreads().execute(timeoutTask);
  }

  public void stopTimeoutThread()
  {
    timeoutTask.stop();
  }

  public byte[] getDigestedUser()
  {
    return digestedUser;
  }

  public byte[] getDigestedPassword()
  {
    return digestedPassword;
  }

  public String getUser()
  {
    return user;
  }

  public String getPassword()
  {
    return password;
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

    String line = "";
    byte[] credentialData = null;
    
    if (client.getUser() != null)
    {
      line = client.getUser();
      user = line;
    }
    else
    {
      line = "";
    }
    blake3Digest.update(blake3Digest.digest(line.getBytes("UTF-8")));
    blake3Digest.update(remoteNonce);
    digestedUser = blake3Digest.digest(localNonce);
    credentialData = digestedUser;
    connection.getAuthenticationWriter().write(credentialData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(randomData, 0, randomData.length);
  
    if (client.getPassword() != null)
    {
      line = client.getPassword();
      password = line;
    }
    else
    {
      line = "";
    }
    blake3Digest.update(blake3Digest.digest(line.getBytes("UTF-8")));
    blake3Digest.update(remoteNonce);
    digestedPassword = blake3Digest.digest(localNonce);
    credentialData = digestedPassword;
    
    connection.getAuthenticationWriter().write(credentialData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(randomData, 0, randomData.length);
    
    connection.getSecureRandom().nextBytes(randomData);
    connection.getAuthenticationWriter().write(randomData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(authResult);
    // connection.getConnectionSocket().setSoTimeout(0);
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (VTArrayComparator.arrayEquals(authResult, blake3Digest.digest(VT_AUTHENTICATION_ACCEPTED_STRING)))
    {
      // VTConsole.print("\nVT>Authentication successful!");
      accepted = true;
      stopTimeoutThread();
      return true;
    }
    else
    {
      // VTConsole.print("\nVT>Authentication failed!");
      stopTimeoutThread();
      return false;
    }
  }
}