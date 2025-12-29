package org.vash.vate.server.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.zip.Deflater;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.net.jpountz.xxhash.XXHashFactory;
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

public class VTServerConnection
{
  private static final String MAJOR_MINOR_VERSION = VTSystem.VT_MAJOR_VERSION + "/" + VTSystem.VT_MINOR_VERSION;
  
  private static final byte[] VT_SERVER_CHECK_STRING_NONE = ("/VARIABLE-TERMINAL/SERVER/NONE/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_NONE = ("/VARIABLE-TERMINAL/CLIENT/NONE/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_SALSA = ("/VARIABLE-TERMINAL/SERVER/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_SALSA = ("/VARIABLE-TERMINAL/CLIENT/SALSA/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_HC = ("/VARIABLE-TERMINAL/SERVER/HC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_HC = ("/VARIABLE-TERMINAL/CLIENT/HC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_ZUC = ("/VARIABLE-TERMINAL/SERVER/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_ZUC = ("/VARIABLE-TERMINAL/CLIENT/ZUC/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_SERVER_CHECK_STRING_THREEFISH = ("/VARIABLE-TERMINAL/SERVER/THREEFISH/" + MAJOR_MINOR_VERSION).getBytes();
  private static final byte[] VT_CLIENT_CHECK_STRING_THREEFISH = ("/VARIABLE-TERMINAL/CLIENT/THREEFISH/" + MAJOR_MINOR_VERSION).getBytes();
  
  private volatile boolean connected = false;
  private volatile boolean closed = true;
  
  private int encryptionType;
  private int availableInputChannel;
  private int availableOutputChannel;
  private byte[] encryptionKey;
  private byte[] digestedCredentials;
  private final byte[] localNonce = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] remoteNonce = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
  private final byte[] randomData = new byte[VTSystem.VT_SECURITY_DIGEST_SIZE_BYTES];
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
  
  private VTLinkableDynamicMultiplexedInputStream pingClientInputStream;
  private VTLinkableDynamicMultiplexedInputStream pingServerInputStream;
  private VTLinkableDynamicMultiplexedInputStream shellInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream fileTransferDataInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsControlInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsDirectImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsHeavyImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsFastImageInputStream;
  private VTLinkableDynamicMultiplexedInputStream graphicsClipboardInputStream;
  private VTLinkableDynamicMultiplexedInputStream audioDataInputStream;
  private VTLinkableDynamicMultiplexedInputStream tunnelControlInputStream;
  
  private VTLinkableDynamicMultiplexedOutputStream pingClientOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream pingServerOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream shellOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream fileTransferDataOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsControlOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsDirectImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsHeavyImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsFastImageOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream graphicsClipboardOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream audioDataOutputStream;
  private VTLinkableDynamicMultiplexedOutputStream tunnelControlOutputStream;
  
  private VTLittleEndianInputStream authenticationReader;
  private VTLittleEndianOutputStream authenticationWriter;
  private VTLittleEndianInputStream commandReader;
  private VTLittleEndianOutputStream resultWriter;
  
  private InputStream shellDataInputStream;
  private OutputStream shellDataOutputStream;
  
  private VTLittleEndianInputStream fileTransferControlDataInputStream;
  private VTLittleEndianOutputStream fileTransferControlDataOutputStream;
  
  private VTLittleEndianInputStream graphicsControlDataInputStream;
  private VTLittleEndianOutputStream graphicsControlDataOutputStream;
  private VTLittleEndianInputStream graphicsDirectImageDataInputStream;
  private VTLittleEndianOutputStream graphicsDirectImageDataOutputStream;
  private VTLittleEndianInputStream graphicsHeavyImageDataInputStream;
  private VTLittleEndianOutputStream graphicsHeavyImageDataOutputStream;
  private VTLittleEndianInputStream graphicsFastImageDataInputStream;
  private VTLittleEndianOutputStream graphicsFastImageDataOutputStream;
  
  private VTLittleEndianInputStream clipboardDataInputStream;
  private VTLittleEndianOutputStream clipboardDataOutputStream;
  
  private final ExecutorService executorService;
  
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
  
  public InputStream getShellInputStream()
  {
    return shellInputStream;
  }
  
  public InputStream getFileTransferDataInputStream()
  {
    return fileTransferDataInputStream;
  }
  
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
    return graphicsDirectImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsHeavyImageDataInputStream()
  {
    return graphicsHeavyImageDataInputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsFastImageDataInputStream()
  {
    return graphicsFastImageDataInputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsDirectImageDataOutputStream()
  {
    return graphicsDirectImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsHeavyImageDataOutputStream()
  {
    return graphicsHeavyImageDataOutputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsFastImageDataOutputStream()
  {
    return graphicsFastImageDataOutputStream;
  }
  
  public OutputStream getTunnelControlOutputStream()
  {
    return tunnelControlOutputStream;
  }
  
  public OutputStream getAudioDataOutputStream()
  {
    return audioDataOutputStream;
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
  
  public OutputStream getGraphicsClipboardOutputStream()
  {
    return graphicsClipboardOutputStream;
  }
  
  public VTLittleEndianInputStream getGraphicsClipboardDataInputStream()
  {
    return clipboardDataInputStream;
  }
  
  public VTLittleEndianOutputStream getGraphicsClipboardDataOutputStream()
  {
    return clipboardDataOutputStream;
  }
  
  public void closeSockets()
  {
    if (closed)
    {
      return;
    }
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
    connectionSocketInputStream = connectionSocket.getInputStream();
    connectionSocketOutputStream = connectionSocket.getOutputStream();
    nonceReader = new VTLittleEndianInputStream(connectionSocketInputStream);
    nonceWriter = new VTLittleEndianOutputStream(connectionSocketOutputStream);
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
    blake3Digest.setSeed(seed);
    blake3Digest.reset();
  }
  
  private void setVerificationStreams() throws IOException
  {
    cryptoEngine.initializeServerEngine(VTSystem.VT_CONNECTION_ENCRYPTION_NONE, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    nonceReader.setInputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }
  
  public void setAuthenticationStreams() throws IOException
  {
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey);
    authenticationReader.setInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    authenticationWriter.setOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES));
    nonceReader.setInputStream(authenticationReader.getInputStream());
    nonceWriter.setOutputStream(authenticationWriter.getOutputStream());
  }
  
  public boolean setConnectionStreams(byte[] digestedCredentials) throws IOException
  {
    exchangeNonces(true);
    this.digestedCredentials = digestedCredentials;
    cryptoEngine.initializeServerEngine(encryptionType, remoteNonce, localNonce, encryptionKey, digestedCredentials);
    connectionInputStream = new BufferedInputStream(cryptoEngine.getDecryptedInputStream(connectionSocketInputStream, VTSystem.VT_CONNECTION_INPUT_PACKET_BUFFER_SIZE_BYTES), VTSystem.VT_CONNECTION_INPUT_PACKET_BUFFER_SIZE_BYTES);
    connectionOutputStream = new BufferedOutputStream(cryptoEngine.getEncryptedOutputStream(connectionSocketOutputStream, VTSystem.VT_CONNECTION_OUTPUT_PACKET_BUFFER_SIZE_BYTES), VTSystem.VT_CONNECTION_OUTPUT_PACKET_BUFFER_SIZE_BYTES);
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
    
    audioDataInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    audioDataOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    tunnelControlInputStream = multiplexedConnectionInputStream.linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, inputChannel++);
    tunnelControlOutputStream = multiplexedConnectionOutputStream.linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, outputChannel++);
    
    availableInputChannel = inputChannel;
    availableOutputChannel = outputChannel;
    
    shellDataInputStream = VTCompressorSelector.createBufferedLz4InputStream(shellInputStream);
    shellDataOutputStream = VTCompressorSelector.createBufferedLz4OutputStream(shellOutputStream);
    
    commandReader = new VTLittleEndianInputStream(shellDataInputStream);
    resultWriter = new VTLittleEndianOutputStream(shellDataOutputStream);
    
    fileTransferControlDataInputStream = new VTLittleEndianInputStream(fileTransferControlInputStream);
    fileTransferControlDataOutputStream = new VTLittleEndianOutputStream(fileTransferControlOutputStream);
    
    graphicsControlDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsControlOutputStream));
    
    graphicsDirectImageDataInputStream = new VTLittleEndianInputStream(graphicsDirectImageInputStream);
    graphicsDirectImageDataOutputStream = new VTLittleEndianOutputStream(new VTFlushBufferedOutputStream(graphicsDirectImageOutputStream, new VTByteArrayOutputStream(VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES)));
    
    graphicsHeavyImageDataInputStream = new VTLittleEndianInputStream(graphicsHeavyImageInputStream);
    graphicsHeavyImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedZstdOutputStream(graphicsHeavyImageOutputStream));
    
    graphicsFastImageDataInputStream = new VTLittleEndianInputStream(graphicsFastImageInputStream);
    graphicsFastImageDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedSyncFlushZlibOutputStream(graphicsFastImageOutputStream, Deflater.DEFAULT_STRATEGY));
    
    clipboardDataInputStream = new VTLittleEndianInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsClipboardInputStream));
    clipboardDataOutputStream = new VTLittleEndianOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsClipboardOutputStream));
    
    fileTransferControlInputStream.close();
    graphicsControlInputStream.close();
    graphicsClipboardInputStream.close();
    audioDataInputStream.close();
  }
  
  private byte[] exchangeCheckString(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString) throws IOException
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
  
  private int discoverRemoteEncryptionType(byte[] localNonce, byte[] remoteNonce, byte[] encryptionKey, byte[] localCheckString) throws IOException
  {
    byte[] digestedClientNONE = computeSecurityDigest(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_NONE);
    byte[] digestedClientSALSA = computeSecurityDigest(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_SALSA);
    byte[] digestedClientHC = computeSecurityDigest(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_HC);
    byte[] digestedClientZUC = computeSecurityDigest(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_ZUC);
    byte[] digestedClientTHREEFISH = computeSecurityDigest(localNonce, remoteNonce, encryptionKey, VT_CLIENT_CHECK_STRING_THREEFISH);
    
    byte[] digestedClient = exchangeCheckString(localNonce, remoteNonce, encryptionKey, localCheckString);
   
    if (VTArrayComparator.arrayEquals(digestedClient, digestedClientNONE))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_NONE;
    }
    
    if (VTArrayComparator.arrayEquals(digestedClient, digestedClientSALSA))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_SALSA;
    }
    
    if (VTArrayComparator.arrayEquals(digestedClient, digestedClientHC))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_HC;
    }
    
    if (VTArrayComparator.arrayEquals(digestedClient, digestedClientZUC))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_ZUC;
    }
    
    if (VTArrayComparator.arrayEquals(digestedClient, digestedClientTHREEFISH))
    {
      return VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH;
    }
    
    return -1;
  }
  
  public boolean verifyConnection() throws IOException
  {
    connected = true;
    setNonceStreams();
    exchangeNonces(false);
    setVerificationStreams();
    
    int remoteEncryptionType = 0;
    if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_NONE);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_SALSA)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_SALSA);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_SALSA);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_HC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_HC);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_HC);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_ZUC);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
        return true;
      }
    }
    else if (encryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH)
    {
      remoteEncryptionType = discoverRemoteEncryptionType(localNonce, remoteNonce, encryptionKey, VT_SERVER_CHECK_STRING_THREEFISH);
      if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_NONE)
      {
        setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH);
        return true;
      }
    }
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
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_ZUC)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
      return true;
    }
    if (remoteEncryptionType == VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH)
    {
      setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_THREEFISH);
      return true;
    }
    return false;
  }
  
  public void startConnection() throws IOException
  {
    setMultiplexedStreams();
    multiplexedConnectionInputStream.startPacketReader();
  }
  
  public void closeGraphicsStreams() throws IOException
  {
    graphicsDirectImageOutputStream.close();
    graphicsHeavyImageDataOutputStream.close();
    graphicsFastImageDataOutputStream.close();
    graphicsControlInputStream.close();
    graphicsControlOutputStream.close();
    closeClipboardStreams();
  }
  
  public void closeClipboardStreams() throws IOException
  {
    clipboardDataInputStream.close();
    clipboardDataOutputStream.close();
  }
  
  public void resetGraphicsStreams() throws IOException
  {
    graphicsDirectImageOutputStream.open();
    graphicsHeavyImageOutputStream.open();
    graphicsFastImageOutputStream.open();
    graphicsControlOutputStream.open();
    graphicsControlInputStream.ready();
    graphicsControlDataInputStream.setInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsControlInputStream));
    graphicsControlDataOutputStream.setOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsControlOutputStream));
    graphicsHeavyImageDataInputStream.setInputStream(graphicsHeavyImageInputStream);
    graphicsHeavyImageDataOutputStream.setOutputStream(VTCompressorSelector.createBufferedZstdOutputStream(graphicsHeavyImageOutputStream));
    graphicsFastImageDataInputStream.setInputStream(graphicsFastImageInputStream);
    graphicsFastImageDataOutputStream.setOutputStream(VTCompressorSelector.createBufferedSyncFlushZlibOutputStream(graphicsFastImageOutputStream, Deflater.DEFAULT_STRATEGY));
    resetClipboardStreams();
  }
  
  public void resetClipboardStreams() throws IOException
  {
    graphicsClipboardOutputStream.open();
    graphicsClipboardInputStream.ready();
    clipboardDataInputStream.setInputStream(VTCompressorSelector.createBufferedLz4InputStream(graphicsClipboardInputStream));
    clipboardDataOutputStream.setOutputStream(VTCompressorSelector.createBufferedLz4OutputStream(graphicsClipboardOutputStream));
  }
  
  public void resetFileTransferStreams() throws IOException
  {
    fileTransferDataOutputStream.open();
    fileTransferControlOutputStream.open();
    fileTransferControlInputStream.ready();
  }
  
  public void closeFileTransferStreams() throws IOException
  {
    fileTransferDataInputStream.close();
    fileTransferControlInputStream.close();
    fileTransferDataOutputStream.close();
    fileTransferControlOutputStream.close();
  }
  
  public void closeAudioStreams() throws IOException
  {
    audioDataInputStream.close();
    audioDataOutputStream.close();
  }
  
  public void resetAudioStreams() throws IOException
  {
    audioDataOutputStream.open();
    audioDataInputStream.ready();
  }
  
  public void setRateInBytesPerSecond(long bytesPerSecond)
  {
    multiplexedConnectionOutputStream.setBytesPerSecond(bytesPerSecond);
  }
  
  public long getRateInBytesPerSecond()
  {
    return multiplexedConnectionOutputStream.getBytesPerSecond();
  }
  
  public int getAvailableInputChannel()
  {
    return this.availableInputChannel;
  }
  
  public int getAvailableOutputChannel()
  {
    return this.availableOutputChannel;
  }
}