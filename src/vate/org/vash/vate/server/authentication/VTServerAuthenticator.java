package org.vash.vate.server.authentication;

import java.io.IOException;

import org.vash.vate.VT;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTCredential;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;

public class VTServerAuthenticator
{
  private boolean accepted = false;
  private byte[] digestedCredential = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] receivedCredential = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] localNonce;
  private byte[] remoteNonce;
  private byte[] encryptionKey;
  private String user;
  private String password;
  private final VTBlake3MessageDigest blake3Digest;
  private VTServer server;
  private VTServerConnection connection;
  private VTServerAuthenticatorTimeoutTask timeoutTask = new VTServerAuthenticatorTimeoutTask();
  
  private class VTServerAuthenticatorTimeoutTask implements Runnable
  {
    public void start()
    {
      accepted = false;
      server.getExecutorService().execute(this);
    }
    
    public void run()
    {
      try
      {
        if (!accepted)
        {
          synchronized (this)
          {
            wait(server.getPingLimitMilliseconds());
          }
        }
      }
      catch (Throwable e)
      {
        
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
    }
    
    public void stop()
    {
      try
      {
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
  }
  
  public void startTimeoutThread()
  {
    timeoutTask.start();
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
  
  public byte[] getDigestedCredential()
  {
    return digestedCredential;
  }
  
  public boolean tryAuthentication() throws IOException
  {
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    encryptionKey = connection.getEncryptionKey();
    
    blake3Digest.reset();
    byte[] seed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, seed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, seed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
    
    connection.getSecureRandom().nextBytes(digestedCredential);
    connection.getAuthenticationWriter().write(digestedCredential);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(receivedCredential);
    
    if (server.getUserCredentials().size() > 0)
    {
      for (VTCredential credential : server.getUserCredentials())
      {
        String credentialUser = credential.getUser();
        String credentialPassword = credential.getPassword();
        blake3Digest.reset();
        blake3Digest.update(localNonce);
        blake3Digest.update(remoteNonce);
        blake3Digest.update(encryptionKey);
        blake3Digest.update(credentialUser.getBytes("UTF-8"));
        blake3Digest.update(credentialPassword.getBytes("UTF-8"));
        digestedCredential = blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES);
        
        if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
        {
          user = credential.getUser();
          password = credential.getPassword();
          accepted = true;
          stopTimeoutThread();
          return true;
        }
      }
    }
    else
    {
      blake3Digest.reset();
      blake3Digest.update(localNonce);
      blake3Digest.update(remoteNonce);
      blake3Digest.update(encryptionKey);
      digestedCredential = blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES);
      
      if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
      {
        user = "";
        password = "";
        accepted = true;
        stopTimeoutThread();
        return true;
      }
    }
    stopTimeoutThread();
    return false;
  }
}