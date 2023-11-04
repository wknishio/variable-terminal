package org.vash.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vash.vate.VT;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.compress.VTPacketDecompressor;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public final class VTLinkableDynamicMultiplexingInputStream
{
  private int type;
  private int channel;
  private final int bufferSize;
  // private int padding;
  private int length;
  private int copied;
  private int readed;
  private int remaining;
  private final byte[] packetBuffer;
  //private OutputStream out;
  private final Thread packetReaderThread;
  // private byte[] compressedBuffer = new byte[VT.VT_IO_BUFFFER_SIZE];
  private final VTLittleEndianInputStream in;
  private final VTLinkableDynamicMultiplexingInputStreamPacketReader packetReader;
  private final Map<Integer, VTLinkableDynamicMultiplexedInputStream> bufferedChannels;
  private final Map<Integer, VTLinkableDynamicMultiplexedInputStream> directChannels;
  private volatile boolean closed = false;
  
  public VTLinkableDynamicMultiplexingInputStream(InputStream in, int packetSize, int bufferSize, boolean startPacketReader)
  {
    this.bufferSize = bufferSize;
    this.packetBuffer = new byte[packetSize * 2];
    this.in = new VTLittleEndianInputStream(in);
//    this.bufferedChannels = Collections.synchronizedMap(new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>());
//    this.directChannels = Collections.synchronizedMap(new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>());
    this.bufferedChannels = new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>();
    this.directChannels = new LinkedHashMap<Integer, VTLinkableDynamicMultiplexedInputStream>();
    this.packetReader = new VTLinkableDynamicMultiplexingInputStreamPacketReader(this);
    this.packetReaderThread = new Thread(null, packetReader, packetReader.getClass().getSimpleName());
    this.packetReaderThread.setDaemon(true);
    //this.packetReaderThread.setPriority((Thread.NORM_PRIORITY));
    if (startPacketReader)
    {
      this.packetReaderThread.start();
    }
  }
  
//  public synchronized final VTLinkableDynamicMultiplexedInputStream linkInputStream(int type, int number)
//  {
//    VTLinkableDynamicMultiplexedInputStream stream = null;
//    stream = getInputStream(type, number);
//    return stream;
//  }
  
  public synchronized final VTLinkableDynamicMultiplexedInputStream linkInputStream(int type, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if (link instanceof Integer)
    {
      stream = getInputStream(type, (Integer) link);
      if (stream.getLink() == null)
      {
        stream.setLink(link);
      }
      //stream.setLink(link);
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    for (int i = 0; i < 16777215 && i >= 0; i++)
    {
      stream = getInputStream(type, i);
      if (stream.getLink() == null)
      {
        stream.setLink(link);
        return stream;
      }
    }
    return stream;
  }
  
  public synchronized final VTLinkableDynamicMultiplexedInputStream linkInputStream(int type, int number, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    stream = getInputStream(type, number);
    if (stream.getLink() == null)
    {
      stream.setLink(link);
    }
    //stream.setLink(link);
    return stream;
  }
  
  public synchronized final void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
    //stream.setLink(null);
  }
  
  private synchronized final VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT) == VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED)
    {
      stream = bufferedChannels.get(number);
      if (stream != null)
      {
        stream.type(type);
        return stream;
      }
      stream = new VTLinkableDynamicMultiplexedInputStream(type, number, bufferSize);
      bufferedChannels.put(number, stream);
    }
    else
    {
      stream = directChannels.get(number);
      if (stream != null)
      {
        stream.type(type);
        return stream;
      }
      stream = new VTLinkableDynamicMultiplexedInputStream(type, number, bufferSize);
      directChannels.put(number, stream);
    }
    return stream;
  }
  
  public final void startPacketReader()
  {
    if (!packetReaderThread.isAlive())
    {
      packetReaderThread.start();
    }
  }
  
  public final boolean isPacketReaderStarted()
  {
    if (packetReaderThread != null)
    {
      return packetReaderThread.isAlive();
    }
    return false;
  }
  
  public final void stopPacketReader() throws IOException, InterruptedException
  {
    close();
    packetReaderThread.join();
  }
  
  public final void open(int type, int number) throws IOException
  {
    getInputStream(type, number).open();
  }
  
  public final void close(int type, int number) throws IOException
  {
    getInputStream(type, number).close();
  }
  
  public final void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    //packetReader.setRunning(false);
    //synchronized (pipedChannels)
    //{
      for (VTLinkableDynamicMultiplexedInputStream stream : bufferedChannels.values().toArray(new VTLinkableDynamicMultiplexedInputStream []{ }))
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
    //}
    //synchronized (directChannels)
    //{
      for (VTLinkableDynamicMultiplexedInputStream stream : directChannels.values().toArray(new VTLinkableDynamicMultiplexedInputStream []{ }))
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
    //}
    bufferedChannels.clear();
    directChannels.clear();
    in.close();
    closed = true;
  }
  
  // critical method, handle with care
  private final void readPackets() throws IOException
  {
    while (!closed)
    {
      readed = 0;
      copied = 0;
      type = in.readByte();
      channel = in.readSubInt();
      length = in.readInt();
      if (length > 0)
      {
        remaining = length;
        while (remaining > 0)
        {
          copied += readed;
          remaining -= readed;
          readed = in.read(packetBuffer, copied, remaining);
          if (readed < 0)
          {
            close();
            return;
          }
        }
        try
        {
          OutputStream out = getInputStream(type, channel).getOutputStream();
          if (out != null)
          {
            out.write(packetBuffer, 0, length);
            out.flush();
          }
        }
        catch (Throwable e)
        {
          //e.printStackTrace();
        }
      }
      else if (length == -2)
      {
        close(type, channel);
      }
      else if (length == -3)
      {
        open(type, channel);
      }
    }
  }
  
  public final class VTLinkableDynamicMultiplexedInputStream extends InputStream
  {
    private volatile boolean closed;
    private volatile Object link = null;
    private final int number;
    private int type;
    private final VTPipedInputStream bufferedInputStream;
    private final VTPipedOutputStream bufferedOutputStream;
    private InputStream in;
    private OutputStream directOutputStream;
    private Closeable directCloseable;
    private InputStream compressedInputStream;
    private List<Closeable> propagated;
    
    private VTLinkableDynamicMultiplexedInputStream(int type, int number, int bufferSize)
    {
      // this.multiplexingInputStream = multiplexingInputStream;
      this.type = type;
      this.number = number;
      this.propagated = new ArrayList<Closeable>();
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
          this.in = bufferedInputStream;
        }
        else
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
          {
            //this.compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedInputStream);
            this.compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(bufferedInputStream);
          }
          else
          {
            this.compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(bufferedInputStream);
          }
          this.in = compressedInputStream;
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
    
    public final void type(int type)
    {
      this.type = type;
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
      //return pipedInputStream == null || pipedInputStream.isClosed() || pipedInputStream.isEof();
    }
    
    private final OutputStream getOutputStream()
    {
      if (bufferedOutputStream != null)
      {
        return bufferedOutputStream;
      }
      return directOutputStream;
    }
    
    public final void setOutputStream(OutputStream outputStream, Closeable closeable)
    {
      this.directCloseable = closeable;
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        this.directOutputStream = outputStream;
      }
      else
      {
        VTPacketDecompressor packetDecompressor = new VTPacketDecompressor(outputStream);
        this.directOutputStream = packetDecompressor;
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
        {
          //compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedDecompressor.getPipedInputStream());
          compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(packetDecompressor.getCompressedPacketInputStream());
        }
        else
        {
          compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(packetDecompressor.getCompressedPacketInputStream());
        }
        packetDecompressor.setPacketDecompressorInputStream(compressedInputStream);
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
    
    public final void open() throws IOException
    {
      //if (!closed)
      //{
        //return;
      //}
      closed = false;
      if (bufferedInputStream != null)
      {
        bufferedInputStream.open();
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
          {
            //compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedInputStream);
            compressedInputStream = VTCompressorSelector.createDirectZstdInputStream(bufferedInputStream);
          }
          else
          {
            compressedInputStream = VTCompressorSelector.createDirectLz4InputStream(bufferedInputStream);
          }
          in = compressedInputStream;
        }
      }
      else
      {
        //work already done by setDirectOutputStream
      }
    }
    
    public final void close() throws IOException
    {
      //if (closed)
      //{
        //return;
      //}
      closed = true;
      compressedInputStream = null;
      if (bufferedOutputStream != null)
      {
        bufferedOutputStream.close();
      }
      else
      {
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
      }
      if (propagated.size() > 0)
      {
        // propagated.close();
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
      //propagated.clear();
      
    }
    
    public final int available() throws IOException
    {
      return in.available();
    }
    
    public final int read() throws IOException
    {
      return in.read();
    }
    
    public final int read(byte[] data) throws IOException
    {
      return in.read(data);
    }
    
    public final int read(byte[] data, int offset, int length) throws IOException
    {
      return in.read(data, offset, length);
    }
    
    public final long skip(long count) throws IOException
    {
      return in.skip(count);
    }
  }
  
  private final class VTLinkableDynamicMultiplexingInputStreamPacketReader implements Runnable
  {
    //private volatile boolean running;
    private final VTLinkableDynamicMultiplexingInputStream multiplexingInputStream;
    
    private VTLinkableDynamicMultiplexingInputStreamPacketReader(VTLinkableDynamicMultiplexingInputStream multiplexingInputStream)
    {
      this.multiplexingInputStream = multiplexingInputStream;
      //this.running = true;
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
        //running = false;
      }
      try
      {
        multiplexingInputStream.close();
      }
      catch (Throwable e1)
      {
        // e1.printStackTrace();
      }
    }
  }
}