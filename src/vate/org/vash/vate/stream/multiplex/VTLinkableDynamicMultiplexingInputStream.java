package org.vash.vate.stream.multiplex;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vash.vate.VT;
import org.vash.vate.stream.compress.VTCompressorSelector;
import org.vash.vate.stream.compress.VTPipedDecompressor;
import org.vash.vate.stream.endian.VTLittleEndianInputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public final class VTLinkableDynamicMultiplexingInputStream
{
  public final class VTLinkableDynamicMultiplexedInputStream extends InputStream
  {
    // private VTLinkableDynamicMultiplexingInputStream multiplexingInputStream;
    private final VTPipedInputStream pipedInputStream;
    private final VTPipedOutputStream pipedOutputStream;
    private OutputStream directOutputStream;
    private InputStream compressedDirectInputStream;
    // private OutputStream uncompressedDirectOutputStream;
    private InputStream in;
    // private short type;
    private volatile Object link = null;
    private List<Closeable> propagated;
    private VTPipedDecompressor pipedDecompressor;
    private final int type;
    private final int number;
    
    private VTLinkableDynamicMultiplexedInputStream(int type, int number, int bufferSize)
    {
      // this.multiplexingInputStream = multiplexingInputStream;
      this.type = type;
      this.number = number;
      this.propagated = new ArrayList<Closeable>();
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT) == 0)
      {
        this.pipedInputStream = new VTPipedInputStream(bufferSize);
        this.pipedOutputStream = new VTPipedOutputStream();
        try
        {
          this.pipedInputStream.connect(this.pipedOutputStream);
        }
        catch (IOException e)
        {
          
        }
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
        {
          this.in = pipedInputStream;
        }
        else
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
          {
            //this.compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedInputStream);
            this.compressedDirectInputStream = VTCompressorSelector.createDirectZstdInputStream(pipedInputStream);
          }
          else
          {
            this.compressedDirectInputStream = VTCompressorSelector.createDirectLz4InputStream(pipedInputStream);
          }
          this.in = compressedDirectInputStream;
        }
      }
      else
      {
        this.pipedInputStream = null;
        this.pipedOutputStream = null;
      }
    }
    
    public final int number()
    {
      return number;
    }
    
    public final synchronized Object getLink()
    {
      return link;
    }
    
    public final synchronized void setLink(Object link)
    {
      this.link = link;
    }
    
    public final boolean closed()
    {
      return pipedInputStream == null || pipedInputStream.isClosed() || pipedInputStream.isEof();
    }
    
    private final OutputStream getOutputStream()
    {
      if (pipedOutputStream != null)
      {
        return pipedOutputStream;
      }
      if (pipedDecompressor != null)
      {
        return pipedDecompressor;
      }
      return directOutputStream;
    }
    
    public final void setDirectOutputStream(OutputStream directOutputStream)
    {
      if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
      {
        this.directOutputStream = directOutputStream;
      }
      else
      {
        this.directOutputStream = directOutputStream;
        pipedDecompressor = new VTPipedDecompressor(directOutputStream);
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
        {
          //compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedDecompressor.getPipedInputStream());
          compressedDirectInputStream = VTCompressorSelector.createDirectZstdInputStream(pipedDecompressor.getPipedInputStream());
        }
        else
        {
          compressedDirectInputStream = VTCompressorSelector.createDirectLz4InputStream(pipedDecompressor.getPipedInputStream());
        }
        pipedDecompressor.setDecompressor(compressedDirectInputStream);
      }
    }
    
    public final void addPropagated(Closeable propagated)
    {
      // this.propagated = propagated;
      this.propagated.add(propagated);
    }
    
    public final void removePropagated(Closeable propagated)
    {
      this.propagated.remove(propagated);
    }
    
    public final void open() throws IOException
    {
      if (pipedInputStream != null)
      {
        pipedInputStream.open();
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
        {
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
          {
            //compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedInputStream);
            compressedDirectInputStream = VTCompressorSelector.createDirectZstdInputStream(pipedInputStream);
          }
          else
          {
            compressedDirectInputStream = VTCompressorSelector.createDirectLz4InputStream(pipedInputStream);
          }
          in = compressedDirectInputStream;
        }
      }
      else
      {
        if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
        {
          pipedDecompressor = new VTPipedDecompressor(directOutputStream);
          if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD) != 0)
          {
            //compressedDirectInputStream = VTCompressorSelector.createDirectZlibInputStream(pipedDecompressor.getPipedInputStream());
            compressedDirectInputStream = VTCompressorSelector.createDirectZstdInputStream(pipedDecompressor.getPipedInputStream());
          }
          else
          {
            compressedDirectInputStream = VTCompressorSelector.createDirectLz4InputStream(pipedDecompressor.getPipedInputStream());
          }
          pipedDecompressor.setDecompressor(compressedDirectInputStream);
        }
      }
    }
    
    public final void close() throws IOException
    {
      if (closed())
      {
        return;
      }
      if (pipedOutputStream != null)
      {
        pipedOutputStream.close();
      }
      else
      {
        if (directOutputStream != null)
        {
          directOutputStream.close();
          directOutputStream = null;
        }
      }
      if (propagated.size() > 0)
      {
        // propagated.close();
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
    private volatile boolean running;
    private final VTLinkableDynamicMultiplexingInputStream multiplexingInputStream;
    
    private VTLinkableDynamicMultiplexingInputStreamPacketReader(VTLinkableDynamicMultiplexingInputStream multiplexingInputStream)
    {
      this.multiplexingInputStream = multiplexingInputStream;
      this.running = true;
    }
    
    private final void setRunning(boolean running)
    {
      this.running = running;
    }
    
    public final void run()
    {
      while (running)
      {
        try
        {
          multiplexingInputStream.readPacket();
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
          running = false;
        }
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
  
  private int type;
  private int channel;
  private final int bufferSize;
  // private int padding;
  private int length;
  private int copied;
  private int readed;
  private int remaining;
  private byte[] packetBuffer;
  private final Thread packetReaderThread;
  // private byte[] compressedBuffer = new byte[VT.VT_IO_BUFFFER_SIZE];
  private final VTLittleEndianInputStream in;
  private VTLinkableDynamicMultiplexingInputStreamPacketReader packetReader;
  private Map<Integer, VTLinkableDynamicMultiplexedInputStream> pipedChannels;
  private Map<Integer, VTLinkableDynamicMultiplexedInputStream> directChannels;
  
  public VTLinkableDynamicMultiplexingInputStream(InputStream in, int packetSize, int bufferSize, boolean startPacketReader)
  {
    this.bufferSize = bufferSize;
    this.packetBuffer = new byte[packetSize * 2];
    this.in = new VTLittleEndianInputStream(in);
    this.pipedChannels = Collections.synchronizedMap(new HashMap<Integer, VTLinkableDynamicMultiplexedInputStream>());
    this.directChannels = Collections.synchronizedMap(new HashMap<Integer, VTLinkableDynamicMultiplexedInputStream>());
    this.packetReader = new VTLinkableDynamicMultiplexingInputStreamPacketReader(this);
    this.packetReaderThread = new Thread(null, packetReader, packetReader.getClass().getSimpleName());
    this.packetReaderThread.setDaemon(true);
    //this.packetReaderThread.setPriority((Thread.NORM_PRIORITY));
    if (startPacketReader)
    {
      this.packetReaderThread.start();
    }
  }
  
  public final synchronized VTLinkableDynamicMultiplexedInputStream linkInputStream(int type, Object link)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if (link instanceof Integer)
    {
      stream = getInputStream(type, (Integer) link);
      if (stream.getLink() == null)
      {
        stream.setLink(link);
      }
      return stream;
    }
    // search for a multiplexed outputstream that has no link
    for (int i = 0; i < Integer.MAX_VALUE && i >= 0; i++)
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
  
  public final synchronized void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
  {
    if (stream != null)
    {
      stream.setLink(null);
    }
  }
  
  private final VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number)
  {
    VTLinkableDynamicMultiplexedInputStream stream = null;
    if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT) == 0)
    {
      stream = pipedChannels.get(number);
      if (stream != null)
      {
        return stream;
      }
      stream = new VTLinkableDynamicMultiplexedInputStream(type, number, bufferSize);
      pipedChannels.put(number, stream);
    }
    else
    {
      stream = directChannels.get(number);
      if (stream != null)
      {
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
  
  public final int getPipedChannelsNumber()
  {
    return pipedChannels.size();
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
    if (!packetReader.running)
    {
      return;
    }
    packetReader.setRunning(false);
    synchronized (pipedChannels)
    {
      for (VTLinkableDynamicMultiplexedInputStream stream : pipedChannels.values())
      {
        try
        {
          stream.close();
          if (stream.pipedInputStream != null)
          {
            stream.pipedOutputStream.close();
          }
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    }
    synchronized (directChannels)
    {
      for (VTLinkableDynamicMultiplexedInputStream stream : directChannels.values())
      {
        try
        {
          stream.close();
          if (stream.pipedInputStream != null)
          {
            stream.pipedOutputStream.close();
          }
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    }
    pipedChannels.clear();
    directChannels.clear();
    in.close();
  }
  
  // critical method, handle with care
  private final void readPacket() throws IOException
  {
    readed = 0;
    copied = 0;
    type = in.readUnsignedShort();
    channel = in.readInt();
    length = in.readShort();
    if (length > 0)
    {
      remaining = length;
      while (remaining > 0)
      {
        copied += readed;
        readed = in.read(packetBuffer, copied, remaining);
        remaining -= readed;
        if (readed >= 0)
        {
          try
          {
            getInputStream(type, channel).getOutputStream().write(packetBuffer, copied, readed);
            getInputStream(type, channel).getOutputStream().flush();
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
            while (remaining > 0 && readed >= 0)
            {
              readed = in.read(packetBuffer, 0, remaining);
              remaining -= readed;
            }
            break;
          }
        }
        else
        {
          close();
          return;
        }
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