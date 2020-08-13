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
	//private byte[] output = new byte[1];
	private VTByteArrayInputStream stream = new VTByteArrayInputStream(new byte[]{});
	//private volatile int index = 0;
	//private volatile int available = 0;
	
	public VTAirliftInputStream(InputStream in, Decompressor decompressor)
	{
		this.in = new VTLittleEndianInputStream(in);
		this.decompressor = decompressor;
	}
	
	public int available() throws IOException
	{
		return stream.available();
	}
	
	public int read(byte[] data, int off, int len) throws IOException
	{
		if (stream.available() == 0)
		{
			readBlock();
		}
		if (stream.available() > 0)
		{
			return stream.read(data, off, len);
		}
		System.out.println("EOF detected");
		return -1;
	}

	public int read() throws IOException
	{
		if (stream.available() == 0)
		{
			readBlock();
		}
		if (stream.available() > 0)
		{
			return stream.read();
		}
		System.out.println("EOF detected");
		return -1;
	}
	
	private void readBlock() throws IOException
	{
		//System.out.println("readBlock()");
		int compressed = in.readInt();
		int decompressed = in.readInt();
		//System.out.println("decompressed = " + decompressed);
		//System.out.println("compressed = " + compressed);
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
		//System.out.println("available = " + stream.count());
		//System.out.println("data = " + Arrays.toString(output));
		//index = 0;
		//return available;
	}
	
	public synchronized void close() throws IOException
	{
		in.close();
	}
}
