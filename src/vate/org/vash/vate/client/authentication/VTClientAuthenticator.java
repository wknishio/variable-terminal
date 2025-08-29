package org.vash.vate.client.authentication;

import java.io.IOException;
import org.vash.vate.VTSystem;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.security.VTBlake3MessageDigest;

public class VTClientAuthenticator
{
  private boolean accepted = false;
  private byte[] digestedCredential = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] receivedCredential = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private byte[] localNonce;
  private byte[] remoteNonce;
  private byte[] encryptionKey;
  private String user;
  private String password;
  private final VTBlake3MessageDigest blake3Digest;
  private VTClient client;
  private VTClientConnection connection;
  private VTClientAuthenticatorTimeoutTask timeoutTask = new VTClientAuthenticatorTimeoutTask();
  
  private class VTClientAuthenticatorTimeoutTask implements Runnable
  {
    public void start()
    {
      accepted = false;
      client.getExecutorService().execute(this);
    }
    
    public void run()
    {
      try
      {
        if (!accepted)
        {
          synchronized (this)
          {
            wait(client.getPingLimitMilliseconds());
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
  
  public VTClientAuthenticator(VTClient client, VTClientConnection connection)
  {
    this.client = client;
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
  
  public byte[] getDigestedCredential()
  {
    return digestedCredential;
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
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    encryptionKey = connection.getEncryptionKey();
    
    blake3Digest.reset();
    byte[] seed = new byte[VTSystem.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(localNonce, 0, seed, 0, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(remoteNonce, 0, seed, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
    
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    blake3Digest.update(encryptionKey);
    
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
    
    digestedCredential = blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    
    connection.getAuthenticationWriter().write(digestedCredential);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(receivedCredential, 0, receivedCredential.length);
    
    accepted = true;
    stopTimeoutThread();
    return true;
  }
}