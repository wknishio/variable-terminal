package org.vate.client.connection;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

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
import org.vate.stream.endian.VTLittleEndianInputStream;
import org.vate.stream.endian.VTLittleEndianOutputStream;
import org.vate.stream.filter.VTBufferedOutputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

import com.jcraft.jzlib.InflaterInputStream;

public class VTClientConnection
{
	private static byte[] VT_SERVER_CHECK_STRING = new byte[16];
	private static byte[] VT_CLIENT_CHECK_STRING = new byte[16];
	
	static
	{
		try
		{
			VT_SERVER_CHECK_STRING = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION).getBytes("UTF-8");
			VT_CLIENT_CHECK_STRING = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION).getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			
		}
	}
	
	//private volatile boolean dialog = false;
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
	
	private boolean zstdAvailable;
	
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
	
	public void closeStreams()
	{
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
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, 1024, 1024 * 32, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, 1024, 1, false);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
		{
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, 1024, 1024 * 32, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, 1024, 1, false);
		}
		else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
		{
			multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, 1024, 1024 * 32, false);
			multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, 1024, 1, false);
		}
		
		multiplexedConnectionInputStream.startPacketReader();
		
		pingInputStream = multiplexedConnectionInputStream.getInputStream((short) (VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_PERFORMANCE_UNLIMITED), 0);
		pingOutputStream = multiplexedConnectionOutputStream.linkOutputStream((short) (VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_PERFORMANCE_UNLIMITED), 0);
		
		shellInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);
		shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);
		
		fileTransferControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
		fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
		fileTransferDataInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
		fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
		fileTransferDataInputStream.setPropagated(fileTransferDataOutputStream);
		
		// graphicsCheckInputStream =
		// multiplexedConnectionInputStream.getInputStream(4);
		// graphicsCheckOutputStream =
		// multiplexedConnectionOutputStream.getOutputStream(4);
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
		
		audioDataInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
		audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
		audioControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
		audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
		
		tunnelControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);
		tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);
		
		socksControlInputStream = multiplexedConnectionInputStream.getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);
		socksControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);
		
		shellDataOutputStream = new VTBufferedOutputStream(new LZ4BlockOutputStream(shellOutputStream, 1024 * 8, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), 1024 * 32);
//		try
//		{
//			java.util.zip.Deflater javaShellDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
//			javaShellDeflater.setStrategy(Deflater.FILTERED);
//			javaShellDeflater.setLevel(Deflater.BEST_SPEED);
//			VTDeflaterOutputStream javaShellDeflaterOutputStream = new VTDeflaterOutputStream(shellOutputStream, javaShellDeflater, 1024 * 8, true);
//			shellDataOutputStream = javaShellDeflaterOutputStream;
//		}
//		catch (Throwable t)
//		{
//			DeflaterOutputStream jzlibShellDeflater = new DeflaterOutputStream(shellOutputStream, 1024 * 8, JZlib.Z_BEST_SPEED, true);
//			jzlibShellDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
//			jzlibShellDeflater.setSyncFlush(true);
//			shellDataOutputStream = jzlibShellDeflater;
//		}
		//shellDataOutputStream = shellOutputStream;
		
		shellDataInputStream = new BufferedInputStream(new LZ4BlockInputStream(shellInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 32);
		//shellDataInputStream = new InflaterInputStream(shellInputStream, true);
		//shellDataInputStream = shellInputStream;
		
		resultReader = new BufferedReader(new InputStreamReader(shellDataInputStream, "UTF-8"));
		commandWriter = new BufferedWriter(new OutputStreamWriter(shellDataOutputStream, "UTF-8"));
		
		graphicsControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(graphicsControlInputStream));
		graphicsControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(graphicsControlOutputStream));
		
		// try
		// {
		// Native.load();
		// }
		// catch (Throwable t)
		// {
		//
		// }
		// boolean zstdLoaded = Native.isLoaded();
		// //boolean zstdLoaded = false;
		// graphicsControlDataOutputStream.write(zstdLoaded ? 1 : 0);
		// graphicsControlDataOutputStream.flush();
		// zstdAvailable = graphicsControlDataInputStream.read() > 0 &&
		// zstdLoaded;
		
		directImageDataInputStream = (new BufferedInputStream(graphicsDirectImageInputStream, 1024 * 32));
		directImageDataOutputStream = (graphicsDirectImageOutputStream);
		
		// if (zstdAvailable)
		// {
		// deflatedImageDataInputStream = (new BufferedInputStream(new
		// ZstdInputStream(graphicsDeflatedImageInputStream).setContinuous(true),
		// 1024 * 32));
		// }
		// else
		// {
		// deflatedImageDataInputStream = (new
		// InflaterInputStream(graphicsDeflatedImageInputStream, 1024 * 32,
		// true));
		// }
		deflatedImageDataInputStream = (new InflaterInputStream(graphicsDeflatedImageInputStream, 1024 * 32, true));
		deflatedImageDataOutputStream = graphicsDeflatedImageOutputStream;
		
		snappedImageDataInputStream = (new BufferedInputStream(new LZ4BlockInputStream(graphicsSnappedImageInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 32));
		snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);
		
//		try
//		{
//			java.util.zip.Deflater javaClipDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
//			javaClipDeflater.setStrategy(Deflater.FILTERED);
//			javaClipDeflater.setLevel(Deflater.BEST_SPEED);
//			VTDeflaterOutputStream javaClipDeflaterOutputStream = new VTDeflaterOutputStream(graphicsClipboardOutputStream, javaClipDeflater, 1024 * 32, true);
//			clipboardDataOutputStream = (new VTBufferedOutputStream(javaClipDeflaterOutputStream, 1024 * 32));
//		}
//		catch (Throwable t)
//		{
//			DeflaterOutputStream jzlibClipDeflater = new DeflaterOutputStream(graphicsClipboardOutputStream, 1024 * 32, JZlib.Z_BEST_SPEED, true);
//			jzlibClipDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
//			jzlibClipDeflater.setSyncFlush(true);
//			clipboardDataOutputStream = (new VTBufferedOutputStream(jzlibClipDeflater, 1024 * 32));
//		}
		clipboardDataOutputStream = new VTBufferedOutputStream(new LZ4BlockOutputStream(graphicsClipboardOutputStream, 1024 * 8, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), 1024 * 32);
		//clipboardDataInputStream = new BufferedInputStream(new InflaterInputStream(graphicsClipboardInputStream, 1024 * 32, true));
		clipboardDataInputStream = new BufferedInputStream(new LZ4BlockInputStream(graphicsClipboardInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 32);		
		
		fileTransferControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(fileTransferControlInputStream));
		fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(fileTransferControlOutputStream));
		
		// graphicsCheckDataInputStream = new
		// VTLittleEndianInputStream(graphicsCheckInputStream);
		// graphicsCheckDataOutputStream = new
		// VTLittleEndianOutputStream(graphicsCheckOutputStream);
	}
	
	public boolean verifyConnection() throws IOException, NoSuchAlgorithmException
	{
		exchangeNonces(true);
		sha256Digester.reset();
		sha256Digester.update(localNonce);
		sha256Digester.update(remoteNonce);
		digestedServer = sha256Digester.digest(VT_SERVER_CHECK_STRING);
		sha256Digester.update(remoteNonce);
		sha256Digester.update(localNonce);
		digestedClient = sha256Digester.digest(VT_CLIENT_CHECK_STRING);
		authenticationWriter.write(digestedClient);
		authenticationWriter.flush();
		authenticationReader.readFully(digestedClient);
		if (!VTArrayComparator.arrayEquals(digestedServer, digestedClient))
		{
			return false;
		}
		return true;
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
			graphicsControlOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsDirectImageOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsDeflatedImageOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsSnappedImageOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		try
		{
			graphicsClipboardOutputStream.close();
		}
		catch (Throwable t)
		{
			
		}
		if (zstdAvailable)
		{
			try
			{
				deflatedImageDataInputStream.close();
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
	// public void resetDirectGraphicsModeStreams() throws IOException
	// {
	// graphicsDirectImageOutputStream.open();
	// graphicsDirectImageInputStream.open();
	// }
	
	public void resetGraphicsModeStreams() throws IOException
	{
		/* if (zstdImageInputStream != null) { zstdImageInputStream.close();
		 * } */
		
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
		
		// if (zstdAvailable)
		// {
		// deflatedImageDataInputStream = (new BufferedInputStream(new
		// ZstdInputStream(graphicsDeflatedImageInputStream).setContinuous(true),
		// 1024 * 32));
		// }
		// else
		// {
		// deflatedImageDataInputStream = (new
		// InflaterInputStream(graphicsDeflatedImageInputStream, 1024 * 32,
		// true));
		// }
		deflatedImageDataInputStream = (new InflaterInputStream(graphicsDeflatedImageInputStream, 1024 * 32, true));
		
		snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);
		snappedImageDataInputStream = (new BufferedInputStream(new LZ4BlockInputStream(graphicsSnappedImageInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 32));
		
		resetClipboardStreams();
	}
	
	public void resetClipboardStreams() throws IOException
	{
		graphicsClipboardOutputStream.open();
		graphicsClipboardInputStream.open();
		
//		try
//		{
//			java.util.zip.Deflater javaClipDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
//			javaClipDeflater.setStrategy(Deflater.FILTERED);
//			javaClipDeflater.setLevel(Deflater.BEST_SPEED);
//			VTDeflaterOutputStream javaClipDeflaterOutputStream = new VTDeflaterOutputStream(graphicsClipboardOutputStream, javaClipDeflater, 1024 * 32, true);
//			clipboardDataOutputStream = (new VTBufferedOutputStream(javaClipDeflaterOutputStream, 1024 * 32));
//		}
//		catch (Throwable t)
//		{
//			DeflaterOutputStream jzlibClipDeflater = new DeflaterOutputStream(graphicsClipboardOutputStream, 1024 * 32, JZlib.Z_BEST_SPEED, true);
//			jzlibClipDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
//			jzlibClipDeflater.setSyncFlush(true);
//			clipboardDataOutputStream = (new VTBufferedOutputStream(jzlibClipDeflater, 1024 * 32));
//		}
		clipboardDataOutputStream = new VTBufferedOutputStream(new LZ4BlockOutputStream(graphicsClipboardOutputStream, 1024 * 8, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), 1024 * 32);
		//clipboardDataInputStream = new BufferedInputStream(new InflaterInputStream(graphicsClipboardInputStream, 1024 * 32, true));
		clipboardDataInputStream = new BufferedInputStream(new LZ4BlockInputStream(graphicsClipboardInputStream, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 32);
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