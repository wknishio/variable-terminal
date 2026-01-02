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
import java.util.concurrent.atomic.AtomicLong;

import org.vash.vate.VTSystem;
import org.vash.vate.security.VTSplitMix64Random;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.array.VTByteArrayInputStream;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.compress.VTPacketDecompressor;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public final class VTMultiplexingInputStream
{
  private volatile boolean closed = false;
  private final int bufferSize;
  private final byte[] packetDataBuffer;
  private Future<?> packetReaderThread;
  private final VTLittleEndianInputStream input;
  private final VTMultiplexingInputStreamPacketReader packetReader;
  private final Map<Integer, VTMultiplexedInputStream> bufferedChannels;
  private final Map<Integer, VTMultiplexedInputStream> directChannels;
  private final VTXXHash64MessageDigest packetSeed;
  private final ExecutorService executorService;
  private final boolean server;
  private AtomicLong transferredBytes = new AtomicLong(0);
  
  public VTMultiplexingInputStream(final InputStream input, final boolean server, final int packetSize, final int bufferSize, final VTXXHash64MessageDigest packetSeed, final ExecutorService executorService, final boolean startPacketReader)
  {
    this.server = server;
    this.packetSeed = packetSeed;
    this.executorService = executorService;
    this.bufferSize = bufferSize;
    this.packetDataBuffer = new byte[packetSize * 2];
    this.input = new VTLittleEndianInputStream(input);
    this.bufferedChannels = new ConcurrentHashMap<Integer, VTMultiplexedInputStream>();
    this.directChannels = new ConcurrentHashMap<Integer, VTMultiplexedInputStream>();
    this.packetReader = new VTMultiplexingInputStreamPacketReader(this);
    if (startPacketReader)
    {
      packetReaderThread = executorService.submit(packetReader);
    }
  }
  
  public long getTransferredBytes()
  {
    return transferredBytes.get();
  }
  
  public void resetTransferredBytes()
  {
    transferredBytes.set(0);
  }
  
  public synchronized final VTMultiplexedInputStream linkInputStream(final int type, final Object link)
  {
    VTMultiplexedInputStream stream = null;
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
  
  public synchronized final VTMultiplexedInputStream linkInputStream(final int type, final int number, final Object link)
  {
    VTMultiplexedInputStream stream = null;
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
  
  public synchronized final void releaseInputStream(final VTMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
  }
  
  private synchronized final VTMultiplexedInputStream getInputStream(final int type, final int number)
  {
    VTMultiplexedInputStream stream = null;
    Map<Integer, VTMultiplexedInputStream> channelMap;
    if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
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
    stream = new VTMultiplexedInputStream(type, number, bufferSize, packetSeed);
    channelMap.put(number, stream);
    return stream;
  }
  
  private synchronized final VTMultiplexedInputStream searchInputStream(final int type)
  {
    VTMultiplexedInputStream stream = null;
    Map<Integer, VTMultiplexedInputStream> channelMap;
    if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
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
        stream = new VTMultiplexedInputStream(type, number, bufferSize, packetSeed);
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
    input.close();
    for (VTMultiplexedInputStream stream : bufferedChannels.values())
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
    for (VTMultiplexedInputStream stream : directChannels.values())
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
    VTMultiplexedInputStream stream;
    long sequence;
    int type; 
    int number;
    int length;
    
    while (!closed)
    {
      sequence = input.readLong();
      type = input.readByte();
      number = input.readSubInt();
      length = input.readInt();
      input.readFully(packetDataBuffer, 0, length);
      stream = getInputStream(type, number);
      if (stream.getPacketSequencer().nextLong() != sequence || stream == null)
      {
        close();
        return;
      }
      if (length > 0)
      {
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
        transferredBytes.addAndGet(VTSystem.VT_PACKET_HEADER_SIZE_BYTES + length);
      }
      else if (length == -2)
      {
        close(type, number);
        transferredBytes.addAndGet(VTSystem.VT_PACKET_HEADER_SIZE_BYTES);
      }
      else if (length == -3)
      {
        open(type, number);
        transferredBytes.addAndGet(VTSystem.VT_PACKET_HEADER_SIZE_BYTES);
      }
      else
      {
        close();
        return;
      }
    }
  }
  
  public final class VTMultiplexedInputStream extends InputStream
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
    
    private VTMultiplexedInputStream(final int type, final int number, final int bufferSize, final VTXXHash64MessageDigest packetSeed)
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
      if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
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
        if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
        {
          this.input = bufferedInputStream;
        }
        else
        {
          if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_HEAVY) != 0)
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
    
    public final void ready()
    {
      synchronized (this)
      {
        if (closed)
        {
          try
          {
            wait();
          }
          catch (Throwable t)
          {
            
          }
        }
      }
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
      if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        directOutputStream = outputStream;
      }
      else
      {
        if (compressedInputPipe == null)
        {
          compressedInputPipe = new VTByteArrayInputStream(new byte[packetDataBuffer.length]);
        }
        if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_HEAVY) != 0)
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
      packetSequencer.setSeed(seed);
      if (bufferedInputStream != null)
      {
        bufferedInputStream.open();
        if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
        {
          if ((type & VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_HEAVY) != 0)
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
      synchronized (this)
      {
        closed = false;
        notifyAll();
      }
    }
    
    public final void close() throws IOException
    {
      compressedInputStream = null;
      compressedInputPipe = null;
      if (directCloseable != null)
      {
        try
        {
          directCloseable.close();
        }
        catch (Throwable t)
        {
          
        }
        directCloseable = null;
      }
      if (directOutputStream != null)
      {
        //directOutputStream.close();
        directOutputStream = null;
      }
      if (bufferedOutputStream != null)
      {
        try
        {
          bufferedOutputStream.close();
        }
        catch (Throwable t)
        {
          
        }
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
      synchronized (this)
      {
        closed = true;
        notifyAll();
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
  
  private final class VTMultiplexingInputStreamPacketReader implements Runnable
  {
    private final VTMultiplexingInputStream multiplexingInputStream;
    
    private VTMultiplexingInputStreamPacketReader(VTMultiplexingInputStream multiplexingInputStream)
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