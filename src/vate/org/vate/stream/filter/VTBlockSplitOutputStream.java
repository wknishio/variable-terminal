package org.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTBlockSplitOutputStream extends FilterOutputStream
{
	private final int blockSize;
	
	public VTBlockSplitOutputStream(OutputStream out, int blockSize)
	{
		super(out);
		this.blockSize = blockSize;
	}
	
	public final void write(byte[] b, int off, int len) throws IOException
	{
		if (len <= blockSize)
		{
			out.write(b, off, len);
		}
		else
		{
			while (len > 0)
			{
				out.write(b, off, Math.min(blockSize, len));
				off += blockSize;
				len -= blockSize;
			}
		}
	}
	
	public final void write(int b) throws IOException
	{
		out.write(b);
	}
	
	public final void flush() throws IOException
	{
		out.flush();
	}
}