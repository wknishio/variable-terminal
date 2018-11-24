package org.vate.server.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.vate.VT;
import org.vate.security.VTArrayComparator;
import org.vate.server.VTServer;
import org.vate.server.connection.VTServerConnection;

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
	private String login;
	private String password;
	private byte[] digestedLogin = new byte[32];
	private byte[] digestedPassword = new byte[32];
	private byte[] randomData = new byte[32];
	private byte[] receivedCredential;
	private byte[] localNonce;
	private byte[] remoteNonce;
	private MessageDigest sha256Digester;
	private VTServer server;
	private VTServerConnection connection;
	private VTServerAuthenticatorTimeoutTask timeoutTask = new VTServerAuthenticatorTimeoutTask();
	
	private class VTServerAuthenticatorTimeoutTask implements Runnable
	{
		//private volatile boolean finished = true;
		//private Thread timeoutThread;
		
		public void start()
		{
			accepted = false;
			//finished = false;
			//timeoutThread = new Thread(this, getClass().getSimpleName());
			//timeoutThread.start();
			server.getServerThreads().execute(this);
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
	
	public VTServerAuthenticator(VTServer server, VTServerConnection connection)
	{
		this.server = server;
		this.connection = connection;
		// this.localNonce = connection.getLocalNonce();
		// this.remoteNonce = connection.getRemoteNonce();
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
		accepted = false;
		timeoutTask.start();
		//server.getServerThreads().execute(timeoutTask);
	}
	
	public void stopTimeoutThread()
	{
		timeoutTask.stop();
	}
	
	public String getLogin()
	{
		return login;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public byte[] getDigestedLogin()
	{
		return digestedLogin;
	}
	
	public byte[] getDigestedPassword()
	{
		return digestedPassword;
	}
	
	public boolean tryAuthentication()
	{
		try
		{
			// connection.getConnectionSocket().setSoTimeout(10000);
			// VTConsole.print("\rVT>Starting authentication...\nVT>");
			connection.exchangeNonces(true);
			localNonce = connection.getLocalNonce();
			remoteNonce = connection.getRemoteNonce();
			connection.getSecureRandom().nextBytes(randomData);
			connection.getAuthenticationWriter().write(randomData);
			connection.getAuthenticationWriter().flush();
			connection.getAuthenticationReader().readFully(digestedLogin);
			connection.getSecureRandom().nextBytes(randomData);
			connection.getAuthenticationWriter().write(randomData);
			connection.getAuthenticationWriter().flush();
			connection.getAuthenticationReader().readFully(digestedPassword);
			receivedCredential = new byte[64];
			byte[] digestedCredential = new byte[64];
			System.arraycopy(digestedLogin, 0, receivedCredential, 0, 32);
			System.arraycopy(digestedPassword, 0, receivedCredential, 32, 32);
			if (server.getUserCredentials().size() > 0)
			{
				for (byte[] storedCredential : server.getUserCredentials().keySet())
				{
					System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
					sha256Digester.update(digestedCredential, 0, 32);
					sha256Digester.update(localNonce);
					System.arraycopy(sha256Digester.digest(remoteNonce), 0, digestedCredential, 0, 32);
					sha256Digester.update(digestedCredential, 32, 32);
					sha256Digester.update(localNonce);
					System.arraycopy(sha256Digester.digest(remoteNonce), 0, digestedCredential, 32, 32);
					if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
					{
						sha256Digester.update(remoteNonce);
						sha256Digester.update(localNonce);
						// connection.getConnectionSocket().setSoTimeout(0);
						connection.getAuthenticationWriter().write(sha256Digester.digest(VT_AUTHENTICATION_ACCEPTED_STRING));
						connection.getAuthenticationWriter().flush();
						connection.getAuthenticationReader().readFully(randomData);
						// VTConsole.print("\rVT>Authentication
						// successful!\nVT>");
						login = server.getUserCredentials().get(storedCredential).login;
						password = server.getUserCredentials().get(storedCredential).password;
						accepted = true;
						stopTimeoutThread();
						return true;
					}
				}
			}
			else
			{
				byte[] storedCredential = new byte[64];
				System.arraycopy(storedCredential, 0, digestedCredential, 0, storedCredential.length);
				sha256Digester.update(digestedCredential, 0, 32);
				sha256Digester.update(localNonce);
				System.arraycopy(sha256Digester.digest(remoteNonce), 0, digestedCredential, 0, 32);
				sha256Digester.update(digestedCredential, 32, 32);
				sha256Digester.update(localNonce);
				System.arraycopy(sha256Digester.digest(remoteNonce), 0, digestedCredential, 32, 32);
				if (VTArrayComparator.arrayEquals(digestedCredential, receivedCredential))
				{
					sha256Digester.update(remoteNonce);
					sha256Digester.update(localNonce);
					// connection.getConnectionSocket().setSoTimeout(0);
					connection.getAuthenticationWriter().write(sha256Digester.digest(VT_AUTHENTICATION_ACCEPTED_STRING));
					connection.getAuthenticationWriter().flush();
					connection.getAuthenticationReader().readFully(randomData);
					// VTConsole.print("\rVT>Authentication
					// successful!\nVT>");
					login = server.getUserCredentials().get(storedCredential).login;
					password = server.getUserCredentials().get(storedCredential).password;
					accepted = true;
					stopTimeoutThread();
					return true;
				}
			}
			// VTConsole.print("\rVT>Authentication failed!\nVT>");
			sha256Digester.update(remoteNonce);
			sha256Digester.update(localNonce);
			// connection.getConnectionSocket().setSoTimeout(0);
			connection.getAuthenticationWriter().write(sha256Digester.digest(VT_AUTHENTICATION_REJECTED_STRING));
			connection.getAuthenticationWriter().flush();
			connection.getAuthenticationReader().readFully(randomData);
			stopTimeoutThread();
			return false;
		}
		catch (Throwable e)
		{
			// VTConsole.print("\rVT>Authentication failed!\nVT>");
			try
			{
				sha256Digester.update(remoteNonce);
				sha256Digester.update(localNonce);
				// connection.getConnectionSocket().setSoTimeout(0);
				connection.getAuthenticationWriter().write(sha256Digester.digest(VT_AUTHENTICATION_REJECTED_STRING));
				connection.getAuthenticationWriter().flush();
				connection.getAuthenticationReader().readFully(randomData);
				stopTimeoutThread();
				return false;
			}
			catch (Throwable e1)
			{
				stopTimeoutThread();
				return false;
			}
		}
	}
}