package org.vash.vate.server.connection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.security.VTBlake3MessageDigest;
import org.vash.vate.security.VTCryptographicEngine;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.filter.VTBufferedOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTServerConnection
{
  private static final String MAJOR_MINOR_VERSION = VT.VT_MAJOR_VERSION + "/" + VT.VT_MINOR_VERSION;
  
  private static final byte[] VT_SERVER_CHECK_STRING_NONE = (StringUtils.reverse("VT/SERVER/NONE/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/NONE/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_NONE = (StringUtils.reverse("VT/CLIENT/NONE/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/NONE/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_VMPC = (StringUtils.reverse("VT/SERVER/VMPC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/VMPC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_VMPC = (StringUtils.reverse("VT/CLIENT/VMPC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/VMPC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_ZUC = (StringUtils.reverse("VT/SERVER/ZUC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_ZUC = (StringUtils.reverse("VT/CLIENT/ZUC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_SALSA = (StringUtils.reverse("VT/SERVER/SALSA/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_SALSA = (StringUtils.reverse("VT/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_HC256 = (StringUtils.reverse("VT/SERVER/HC256/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/HC256/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_HC256 = (StringUtils.reverse("VT/CLIENT/HC256/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/HC256/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_ISAAC = (StringUtils.reverse("VT/SERVER/ISAAC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/SERVER/ISAAC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_ISAAC = (StringUtils.reverse("VT/CLIENT/ISAAC/" + MAJOR_MINOR_VERSION).toLowerCase() + "/VT/CLIENT/ISAAC/" + MAJOR_MINOR_VERSION).getBytes();
  
  private volatile boolean connected = false;
  private volatile boolean closed = true;
  
  private int encryptionType;
  private byte[] encryptionKey;
  // private byte[] digestedClient;
  // private byte[] digestedServer;
  private final byte[] localNonce = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] remoteNonce = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] randomData = new byte[VT.VT_SECURITY_DIGEST_SIZE_BYTES];
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
  private VTLinkableDynamicMultiplexedInputStream pingInputStream;
  private VTLinkableDynamicMultiplexedInputStream tunnelControlInputStream;
  // private VTLinkableDynamicMultiplexedInputStream socksControlInputStream;
  
  // private OutputStream authenticationOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream shellOutputStream;
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
  private VTLinkableDynamicMultiplexedOutputStream pingOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream tunnelControlOutputStream;
  // private VTLinkableDynamicMultiplexedOutputStream socksControlOutputStream;
  
  // private VTLittleEndianInputStream verificationReader;
  // private VTLittleEndianOutputStream verificationWriter;
  private VTLittleEndianInputStream authenticationReader;
  private VTLittleEndianOutputStream authenticationWriter;
  private VTLittleEndianInputStream commandReader;
  private BufferedWriter resultWriter;
  private InputStream shellDataInputStream;
  private OutputStream shellDataOutputStream;
  private InputStream clipboardDataInputStream;
  private OutputStream clipboardDataOutputStream;
  
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
  
  // private boolean zstdAvailable;
  
  // private OutputStream bufferedGraphicsHeavyImageOutputStream;
  // private OutputStream bufferedGraphicsFastImageOutputStream;
  
  // private ZstdOutputStream zstdImageOutputStream;
  
  // private ZstdInputStream zstdClipboardInputStream;
  // private ZstdOutputStream zstdClipboardOutputStream;
  
  public VTServerConnection()
  {
    // try
    // {
    // this.sha256Digester = MessageDigest.getInstance("SHA-256");
    // }
    // catch (NoSuchAlgorithmException e)
    // {
    // e.printStackTrace();
    // }
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
  
  public BufferedWriter getResultWriter()
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
  
  public OutputStream getPingOutputStream()
  {
    return pingOutputStream;
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
  
  public InputStream getPingInputStream()
  {
    return pingInputStream;
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
      VTConsole.print("\rVT>Connection with client closed!\nVT>");
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
    byte[] seed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, seed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, seed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    //secureRandom.setSeed(seed);
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
  }
  
  private void setVerificationStreams() throws IOException
  {
    cryptoEngine.initializeServerEngine(VT.VT_CONNECTION_ENCRYPT_NONE, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
    nonceReader.setIntputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }
  
  public void setAuthenticationStreams() throws IOException
  {
    //exchangeNonces(true);
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setIntputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream));
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
    //cryptoEngine.initializeServerEngine(encryptionType, encryptionKey, remoteNonce, localNonce, digestedCredentials, user != null ? user.getBytes("UTF-8") : null, password != null ? password.getBytes("UTF-8") : null);
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey, digestedCredentials);
    connectionInputStream = cryptoEngine.getDecryptedInputStream(connectionSocketInputStream);
    connectionOutputStream = cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream);
    //authenticationReader.setIntputStream(connectionInputStream);
    //authenticationWriter.setOutputStream(connectionOutputStream);
    //nonceReader.setIntputStream(authenticationReader.getInputStream());
    //nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
    return true;
  }
  
  private void setMultiplexedStreams() throws IOException
  {
    byte[] inputSeed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(remoteNonce, 0, inputSeed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(localNonce, 0, inputSeed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    
    byte[] outputSeed = new byte[VT.VT_SECURITY_SEED_SIZE_BYTES];
    System.arraycopy(localNonce, 0, outputSeed, 0, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    System.arraycopy(remoteNonce, 0, outputSeed, VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT.VT_SECURITY_DIGEST_SIZE_BYTES);
    
    VTBlake3SecureRandom secureInputputSeed = new VTBlake3SecureRandom(inputSeed);
    VTBlake3SecureRandom secureOutputSeed = new VTBlake3SecureRandom(outputSeed);
    
    multiplexedConnectionInputStream = new VTLinkableDynamicMultiplexingInputStream(connectionInputStream, VT.VT_PACKET_DATA_SIZE_BYTES, VT.VT_CHANNEL_PACKET_BUFFER_SIZE_BYTES, false, secureInputputSeed);
    multiplexedConnectionOutputStream = new VTLinkableDynamicMultiplexingOutputStream(connectionOutputStream, VT.VT_PACKET_DATA_SIZE_BYTES, secureOutputSeed);
    
    pingInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, 0);
    pingOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED, 0);
    
    shellInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 1);
    shellOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 1);
    
    fileTransferControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 2);
    fileTransferControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 2);
    fileTransferDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 3);
    fileTransferDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 3);
    
    graphicsControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 4);
    graphicsControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 4);
    graphicsDirectImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 5);
    graphicsDirectImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 5);
    graphicsHeavyImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 6);
    graphicsHeavyImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 6);
    graphicsFastImageInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 7);
    graphicsFastImageOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 7);
    graphicsClipboardInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 8);
    graphicsClipboardOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 8);
    
    //graphicsControlInputStream.addPropagated(graphicsControlOutputStream);
    //graphicsControlInputStream.addPropagated(graphicsDirectImageOutputStream);
    //graphicsControlInputStream.addPropagated(graphicsHeavyImageOutputStream);
    //graphicsControlInputStream.addPropagated(graphicsFastImageOutputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardInputStream);
    // graphicsControlInputStream.addPropagated(graphicsClipboardOutputStream);
    
    audioControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 9);
    audioControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 9);
    audioDataInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 10);
    audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 10);
    
    //audioDataOutputStream.addPropagated(audioDataInputStream);
    //audioDataInputStream.addPropagated(audioDataOutputStream);
    
    tunnelControlInputStream = multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 11);
    tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 11);
    
    // socksControlInputStream =
    // multiplexedConnectionInputStream.linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED,
    // 12);
    // socksControlOutputStream =
    // multiplexedConnectionOutputStream.linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED,
    // 12);
    
    shellDataOutputStream = VTCompressorSelector.createBufferedZlibOutputStreamFiltered(shellOutputStream);
    // shellDataOutputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushDeflaterOutputStream(shellOutputStream);
    // shellDataOutputStream = shellOutputStream;
    
    shellDataInputStream = VTCompressorSelector.createBufferedZlibInputStream(shellInputStream);
    // shellDataInputStream =
    // VTCompressorSelector.createFlushBufferedSyncFlushInflaterInputStream(shellInputStream);
    // shellDataInputStream = shellInputStream;
    
    commandReader = new VTLittleEndianInputStream(shellDataInputStream);
    resultWriter = new BufferedWriter(new OutputStreamWriter(shellDataOutputStream, "UTF-8"));
    
    graphicsControlDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsControlOutputStream));
    
    directImageDataInputStream = new VTLittleEndianInputStream(graphicsDirectImageInputStream);
    directImageDataOutputStream = new VTLittleEndianOutputStream(new VTBufferedOutputStream(graphicsDirectImageOutputStream, VT.VT_STANDARD_BUFFER_SIZE_BYTES, false));
    
    heavyImageDataInputStream = new VTLittleEndianInputStream(graphicsHeavyImageInputStream);
    heavyImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZstdOutputStream(graphicsHeavyImageOutputStream));
    
    fastImageDataInputStream = new VTLittleEndianInputStream(graphicsFastImageInputStream);
    fastImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZlibOutputStreamFiltered(graphicsFastImageOutputStream));
    
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(graphicsClipboardOutputStream);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZstdInputStream(graphicsClipboardInputStream);
    
    fileTransferControlDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedLz4InputStream(fileTransferControlInputStream));
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(fileTransferControlOutputStream));
    
    // closeAudioStreams();
    // audioDataInputStream.addPropagated(audioDataOutputStream);
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
    byte[] data = blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, localCheckString);
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
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_NONE)))
    {
      return VT.VT_CONNECTION_ENCRYPT_NONE;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_VMPC)))
    {
      return VT.VT_CONNECTION_ENCRYPT_VMPC;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_ZUC)))
    {
      return VT.VT_CONNECTION_ENCRYPT_ZUC;
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
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_SALSA)))
    {
      return VT.VT_CONNECTION_ENCRYPT_SALSA;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_HC256)))
    {
      return VT.VT_CONNECTION_ENCRYPT_HC256;
    }
    
    blake3Digest.reset();
    blake3Digest.update(localNonce);
    blake3Digest.update(remoteNonce);
    if (encryptionKey != null)
    {
      blake3Digest.update(encryptionKey);
    }
    if (VTArrayComparator.arrayEquals(digestedClient, blake3Digest.digest(VT.VT_SECURITY_DIGEST_SIZE_BYTES, VT_CLIENT_CHECK_STRING_ISAAC)))
    {
      return VT.VT_CONNECTION_ENCRYPT_ISAAC;
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
    if (encryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_NONE, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_VMPC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_VMPC, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_VMPC);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ZUC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_ZUC, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ZUC);
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
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_SALSA)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_SALSA, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_HC256)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_HC256, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
        return true;
      }
    }
    else if (encryptionType == VT.VT_CONNECTION_ENCRYPT_ISAAC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_ISAAC, encryptionType);
      if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_NONE)
      {
        setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
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
    
    if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_VMPC)
    {
      setEncryptionType(VT.VT_CONNECTION_ENCRYPT_VMPC);
      return true;
    }
    if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_ZUC)
    {
      setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ZUC);
      return true;
    }
    // if (remoteEncryptionType == VT.VT_CONNECTION_ENCRYPT_BLOWFISH)
    // {
    // setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
    // return true;
    // }
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
  
  public void closeGraphicsModeStreams() throws IOException
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
  
  // public void resetDirectGraphicsModeStreams() throws IOException
  // {
  // graphicsDirectImageOutputStream.open();
  // graphicsDirectImageInputStream.open();
  // }
  
  public void resetGraphicsModeStreams() throws IOException
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
    fastImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZlibOutputStreamFiltered(graphicsFastImageOutputStream));
    
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
    
    clipboardDataOutputStream = VTCompressorSelector.createBufferedZstdOutputStream(graphicsClipboardOutputStream);
    clipboardDataInputStream = VTCompressorSelector.createBufferedZstdInputStream(graphicsClipboardInputStream);
    
    // graphicsControlInputStream.addPropagated(clipboardDataOutputStream);
    // graphicsControlInputStream.addPropagated(clipboardDataInputStream);
  }
  
  public void resetFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.open();
    //fileTransferDataInputStream.open();
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
}