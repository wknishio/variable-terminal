package org.vate.client.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.NoSuchPaddingException;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.security.VTArrayComparator;
import org.vate.security.VTCryptographicEngine;
import org.vate.stream.compress.VTCompressorSelector;
import org.vate.stream.endian.VTLittleEndianInputStream;
import org.vate.stream.endian.VTLittleEndianOutputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTClientConnection
{
	private static byte[] VT_SERVER_CHECK_STRING_NONE = new byte[16];
	private static byte[] VT_CLIENT_CHECK_STRING_NONE = new byte[16];
	private static byte[] VT_SERVER_CHECK_STRING_RC4 = new byte[16];
	private static byte[] VT_CLIENT_CHECK_STRING_RC4 = new byte[16];
	private static byte[] VT_SERVER_CHECK_STRING_AES = new byte[16];
	private static byte[] VT_CLIENT_CHECK_STRING_AES = new byte[16];
	
	static
	{
		try
		{
			VT_SERVER_CHECK_STRING_NONE = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/NONE").getBytes("UTF-8");
			VT_CLIENT_CHECK_STRING_NONE = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/NONE").getBytes("UTF-8");
			VT_SERVER_CHECK_STRING_RC4 = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/RC4").getBytes("UTF-8");
			VT_CLIENT_CHECK_STRING_RC4 = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/RC4").getBytes("UTF-8");
			VT_SERVER_CHECK_STRING_AES = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/AES").getBytes("UTF-8");
			VT_CLIENT_CHECK_STRING_AES = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/AES").getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			
		}
	}
	
	private int encryptionType;
	private byte[] encryptionKey;
	private byte[] digestedClient;
	private byte[] digestedServer;
	private byte[] localNonce = new byte[512];
	private byte[] remoteNonce = new byte[512];
	private byte[] randomData = new byte[512];
	private MessageDigest sha256Digester;
	private SecureRandom secureRandom;
	private VTCryptographicEngine cryptoEngine;
	private Socket connectionSocket;
	private InputStream connectionSocketInputStream;
	private OutputStream connectionSocketOutputStream;
	private InputStream connectionInputStream;
	private OutputStream connectionOutputStream;
	private VTLittleEndianInputStream nonceReader;
	private VTLittleEndianOutputStream nonceWriter;
	private VTLinkableDynamicMultiplexingInputStream multiplexedConnectionInputStream;
	private VTLinkableDynamicMultiplexingOutputStream multiplexedConnectionOutputStream;
	
	// private InputStream authenticationInputStream;
	private VTLinkableDynamicMultiplexedInputStream shellInputStream;
	private VTLinkableDynamicMultiplexedInputStream fileTransferControlInputStream;
	private VTLinkableDynamicMultiplexedInputStream fileTransferDataInputStream;
	// private VTMultiplexedInputStream graphicsCheckInputStream;
	private VTLinkableDynamicMultiplexedInputStream graphicsControlInputStream;
	private VTLinkableDynamicMultiplexedInputStream graphicsDirectImageInputStream;
	private VTLinkableDynamicMultiplexedInputStream graphicsDeflatedImageInputStream;
	private VTLinkableDynamicMultiplexedInputStream graphicsSnappedImageInputStream;
	private VTLinkableDynamicMultiplexedInputStream graphicsClipboardInputStream;
	private InputStream clipboardDataInputStream;
	private VTLinkableDynamicMultiplexedInputStream audioDataInputStream;
	private VTLinkableDynamicMultiplexedInputStream audioControlInputStream;
	private VTLinkableDynamicMultiplexedInputStream pingInputStream;
	private VTLinkableDynamicMultiplexedInputStream tunnelControlInputStream;
	private VTLinkableDynamicMultiplexedInputStream socksControlInputStream;
	
	// private OutputStream authenticationOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream shellOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream fileTransferControlOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream fileTransferDataOutputStream;
	// private VTMultiplexedOutputStream graphicsCheckOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream graphicsControlOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream graphicsDirectImageOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream graphicsDeflatedImageOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream graphicsSnappedImageOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream graphicsClipboardOutputStream;
	private OutputStream clipboardDataOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream audioDataOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream audioControlOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream pingOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream tunnelControlOutputStream;
	private VTLinkableDynamicMultiplexedOutputStream socksControlOutputStream;
	
	private VTLittleEndianInputStream verificationReader;
	private VTLittleEndianOutputStream verificationWriter;
	private VTLittleEndianInputStream authenticationReader;
	private VTLittleEndianOutputStream authenticationWriter;
	private BufferedReader resultReader;
	private BufferedWriter commandWriter;
	private InputStream shellDataInputStream;
	private OutputStream shellDataOutputStream;
	private VTLittleEndianInputStream fileTransferControlDataInputStream;
	private VTLittleEndianOutputStream fileTransferControlDataOutputStream;
	// private VTLittleEndianInputStream graphicsCheckDataInputStream;
	// private VTLittleEndianOutputStream graphicsCheckDataOutputStream;
	private VTLittleEndianInputStream graphicsControlDataInputStream;
	private VTLittleEndianOutputStream graphicsControlDataOutputStream;
	
	private InputStream directImageDataInputStream;
	private OutputStream directImageDataOutputStream;
	private InputStream deflatedImageDataInputStream;
	private OutputStream deflatedImageDataOutputStream;
	private InputStream snappedImageDataInputStream;
	private OutputStream snappedImageDataOutputStream;
	
	//private boolean zstdAvailable;
	
	// private ZstdInputStream zstdImageInputStream;
	
	// private ZstdInputStream zstdClipboardInputStream;
	// private ZstdOutputStream zstdClipboardOutputStream;
	
	public VTClientConnection()
	{
		try
		{
			this.sha256Digester = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			// e.printStackTrace();
		}
		this.cryptoEngine = new VTCryptographicEngine(sha256Digester);
		this.secureRandom = new SecureRandom();
	}
	
	public SecureRandom getSecureRandom()
	{
		return secureRandom;
	}
	
	public VTLinkableDynamicMultiplexingInputStream getMultiplexedConnectionInputStream()
	{
		return multiplexedConnectionInputStream;
	}
	
	public VTLinkableDynamicMultiplexingOutputStream getMultiplexedConnectionOutputStream()
	{
		return multiplexedConnectionOutputStream;
	}
	
	public byte[] getLocalNonce()
	{
		return localNonce;
	}
	
	public byte[] getRemoteNonce()
	{
		return remoteNonce;
	}
	
	public void setEncryptionType(int encryptionType)
	{
		this.encryptionType = encryptionType;
	}
	
	public void setEncryptionKey(byte[] encryptionKey)
	{
		this.encryptionKey = encryptionKey;
	}
	
	public Socket getConnectionSocket()
	{
		return connectionSocket;
	}
	
	public void setConnectionSocket(Socket connectionSocket)
	{
		this.connectionSocket = connectionSocket;
	}
	
	/* public InputStream getAuthenticationInputStream() { return
	 * authenticationInputStream; } */
	
	public InputStream getShellInputStream()
	{
		return shellInputStream;
	}
	
	public InputStream getFileTransferDataInputStream()
	{
		return fileTransferDataInputStream;
	}
	
	public InputStream getGraphicsControlInputStream()
	{
		return graphicsControlInputStream;
	}
	
	/* public InputStream getGraphicsImageInputStream() { return
	 * graphicsImageInputStream; } */
	
		
	public InputStream getAudioDataInputStream()
	{
		return audioDataInputStream;
	}
	
	public InputStream getAudioControlInputStream()
	{
		return audioControlInputStream;
	}
	
	public InputStream getPingInputStream()
	{
		return pingInputStream;
	}
	
	public InputStream getTunnelControlInputStream()
	{
		return tunnelControlInputStream;
	}

	public InputStream getSocksControlInputStream()
	{
		return socksControlInputStream;
	}
		
	/* public OutputStream getAuthenticationOutputStream() { return
	 * authenticationOutputStream; } */
	
	public OutputStream getShellOutputStream()
	{
		return shellOutputStream;
	}
	
	public OutputStream getFileTransferDataOutputStream()
	{
		return fileTransferDataOutputStream;
	}
	
	/* public OutputStream getGraphicsImageOutputStream() { return
	 * graphicsImageOutputStream; } */
			
	public OutputStream getAudioDataOutputStream()
	{
		return audioDataOutputStream;
	}
	
	public OutputStream getAudioControlOutputStream()
	{
		return audioControlOutputStream;
	}
	
	public OutputStream getPingOutputStream()
	{
		return pingOutputStream;
	}
	
	public OutputStream getTunnelControlOutputStream()
	{
		return tunnelControlOutputStream;
	}
		
	public OutputStream getSocksControlOutputStream()
	{
		return socksControlOutputStream;
	}
		
	public VTLittleEndianInputStream getAuthenticationReader()
	{
		return authenticationReader;
	}
	
	public BufferedReader getResultReader()
	{
		return resultReader;
	}
	
	public VTLittleEndianOutputStream getAuthenticationWriter()
	{
		return authenticationWriter;
	}
	
	public BufferedWriter getCommandWriter()
	{
		return commandWriter;
	}
	
	public VTLittleEndianInputStream getFileTransferControlDataInputStream()
	{
		return fileTransferControlDataInputStream;
	}
	
	public VTLittleEndianOutputStream getFileTransferControlDataOutputStream()
	{
		return fileTransferControlDataOutputStream;
	}
	
	// public VTLittleEndianInputStream getGraphicsCheckDataInputStream()
	// {
	// return graphicsCheckDataInputStream;
	// }
	
	// public VTLittleEndianOutputStream getGraphicsCheckDataOutputStream()
	// {
	// return graphicsCheckDataOutputStream;
	// }
	
	public VTLittleEndianInputStream getGraphicsControlDataInputStream()
	{
		return graphicsControlDataInputStream;
	}
	
	public VTLittleEndianOutputStream getGraphicsControlDataOutputStream()
	{
		return graphicsControlDataOutputStream;
	}
	
	public InputStream getGraphicsDirectImageDataInputStream()
	{
		return directImageDataInputStream;
	}
	
	public InputStream getGraphicsDeflatedImageDataInputStream()
	{
		return deflatedImageDataInputStream;
	}
	
	public InputStream getGraphicsSnappedImageDataInputStream()
	{
		return snappedImageDataInputStream;
	}
	
	public OutputStream getGraphicsDirectImageDataOutputStream()
	{
		return directImageDataOutputStream;
	}
	
	public OutputStream getGraphicsDeflatedImageDataOutputStream()
	{
		return deflatedImageDataOutputStream;
	}
	
	public OutputStream getGraphicsSnappedImageDataOutputStream()
	{
		return snappedImageDataOutputStream;
	}
	
	public InputStream getGraphicsClipboardInputStream()
	{
		return graphicsClipboardInputStream;
	}
	
	public OutputStream getGraphicsClipboardOutputStream()
	{
		return graphicsClipboardOutputStream;
	}
	
	public InputStream getGraphicsClipboardDataInputStream()
	{
		return clipboardDataInputStream;
	}
	
	public OutputStream getGraphicsClipboardDataOutputStream()
	{
		return clipboardDataOutputStream;
	}
	
	public void closeSockets()
	{
//		StringBuilder message = new StringBuilder();
//		message.append("\nVT>closeSockets!");
//		message.append("\nVT>StackTrace: ");
//		StackTraceElement[] stackStrace = Thread.currentThread().getStackTrace();
//		for (int i = stackStrace.length - 1; i >= 0; i--)
//		{
//			message.append(stackStrace[i].toString() + "\n");
//		}
//		VTConsole.print(message.toString());
//		VTConsole.print("\nVT>CloseSockets: [" + Thread.currentThread().getName() + "]\nVT>");
		if (connectionSocket != null)
		{
			try
			{
				connectionSocket.close();
			}
			catch (IOException e)
			{
				
			}
		}
		if (multiplexedConnectionOutputStream != null)
		{
			try
			{
				multiplexedConnectionOutputStream.close();
			}
			catch (IOException e)
			{
				
			}
		}
		if (multiplexedConnectionInputStream != null)
		{
			try
			{
				multiplexedConnectionInputStream.stopPacketReader();
			}
			catch (IOException e)
			{
				
			}
			catch (InterruptedException e)
			{
				
			}
		}
		if (authenticationReader != null)
		{
			try
			{
				authenticationReader.close();
			}
			catch (Throwable t)
			{
				
			}
		}
		if (authenticationWriter != null)
		{
			try
			{
				authenticationWriter.close();
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
//	public void closeSocketsFromDialog()
//	{
//		dialog = true;
//		closeSockets();
//	}
	
	public void closeConnection()
	{
//		if (dialog)
//		{
//			VTConsole.print("\nVT>Connection with server closed!\n");
//		}
//		else
//		{
//			VTConsole.print("\nVT>Connection with server closed!\n");
//		}
//		dialog = false;
		VTConsole.print("\nVT>Connection with server closed!");
		VTConsole.setCommandEcho(true);
		closeSockets();
		try
		{
			VTConsole.interruptReadLine();
		}
		catch (Throwable t)
		{
			
		}
		
		synchronized (this)
		{
			notifyAll();
		}
	}
	
	public boolean isConnected()
	{
		return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed();
	}
	
	public void setNonceStreams() throws IOException
	{
		connectionSocketInputStream = connectionSocket.getInputStream();
		connectionSocketOutputStream = connectionSocket.getOutputStream();
		nonceReader = new VTLittleEndianInputStream(connectionSocketInputStream);
		nonceWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
		// Arrays.fill(localNonce, (byte)0);
		// Arrays.fill(remoteNonce, (byte)0);
	}
	
	public void exchangeNonces(boolean update) throws IOException
	{
		secureRandom.nextBytes(randomData);
		nonceWriter.write(randomData);
		nonceWriter.flush();
		if (update)
		{
			for (int i = 0; i < randomData.length; i++)
			{
				localNonce[i] ^= randomData[i];
			}
		}
		else
		{
			for (int i = 0; i < randomData.length; i++)
			{
				localNonce[i] = randomData[i];
			}
		}
		nonceReader.readFully(randomData);
		if (update)
		{
			for (int i = 0; i < randomData.length; i++)
			{
				remoteNonce[i] ^= randomData[i];
			}
		}
		else
		{
			for (int i = 0; i < randomData.length; i++)
			{
				remoteNonce[i] = randomData[i];
			}
		}
	}
	
	public void setVerificationStreams()
	{
		verificationReader = new VTLittleEndianInputStream(connectionSocketInputStream);
		verificationWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
	}
	
	public void setAuthenticationStreams() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
			authenticationReader = new VTLittleEndianInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
			authenticationWriter = new VTLittleEndianOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
			authenticationReader = new VTLittleEndianInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
			authenticationWriter = new VTLittleEndianOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
		}
		else
		{
			authenticationReader = new VTLittleEndianInputStream(connectionSocketInputStream);
			authenticationWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
		}
	}
	
	public void setConnectionStreams(byte[] digestedLogin, byte[] digestedPassword, String login, String password) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce, digestedLogin, digestedPassword, login != null ? login.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
			connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
			connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce, digestedLogin, digestedPassword, login != null ? login.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
			connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
			connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
		}
		else
		{
			connectionInputStream = connectionSocketInputStream;
			connectionOutputStream = connectionSocketOutputStream;
		}
		authenticationReader = new VTLittleEndianInputStream(connectionInputStream);
		authenticationWriter = new VTLittleEndianOutputStream(connectionOutputStream);
	}
	
	private void setMultiplexedStreams() throws IOException
	{
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
		{
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_BUFFER_SIZE, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_SIZE, false);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_BUFFER_SIZE, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_SIZE, false);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_BUFFER_SIZE, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_SIZE, false);
		}
		
		multiplexedConnectionInputStream.startPacketReader();
		
		pingInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED, 0);
		pingOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED, 0);
		
		shellInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);
		shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);
		
		fileTransferControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
		fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
		fileTransferDataInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
		fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
		fileTransferDataInputStream.addPropagated(fileTransferDataOutputStream);
		
		graphicsControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 4);
		graphicsControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 4);
		graphicsDirectImageInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 5);
		graphicsDirectImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 5);
		graphicsDeflatedImageInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 6);
		graphicsDeflatedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 6);
		graphicsSnappedImageInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 7);
		graphicsSnappedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 7);
		graphicsClipboardInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 8);
		graphicsClipboardOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 8);
		
		//graphicsControlInputStream.addPropagated(graphicsControlOutputStream);
		graphicsControlInputStream.addPropagated(graphicsDirectImageInputStream);
		graphicsControlInputStream.addPropagated(graphicsDeflatedImageInputStream);
		graphicsControlInputStream.addPropagated(graphicsSnappedImageInputStream);
		//graphicsControlInputStream.addPropagated(graphicsClipboardInputStream);
		//graphicsControlInputStream.addPropagated(graphicsClipboardOutputStream);
		
		audioDataInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
		audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
		audioControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
		audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
		
		tunnelControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);
		tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);
		
		socksControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);
		socksControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);
		
		shellDataOutputStream = VTCompressorSelector.createFlushBufferedZstdOutputStream(shellOutputStream);
		//shellDataOutputStream = VTCompressorSelector.createCompatibleSyncFlushDeflaterOutputStream(shellOutputStream);
		//shellDataOutputStream = shellOutputStream;
		
		shellDataInputStream = VTCompressorSelector.createFlushBufferedZstdInputStream(shellInputStream);
		//shellDataInputStream = VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(shellInputStream);
		//shellDataInputStream = shellInputStream;
		
		resultReader = new BufferedReader(new InputStreamReader(shellDataInputStream, "UTF-8"));
		commandWriter = new BufferedWriter(new OutputStreamWriter(shellDataOutputStream, "UTF-8"));
		
		graphicsControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(graphicsControlInputStream));
		graphicsControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
				
		directImageDataInputStream = (new BufferedInputStream(graphicsDirectImageInputStream, VT.VT_STANDARD_DATA_BUFFER_SIZE));
		directImageDataOutputStream = (graphicsDirectImageOutputStream);
				
		//deflatedImageDataInputStream = VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(graphicsDeflatedImageInputStream);
		deflatedImageDataInputStream = VTCompressorSelector.createFlushBufferedZstdInputStream(graphicsDeflatedImageInputStream);
		deflatedImageDataOutputStream = graphicsDeflatedImageOutputStream;
		
		snappedImageDataInputStream = VTCompressorSelector.createFlushBufferedLZ4InputStream(graphicsSnappedImageInputStream);
		snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);
		
		//clipboardDataOutputStream = new VTBufferedOutputStream(new LZ4BlockOutputStream(graphicsClipboardOutputStream, VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		clipboardDataOutputStream = VTCompressorSelector.createFlushBufferedZstdOutputStream(graphicsClipboardOutputStream);
		//clipboardDataInputStream = new BufferedInputStream(new InflaterInputStream(graphicsClipboardInputStream, VT.VT_IO_BUFFFER_SIZE, true));
		
		//clipboardDataInputStream = new BufferedInputStream(new LZ4BlockInputStream(graphicsClipboardInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_STANDARD_DATA_BUFFER_SIZE);		
		clipboardDataInputStream = VTCompressorSelector.createFlushBufferedZstdInputStream(graphicsClipboardInputStream);
		
		fileTransferControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(fileTransferControlInputStream));
		fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(fileTransferControlOutputStream));
		
		//graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
		//graphicsControlInputStream.addPropagated(snappedImageDataInputStream);
		
		//graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
		//graphicsControlInputStream.addPropagated(clipboardDataInputStream);
	}
	
	public boolean verifyConnection() throws IOException, NoSuchAlgorithmException
	{
		exchangeNonces(true);
		byte[] VT_CLIENT_STRING = VT_CLIENT_CHECK_STRING_NONE;
		byte[] VT_SERVER_STRING = VT_SERVER_CHECK_STRING_NONE;
		if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
		{
			VT_CLIENT_STRING = VT_CLIENT_CHECK_STRING_NONE;
			sha256Digester.reset();
			sha256Digester.update(remoteNonce);
			sha256Digester.update(localNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedClient = sha256Digester.digest(VT_CLIENT_STRING);
			verificationWriter.write(digestedClient);
			verificationWriter.flush();
			verificationReader.readFully(digestedClient);
			
			//check for server string none
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_NONE;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
				return true;
			}
			
			//check for server string rc4
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_RC4;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
				return true;
			}
			
			//check for server string aes
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_AES;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
				return true;
			}
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			VT_CLIENT_STRING = VT_CLIENT_CHECK_STRING_RC4;
			sha256Digester.reset();
			sha256Digester.update(remoteNonce);
			sha256Digester.update(localNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedClient = sha256Digester.digest(VT_CLIENT_STRING);
			verificationWriter.write(digestedClient);
			verificationWriter.flush();
			verificationReader.readFully(digestedClient);
			
			//check for server string none
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_NONE;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
				return true;
			}
			
			//check for server string rc4
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_RC4;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
				return true;
			}
			
			//check for server string aes
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_AES;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
				return true;
			}
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			VT_CLIENT_STRING = VT_CLIENT_CHECK_STRING_AES;
			sha256Digester.reset();
			sha256Digester.update(remoteNonce);
			sha256Digester.update(localNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedClient = sha256Digester.digest(VT_CLIENT_STRING);
			verificationWriter.write(digestedClient);
			verificationWriter.flush();
			verificationReader.readFully(digestedClient);
			
			//check for server string none
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_NONE;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
				return true;
			}
			
			//check for server string rc4
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_RC4;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
				return true;
			}
			
			//check for server string aes
			VT_SERVER_STRING = VT_SERVER_CHECK_STRING_AES;
			sha256Digester.reset();
			sha256Digester.update(localNonce);
			sha256Digester.update(remoteNonce);
			if (encryptionKey != null)
			{
				sha256Digester.update(encryptionKey);
			}
			digestedServer = sha256Digester.digest(VT_SERVER_STRING);
			if (VTArrayComparator.arrayEquals(digestedServer, digestedClient))
			{
				//setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
				return true;
			}
		}
		return false;
	}
	
	/* public void startConnection() {
	 * multiplexedConnectionInputStream.startPacketReader(); } */
	
	public void startConnection() throws IOException
	{
		setMultiplexedStreams();
	}
	
	/* public boolean startedConnection() { return
	 * multiplexedConnectionInputStream != null &&
	 * multiplexedConnectionInputStream.isPacketReaderStarted(); } */
	
	public void closeGraphicsModeStreams() throws IOException
	{
		try
		{
			graphicsControlInputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsControlOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsDirectImageInputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			deflatedImageDataInputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			snappedImageDataInputStream.close();
		}
		catch (Throwable t)
		{
			
		}
//		try
//		{
//			graphicsDeflatedImageInputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		try
//		{
//			graphicsSnappedImageInputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		try
//		{
//			graphicsClipboardOutputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		try
//		{
//			graphicsDeflatedImageInputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		try
//		{
//			graphicsSnappedImageInputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		try
//		{
//			graphicsDirectImageInputStream.close();
//		}
//		catch (Throwable t)
//		{
//			
//		}
//		if (zstdAvailable)
//		{
//			try
//			{
//				deflatedImageDataInputStream.close();
//			}
//			catch (Throwable t)
//			{
//				
//			}
//		}
	}
	
	// public void resetDirectGraphicsModeStreams() throws IOException
	// {
	// graphicsDirectImageOutputStream.open();
	// graphicsDirectImageInputStream.open();
	// }
	
	public void resetGraphicsModeStreams() throws IOException
	{
		graphicsControlInputStream.open();
		graphicsControlOutputStream.open();
		
		graphicsDirectImageOutputStream.open();
		graphicsDirectImageInputStream.open();
		graphicsDeflatedImageOutputStream.open();
		graphicsDeflatedImageInputStream.open();
		graphicsSnappedImageOutputStream.open();
		graphicsSnappedImageInputStream.open();
		
		graphicsControlDataInputStream.setIntputStream(new BufferedInputStream(graphicsControlInputStream));
		graphicsControlDataOutputStream.setOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
		
		deflatedImageDataOutputStream = (graphicsDeflatedImageOutputStream);
		
		//graphicsControlInputStream.removePropagated(deflatedImageDataInputStream);
		//graphicsControlInputStream.removePropagated(snappedImageDataInputStream);
				
		//deflatedImageDataInputStream = VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(graphicsDeflatedImageInputStream);
		deflatedImageDataInputStream = VTCompressorSelector.createFlushBufferedZstdInputStream(graphicsDeflatedImageInputStream);
		
		snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);
		snappedImageDataInputStream = VTCompressorSelector.createFlushBufferedLZ4InputStream(graphicsSnappedImageInputStream);
		
		//graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
		//graphicsControlInputStream.addPropagated(snappedImageDataInputStream);
		
		resetClipboardStreams();
	}
	
	public void resetClipboardStreams() throws IOException
	{
		try
		{
			clipboardDataOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			clipboardDataInputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		//graphicsControlInputStream.removePropagated(clipboardDataOutputStream);
		//graphicsControlInputStream.removePropagated(clipboardDataInputStream);
		
		graphicsClipboardOutputStream.open();
		graphicsClipboardInputStream.open();
		
		//clipboardDataOutputStream = new VTBufferedOutputStream(new LZ4BlockOutputStream(graphicsClipboardOutputStream, VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		clipboardDataOutputStream = VTCompressorSelector.createFlushBufferedZstdOutputStream(graphicsClipboardOutputStream);
		
		//clipboardDataInputStream = new BufferedInputStream(new InflaterInputStream(graphicsClipboardInputStream, VT.VT_IO_BUFFFER_SIZE, true));
		//clipboardDataInputStream = new BufferedInputStream(new LZ4BlockInputStream(graphicsClipboardInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		clipboardDataInputStream = VTCompressorSelector.createFlushBufferedZstdInputStream(graphicsClipboardInputStream);
		
		//graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
		//graphicsControlInputStream.addPropagated(clipboardDataInputStream);
	}
	
	public void resetFileTransferStreams() throws IOException
	{
		fileTransferDataOutputStream.open();
		fileTransferDataInputStream.open();
	}
	
	public void closeFileTransferStreams() throws IOException
	{
		fileTransferDataOutputStream.close();
		fileTransferDataInputStream.close();
	}
	
	public void closeAudioStreams() throws IOException
	{
		audioDataOutputStream.close();
		audioDataInputStream.close();
	}
	
	public void resetAudioStreams() throws IOException
	{
		audioDataOutputStream.open();
		audioDataInputStream.open();
		// audioDataOutputStream = audioOutputStream;
		// audioDataInputStream = audioInputStream;
	}
	
	public void setRateInBytesPerSecond(long bytesPerSecond)
	{
		multiplexedConnectionOutputStream.setBytesPerSecond(bytesPerSecond);
	}
	
	public long getRateInBytesPerSecond()
	{
		return multiplexedConnectionOutputStream.getBytesPerSecond();
	}
}