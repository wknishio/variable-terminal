package org.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;

import org.vate.stream.array.VTByteArrayInputStream;
import org.vate.stream.endian.VTLittleEndianInputStream;

import io.airlift.compress.Decompressor;

public class VTAirliftInputStream extends InputStream
{
	private Decompressor decompressor;
	private VTLittleEndianInputStream in;
	private byte[] input = new byte[1];
	private VTByteArrayInputStream stream = new VTByteArrayInputStream(new byte[]{});
	
	public VTAirliftInputStream(InputStream in, Decompressor decompressor)
	{
		this.in = new VTLittleEndianInputStream(in);
		this.decompressor = decompressor;
	}
	
	public synchronized int available() throws IOException
	{
		return stream.available();
	}
	
	public synchronized int read(byte[] data, int off, int len) throws IOException
	{
		if (stream.available() == 0)
		{
			readBlock();
		}
		if (stream.available() > 0)
		{
			return stream.read(data, off, len);
		}
		return -1;
	}

	public synchronized int read() throws IOException
	{
		if (stream.available() == 0)
		{
			readBlock();
		}
		if (stream.available() > 0)
		{
			return stream.read();
		}
		return -1;
	}
	
	private synchronized void readBlock() throws IOException
	{
		int compressed = in.readInt();
		int decompressed = in.readInt();
		if (input.length < compressed)
		{
			input = new byte[compressed];
		}
		if (stream.buf().length < decompressed)
		{
			stream.buf(new byte[decompressed]);
		}
		in.readFully(input, 0, compressed);
		stream.count(decompressor.decompress(input, 0, compressed, stream.buf(), 0, decompressed));
		stream.pos(0);
	}
	
	public synchronized void close() throws IOException
	{
		in.close();
	}
}
