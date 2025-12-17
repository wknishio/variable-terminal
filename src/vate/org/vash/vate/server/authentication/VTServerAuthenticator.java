package org.vash.vate.server.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.VTSystem;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTCredential;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;

public class VTServerAuthenticator
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
  
  private byte[] computeSecurityDigest(byte[]... values)
  {
    blake3Digest.reset();
    for (byte[] value : values)
    {
      if (value != null && value.length > 0)
      {
        blake3Digest.update(value);
      }
    }
    return blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
  }
  
  public boolean tryAuthentication() throws IOException
  {
    localNonce = connection.getLocalNonce();
    remoteNonce = connection.getRemoteNonce();
    encryptionKey = connection.getEncryptionKey();
    
    blake3Digest.reset();
    byte[] seed = new byte[VTSystem.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, seed, 0, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, seed, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
    
    List<byte[]> possibleCredentials = new ArrayList<byte[]>();
    VTCredential[] serverCredentials = server.getUserCredentials().toArray(new VTCredential[] {});
    if (server.getUserCredentials().size() > 0)
    {
      for (VTCredential credential : server.getUserCredentials())
      {
        String credentialUser = credential.getUser();
        String credentialPassword = credential.getPassword();
        possibleCredentials.add(computeSecurityDigest(localNonce, remoteNonce, encryptionKey, credentialUser.getBytes("UTF-8"), credentialPassword.getBytes("UTF-8")));
      }
    }
    else
    {
      possibleCredentials.add(computeSecurityDigest(localNonce, remoteNonce, encryptionKey));
    }
    
    connection.getSecureRandom().nextBytes(digestedCredential);
    connection.getAuthenticationWriter().write(digestedCredential);
    connection.getAuthenticationWriter().flush();
    connection.getAuthenticationReader().readFully(receivedCredential);
    
    int i = 0;
    for (byte[] possibleCredential : possibleCredentials)
    {
      if (VTArrayComparator.arrayEquals(possibleCredential, receivedCredential))
      {
        if (serverCredentials.length > 0)
        {
          VTCredential credential = serverCredentials[i];
          user = credential.getUser();
          password = credential.getPassword();
        }
        else
        {
          user = "";
          password = "";
        }
        digestedCredential = possibleCredential;
        accepted = true;
        stopTimeoutThread();
        return true;
      }
      i++;
    }
    stopTimeoutThread();
    return false;
  }
}