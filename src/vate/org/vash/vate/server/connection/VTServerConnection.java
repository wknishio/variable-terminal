package org.vash.vate.server.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTCryptographicEngine;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.array.VTFlushBufferedOutputStream;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

import net.jpountz.xxhash.XXHashFactory;

public class VTServerConnection
{
  private static final String MAJOR_MINOR_VERSION = VTSystem.VT_MAJOR_VERSION + "/" + VTSystem.VT_MINOR_VERSION;
  
  private static final byte[] VT_SERVER_CHECK_STRING_NONE = ("/VARIABLE-TERMINAL/SERVER/NONE/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_NONE = ("/VARIABLE-TERMINAL/CLIENT/NONE/" + MAJOR_MINOR_VERSION).getBytes();
//  private static final byte[] VT_SERVER_CHECK_STRING_VMPC = ("/VARIABLE-TERMINAL/SERVER/VMPC/" + MAJOR_MINOR_VERSION).getBytes();
//  private static final byte[] VT_CLIENT_CHECK_STRING_VMPC = ("/VARIABLE-TERMINAL/CLIENT/VMPC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_ISAAC = ("/VARIABLE-TERMINAL/SERVER/ISAAC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_ISAAC = ("/VARIABLE-TERMINAL/CLIENT/ISAAC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_SALSA = ("/VARIABLE-TERMINAL/SERVER/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_SALSA = ("/VARIABLE-TERMINAL/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_GRAIN = ("/VARIABLE-TERMINAL/SERVER/GRAIN/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_GRAIN = ("/VARIABLE-TERMINAL/CLIENT/GRAIN/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_HC = ("/VARIABLE-TERMINAL/SERVER/HC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_HC = ("/VARIABLE-TERMINAL/CLIENT/HC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_ZUC = ("/VARIABLE-TERMINAL/SERVER/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_ZUC = ("/VARIABLE-TERMINAL/CLIENT/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  
  private volatile boolean connected = false;
  private volatile boolean closed = true;
  
  private int encryptionType;
  private int availableInputChannel;
  private int availableOutputChannel;
  private byte[] encryptionKey;
  private byte[] digestedCredentials;
  // private byte[] digestedClient;
  // private byte[] digestedServer;
  private final byte[] localNonce = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] remoteNonce = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] randomData = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  // private byte[] paddingData = new byte[1024];
  // private MessageDigest sha256Digester;
  private final VTCryptographicEngine cryptoEngine;
  private final VTBlake3MessageDigest blake3Digest;
  private VTBlake3SecureRandom secureRandom;
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
  private VTLinkableDynamicMultiplexedInputStream fileTransferStartInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferDataInputStream;
  // private VTMultiplexedInputStream graphicsCheckInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsDirectImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsHeavyImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsFastImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsClipboardInputStream;
  // private VTLinkableDynamicMultiplexedInputStream audioInputStream;
  private VTLinkableDynamicMultiplexedInputStream audioDataInputStream;
  private VTLinkableDynamicMultiplexedInputStream audioControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream pingClientInputStream;
  private VTLinkableDynamicMultiplexedInputStream pingServerInputStream;
  private VTLinkableDynamicMultiplexedInputStream tunnelControlInputStream;
  // private VTLinkableDynamicMultiplexedInputStream socksControlInputStream;
  
  // private OutputStream authenticationOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream shellOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferStartOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferDataOutputStream;
  // private VTMultiplexedOutputStream graphicsCheckOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsDirectImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsHeavyImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsFastImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsClipboardOutputStream;
  // private VTLinkableDynamicMultiplexedOutputStream audioOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream audioDataOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream audioControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream pingClientOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream pingServerOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream tunnelControlOutputStream;
  // private VTLinkableDynamicMultiplexedOutputStream socksControlOutputStream;
  
  // private VTLittleEndianInputStream verificationReader;
  // private VTLittleEndianOutputStream verificationWriter;
  private VTLittleEndianInputStream authenticationReader;
  private VTLittleEndianOutputStream authenticationWriter;
  private VTLittleEndianInputStream commandReader;
  private VTLittleEndianOutputStream resultWriter;
  private InputStream shellDataInputStream;
  private OutputStream shellDataOutputStream;
  private InputStream clipboardDataInputStream;
  private OutputStream clipboardDataOutputStream;
  
  private VTLittleEndianInputStream fileTransferStartDataInputStream;
  private VTLittleEndianOutputStream fileTransferStartDataOutputStream;
  private VTLittleEndianInputStream fileTransferControlDataInputStream;
  private VTLittleEndianOutputStream fileTransferControlDataOutputStream;
  // private VTLittleEndianInputStream graphicsCheckDataInputStream;
  // private VTLittleEndianOutputStream graphicsCheckDataOutputStream;
  private VTLittleEndianInputStream graphicsControlDataInputStream;
  private VTLittleEndianOutputStream graphicsControlDataOutputStream;
  
  private VTLittleEndianInputStream directImageDataInputStream;
  private VTLittleEndianOutputStream directImageDataOutputStream;
  private VTLittleEndianInputStream heavyImageDataInputStream;
  private VTLittleEndianOutputStream heavyImageDataOutputStream;
  private VTLittleEndianInputStream fastImageDataInputStream;
  private VTLittleEndianOutputStream fastImageDataOutputStream;

  private final ExecutorService executorService;
  
  // private boolean zstdAvailable;
  
  // private OutputStream bufferedGraphicsHeavyImageOutputStream;
  // private OutputStream bufferedGraphicsFastImageOutputStream;
  
  // private ZstdOutputStream zstdImageOutputStream;
  
  // private ZstdInputStream zstdClipboardInputStream;
  // private ZstdOutputStream zstdClipboardOutputStream;
  
  public VTServerConnection(final ExecutorService executorService)
  {
    this.executorService = executorService;
    this.cryptoEngine = new VTCryptographicEngine();
    this.blake3Digest = new VTBlake3MessageDigest();
    this.authenticationReader = new VTLittleEndianInputStream(null);
    this.authenticationWriter = new VTLittleEndianOutputStream(null);
  }
  
  public VTBlake3SecureRandom getSecureRandom()
  {
    return secureRandom;
  }
  
  public void setSecureRandomSeed(byte[] seed)
  {
    secureRandom = new VTBlake3SecureRandom(seed);
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
  
  public byte[] getDigestedCredentials()
  {
    return digestedCredentials;
  }
  
  public void setEncryptionType(int encryptionType)
  {
    this.encryptionType = encryptionType;
  }
  
  public byte[] getEncryptionKey()
  {
    return encryptionKey;
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
    this.closed = false;
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
  
  /*
   * public InputStream getGraphicsImageInputStream() { return
   * graphicsImageInputStream; }
   */
  
  /*
   * public OutputStream getAuthenticationOutputStream() { return
   * authenticationOutputStream; }
   */
  
  public OutputStream getShellOutputStream()
  {
    return shellOutputStream;
  }
  
  public OutputStream getShellDataOutputStream()
  {
    return shellDataOutputStream;
  }
  
  public OutputStream getFileTransferDataOutputStream()
  {
    return fileTransferDataOutputStream;
  }
  
  /*
   * public OutputStream getGraphicsImageOutputStream() { return
   * graphicsImageOutputStream; }
   */
  
  public VTLittleEndianInputStream getCommandReader()
  {
    return commandReader;
  }
  
  public VTLittleEndianInputStream getAuthenticationReader()
  {
    return authenticationReader;
  }
  
  public VTLittleEndianOutputStream getResultWriter()
  {
    return resultWriter;
  }
  
  public VTLittleEndianOutputStream getAuthenticationWriter()
  {
    return authenticationWriter;
  }
  
  public VTLittleEndianInputStream getFileTransferControlDataInputStream()
  {
    return fileTransferControlDataInputStream;
  }
  
  public VTLittleEndianOutputStream getFileTransferControlDataOutputStream()
  {
    return fileTransferControlDataOutputStream;
  }
  
  public VTLittleEndianInputStream getFileTransferStartDataInputStream()
  {
    return fileTransferStartDataInputStream;
  }
  
  public VTLittleEndianOutputStream getFileTransferStartDataOutputStream()
  {
    return fileTransferStartDataOutputStream;
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
  
  public VTLittleEndianInputStream getGraphicsDirectImageDataInputStream()
  {
    return directImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsHeavyImageDataInputStream()
  {
    return heavyImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsFastImageDataInputStream()
  {
    return fastImageDataInputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsDirectImageDataOutputStream()
  {
    return directImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsHeavyImageDataOutputStream()
  {
    return heavyImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsFastImageDataOutputStream()
  {
    return fastImageDataOutputStream;
  }
  
  public OutputStream getTunnelControlOutputStream()
  {
    return tunnelControlOutputStream;
  }
  
  // public OutputStream getSocksControlOutputStream()
  // {
  // return socksControlOutputStream;
  // }
  
  public OutputStream getAudioDataOutputStream()
  {
    return audioDataOutputStream;
  }
  
  public OutputStream getAudioControlOutputStream()
  {
    return audioControlOutputStream;
  }
  
  public OutputStream getPingClientOutputStream()
  {
    return pingClientOutputStream;
  }
  
  public OutputStream getPingServerOutputStream()
  {
    return pingServerOutputStream;
  }
  
  public InputStream getGraphicsClipboardInputStream()
  {
    return graphicsClipboardInputStream;
  }
  
  public InputStream getAudioDataInputStream()
  {
    return audioDataInputStream;
  }
  
  public InputStream getAudioControlInputStream()
  {
    return audioControlInputStream;
  }
  
  public InputStream getPingClientInputStream()
  {
    return pingClientInputStream;
  }
  
  public InputStream getPingServerInputStream()
  {
    return pingServerInputStream;
  }
  
  public InputStream getTunnelControlInputStream()
  {
    return tunnelControlInputStream;
  }
  
  // public InputStream getSocksControlInputStream()
  // {
  // return socksControlInputStream;
  // }
  
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
    if (closed)
    {
      return;
    }
//    StringBuilder message = new StringBuilder();
//    StackTraceElement[] stackStrace = Thread.currentThread().getStackTrace();
//    for (int i = stackStrace.length - 1; i >= 0; i--)
//    {
//      message.append(stackStrace[i].toString() + "\n");
//    }
//    System.err.println(message.toString());
    if (connectionSocket != null)
    {
      try
      {
        connectionSocket.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    if (multiplexedConnectionOutputStream != null)
    {
      try
      {
        multiplexedConnectionOutputStream.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    if (multiplexedConnectionInputStream != null)
    {
      try
      {
        multiplexedConnectionInputStream.close();
      }
      catch (Throwable e)
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
    closed = true;
  }
  
  public void closeConnection()
  {
    if (!closed || connected)
    {
      VTMainConsole.print("\rVT>Connection with client closed!\nVT>");
    }
    closeSockets();
    connected = false;
  }
  
  public boolean isConnected()
  {
    return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed() && connected && !closed;
  }
  
  private void setNonceStreams() throws IOException
  {
    //connectionSocketInputStream = new BufferedInputStream(connectionSocket.getInputStream(), VT.VT_CONNECTION_PACKET_BUFFER_SIZE_BYTES);
    //connectionSocketOutputStream = new VTBufferedOutputStream(connectionSocket.getOutputStream(), VT.VT_PACKET_DATA_SIZE_BYTES * 2, true);
    connectionSocketInputStream = connectionSocket.getInputStream();
    connectionSocketOutputStream = connectionSocket.getOutputStream();
    nonceReader = new VTLittleEndianInputStream(connectionSocketInputStream);
    nonceWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
    // Arrays.fill(localNonce, (byte)0);
    // Arrays.fill(remoteNonce, (byte)0);
  }
  
  private void exchangeNonces(boolean update) throws IOException
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
    
    blake3Digest.reset();
    byte[] seed = new byte[VTSystem.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, seed, 0, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, seed, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES);
    //secureRandom.setSeed(seed);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
  }
  
  private void setVerificationStreams() throws IOException
  {
    cryptoEngine.initializeServerEngine(VTSystem.VT_CONNECTION_ENCRYPTION_NONE, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }
  
  public void setAuthenticationStreams() throws IOException
  {
    //exchangeNonces(true);
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
    //exchangeNonces(true);
  }
  
  public boolean setConnectionStreams(byte[] digestedCredentials) throws IOException
  {
    try
    {
      exchangeNonces(true);
    }
    catch (Throwable t)
    {
      return false;
    }
    this.digestedCredentials = digestedCredentials;
    //cryptoEngine.initializeServerEngine(encryptionType, encryptionKey, remoteNonce, localNonce, digestedCredentials, user != null ? user.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey, digestedCredentials);
    //connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
    //connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
    connectionInputStream = new BufferedInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_CONNECTION_INPUT_PACKET_BUFFER_SIZE_BYTES), VTSystem.VT_CONNECTION_INPUT_PACKET_BUFFER_SIZE_BYTES);
    connectionOutputStream = new BufferedOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_CONNECTION_OUTPUT_PACKET_BUFFER_SIZE_BYTES), VTSystem.VT_CONNECTION_OUTPUT_PACKET_BUFFER_SIZE_BYTES);
    //authenticationReader.setIntputStream(connectionInputStream);
    //authenticationWriter.setOutputStream(connectionOutputStream);
    //nonceReader.setIntputStream(authenticationReader.getInputStream());
    //nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
    return true;
  }
  
  private void setMultiplexedStreams() throws IOException
  {
    blake3Digest.reset();
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    blake3Digest.update(encryptionKey);
    blake3Digest.update(digestedCredentials);
    long inputSeed = blake3Digest.digestLong();
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    blake3Digest.update(encryptionKey);
    blake3Digest.update(digestedCredentials);
    long outputSeed = blake3Digest.digestLong();
    
    VTXXHash64MessageDigest secureInputSeed = new VTXXHash64MessageDigest(XXHashFactory.safeInstance().newStreamingHash64(inputSeed));
    VTXXHash64MessageDigest secureOutputSeed = new VTXXHash64MessageDigest(XXHashFactory.safeInstance().newStreamingHash64(outputSeed));
    
    int inputChannel = 0;
    int outputChannel = 0;
    
    multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, true, VTSystem.VT_PACKET_DATA_SIZE_BYTES, VTSystem.VT_CHANNEL_PACKET_BUFFER_SIZE_BYTES, secureInputSeed, executorService, false);
    multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, true, VTSystem.VT_PACKET_DATA_SIZE_BYTES, VTSystem.VT_CONNECTION_OUTPUT_PACKET_BUFFER_SIZE_BYTES, secureOutputSeed, executorService);
    
    pingServerInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, inputChannel++);
    pingServerOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, outputChannel++);
    
    pingClientInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, inputChannel++);
    pingClientOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, outputChannel++);
    
    shellInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    fileTransferStartInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    fileTransferStartOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    fileTransferControlInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    fileTransferDataInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    graphicsControlInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    graphicsControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    graphicsDirectImageInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    graphicsDirectImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    graphicsHeavyImageInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    graphicsHeavyImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    graphicsFastImageInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    graphicsFastImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    graphicsClipboardInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    graphicsClipboardOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    audioControlInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    audioDataInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    tunnelControlInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    availableInputChannel = inputChannel;
    availableOutputChannel = outputChannel;
    
    shellDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(shellInputStream);
    // shellDataInputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushInflaterInputStream(shellInputStream);
    // shellDataInputStream = shellInputStream;
    
    shellDataOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(shellOutputStream);
    // shellDataOutputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushDeflaterOutputStream(shellOutputStream);
    // shellDataOutputStream = shellOutputStream;
    
    commandReader = new VTLittleEndianInputStream(shellDataInputStream);
    resultWriter = new VTLittleEndianOutputStream(shellDataOutputStream);
    
    graphicsControlDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsControlOutputStream));
    
    directImageDataInputStream = new VTLittleEndianInputStream(graphicsDirectImageInputStream);
    directImageDataOutputStream = new VTLittleEndianOutputStream(new VTFlushBufferedOutputStream(graphicsDirectImageOutputStream, new VTByteArrayOutputStream(VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES)));
    
    heavyImageDataInputStream = new VTLittleEndianInputStream(graphicsHeavyImageInputStream);
    heavyImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZstdOutputStream(graphicsHeavyImageOutputStream));
    
    fastImageDataInputStream = new VTLittleEndianInputStream(graphicsFastImageInputStream);
    fastImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedSyncFlushFilteredZlibOutputStream(graphicsFastImageOutputStream));
    
    clipboardDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(graphicsClipboardInputStream);
    clipboardDataOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(graphicsClipboardOutputStream);
    
    fileTransferControlDataInputStream = new VTLittleEndianInputStream(fileTransferControlInputStream);
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(fileTransferControlOutputStream);
    
    fileTransferStartDataInputStream = new VTLittleEndianInputStream(fileTransferStartInputStream);
    fileTransferStartDataOutputStream = new VTLittleEndianOutputStream(fileTransferStartOutputStream);
  }
  
//  private boolean exchangeConnectionPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
//  
//  private boolean exchangeAuthenticationPadding() throws IOException
//  {
//    secureRandom.nextBytes(paddingData);
//    authenticationWriter.write(paddingData);
//    authenticationWriter.flush();
//    authenticationReader.readFully(paddingData);
//    return true;
//  }
  
  private byte[] exchangeCheckString(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    blake3Digest.reset();
    blake3Digest.update(remoteNonce);
    blake3Digest.update(localNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    byte[] data = blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, localCheckString);
    authenticationWriter.write(data);
    authenticationWriter.flush();
    authenticationReader.readFully(data);
    return data;
  }
  
  private int discoverRemoteEncryptionType(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString, int encryptionType) throws IOException
  {
    byte[] digestedClient = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString, encryptionType);
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_NONE)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_NONE;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
//    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_VMPC)))
//    {
//      return VTSystem.VT_CONNECTION_ENCRYPTION_VMPC;
//    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_GRAIN)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_ZUC)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_ZUC;
    }
    
    // sha256Digester.reset();
    // sha256Digester.update(localNonce);
    // sha256Digester.update(remoteNonce);
    // if (encryptionKey != null)
    // {
    // sha256Digester.update(encryptionKey);
    // }
    // if (VTArrayComparator.arrayEquals(digestedClient,
    // sha256Digester.digest(VT_CLIENT_CHECK_STRING_BLOWFISH)))
    // {
    // return VT.VT_CONNECTION_ENCRYPT_BLOWFISH;
    // }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_SALSA)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_SALSA;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_HC)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_HC;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_ISAAC)))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC;
    }
    
//    blake3Digest.reset();
//    blake3Digest.update(localNonce);
//    blake3Digest.update(remoteNonce);
//    if (encryptionKey != null)
//    {
//      blake3Digest.update(encryptionKey);
//    }
//    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_GRAIN)))
//    {
//      return VT.VT_CONNECTION_ENCRYPT_GRAIN;
//    }
    
    return -1;
  }
  
  public boolean verifyConnection() throws IOException
  {
    connected = true;
    setNonceStreams();
    exchangeNonces(false);
    setVerificationStreams();
    //exchangeNonces(true);
    // if (matchRemoteEncryptionSettings(localNonce, remoteNonce,
    // encryptionKey))
    // {
    // return true;
    // }
    
    // exchangeNonces(true);
    // setVerificationStreams(true);
    
    int remoteEncryptionType = 0;
    if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_NONE, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
        return true;
      }
    }
//    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_VMPC)
//    {
//      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_VMPC, encryptionType);
//      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
//      {
//        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_VMPC);
//        return true;
//      }
//    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_GRAIN, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_ZUC, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
        return true;
      }
    }
    // else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    // {
    // remoteEncryptionType = discoverRemoteEncryptionType(localNonce,
    // remoteNonce, null, VT_SERVER_CHECK_STRING_BLOWFISH);
    // if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    // {
    // setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
    // return true;
    // }
    // }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_SALSA)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_SALSA, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_SALSA);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_HC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_HC, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_HC);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_ISAAC, encryptionType);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC);
        return true;
      }
    }
//    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_GRAIN, encryptionType);
//      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
//      {
//        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
//        return true;
//      }
//    }
    
//    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_VMPC)
//    {
//      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_VMPC);
//      return true;
//    }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN);
      return true;
    }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
      return true;
    }
    // if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    // {
    // setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
    // return true;
    // }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_SALSA)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_SALSA);
      return true;
    }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_HC)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_HC);
      return true;
    }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ISAAC);
      return true;
    }
//    if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_GRAIN)
//    {
//      setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
//      return true;
//    }
    return false;
  }
  
  public void startConnection() throws IOException
  {
    setMultiplexedStreams();
    //exchangeNonces(true);
    multiplexedConnectionInputStream.startPacketReader();
  }
  
  /*
   * public boolean startedConnection() { return
   * multiplexedConnectionInputStream != null &&
   * multiplexedConnectionInputStream.isPacketReaderStarted(); }
   */
  
  public void closeGraphicsLinkStreams() throws IOException
  {
    try
    {
      graphicsDirectImageOutputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      heavyImageDataOutputStream.close();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      fastImageDataOutputStream.close();
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
      graphicsControlInputStream.close();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  // public void resetDirectGraphicsLinkStreams() throws IOException
  // {
  // graphicsDirectImageOutputStream.open();
  // graphicsDirectImageInputStream.open();
  // }
  
  public void resetGraphicsLinkStreams() throws IOException
  {
    graphicsControlOutputStream.open();
    //graphicsControlInputStream.open();
    
    graphicsDirectImageOutputStream.open();
    //graphicsDirectImageInputStream.open();
    graphicsHeavyImageOutputStream.open();
    //graphicsHeavyImageInputStream.open();
    graphicsFastImageOutputStream.open();
    //graphicsFastImageInputStream.open();
    
    graphicsControlDataInputStream.setIntputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream.setOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsControlOutputStream));
    
    heavyImageDataInputStream = new VTLittleEndianInputStream(graphicsHeavyImageInputStream);
    heavyImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZstdOutputStream(graphicsHeavyImageOutputStream));
    
    fastImageDataInputStream = new VTLittleEndianInputStream(graphicsFastImageInputStream);
    fastImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedSyncFlushFilteredZlibOutputStream(graphicsFastImageOutputStream));
    
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
    //graphicsClipboardInputStream.open();
    
    clipboardDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(graphicsClipboardInputStream);
    clipboardDataOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(graphicsClipboardOutputStream);
    
    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
  }
  
  public void resetFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.open();
    fileTransferControlOutputStream.open();
    fileTransferControlDataInputStream = new VTLittleEndianInputStream(fileTransferControlInputStream);
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(fileTransferControlOutputStream);
    //fileTransferDataInputStream.open();
  }
  
  public void closeFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.close();
    fileTransferDataInputStream.close();
    fileTransferControlOutputStream.close();
    fileTransferControlInputStream.close();
  }
  
  public void closeAudioStreams() throws IOException
  {
    audioDataOutputStream.close();
    audioDataInputStream.close();
  }
  
  public void resetAudioStreams() throws IOException
  {
    audioDataOutputStream.open();
    //audioDataInputStream.open();
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
  
  // public boolean isRunningAudio()
  // {
  // return !audioDataOutputStream.closed() && !audioDataInputStream.closed();
  // }
  
  public int getAvailableInputChannel()
  {
    return this.availableInputChannel;
  }
  
  public int getAvailableOutputChannel()
  {
    return this.availableOutputChannel;
  }
}