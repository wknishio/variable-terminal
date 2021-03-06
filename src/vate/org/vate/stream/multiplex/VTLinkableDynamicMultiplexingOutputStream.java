package org.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.vate.VT;
import org.vate.stream.array.VTByteArrayOutputStream;
import org.vate.stream.compress.VTCompressorSelector;
import org.vate.stream.endian.VTLittleEndianOutputStream;
import org.vate.stream.filter.VTAutoFlushOutputStream;
import org.vate.stream.limit.VTThrottlingOutputStream;

public final class VTLinkableDynamicMultiplexingOutputStream
{
  public final class VTLinkableDynamicMultiplexedOutputStream extends OutputStream
  {
    private static final int headerSize = 8;
    private volatile boolean closed;
    private volatile Object link;
    //private final boolean autoFlushPackets;
    private final int number;
    private final int type;
    private final int packetSize;
    // private final int blockSize;
    // private int blockBits;
    private final OutputStream out;
    private final OutputStream flush;
    private OutputStream intermediatePacketStream;
    private VTByteArrayOutputStream intermediateDataPacketBuffer;

    // private int dataPaddingSize;
    // private byte[] dataPaddingBuffer;
    private final VTByteArrayOutputStream dataPacketBuffer;
    private final VTLittleEndianOutputStream dataPacketStream;
    // private int controlPaddingSize;
    // private byte[] controlPaddingBuffer;
    private final VTByteArrayOutputStream controlPacketBuffer;
    private final VTLittleEndianOutputStream controlPacketStream;
    private List<Closeable> propagated;

    private VTLinkableDynamicMultiplexedOutputStream(OutputStream out, int type, int number, int packetSize, int blockSize, boolean autoFlushPackets)
    {
      this.flush = new VTAutoFlushOutputStream(out);
      if (autoFlushPackets)
      {
        this.out = flush;
      }
      else
      {
        this.out = out;
      }
      this.type = type;
      this.number = number;
      this.packetSize = packetSize;
      // this.blockSize = blockSize;
      // this.blockBits = blockSize - 1;
      //this.autoFlushPackets = autoFlushPackets;
      // this.dataPaddingBuffer = new byte[blockSize];
      this.dataPacketBuffer = new VTByteArrayOutputStream(packetSize + headerSize);
      this.dataPacketStream = new VTLittleEndianOutputStream(dataPacketBuffer);
      // this.controlPaddingBuffer = new byte[blockSize];
      this.controlPacketBuffer = new VTByteArrayOutputStream(headerSize);
      this.controlPacketStream = new VTLittleEndianOutputStream(controlPacketBuffer);
      this.closed = false;
      this.link = null;
      this.propagated = new ArrayList<Closeable>();

      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE);
        intermediatePacketStream = intermediateDataPacketBuffer;
      }
      else
      {
        intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE);
        intermediatePacketStream = VTCompressorSelector.createBufferedLZ4OutputStream(intermediateDataPacketBuffer);
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

    public final synchronized void close() throws IOException
    {
      if (!closed)
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
    }

    public final synchronized void open() throws IOException
    {
      if (closed)
      {
        writeOpenPacket(type, number);
      }
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
      {
        intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE);
        intermediatePacketStream = VTCompressorSelector.createBufferedLZ4OutputStream(intermediateDataPacketBuffer);
      }
      closed = false;
    }

    public final void addPropagated(Closeable propagated)
    {
      // this.propagated = propagated;
      this.propagated.add(propagated);
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
      dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
      dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
      // dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
      out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
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
      dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
      dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
      // dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
      out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      // writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
    }

    private final void writeClosePacket(int type, int number) throws IOException
    {
      // controlPaddingSize = (~(headerSize) + 1) & (blockBits);
      controlPacketBuffer.reset();
      controlPacketStream.writeUnsignedShort(type);
      controlPacketStream.writeInt(number);
      controlPacketStream.writeShort((short) -2);
      // controlPacketStream.writeUnsignedShort(controlPaddingSize);
      // controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
      flush.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      // writeBlocks(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
      // writeBlocks(out, controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      //flush.flush();
    }

    private final void writeOpenPacket(int type, int number) throws IOException
    {
      // controlPaddingSize = (~(headerSize) + 1) & (blockBits);
      controlPacketBuffer.reset();
      controlPacketStream.writeUnsignedShort(type);
      controlPacketStream.writeInt(number);
      controlPacketStream.writeShort((short) -3);
      // controlPacketStream.writeUnsignedShort(controlPaddingSize);
      // controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
      flush.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      // writeBlocks(out, controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
      //flush.flush();
    }
  }

  private final boolean autoFlushPackets;
  private final int packetSize;
  private final int blockSize;
  private final OutputStream original;
  private final VTThrottlingOutputStream throttleable;
  private final HashMap<Integer, VTLinkableDynamicMultiplexedOutputStream> pipedChannels;
  private final HashMap<Integer, VTLinkableDynamicMultiplexedOutputStream> directChannels;

  public VTLinkableDynamicMultiplexingOutputStream(OutputStream out, int packetSize, int blockSize, boolean autoFlushPackets)
  {
    this.original = out;
    this.throttleable = new VTThrottlingOutputStream(out);
    this.pipedChannels = new HashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>();
    this.directChannels = new HashMap<Integer, VTLinkableDynamicMultiplexedOutputStream>();
    this.packetSize = packetSize;
    this.blockSize = blockSize;
    this.autoFlushPackets = autoFlushPackets;
  }

  public final synchronized VTLinkableDynamicMultiplexedOutputStream linkOutputStream(int type, Object link)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    if (link instanceof Integer)
    {
      stream = getOutputStream(type, (Integer) link);
      if (stream.getLink() != null)
      {
        return null;
      }
      stream.setLink(link);
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    for (int i = 0; i < 1048576; i++)
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

  public final synchronized void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
  }

  public final VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number)
  {
    VTLinkableDynamicMultiplexedOutputStream stream = null;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT) == 0)
    {
      stream = pipedChannels.get(number);
      if (stream != null)
      {
        return stream;
      }
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED) == 0)
      {
        stream = new VTLinkableDynamicMultiplexedOutputStream(throttleable, type, number, packetSize, blockSize, autoFlushPackets);
      }
      else
      {
        stream = new VTLinkableDynamicMultiplexedOutputStream(original, type, number, packetSize, blockSize, autoFlushPackets);
      }
      pipedChannels.put(number, stream);
    }
    else
    {
      stream = directChannels.get(number);
      if (stream != null)
      {
        return stream;
      }
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED) == 0)
      {
        stream = new VTLinkableDynamicMultiplexedOutputStream(throttleable, type, number, packetSize, blockSize, autoFlushPackets);
      }
      else
      {
        stream = new VTLinkableDynamicMultiplexedOutputStream(original, type, number, packetSize, blockSize, autoFlushPackets);
      }
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

  public final boolean isAutoFlushPackets()
  {
    return autoFlushPackets;
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
}