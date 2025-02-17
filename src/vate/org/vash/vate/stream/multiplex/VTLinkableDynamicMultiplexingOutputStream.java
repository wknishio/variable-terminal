package org.vash.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
import org.vash.vate.security.VTSplitMix64Random;
import org.vash.vate.security.VTXXHash64MessageDigest;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.limit.VTThrottledOutputStream;

import engineering.clientside.throttle.NanoThrottle;

public final class VTLinkableDynamicMultiplexingOutputStream
{
  private final int packetSize;
  private final OutputStream original;
  private final OutputStream throttled;
  private final NanoThrottle throttler;
  private final Map<Integer, VTLinkableDynamicMultiplexedOutputStream> bufferedChannels;
  private final Map<Integer, VTLinkableDynamicMultiplexedOutputStream> directChannels;
  private final VTXXHash64MessageDigest packetSeed;
  @SuppressWarnings("unused")
  private final ExecutorService executorService;
  private long transferredBytes = 0;
  
  public VTLinkableDynamicMultiplexingOutputStream(final OutputStream out, final int packetSize, final VTXXHash64MessageDigest packetSeed, final ExecutorService executorService)
  {
    this.packetSeed = packetSeed;
    this.executorService = executorService;
    this.throttler = new NanoThrottle(Long.MAX_VALUE, (1d / 8d), true);
    this.original = out;
    this.throttled = new VTThrottledOutputStream(original, throttler);
    this.bufferedChannels = new ConcurrentHashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>();
    this.directChannels = new ConcurrentHashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>();
    this.packetSize = packetSize;
  }
  
  public long getTransferredBytes()
  {
    return transferredBytes;
  }
  
  public void resetTransferredBytes()
  {
    transferredBytes = 0;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedOutputStream linkOutputStream(final int type, final Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    if (link == null)
    {
      return stream;
    }
    if (link instanceof Integer)
    {
      stream = getOutputStream(type, (Integer) link);
      if (stream != null && stream.getLink() == null)
      {
        stream.setLink(link);
      }
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    stream = getOutputStream(type);
    if (stream != null && stream.getLink() == null)
    {
      stream.setLink(link);
    }
    return stream;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedOutputStream linkOutputStream(final int type, final int number, final Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    if (link == null)
    {
      return stream;
    }
    stream = getOutputStream(type, number);
    if (stream != null)
    {
      stream.setLink(link);
    }
    return stream;
  }
  
  public synchronized final void releaseOutputStream(final VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
  }
  
  private synchronized final VTLinkableDynamicMultiplexedOutputStream getOutputStream(final int type, final int number)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    Map<Integer, VTLinkableDynamicMultiplexedOutputStream> channelMap;
    OutputStream output = null;
    boolean unlimited = ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED) != 0);
    if (unlimited)
    {
      output = original;
    }
    else
    {
      output = throttled;
    }
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
      stream.output(output);
      stream.control(original);
      return stream;
    }
    stream = new VTLinkableDynamicMultiplexedOutputStream(output, original, type, number, packetSize, packetSeed);
    channelMap.put(number, stream);
    return stream;
  }
  
  private synchronized final VTLinkableDynamicMultiplexedOutputStream getOutputStream(final int type)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    Map<Integer, VTLinkableDynamicMultiplexedOutputStream> channelMap;
    OutputStream output = null;
    boolean unlimited = ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED) != 0);
    if (unlimited)
    {
      output = original;
    }
    else
    {
      output = throttled;
    }
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
    {
      channelMap = bufferedChannels;
    }
    else
    {
      channelMap = directChannels;
    }
    for (int number = 0; number < 16777216; number++)
    {
      stream = channelMap.get(number);
      if (stream != null && stream.getLink() == null)
      {
        stream.type(type);
        stream.output(output);
        stream.control(original);
        return stream;
      }
      else if (stream == null)
      {
        stream = new VTLinkableDynamicMultiplexedOutputStream(output, original, type, number, packetSize, packetSeed);
        channelMap.put(number, stream);
        return stream;
      }
    }
    return stream;
  }
  
  public final int getPacketSize()
  {
    return packetSize;
  }
  
  public final void setBytesPerSecond(final long bytesPerSecond)
  {
    if (bytesPerSecond > 0)
    {
      throttler.setRate(bytesPerSecond);
    }
    else
    {
      throttler.setRate(Long.MAX_VALUE);
    }
  }
  
  public final long getBytesPerSecond()
  {
    long rate = (long) throttler.getRate();
    if (rate == Long.MAX_VALUE)
    {
      return 0;
    };
    return rate;
  }
  
  public final void close() throws IOException
  {
    throttled.close();
    for (VTLinkableDynamicMultiplexedOutputStream stream : bufferedChannels.values())
    {
      try
      {
        stream.close();
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
      }
    }
    for (VTLinkableDynamicMultiplexedOutputStream stream : directChannels.values())
    {
      try
      {
        stream.close();
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
      }
    }
    bufferedChannels.clear();
    directChannels.clear();
  }
  
  public final void open(final int type, final int number) throws IOException
  {
    getOutputStream(type, number).open();
  }
  
  public final void close(final int type, final int number) throws IOException
  {
    getOutputStream(type, number).close();
  }
  
  public final class VTLinkableDynamicMultiplexedOutputStream extends OutputStream
  {
    private volatile boolean closed;
    private volatile Object link = null;
    private final int number;
    private final long seed;
    private volatile int type;
    private final int packetSize;
    private final byte[] single = new byte[1];
    private final VTByteArrayOutputStream intermediateDataPacketBuffer;
    private final VTByteArrayOutputStream dataPacketBuffer;
    private final VTLittleEndianOutputStream dataPacketStream;
    private final VTByteArrayOutputStream controlPacketBuffer;
    private final VTLittleEndianOutputStream controlPacketStream;
    private OutputStream output;
    private OutputStream control;
    private OutputStream intermediatePacketStream;
    private final Collection<Closeable> propagated;
    private final Random packetSequencer;
    
    private VTLinkableDynamicMultiplexedOutputStream(final OutputStream output, final OutputStream control, final int type, final int number, final int packetSize, final VTXXHash64MessageDigest packetSeed)
    {
      packetSeed.reset();
      packetSeed.update((byte)(number));
      packetSeed.update((byte)(number >> 8));
      packetSeed.update((byte)(number >> 16));
      packetSeed.update((byte)(number >> 24));
      this.seed = packetSeed.digestLong();
      this.packetSequencer = new VTSplitMix64Random(seed);
      this.output = output;
      this.control = control;
      this.type = type;
      this.number = number;
      this.packetSize = packetSize;
      this.intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
      this.dataPacketBuffer = new VTByteArrayOutputStream(VT.VT_PACKET_HEADER_SIZE_BYTES + packetSize);
      this.dataPacketStream = new VTLittleEndianOutputStream(dataPacketBuffer);
      this.controlPacketBuffer = new VTByteArrayOutputStream(VT.VT_PACKET_HEADER_SIZE_BYTES);
      this.controlPacketStream = new VTLittleEndianOutputStream(controlPacketBuffer);
      this.closed = false;
      this.propagated = new ConcurrentLinkedQueue<Closeable>();
      
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        intermediatePacketStream = intermediateDataPacketBuffer;
      }
      else
      {
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY) != 0)
        {
          intermediatePacketStream = VTCompressorSelector.createDirectZstdOutputStream(intermediateDataPacketBuffer);
        }
        else
        {
          intermediatePacketStream = VTCompressorSelector.createDirectLz4OutputStream(intermediateDataPacketBuffer);
        }
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
    
    public final void output(final OutputStream out)
    {
      this.output = out;
    }
    
    public final void control(final OutputStream control)
    {
      this.control = control;
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
    
    public final void write(final byte[] data, final int offset, final int length) throws IOException
    {
      int written = 0;
      int position = offset;
      int remaining = length;
      while (remaining > 0)
      {
        if (closed)
        {
          throw new IOException("OutputStream closed");
        }
        written = Math.min(remaining, packetSize);
        writeDataPacket(data, position, written, type, number);
        position += written;
        remaining -= written;
      }
    }
    
    public final void write(final byte[] data) throws IOException
    {
      write(data, 0, data.length);
    }
    
    public final void write(final int data) throws IOException
    {
      single[0] = (byte) data;
      write(single);
    }
    
    public final void flush() throws IOException
    {
      
    }
    
    public final void close() throws IOException
    {
      closed = true;
      writeClosePacket(type, number);
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
    
    public final void open() throws IOException
    {
      closed = false;
      writeOpenPacket(type, number);
      packetSequencer.setSeed(seed);
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
      {
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY) != 0)
        {
          intermediatePacketStream = VTCompressorSelector.createDirectZstdOutputStream(intermediateDataPacketBuffer);
        }
        else
        {
          intermediatePacketStream = VTCompressorSelector.createDirectLz4OutputStream(intermediateDataPacketBuffer);
        }
      }
      else
      {
        intermediatePacketStream = intermediateDataPacketBuffer;
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
    
    private synchronized final void writeDataPacket(final byte[] data, final int offset, final int length, final int type, final int number) throws IOException
    {
      dataPacketBuffer.reset();
      intermediateDataPacketBuffer.reset();
      intermediatePacketStream.write(data, offset, length);
      intermediatePacketStream.flush();
      dataPacketStream.writeLong(packetSequencer.nextLong());
      dataPacketStream.writeByte(type);
      dataPacketStream.writeSubInt(number);
      dataPacketStream.writeInt(intermediateDataPacketBuffer.count());
      dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
      output.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      output.flush();
      transferredBytes += dataPacketBuffer.count();
    }
    
    private synchronized final void writeClosePacket(final int type, final int number) throws IOException
    {
      controlPacketBuffer.reset();
      controlPacketStream.writeLong(packetSequencer.nextLong());
      controlPacketStream.writeByte(type);
      controlPacketStream.writeSubInt(number);
      controlPacketStream.writeInt(-2);
      control.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      control.flush();
      transferredBytes += VT.VT_PACKET_HEADER_SIZE_BYTES;
    }
    
    private synchronized final void writeOpenPacket(final int type, final int number) throws IOException
    {
      controlPacketBuffer.reset();
      controlPacketStream.writeLong(packetSequencer.nextLong());
      controlPacketStream.writeByte(type);
      controlPacketStream.writeSubInt(number);
      controlPacketStream.writeInt(-3);
      control.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      control.flush();
      transferredBytes += VT.VT_PACKET_HEADER_SIZE_BYTES;
    }
  }
}