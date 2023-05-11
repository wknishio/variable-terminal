package org.vash.vate.client.authentication;

import java.io.IOException;
import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.security.VTBlake3MessageDigest;

public class VTClientAuthenticator
{
  @SuppressWarnings("unused")
  //private static byte[] VT_AUTHENTICATION_REJECTED_STRING = new byte[16];
  //private static byte[] VT_AUTHENTICATION_ACCEPTED_STRING = new byte[16];
  //private static final String MAJOR_MINOR_VERSION = VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION;
  
//  static
//  {
//    try
//    {
//      VT_AUTHENTICATION_REJECTED_STRING = (StringUtils.reverse("VT/SERVER/REJECT/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/REJECT/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
//      VT_AUTHENTICATION_ACCEPTED_STRING = (StringUtils.reverse("VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
//    }
//    catch (Throwable e)
//    {
//      
//    }
//  }
  
  private volatile boolean accepted = false;
  private byte[] digestedCredential = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  //private byte[] authResult = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] randomData = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] localNonce;
  private byte[] remoteNonce;
  private String user;
  private String password;
  private VTBlake3MessageDigest blake3Digest;
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
            wait(VT.VT_TIMEOUT_AUTHENTICATION_MILLISECONDS);
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
    this.blake3Digest = new VTBlake3MessageDigest();
    // this.localNonce = connection.getLocalNonce();
    // this.remoteNonce = connection.getRemoteNonce();
    // this.credentialCounter = 0;
    // try
    // {
    // this.sha256Digester = MessageDigest.getInstance("SHA-256");
    // }
    // catch (NoSuchAlgorithmException e)
    // {
    
    // }
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
  
  public byte[] getDigestedCredential()
  {
    return digestedCredential;
  }
  
//  public byte[] getDigestedPassword()
//  {
//    return digestedPassword;
//  }
  
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
    // connection.getSecureRandom().nextBytes(paddingData);
    // connection.getAuthenticationWriter().write(paddingData);
    // connection.getAuthenticationWriter().flush();
    // connection.getAuthenticationReader().readFully(paddingData);
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    
    blake3Digest.reset();
    byte[] seed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(localNonce, 0, seed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(remoteNonce, 0, seed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
    
    // connection.getSecureRandom().nextBytes(randomData);
    // connection.getAuthenticationWriter().write(randomData);
    // connection.getAuthenticationWriter().flush();
    // connection.getAuthenticationReader().readFully(randomData);
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    
    String line = "";
    //byte[] credentialData = null;
    
    if (client.getUser() != null)
    {
      line = client.getUser();
      user = line;
    }
    else
    {
      line = "";
    }
    
    blake3Digest.update(line.getBytes("UTF-8"));
    
    if (client.getPassword() != null)
    {
      line = client.getPassword();
      password = line;
    }
    else
    {
      line = "";
    }
    
    blake3Digest.update(line.getBytes("UTF-8"));
    
    digestedCredential = blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    //credentialData = digestedCredential;
    
    connection.getAuthenticationWriter().write(digestedCredential);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(randomData, 0, randomData.length);
    
    accepted = true;
    stopTimeoutThread();
    return true;
//    connection.getSecureRandom().nextBytes(randomData);
//    connection.getAuthenticationWriter().write(randomData);
//    connection.getAuthenticationWriter().flush();
//    connection.getAuthenticationReader().readFully(authResult);
//    blake3Digest.update(localNonce);
//    blake3Digest.update(remoteNonce);
//    if (VTArrayComparator.arrayEquals(authResult, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_AUTHENTICATION_ACCEPTED_STRING)))
//    {
//      // VTConsole.print("\nVT>Authentication successful!");
//      accepted = true;
//      stopTimeoutThread();
//      return true;
//    }
//    else
//    {
//      // VTConsole.print("\nVT>Authentication failed!");
//      stopTimeoutThread();
//      return false;
//    }
  }
}