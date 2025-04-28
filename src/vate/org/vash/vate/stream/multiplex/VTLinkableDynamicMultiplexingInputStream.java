package org.vash.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.vash.vate.VT;
import org.vash.vate.security.VTSplitMix64Random;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.compress.VTPacketDecompressor;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public final class VTLinkableDynamicMultiplexingInputStream
{
  private volatile boolean closed = false;
  private final int bufferSize;
  private final byte[] packetDataBuffer;
  private Future<?> packetReaderThread;
  private final VTLittleEndianInputStream lin;
  private final VTLinkableDynamicMultiplexingInputStreamPacketReader packetReader;
  private final Map<Integer, VTLinkableDynamicMultiplexedInputStream> bufferedChannels;
  private final Map<Integer, VTLinkableDynamicMultiplexedInputStream> directChannels;
  private final VTXXHash64MessageDigest packetSeed;
  private final ExecutorService executorService;
  private final boolean server;
  private long transferredBytes = 0;
  
  public VTLinkableDynamicMultiplexingInputStream(final InputStream in, final int packetSize, final int bufferSize, boolean server, final boolean startPacketReader, final VTXXHash64MessageDigest packetSeed, final ExecutorService executorService)
  {
    this.server = server;
    this.packetSeed = packetSeed;
    this.executorService = executorService;
    this.bufferSize = bufferSize;
    this.packetDataBuffer = new byte[packetSize * 2];
    this.lin = new VTLittleEndianInputStream(in);
    this.bufferedChannels = new ConcurrentHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>();
    this.directChannels = new ConcurrentHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>();
    this.packetReader = new VTLinkableDynamicMultiplexingInputStreamPacketReader(this);
    if (startPacketReader)
    {
      packetReaderThread = executorService.submit(packetReader);
    }
  }
  
  public long getTransferredBytes()
  {
    return transferredBytes;
  }
  
  public void resetTransferredBytes()
  {
    transferredBytes = 0;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedInputStream linkInputStream(final int type, final Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if (link == null)
    {
      return stream;
    }
    if (link instanceof Integer)
    {
      stream = getInputStream(type, (Integer) link);
      if (stream != null && stream.getLink() == null)
      {
        stream.setLink(link);
      }
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    stream = searchInputStream(type);
    if (stream != null && stream.getLink() == null)
    {
      stream.setLink(link);
    }
    return stream;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedInputStream linkInputStream(final int type, final int number, final Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if (link == null)
    {
      return stream;
    }
    stream = getInputStream(type, number);
    if (stream != null)
    {
      stream.setLink(link);
    }
    return stream;
  }
  
  public synchronized final void releaseInputStream(final VTLinkableDynamicMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
  }
  
  private synchronized final VTLinkableDynamicMultiplexedInputStream getInputStream(final int type, final int number)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    Map<Integer, VTLinkableDynamicMultiplexedInputStream> channelMap;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
    {
      channelMap = bufferedChannels;
    }
    else
    {
      channelMap = directChannels;
    }
    stream = channelMap.get(number);
    if (stream != null)
    {
      stream.type(type);
      return stream;
    }
    stream = new VTLinkableDynamicMultiplexedInputStream(type, number, bufferSize, packetSeed);
    channelMap.put(number, stream);
    return stream;
  }
  
  private synchronized final VTLinkableDynamicMultiplexedInputStream searchInputStream(final int type)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    Map<Integer, VTLinkableDynamicMultiplexedInputStream> channelMap;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
    {
      channelMap = bufferedChannels;
    }
    else
    {
      channelMap = directChannels;
    }
    int start;
    if (server)
    {
      start = 1;
    }
    else
    {
      start = 0;
    }
    for (int number = start; number < 16777216; number += 2)
    {
      stream = channelMap.get(number);
      if (stream != null && stream.getLink() == null)
      {
        stream.type(type);
        return stream;
      }
      else if (stream == null)
      {
        stream = new VTLinkableDynamicMultiplexedInputStream(type, number, bufferSize, packetSeed);
        channelMap.put(number, stream); 
        return stream;
      }
    }
    return stream;
  }
  
  public final void startPacketReader()
  {
    if (packetReaderThread == null || packetReaderThread.isDone())
    {
      packetReaderThread = executorService.submit(packetReader);
    }
  }
  
  public final boolean isPacketReaderStarted()
  {
    if (packetReaderThread != null)
    {
      return !packetReaderThread.isDone();
    }
    return false;
  }
  
  public final void stopPacketReader() throws IOException, InterruptedException, ExecutionException
  {
    close();
    packetReaderThread.get();
  }
  
  private final void open(final int type, final int number) throws IOException
  {
    getInputStream(type, number).open();
  }
  
  public final void close(final int type, final int number) throws IOException
  {
    getInputStream(type, number).close();
  }
  
  public final void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    closed = true;
    lin.close();
    for (VTLinkableDynamicMultiplexedInputStream stream : bufferedChannels.values())
    {
      try
      {
        stream.close();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
    for (VTLinkableDynamicMultiplexedInputStream stream : directChannels.values())
    {
      try
      {
        stream.close();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
    }
    bufferedChannels.clear();
    directChannels.clear();
  }
  
  // critical method, handle with care
  private final void readPackets() throws IOException
  {
    VTLinkableDynamicMultiplexedInputStream stream;
    long sequence;
    int type; 
    int number;
    int length;
    
    while (!closed)
    {
      sequence = lin.readLong();
      type = lin.readByte();
      number = lin.readSubInt();
      length = lin.readInt();
      stream = getInputStream(type, number);
      if (stream == null)
      {
        close();
        return;
      }
      if (sequence != stream.getPacketSequencer().nextLong())
      {
        close();
        return;
      }
      if (length > 0)
      {
        lin.readFully(packetDataBuffer, 0, length);
        OutputStream out = stream.getOutputStream();
        try
        {
          out.write(packetDataBuffer, 0, length);
          out.flush();
        }
        catch (Throwable e)
        {
          //e.printStackTrace();
        }
        transferredBytes += VT.VT_PACKET_HEADER_SIZE_BYTES + length;
      }
      else if (length == -2)
      {
        close(type, number);
        transferredBytes += VT.VT_PACKET_HEADER_SIZE_BYTES;
      }
      else if (length == -3)
      {
        open(type, number);
        transferredBytes += VT.VT_PACKET_HEADER_SIZE_BYTES;
      }
      else
      {
        close();
        return;
      }
    }
  }
  
  public final class VTLinkableDynamicMultiplexedInputStream extends InputStream
  {
    private volatile boolean closed;
    private volatile Object link = null;
    private final int number;
    private final long seed;
    private volatile int type;
    private final VTPipedInputStream bufferedInputStream;
    private final VTPipedOutputStream bufferedOutputStream;
    private InputStream input;
    private OutputStream directOutputStream;
    private Closeable directCloseable;
    //private VTByteArrayOutputStream compressedPacketOutputPipe;
    private VTByteArrayInputStream compressedInputPipe;
    private InputStream compressedInputStream;
    private final Collection<Closeable> propagated;
    private final Random packetSequencer;
    
    private VTLinkableDynamicMultiplexedInputStream(final int type, final int number, final int bufferSize, final VTXXHash64MessageDigest packetSeed)
    {
      packetSeed.reset();
      packetSeed.update((byte)(number));
      packetSeed.update((byte)(number >> 8));
      packetSeed.update((byte)(number >> 16));
      packetSeed.update((byte)(number >> 24));
      this.seed = packetSeed.digestLong();
      this.packetSequencer = new VTSplitMix64Random(seed);
      this.type = type;
      this.number = number;
      this.propagated = new ConcurrentLinkedQueue<Closeable>();
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
      {
        this.bufferedInputStream = new VTPipedInputStream(bufferSize);
        this.bufferedOutputStream = new VTPipedOutputStream();
        try
        {
          this.bufferedInputStream.connect(this.bufferedOutputStream);
        }
        catch (IOException e)
        {
          
        }
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
        {
          this.input = bufferedInputStream;
        }
        else
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY) != 0)
          {
            //this.compressedInputStream = VTCompressorSelector.createDirectZlibInputStream(bufferedInputStream);
            this.compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(bufferedInputStream);
          }
          else
          {
            this.compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(bufferedInputStream);
          }
          this.input = compressedInputStream;
        }
      }
      else
      {
        this.bufferedInputStream = null;
        this.bufferedOutputStream = null;
      }
    }
    
    public final int number()
    {
      return number;
    }
    
    public final int type()
    {
      return type;
    }
    
    public final void type(final int type)
    {
      this.type = type;
    }
    
    public final Object getLink()
    {
      return link;
    }
    
    public final void setLink(final Object link)
    {
      this.link = link;
    }
    
    public final boolean closed()
    {
      return closed;
    }
    
    private final OutputStream getOutputStream()
    {
      if (directOutputStream != null)
      {
        return directOutputStream;
      }
      if (bufferedOutputStream != null)
      {
        return bufferedOutputStream;
      }
      return null;
    }
    
    public final void setOutputStream(final OutputStream outputStream, final Closeable closeable)
    {
      directCloseable = closeable;
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        directOutputStream = outputStream;
      }
      else
      {
        if (compressedInputPipe == null)
        {
          compressedInputPipe = new VTByteArrayInputStream(new byte[packetDataBuffer.length]);
        }
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY) != 0)
        {
          compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(compressedInputPipe);
        }
        else
        {
          compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(compressedInputPipe);
        }
        directOutputStream = new VTPacketDecompressor(compressedInputStream, outputStream, compressedInputPipe);
      }
    }
    
    public final void addPropagated(final Closeable propagated)
    {
      this.propagated.add(propagated);
    }
    
    public final void removePropagated(final Closeable propagated)
    {
      this.propagated.remove(propagated);
    }
    
    private final void open() throws IOException
    {
      closed = false;
      packetSequencer.setSeed(seed);
      if (bufferedInputStream != null)
      {
        bufferedInputStream.open();
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY) != 0)
          {
            compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(bufferedInputStream);
          }
          else
          {
            compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(bufferedInputStream);
          }
          input = compressedInputStream;
        }
        else
        {
          
        }
      }
      else
      {
        //work already done by setDirectOutputStream
      }
    }
    
    public final void close() throws IOException
    {
      closed = true;
      compressedInputStream = null;
      compressedInputPipe = null;
      if (directCloseable != null)
      {
        directCloseable.close();
        directCloseable = null;
      }
      if (directOutputStream != null)
      {
        //directOutputStream.close();
        directOutputStream = null;
      }
      if (bufferedOutputStream != null)
      {
        bufferedOutputStream.close();
      }
      if (propagated.size() > 0)
      {
        for (Closeable closeable : propagated)
        {
          try
          {
            closeable.close();
          }
          catch (Throwable t)
          {
            
          }
        }
      }
    }
    
    public final int available() throws IOException
    {
      return input.available();
    }
    
    public final int read() throws IOException
    {
      return input.read();
    }
    
    public final int read(final byte[] data) throws IOException
    {
      return input.read(data);
    }
    
    public final int read(final byte[] data, final int offset, final int length) throws IOException
    {
      return input.read(data, offset, length);
    }
    
    public final long skip(final long count) throws IOException
    {
      return input.skip(count);
    }
    
    private final Random getPacketSequencer()
    {
      return packetSequencer;
    }
  }
  
  private final class VTLinkableDynamicMultiplexingInputStreamPacketReader implements Runnable
  {
    private final VTLinkableDynamicMultiplexingInputStream multiplexingInputStream;
    
    private VTLinkableDynamicMultiplexingInputStreamPacketReader(VTLinkableDynamicMultiplexingInputStream multiplexingInputStream)
    {
      this.multiplexingInputStream = multiplexingInputStream;
    }
    
    public final void run()
    {
      try
      {
        multiplexingInputStream.readPackets();
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
      }
      try
      {
        multiplexingInputStream.close();
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
      }
    }
  }
}