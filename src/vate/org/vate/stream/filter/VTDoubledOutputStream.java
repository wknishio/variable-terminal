package org.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.vate.console.graphical.VTGraphicalConsoleNullOutputStream;

public class VTDoubledOutputStream extends FilterOutputStream
{
	private OutputStream first;
	private OutputStream second;

	public VTDoubledOutputStream(OutputStream first, OutputStream second)
	{
		super(new VTGraphicalConsoleNullOutputStream());
		this.first = first;
		this.second = second;
	}
	
	public void write(byte[] b, int off, int len) throws IOException
	{
		first.write(b, off, len);
		second.write(b, off, len);
	}
	
	public void write(byte[] b) throws IOException
	{
		first.write(b);
		second.write(b);
	}
	
	public void write(int b) throws IOException
	{
		first.write(b);
		second.write(b);
	}
	
	public void flush() throws IOException
	{
		first.flush();
		second.flush();
	}
	
	public void close() throws IOException
	{
		//nothing
	}
}
