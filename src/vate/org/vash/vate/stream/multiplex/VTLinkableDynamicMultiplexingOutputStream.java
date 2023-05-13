package org.vash.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vash.vate.VT;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;
import org.vash.vate.stream.limit.VTThrottlingOutputStream;

public final class VTLinkableDynamicMultiplexingOutputStream
{
  //private final boolean autoFlushPackets;
  private final int packetSize;
  private final int blockSize;
  private final OutputStream original;
  private final VTThrottlingOutputStream throttleable;
  private final Map<Integer, VTLinkableDynamicMultiplexedOutputStream> pipedChannels;
  private final Map<Integer, VTLinkableDynamicMultiplexedOutputStream> directChannels;
  
  public VTLinkableDynamicMultiplexingOutputStream(OutputStream out, int packetSize, int blockSize)
  {
    this.original = out;
    this.throttleable = new VTThrottlingOutputStream(out);
    this.pipedChannels = Collections.synchronizedMap(new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>());
    this.directChannels = Collections.synchronizedMap(new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>());
    this.packetSize = packetSize;
    this.blockSize = blockSize;
    //this.autoFlushPackets = autoFlushPackets;
  }
  
//  public synchronized final VTLinkableDynamicMultiplexedOutputStream linkOutputStream(int type, int number)
//  {
//    VTLinkableDynamicMultiplexedOutputStream stream = null;
//    stream = getOutputStream(type, number);
//    return stream;
//  }
  
  public synchronized final VTLinkableDynamicMultiplexedOutputStream linkOutputStream(int type, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    if (link instanceof Integer)
    {
      stream = getOutputStream(type, (Integer) link);
      if (stream.getLink() == null)
      {
        stream.setLink(link);
      }
      //stream.setLink(link);
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    for (int i = 0; i < Integer.MAX_VALUE - 1 && i >= 0; i++)
    {
      stream = getOutputStream(type, i);
      if (stream.getLink() == null)
      {
        stream.setLink(link);
        return stream;
      }
    }
    return stream;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedOutputStream linkOutputStream(int type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    stream = getOutputStream(type, number);
    if (stream.getLink() == null)
    {
      stream.setLink(link);
    }
    //stream.setLink(link);
    return stream;
  }
  
  public synchronized final void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
    //stream.setLink(null);
  }
  
  private synchronized final VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    OutputStream output = null;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED) == 0)
    {
      output = throttleable;
    }
    else
    {
      output = original;
    }
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT) == 0)
    {
      stream = pipedChannels.get(number);
      if (stream != null)
      {
        stream.type(type);
        stream.out(output);
        return stream;
      }
      stream = new VTLinkableDynamicMultiplexedOutputStream(output, type, number, packetSize, blockSize);
      pipedChannels.put(number, stream);
    }
    else
    {
      stream = directChannels.get(number);
      if (stream != null)
      {
        stream.type(type);
        stream.out(output);
        return stream;
      }
      stream = new VTLinkableDynamicMultiplexedOutputStream(output, type, number, packetSize, blockSize);
      directChannels.put(number, stream);
    }
    return stream;
  }
  
  public final int getPipedChannelsNumber()
  {
    return pipedChannels.size();
  }
  
  public final int getPacketSize()
  {
    return packetSize;
  }
  
  public final int getBlockSize()
  {
    return blockSize;
  }
  
  public final void setBytesPerSecond(long bytesPerSecond)
  {
    throttleable.setBytesPerSecond(bytesPerSecond);
  }
  
  public final long getBytesPerSecond()
  {
    // return 0;
    return throttleable.getBytesPerSecond();
  }
  
  public final void close() throws IOException
  {
    pipedChannels.clear();
    directChannels.clear();
    throttleable.close();
  }
  
  public final void open(short type, int number) throws IOException
  {
    getOutputStream(type, number).open();
  }
  
  public final void close(short type, int number) throws IOException
  {
    getOutputStream(type, number).close();
  }
  
//	private synchronized void writeBlocks(OutputStream out, byte[] data, int off, int length) throws IOException
//	{
//		int current = 0;
//		int written = 0;
//		int remaining = length;
//		while (remaining > 0)
//		{
//			current = Math.min(blockSize, remaining);
//			out.write(data, off + written, current);
//			written += current;
//			remaining -= current;
//		}
//	}
  
  public final class VTLinkableDynamicMultiplexedOutputStream extends OutputStream
  {
    private volatile boolean closed;
    private volatile Object link = null;
    private final int number;
    private int type;
    private final int packetSize;
    private final VTByteArrayOutputStream intermediateDataPacketBuffer;
    private final VTByteArrayOutputStream dataPacketBuffer;
    private final VTLittleEndianOutputStream dataPacketStream;
    private final VTByteArrayOutputStream controlPacketBuffer;
    private final VTLittleEndianOutputStream controlPacketStream;
    private OutputStream out;
    private OutputStream intermediatePacketStream;
    private List<Closeable> propagated;
    
    private VTLinkableDynamicMultiplexedOutputStream(OutputStream out, int type, int number, int packetSize, int blockSize)
    {
      this.out = out;
      this.type = type;
      this.number = number;
      this.packetSize = packetSize;
      // this.blockSize = blockSize;
      // this.blockBits = blockSize - 1;
      // this.autoFlushPackets = autoFlushPackets;
      // this.dataPaddingBuffer = new byte[blockSize];
      this.intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
      this.dataPacketBuffer = new VTByteArrayOutputStream(packetSize + VT.VT_PACKET_HEADER_SIZE_BYTES);
      this.dataPacketStream = new VTLittleEndianOutputStream(dataPacketBuffer);
      // this.controlPaddingBuffer = new byte[blockSize];
      this.controlPacketBuffer = new VTByteArrayOutputStream(VT.VT_PACKET_HEADER_SIZE_BYTES);
      this.controlPacketStream = new VTLittleEndianOutputStream(controlPacketBuffer);
      this.closed = false;
      // this.link = null;
      this.propagated = new ArrayList<Closeable>();
      
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        intermediatePacketStream = intermediateDataPacketBuffer;
      }
      else
      {
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
        {
          //intermediatePacketStream = VTCompressorSelector.createDirectZlibOutputStream(intermediateDataPacketBuffer);
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
    
    public final void type(int type)
    {
      this.type = type;
    }
    
    public final void out(OutputStream out)
    {
      this.out = out;
    }
    
    public final Object getLink()
    {
      return link;
    }
    
    public final void setLink(Object link)
    {
      this.link = link;
    }
    
    public final boolean closed()
    {
      return closed;
    }
    
    public final void write(byte[] data, int offset, int length) throws IOException
    {
      int written = 0;
      int position = offset;
      int remaining = length;
      while (remaining > 0)
      {
        written = Math.min(remaining, packetSize);
        if (closed)
        {
          throw new IOException("OutputStream closed");
        }
        writePacket(data, position, written, type, number);
        position += written;
        remaining -= written;
      }
    }
    
    public final void write(byte[] data) throws IOException
    {
      write(data, 0, data.length);
    }
    
    public final void write(int data) throws IOException
    {
      if (closed)
      {
        throw new IOException("OutputStream closed");
      }
      writePacket(data, type, number);
    }
    
    public final void flush() throws IOException
    {
      if (closed)
      {
        throw new IOException("OutputStream closed");
      }
      out.flush();
    }
    
    public final void close() throws IOException
    {
      //if (closed)
      //{
        //return;
      //}
      closed = true;
      writeClosePacket(type, number);
      if (propagated.size() > 0)
      {
        for (Closeable closeable : propagated.toArray(new Closeable[]{ }))
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
      //if (!closed)
      //{
        //return;
      //}
      closed = false;
      writeOpenPacket(type, number);
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
      {
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
        {
          //intermediatePacketStream = VTCompressorSelector.createDirectZlibOutputStream(intermediateDataPacketBuffer);
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
    
    public final void addPropagated(Closeable propagated)
    {
      this.propagated.add(propagated);
    }
    
    public final void removePropagated(Closeable propagated)
    {
      this.propagated.remove(propagated);
    }
    
    private final void writePacket(int data, int type, int number) throws IOException
    {
      // dataPaddingSize = (~(headerSize + 1) + 1) & (blockBits);
      dataPacketBuffer.reset();
      intermediateDataPacketBuffer.reset();
      dataPacketStream.writeUnsignedShort(type);
      dataPacketStream.writeInt(number);
      // dataPacketStream.writeUnsignedShort(dataPaddingSize);
      intermediatePacketStream.write(data);
      intermediatePacketStream.flush();
      dataPacketStream.writeShort(intermediateDataPacketBuffer.count());
      dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
      // dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
      out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      out.flush();
      // writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
    }
    
    private final void writePacket(byte[] data, int offset, int length, int type, int number) throws IOException
    {
      // dataPaddingSize = (~(headerSize + length) + 1) & (blockBits);
      dataPacketBuffer.reset();
      intermediateDataPacketBuffer.reset();
      dataPacketStream.writeUnsignedShort(type);
      dataPacketStream.writeInt(number);
      // dataPacketStream.writeUnsignedShort(dataPaddingSize);
      intermediatePacketStream.write(data, offset, length);
      intermediatePacketStream.flush();
      dataPacketStream.writeShort(intermediateDataPacketBuffer.count());
      dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
      // dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
      out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      out.flush();
      // writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
    }
    
    private final void writeClosePacket(int type, int number) throws IOException
    {
      // controlPaddingSize = (~(headerSize) + 1) & (blockBits);
      controlPacketBuffer.reset();
      controlPacketStream.writeUnsignedShort(type);
      controlPacketStream.writeInt(number);
      controlPacketStream.writeShort(-2);
      // controlPacketStream.writeUnsignedShort(controlPaddingSize);
      // controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
      out.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      out.flush();
      // writeBlocks(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      // writeBlocks(out, controlPacketBuffer.buf(), 0,
      // controlPacketBuffer.count());
      // flush.flush();
    }
    
    private final void writeOpenPacket(int type, int number) throws IOException
    {
      // controlPaddingSize = (~(headerSize) + 1) & (blockBits);
      controlPacketBuffer.reset();
      controlPacketStream.writeUnsignedShort(type);
      controlPacketStream.writeInt(number);
      controlPacketStream.writeShort(-3);
      // controlPacketStream.writeUnsignedShort(controlPaddingSize);
      // controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
      out.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      out.flush();
      // writeBlocks(out, controlPacketBuffer.buf(), 0,
      // controlPacketBuffer.count());
      // flush.flush();
    }
  }
}