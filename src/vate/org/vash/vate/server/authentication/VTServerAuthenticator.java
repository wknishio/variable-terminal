package org.vash.vate.server.authentication;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.vash.vate.VT;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.VTServer.Credential;
import org.vash.vate.server.connection.VTServerConnection;

public class VTServerAuthenticator
{
  private static final String MAJOR_MINOR_VERSION = VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION;
  private static byte[] VT_AUTHENTICATION_ACCEPTED_STRING = new byte[16];
  private static byte[] VT_AUTHENTICATION_REJECTED_STRING = new byte[16];
  
  static
  {
    try
    {
      VT_AUTHENTICATION_ACCEPTED_STRING = (StringUtils.reverse("VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/ACCEPT/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
      VT_AUTHENTICATION_REJECTED_STRING = (StringUtils.reverse("VT/SERVER/REJECT/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/REJECT/" + MAJOR_MINOR_VERSION).getBytes("UTF-8");
    }
    catch (Throwable e)
    {
      
    }
  }
  
  private volatile boolean accepted = false;
  private String user;
  private String password;
  private byte[] digestedUser = new byte[VT.VT_SECURITY_DIGEST_SIZE];
  private byte[] digestedPassword = new byte[VT.VT_SECURITY_DIGEST_SIZE];
  private byte[] randomData = new byte[VT.VT_SECURITY_DIGEST_SIZE];
  // private byte[] paddingData = new byte[64];
  private byte[] receivedCredential;
  private byte[] localNonce;
  private byte[] remoteNonce;
  private VTBlake3MessageDigest blake3Digest;
  // private MessageDigest sha256Digester;
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
    this.blake3Digest = new VTBlake3MessageDigest();
    // this.localNonce = connection.getLocalNonce();
    // this.remoteNonce = connection.getRemoteNonce();
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
    
    // connection.getSecureRandom().nextBytes(paddingData);
    // connection.getAuthenticationWriter().write(paddingData);
    // connection.getAuthenticationWriter().flush();
    // connection.getAuthenticationReader().readFully(paddingData);
    
    connection.exchangeNonces(true);
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    
    blake3Digest.reset();
    byte[] seed = new byte[VT.VT_SECURITY_SEED_SIZE];
    System.arraycopy(remoteNonce, 0, seed, 0, VT.VT_SECURITY_DIGEST_SIZE);
    System.arraycopy(localNonce, 0, seed, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
    
    // connection.getSecureRandom().nextBytes(randomData);
    // connection.getAuthenticationWriter().write(randomData);
    // connection.getAuthenticationWriter().flush();
    // connection.getAuthenticationReader().readFully(randomData);
    
    connection.getSecureRandom().nextBytes(randomData);
    connection.getAuthenticationWriter().write(randomData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(digestedUser);
    
    connection.getSecureRandom().nextBytes(randomData);
    connection.getAuthenticationWriter().write(randomData);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(digestedPassword);
    
    receivedCredential = new byte[VT.VT_SECURITY_SEED_SIZE];
    byte[] digestedCredential = new byte[VT.VT_SECURITY_SEED_SIZE];
    byte[] storedCredential = new byte[VT.VT_SECURITY_SEED_SIZE];
    System.arraycopy(digestedUser, 0, receivedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
    System.arraycopy(digestedPassword, 0, receivedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
    if (server.getUserCredentials().size() > 0)
    {
      for (Credential credential : server.getUserCredentials())
      {
        blake3Digest.reset();
        String credentialUser = credential.getUser();
        String credentialPassword = credential.getPassword();
        System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, credentialUser.getBytes("UTF-8")), 0, storedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
        System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, credentialPassword.getBytes("UTF-8")), 0, storedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
        System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
        blake3Digest.update(digestedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
        blake3Digest.update(localNonce);
        System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, remoteNonce), 0, digestedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
        blake3Digest.update(digestedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
        blake3Digest.update(localNonce);
        System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, remoteNonce), 0, digestedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
        if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
        {
          blake3Digest.update(remoteNonce);
          blake3Digest.update(localNonce);
          // connection.getConnectionSocket().setSoTimeout(0);
          connection.getAuthenticationWriter().write(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, VT_AUTHENTICATION_ACCEPTED_STRING));
          connection.getAuthenticationWriter().flush();
          connection.getAuthenticationReader().readFully(randomData);
          // VTConsole.print("\rVT>Authentication
          // successful!\nVT>");
          user = credential.getUser();
          password = credential.getPassword();
          //user = server.getUserCredentials().get(storedCredential).getUser();
          //password = server.getUserCredentials().get(storedCredential).getPassword();
          accepted = true;
          stopTimeoutThread();
          return true;
        }
      }
    }
    else
    {
      blake3Digest.reset();
      System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
      blake3Digest.update(digestedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
      blake3Digest.update(localNonce);
      System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, remoteNonce), 0, digestedCredential, 0, VT.VT_SECURITY_DIGEST_SIZE);
      blake3Digest.update(digestedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
      blake3Digest.update(localNonce);
      System.arraycopy(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, remoteNonce), 0, digestedCredential, VT.VT_SECURITY_DIGEST_SIZE, VT.VT_SECURITY_DIGEST_SIZE);
      if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
      {
        blake3Digest.update(remoteNonce);
        blake3Digest.update(localNonce);
        // connection.getConnectionSocket().setSoTimeout(0);
        connection.getAuthenticationWriter().write(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, VT_AUTHENTICATION_ACCEPTED_STRING));
        connection.getAuthenticationWriter().flush();
        connection.getAuthenticationReader().readFully(randomData);
        // VTConsole.print("\rVT>Authentication
        // successful!\nVT>");
        //user = server.getUserCredentials().get(storedCredential).getUser();
        //password = server.getUserCredentials().get(storedCredential).getPassword();
        user = "";
        password = "";
        accepted = true;
        stopTimeoutThread();
        return true;
      }
    }
    // VTConsole.print("\rVT>Authentication failed!\nVT>");
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    // connection.getConnectionSocket().setSoTimeout(0);
    connection.getAuthenticationWriter().write(blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE, VT_AUTHENTICATION_REJECTED_STRING));
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(randomData);
    stopTimeoutThread();
    return false;
  }
}