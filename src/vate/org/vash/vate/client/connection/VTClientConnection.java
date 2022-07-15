package org.vash.vate.client.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.SecureRandom;

import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3Digest;
import org.vash.vate.security.VTCryptographicEngine;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTClientConnection
{
  private static byte[] VT_SERVER_CHECK_STRING_NONE = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_NONE = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_RC4 = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_RC4 = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_AES = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_AES = new byte[16];
  //private static byte[] VT_SERVER_CHECK_STRING_BLOWFISH = new byte[16];
  //private static byte[] VT_CLIENT_CHECK_STRING_BLOWFISH = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_SALSA = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_SALSA = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_HC256 = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_HC256 = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_ISAAC = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_ISAAC = new byte[16];
  private static byte[] VT_SERVER_CHECK_STRING_GRAIN = new byte[16];
  private static byte[] VT_CLIENT_CHECK_STRING_GRAIN = new byte[16];

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
      //VT_SERVER_CHECK_STRING_BLOWFISH = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/BLOWFISH").getBytes("UTF-8");
      //VT_CLIENT_CHECK_STRING_BLOWFISH = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/BLOWFISH").getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_SALSA = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/SALSA").getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_SALSA = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/SALSA").getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_HC256 = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/HC256").getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_HC256 = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/HC256").getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_ISAAC = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/ISAAC").getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_ISAAC = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/ISAAC").getBytes("UTF-8");
      VT_SERVER_CHECK_STRING_GRAIN = ("VT/SERVER/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/GRAIN").getBytes("UTF-8");
      VT_CLIENT_CHECK_STRING_GRAIN = ("VT/CLIENT/" + VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION + "/GRAIN").getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {

    }
  }
  
  private volatile boolean connected = false;

  private int encryptionType;
  private byte[] encryptionKey;
  //private byte[] digestedClient;
  //private byte[] digestedServer;
  private byte[] localNonce = new byte[64];
  private byte[] remoteNonce = new byte[64];
  private byte[] randomData = new byte[64];
  //private byte[] paddingData = new byte[1024];
  //private MessageDigest sha256Digester;
  private VTBlake3Digest blake3Digester = new VTBlake3Digest();
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

  //private VTLittleEndianInputStream verificationReader;
  //private VTLittleEndianOutputStream verificationWriter;
  private VTLittleEndianInputStream authenticationReader;
  private VTLittleEndianOutputStream authenticationWriter;
  private Reader resultReader;
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

  // private boolean zstdAvailable;

  // private ZstdInputStream zstdImageInputStream;

  // private ZstdInputStream zstdClipboardInputStream;
  // private ZstdOutputStream zstdClipboardOutputStream;

  public VTClientConnection()
  {
    //try
    //{
      //this.sha256Digester = MessageDigest.getInstance("SHA-256");
    //}
    //catch (NoSuchAlgorithmException e)
    //{
      // e.printStackTrace();
    //}
    this.cryptoEngine = new VTCryptographicEngine();
    this.secureRandom = new SecureRandom();
    this.authenticationReader = new VTLittleEndianInputStream(null);
    this.authenticationWriter = new VTLittleEndianOutputStream(null);
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

  /*
   * public InputStream getAuthenticationInputStream() { return
   * authenticationInputStream; }
   */

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

  /*
   * public InputStream getGraphicsImageInputStream() { return
   * graphicsImageInputStream; }
   */

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

  /*
   * public OutputStream getAuthenticationOutputStream() { return
   * authenticationOutputStream; }
   */

  public OutputStream getShellOutputStream()
  {
    return shellOutputStream;
  }

  public OutputStream getFileTransferDataOutputStream()
  {
    return fileTransferDataOutputStream;
  }

  /*
   * public OutputStream getGraphicsImageOutputStream() { return
   * graphicsImageOutputStream; }
   */

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

  public Reader getResultReader()
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
    VTConsole.setCommandEcho(true);
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
    //VTConsole.setLogReadLine(null);
    //VTConsole.setLogOutput(null);
    VTConsole.print("\nVT>Connection with server closed!");
    //VTConsole.setCommandEcho(true);
    closeSockets();
    if (connected)
    {
      try
      {
        VTConsole.interruptReadLine();
      }
      catch (Throwable t)
      {

      }
    }
    connected = false;
    synchronized (this)
    {
      notifyAll();
    }
  }
  
  public boolean isConnected()
  {
    return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed() && connected;
  }

  private void setNonceStreams() throws IOException
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

  private void setVerificationStreams(boolean encrypted) throws IOException
  {
    if (encrypted)
    {
      cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
      authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
      authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
    }
    else
    {
      authenticationReader.setIntputStream(connectionSocketInputStream);
      authenticationWriter.setOutputStream(connectionSocketOutputStream);
    }
  }

  public void setAuthenticationStreams() throws IOException
  {
    cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }

  public void setConnectionStreams(byte[] digestedUser, byte[] digestedPassword, String user, String password) throws IOException
  {
    connected = true;
    cryptoEngine.initializeClientEngine(encryptionType, encryptionKey, localNonce, remoteNonce, digestedUser, digestedPassword, user != null ? user.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
    connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
    connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
    authenticationReader.setIntputStream(connectionInputStream);
    authenticationWriter.setOutputStream(connectionOutputStream);
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }

  private void setMultiplexedStreams() throws IOException
  {
    multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_BUFFER_SIZE, false);
    multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_NETWORK_PACKET_SIZE, VT.VT_NETWORK_PACKET_SIZE, true);
    multiplexedConnectionInputStream.startPacketReader();

    pingInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED, 0);
    pingOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED, 0);

    shellInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);
    shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 1);

    fileTransferControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
    fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 2);
    fileTransferDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
    fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 3);
    fileTransferDataInputStream.addPropagated(fileTransferDataOutputStream);

    graphicsControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 4);
    graphicsControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 4);
    graphicsDirectImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 5);
    graphicsDirectImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 5);
    graphicsDeflatedImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 6);
    graphicsDeflatedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 6);
    graphicsSnappedImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 7);
    graphicsSnappedImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 7);
    graphicsClipboardInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 8);
    graphicsClipboardOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 8);

    // graphicsControlInputStream.addPropagated(graphicsControlOutputStream);
    graphicsControlInputStream.addPropagated(graphicsDirectImageInputStream);
    graphicsControlInputStream.addPropagated(graphicsDeflatedImageInputStream);
    graphicsControlInputStream.addPropagated(graphicsSnappedImageInputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardInputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardOutputStream);

    audioDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
    audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 9);
    audioControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
    audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 10);
    
    audioDataInputStream.addPropagated(audioDataOutputStream);

    tunnelControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);
    tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 11);

    socksControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);
    socksControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED, 12);

    shellDataOutputStream = VTCompressorSelector.createBufferedZlibOutputStream(shellOutputStream);
    // shellDataOutputStream = VTCompressorSelector.createFlushBufferedSyncFlushDeflaterOutputStream(shellOutputStream);
    // shellDataOutputStream = shellOutputStream;

    shellDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(shellInputStream);
    // shellDataInputStream = VTCompressorSelector.createFlushBufferedSyncFlushInflaterInputStream(shellInputStream);
    // shellDataInputStream = shellInputStream;

    resultReader = new InputStreamReader(shellDataInputStream, "UTF-8");
    commandWriter = new BufferedWriter(new OutputStreamWriter(shellDataOutputStream, "UTF-8"));

    graphicsControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(graphicsControlOutputStream));

    directImageDataInputStream = (new BufferedInputStream(graphicsDirectImageInputStream, VT.VT_STANDARD_DATA_BUFFER_SIZE));
    directImageDataOutputStream = (graphicsDirectImageOutputStream);

    // deflatedImageDataInputStream =
    // VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(graphicsDeflatedImageInputStream);
    deflatedImageDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(graphicsDeflatedImageInputStream);
    deflatedImageDataOutputStream = graphicsDeflatedImageOutputStream;

    snappedImageDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(graphicsSnappedImageInputStream);
    snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);

    // clipboardDataOutputStream = new VTBufferedOutputStream(new
    // LZ4BlockOutputStream(graphicsClipboardOutputStream,
    // VT.VT_STANDARD_DATA_BUFFER_SIZE,
    // LZ4Factory.fastestJavaInstance().fastCompressor(),
    // XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(),
    // true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZlibOutputStream(graphicsClipboardOutputStream);
    // clipboardDataInputStream = new BufferedInputStream(new
    // InflaterInputStream(graphicsClipboardInputStream, VT.VT_IO_BUFFFER_SIZE,
    // true));

    // clipboardDataInputStream = new BufferedInputStream(new
    // LZ4BlockInputStream(graphicsClipboardInputStream,
    // LZ4Factory.fastestJavaInstance().fastDecompressor(),
    // XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(),
    // false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(graphicsClipboardInputStream);

    fileTransferControlDataInputStream = new VTLittleEndianInputStream(new BufferedInputStream(fileTransferControlInputStream));
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(new BufferedOutputStream(fileTransferControlOutputStream));

    // graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
    // graphicsControlInputStream.addPropagated(snappedImageDataInputStream);

    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
    //closeAudioStreams();
    //audioDataInputStream.addPropagated(audioDataOutputStream);
  }
  
//  public boolean exchangeConnectionPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
//  
//  public boolean exchangeAuthenticationPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
  
  private byte[] exchangeCheckString(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    blake3Digester.reset();
    blake3Digester.update(remoteNonce);
    blake3Digester.update(localNonce);
    if (encryptionKey != null && encryptionType != VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      blake3Digester.update(encryptionKey);
    }
    byte[] data = blake3Digester.digest(localCheckString);
    authenticationWriter.write(data);
    authenticationWriter.flush();
    authenticationReader.readFully(data);
    return data;
  }
  
//  private boolean matchRemoteEncryptionSettings(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, int encryptionType) throws IOException
//  {
//    byte[] localCheckString = null;
//    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_NONE;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_RC4;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_AES;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_SALSA;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_HC256;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_ISAAC;
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      localCheckString = VT_CLIENT_CHECK_STRING_GRAIN;
//    }
//    
//    byte[] digestedServer = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString, encryptionType);
//    
//    blake3Digester.reset();
//    blake3Digester.update(localNonce);
//    blake3Digester.update(remoteNonce);
//    if (encryptionKey != null)
//    {
//      blake3Digester.update(encryptionKey);
//    }
//    
//    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_NONE));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_RC4));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_AES));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_SALSA));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_HC256));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_ISAAC));
//    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      return VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_GRAIN));
//    }
//    return false;
//  }
  
  private int discoverRemoteEncryptionType(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    byte[] digestedServer = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString, encryptionType);
    
    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    //if (encryptionKey != null)
    //{
      //blake3Digester.update(encryptionKey);
    //}
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_NONE)))
    {
      return VT.VT_CONNECTION_ENCRYPT_NONE;
    }

    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_RC4)))
    {
      return VT.VT_CONNECTION_ENCRYPT_RC4;
    }

    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_AES)))
    {
      return VT.VT_CONNECTION_ENCRYPT_AES;
    }
    
    //sha256Digester.reset();
    //sha256Digester.update(localNonce);
    //sha256Digester.update(remoteNonce);
    //if (encryptionKey != null)
    //{
      //sha256Digester.update(encryptionKey);
    //}
    //if (VTArrayComparator.arrayEquals(digestedServer, sha256Digester.digest(VT_SERVER_CHECK_STRING_BLOWFISH)))
    //{
      //return VT.VT_CONNECTION_ENCRYPT_BLOWFISH;
    //}
    
    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_SALSA)))
    {
      return VT.VT_CONNECTION_ENCRYPT_SALSA;
    }
    
    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_HC256)))
    {
      return VT.VT_CONNECTION_ENCRYPT_HC256;
    }
    
    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_ISAAC)))
    {
      return VT.VT_CONNECTION_ENCRYPT_ISAAC;
    }
    
    blake3Digester.reset();
    blake3Digester.update(localNonce);
    blake3Digester.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digester.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedServer, blake3Digester.digest(VT_SERVER_CHECK_STRING_GRAIN)))
    {
      return VT.VT_CONNECTION_ENCRYPT_GRAIN;
    }
    
    return -1;
  }

  public boolean verifyConnection() throws IOException
  {
    setNonceStreams();
    exchangeNonces(false);
    setVerificationStreams(false);
    exchangeNonces(true);
    //if (matchRemoteEncryptionSettings(localNonce, remoteNonce, encryptionKey))
    //{
      //return true;
    //}
    
    //exchangeNonces(true);
    //setVerificationStreams(true);
    
    int remoteEncryptionType = 0;
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_NONE, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
        return true;
      }
      //if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
      //{
        //setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
        //return true;
      //}
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
        return true;
      }
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_RC4)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_RC4, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_AES)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_AES, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
        return true;
      }
    }
    //else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    //{
      //remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, null, VT_CLIENT_CHECK_STRING_BLOWFISH);
      //if (remoteEncryptionType != -1)
      //{
        //setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
        //return true;
      //}
    //}
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_SALSA, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_HC256, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_ISAAC, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_GRAIN, encryptionType);
      if (remoteEncryptionType != -1)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
        return true;
      }
    }
    return false;
  }

  /*
   * public void startConnection() {
   * multiplexedConnectionInputStream.startPacketReader(); }
   */

  public void startConnection() throws IOException
  {
    setMultiplexedStreams();
  }

  /*
   * public boolean startedConnection() { return multiplexedConnectionInputStream
   * != null && multiplexedConnectionInputStream.isPacketReaderStarted(); }
   */

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

    // graphicsControlInputStream.removePropagated(deflatedImageDataInputStream);
    // graphicsControlInputStream.removePropagated(snappedImageDataInputStream);

    // deflatedImageDataInputStream =
    // VTCompressorSelector.createCompatibleSyncFlushInflaterInputStream(graphicsDeflatedImageInputStream);
    deflatedImageDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(graphicsDeflatedImageInputStream);

    snappedImageDataOutputStream = (graphicsSnappedImageOutputStream);
    snappedImageDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(graphicsSnappedImageInputStream);

    // graphicsControlInputStream.addPropagated(deflatedImageDataInputStream);
    // graphicsControlInputStream.addPropagated(snappedImageDataInputStream);

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
    // graphicsControlInputStream.removePropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.removePropagated(clipboardDataInputStream);

    graphicsClipboardOutputStream.open();
    graphicsClipboardInputStream.open();

    // clipboardDataOutputStream = new VTBufferedOutputStream(new
    // LZ4BlockOutputStream(graphicsClipboardOutputStream,
    // VT.VT_STANDARD_DATA_BUFFER_SIZE,
    // LZ4Factory.fastestJavaInstance().fastCompressor(),
    // XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(),
    // true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZlibOutputStream(graphicsClipboardOutputStream);

    // clipboardDataInputStream = new BufferedInputStream(new
    // InflaterInputStream(graphicsClipboardInputStream, VT.VT_IO_BUFFFER_SIZE,
    // true));
    // clipboardDataInputStream = new BufferedInputStream(new
    // LZ4BlockInputStream(graphicsClipboardInputStream,
    // LZ4Factory.fastestJavaInstance().fastDecompressor(),
    // XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(),
    // false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(graphicsClipboardInputStream);

    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
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
  
  //public boolean isRunningAudio()
  //{
    //return !audioDataOutputStream.closed();
  //}
}