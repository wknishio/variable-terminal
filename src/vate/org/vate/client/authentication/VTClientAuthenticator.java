package org.vate.client.authentication;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.client.connection.VTClientConnection;
import org.vate.security.VTArrayComparator;

public class VTClientAuthenticator
{
	private static byte[] VT_AUTHENTICATION_ACCEPTED_STRING = new byte[16];
	
	static
	{
		try
		{
			VT_AUTHENTICATION_ACCEPTED_STRING = ("VT/ACCEPT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION).getBytes("UTF-8");
		}
		catch (Throwable e)
		{
			
		}
	}
	
	private volatile boolean accepted = false;
	private int credentialCounter;
	private byte[] localNonce;
	private byte[] remoteNonce;
	private byte[] digestedLogin = new byte[32];
	private byte[] digestedPassword = new byte[32];
	private byte[] authResult = new byte[32];
	private byte[] randomData = new byte[32];
	private String login;
	private String password;
	private MessageDigest sha256Digester;
	private VTClient client;
	private VTClientConnection connection;
	private VTClientAuthenticatorTimeoutTask timeoutTask = new VTClientAuthenticatorTimeoutTask();
	
	private class VTClientAuthenticatorTimeoutTask implements Runnable
	{
		//private volatile boolean finished = true;
		//private Thread timeoutThread;
		
		public void start()
		{
			accepted = false;
			//finished = false;
			//timeoutThread = new Thread(this, getClass().getSimpleName());
			//timeoutThread.start();
			client.getClientThreads().execute(this);
		}
		
		public void run()
		{
			try
			{
				//Thread.currentThread().setName(getClass().getSimpleName());
				//timeoutThread = Thread.currentThread();
				//Thread.sleep(VT.VT_AUTHENTICATION_TIMEOUT_MILLISECONDS);
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
			//finished = true;
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
		this.credentialCounter = 0;
		try
		{
			this.sha256Digester = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			
		}
	}
	
	public void startTimeoutThread()
	{
		
		timeoutTask.start();
		//client.getClientThreads().execute(timeoutTask);
	}
	
	public void stopTimeoutThread()
	{
		timeoutTask.stop();
	}
	
	public byte[] getDigestedLogin()
	{
		return digestedLogin;
	}
	
	public byte[] getDigestedPassword()
	{
		return digestedPassword;
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public boolean tryAuthentication() throws InterruptedException, IOException
	{
		credentialCounter = 0;
		connection.exchangeNonces(true);
		localNonce = connection.getLocalNonce();
		remoteNonce = connection.getRemoteNonce();
		try
		{
			// connection.getConnectionSocket().setSoTimeout(0);
			// VTConsole.print("\nVT>Starting authentication...");
			while (credentialCounter < 2)
			{
				if (!writeCredential())
				{
					stopTimeoutThread();
					return false;
				}
			}
			connection.getSecureRandom().nextBytes(randomData);
			connection.getAuthenticationWriter().write(randomData);
			connection.getAuthenticationWriter().flush();
			connection.getAuthenticationReader().readFully(authResult);
			// connection.getConnectionSocket().setSoTimeout(0);
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (VTArrayComparator.arrayEquals(authResult, sha256Digester.digest(VT_AUTHENTICATION_ACCEPTED_STRING)))
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
		catch (Throwable e)
		{
			// VTConsole.print("\nVT>Authentication failed!");
			stopTimeoutThread();
			return false;
		}
	}
	
	public boolean writeCredential() throws IOException, InterruptedException
	{
		String line = "";
		byte[] credentialData = null;
		if (credentialCounter == 0)
		{
			if (client.getLogin() != null)
			{
				line = client.getLogin();
				login = line;
			}
			else
			{
				
			}
			sha256Digester.update(sha256Digester.digest(line.getBytes("UTF-8")));
			sha256Digester.update(remoteNonce);
			digestedLogin = sha256Digester.digest(localNonce);
			credentialData = digestedLogin;
		}
		else if (credentialCounter == 1)
		{
			if (client.getPassword() != null)
			{
				line = client.getPassword();
				password = line;
			}
			else
			{
				
			}
			sha256Digester.update(sha256Digester.digest(line.getBytes("UTF-8")));
			sha256Digester.update(remoteNonce);
			digestedPassword = sha256Digester.digest(localNonce);
			credentialData = digestedPassword;
		}
		else
		{
			
		}
		connection.getAuthenticationWriter().write(credentialData);
		connection.getAuthenticationWriter().flush();
		connection.getAuthenticationReader().readFully(randomData, 0, randomData.length);
		credentialCounter++;
		return true;
	}
}