package org.vate.stream.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.vate.stream.array.VTByteArrayInputStream;
import org.vate.stream.array.VTByteArrayOutputStream;

public class VTPipedDecompressor extends OutputStream
{
	private static final int bufferSize = 1024 * 64;
	private InputStream in;
	private OutputStream out;
	private byte[] buffer = new byte[bufferSize];
	//private VTPipedInputStream pipedInputStream;
	//private VTPipedOutputStream pipedOutputStream;
	private VTByteArrayOutputStream outputBuffer = new VTByteArrayOutputStream(bufferSize);
	private VTByteArrayInputStream inputBuffer = new VTByteArrayInputStream(outputBuffer.buf());
	
	public VTPipedDecompressor(OutputStream out)
	{
		this.out = out;
		//this.pipedInputStream = new VTPipedInputStream(bufferSize);
		//this.pipedOutputStream = new VTPipedOutputStream();
		//try
		//{
			//this.pipedInputStream.connect(this.pipedOutputStream);
		//}
		//catch (IOException e)
		//{
			
		//}
	}
	
	public InputStream getPipedInputStream()
	{
		return inputBuffer;
	}
	
	public void setDecompressor(InputStream in)
	{
		this.in = in;
	}

	public void write(int b) throws IOException
	{
		write(new byte[] {(byte) b});
	}
	
	public void write(byte[] data) throws IOException
	{
		write(data, 0, data.length);
	}
	
	public void write(byte[] data, int off, int len) throws IOException
	{
		//System.out.println("compressed:["+len+"]");
		outputBuffer.reset();
		outputBuffer.write(data, off, len);
		outputBuffer.flush();
		inputBuffer.pos(0);
		inputBuffer.count(len);
		int readed = in.read(buffer, 0, bufferSize);
		out.write(buffer, 0, readed);
		out.flush();
		//System.out.println("compressed data:" + len);
		//System.out.println("decompressed data:" + readed);
		//out.write(data, off, len);
	}
	
	public void flush() throws IOException
	{
		
	}
}
