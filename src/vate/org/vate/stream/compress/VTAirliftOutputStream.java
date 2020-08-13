package org.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;

import org.vate.stream.endian.VTLittleEndianOutputStream;

import io.airlift.compress.Compressor;

public class VTAirliftOutputStream extends OutputStream
{
	private Compressor compressor;
	private VTLittleEndianOutputStream out;
	private byte[] output = new byte[1];
	private byte[] single = new byte[1];

	public VTAirliftOutputStream(OutputStream out, Compressor compressor)
	{
		this.out = new VTLittleEndianOutputStream(out);
		this.compressor = compressor;
	}
	
	public synchronized void write(byte[] data, int off, int len) throws IOException
	{
		writeBlock(data, off, len);
	}
	
	public synchronized void write(int data) throws IOException
	{
		single[0] = (byte) data;
		write(single, 0, 1);
	}
	
	private void writeBlock(byte[] data, int off, int len) throws IOException
	{
		//System.out.println("writeBlock()");
		//System.out.println("decompressed = " + len);
		int max = compressor.maxCompressedLength(len);
		if (output.length < max)
		{
			output = new byte[max];
		}
		int compressed = compressor.compress(data, off, len, output, 0, max);
		//System.out.println("compressed = " + compressed);
		out.writeInt(compressed);
		out.writeInt(len);
		out.write(output, 0, compressed);
		//out.flush();
	}
	
	public synchronized void flush() throws IOException
	{
		out.flush();
	}
	
	public synchronized void close() throws IOException
	{
		out.close();
	}
}
