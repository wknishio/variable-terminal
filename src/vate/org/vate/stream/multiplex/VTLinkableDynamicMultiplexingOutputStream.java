package org.vate.stream.multiplex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.vate.VT;
import org.vate.stream.array.VTByteArrayOutputStream;
import org.vate.stream.compress.VTCompressorSelector;
import org.vate.stream.endian.VTLittleEndianOutputStream;
import org.vate.stream.limit.VTThrottlingOutputStream;

public class VTLinkableDynamicMultiplexingOutputStream
{
	public class VTLinkableDynamicMultiplexedOutputStream extends OutputStream
	{
		private static final int headerSize = 8;
		private volatile boolean closed;
		private volatile Object link;
		private final boolean autoFlushPackets;
		private final int number;
		private final short type;
		private final int packetSize;
		//private final int blockSize;
		//private int blockBits;
		private final OutputStream out;
		private OutputStream intermediatePacketStream;
		private VTByteArrayOutputStream intermediateDataPacketBuffer;

		//private int dataPaddingSize;
		//private byte[] dataPaddingBuffer;
		private final VTByteArrayOutputStream dataPacketBuffer;
		private final VTLittleEndianOutputStream dataPacketStream;
		//private int controlPaddingSize;
		//private byte[] controlPaddingBuffer;
		private final VTByteArrayOutputStream controlPacketBuffer;
		private final VTLittleEndianOutputStream controlPacketStream;
		
		private VTLinkableDynamicMultiplexedOutputStream(OutputStream out, short type, int number, int packetSize, int blockSize, boolean autoFlushPackets)
		{
			this.out = out;
			this.type = type;
			this.number = number;
			this.packetSize = packetSize;
			//this.blockSize = blockSize;
			//this.blockBits = blockSize - 1;
			this.autoFlushPackets = autoFlushPackets;
			//this.dataPaddingBuffer = new byte[blockSize];
			this.dataPacketBuffer = new VTByteArrayOutputStream(packetSize + headerSize);
			this.dataPacketStream = new VTLittleEndianOutputStream(dataPacketBuffer);
			//this.controlPaddingBuffer = new byte[blockSize];
			this.controlPacketBuffer = new VTByteArrayOutputStream(headerSize);
			this.controlPacketStream = new VTLittleEndianOutputStream(controlPacketBuffer);
			this.closed = false;
			this.link = null;
			
			if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) == 0)
			{
				intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_DATA_BUFFFER_SIZE);
				intermediatePacketStream = intermediateDataPacketBuffer;
			}
			else
			{
				intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_DATA_BUFFFER_SIZE);
				intermediatePacketStream = VTCompressorSelector.createCompatibleLZ4OutputStream(intermediateDataPacketBuffer);
			}
		}
		
		public int number()
		{
			return number;
		}
		
		public int type()
		{
			return type;
		}
		
		public Object getLink()
		{
			return link;
		}
		
		public void setLink(Object link)
		{
			this.link = link;
		}
		
		public boolean closed()
		{
			return closed;
		}
		
		public void closed(boolean closed)
		{
			this.closed = closed;
		}
		
		public void write(byte[] data, int offset, int length) throws IOException
		{
			int written = 0;
			int position = offset;
			int remaining = length;
			if (autoFlushPackets)
			{
				while (remaining > 0)
				{
					written = Math.min(remaining, packetSize);
					if (closed)
					{
						throw new IOException("OutputStream closed");
					}
					writePacketFlushing(data, position, written, type, number);
					position += written;
					remaining -= written;
				}
			}
			else
			{
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
		}
		
		public void write(byte[] data) throws IOException
		{
			write(data, 0, data.length);
		}
		
		public void write(int data) throws IOException
		{
			if (autoFlushPackets)
			{
				if (closed)
				{
					throw new IOException("OutputStream closed");
				}
				writePacketFlushing(data, type, number);
			}
			else
			{
				if (closed)
				{
					throw new IOException("OutputStream closed");
				}
				writePacket(data, type, number);
			}
		}
		
		public void flush() throws IOException
		{
			if (!autoFlushPackets)
			{
				if (closed)
				{
					throw new IOException("OutputStream closed");
				}
				out.flush();
			}
		}
		
		public void close() throws IOException
		{
			if (!closed)
			{
				writeClosePacketFlushing(type, number);
			}
			closed = true;
		}
		
		public void open() throws IOException
		{
			if (closed)
			{
				writeOpenPacketFlushing(type, number);
			}
			if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
			{
				intermediateDataPacketBuffer = new VTByteArrayOutputStream(VT.VT_DATA_BUFFFER_SIZE);
				intermediatePacketStream = VTCompressorSelector.createCompatibleLZ4OutputStream(intermediateDataPacketBuffer);
			}
			closed = false;
		}
		
		private void writePacket(int data, short type, int number) throws IOException
		{
			//dataPaddingSize = (~(headerSize + 1) + 1) & (blockBits);
			dataPacketBuffer.reset();
			intermediateDataPacketBuffer.reset();
			dataPacketStream.writeShort(type);
			dataPacketStream.writeInt(number);
			//dataPacketStream.writeUnsignedShort(dataPaddingSize);
			intermediatePacketStream.write(data);
			intermediatePacketStream.flush();
			dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
			dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
			//dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
			out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			//writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
		}
		
		private void writePacket(byte[] data, int offset, int length, short type, int number) throws IOException
		{
			//dataPaddingSize = (~(headerSize + length) + 1) & (blockBits);
			dataPacketBuffer.reset();
			intermediateDataPacketBuffer.reset();
			dataPacketStream.writeShort(type);
			dataPacketStream.writeInt(number);
			//dataPacketStream.writeUnsignedShort(dataPaddingSize);
			intermediatePacketStream.write(data, offset, length);
			intermediatePacketStream.flush();
			dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
			dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
			//dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
			out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			//writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
//			if ((type & VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED) != 0)
//			{
//				System.out.println("sent uncompressed data:" + Arrays.toString(Arrays.copyOfRange(data, offset, length)));
//				System.out.println("sent compressed data:" + Arrays.toString(intermediateDataPacketBuffer.toByteArray()));
//			}
		}
		
		private void writePacketFlushing(int data, short type, int number) throws IOException
		{
			//dataPaddingSize = (~(headerSize + 1) + 1) & (blockBits);
			dataPacketBuffer.reset();
			intermediateDataPacketBuffer.reset();
			dataPacketStream.writeShort(type);
			dataPacketStream.writeInt(number);
			//dataPacketStream.writeUnsignedShort(dataPaddingSize);
			intermediatePacketStream.write(data);
			intermediatePacketStream.flush();
			dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
			dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
			//dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
			out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			//writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			out.flush();
		}
		
		private void writePacketFlushing(byte[] data, int offset, int length, short type, int number) throws IOException
		{
			//dataPaddingSize = (~(headerSize + length) + 1) & (blockBits);
			dataPacketBuffer.reset();
			intermediateDataPacketBuffer.reset();
			dataPacketStream.writeShort(type);
			dataPacketStream.writeInt(number);
			//dataPacketStream.writeUnsignedShort(dataPaddingSize);
			intermediatePacketStream.write(data, offset, length);
			intermediatePacketStream.flush();
			dataPacketStream.writeShort((short) intermediateDataPacketBuffer.count());
			dataPacketStream.write(intermediateDataPacketBuffer.buf(), 0, intermediateDataPacketBuffer.count());
			//dataPacketStream.write(dataPaddingBuffer, 0, dataPaddingSize);
			out.write(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			//writeBlocks(out, dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			out.flush();
		}
		
		private void writeClosePacketFlushing(short type, int number) throws IOException
		{
			//controlPaddingSize = (~(headerSize) + 1) & (blockBits);
			controlPacketBuffer.reset();
			controlPacketStream.writeShort(type);
			controlPacketStream.writeInt(number);
			controlPacketStream.writeShort((short) -2);
			//controlPacketStream.writeUnsignedShort(controlPaddingSize);
			//controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
			out.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
			//writeBlocks(dataPacketBuffer.buf(), 0, dataPacketBuffer.count());
			//writeBlocks(out, controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
			out.flush();
		}
		
		private void writeOpenPacketFlushing(short type, int number) throws IOException
		{
			//controlPaddingSize = (~(headerSize) + 1) & (blockBits);
			controlPacketBuffer.reset();
			controlPacketStream.writeShort(type);
			controlPacketStream.writeInt(number);
			controlPacketStream.writeShort((short) -3);
			//controlPacketStream.writeUnsignedShort(controlPaddingSize);
			//controlPacketStream.write(controlPaddingBuffer, 0, controlPaddingSize);
			out.write(controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
			//writeBlocks(out, controlPacketBuffer.buf(), 0, controlPacketBuffer.count());
			out.flush();
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
	
	public synchronized VTLinkableDynamicMultiplexedOutputStream linkOutputStream(short type, Object link)
	{
		VTLinkableDynamicMultiplexedOutputStream stream = null;
		if (link instanceof Integer)
		{
			stream = getOutputStream(type, (Integer)link);
			if (stream.getLink() != null)
			{
				return null;
			}
			stream.setLink(link);
			return stream;
		}
		//search for a multiplexed outputstream that has no link
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
	
	public synchronized void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
	{
		if (stream != null)
		{
			stream.setLink(null);
		}
	}
	
	public VTLinkableDynamicMultiplexedOutputStream getOutputStream(short type, int number)
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
	
	public int getPipedChannelsNumber()
	{
		return pipedChannels.size();
	}
	
	public int getPacketSize()
	{
		return packetSize;
	}
	
	public int getBlockSize()
	{
		return blockSize;
	}
	
	public boolean isAutoFlushPackets()
	{
		return autoFlushPackets;
	}
	
	public void setBytesPerSecond(long bytesPerSecond)
	{
		throttleable.setBytesPerSecond(bytesPerSecond);
	}
	
	public long getBytesPerSecond()
	{
		// return 0;
		return throttleable.getBytesPerSecond();
	}
	
	public void close() throws IOException
	{
		pipedChannels.clear();
		directChannels.clear();
		throttleable.close();
	}
	
	public void open(short type, int number) throws IOException
	{
		getOutputStream(type, number).open();
	}
	
	public void close(short type, int number) throws IOException
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