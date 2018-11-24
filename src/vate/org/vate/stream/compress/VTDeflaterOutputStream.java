package org.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class VTDeflaterOutputStream extends DeflaterOutputStream
{
	private boolean syncFlush;
	
	public VTDeflaterOutputStream(OutputStream out, Deflater def, int size, boolean syncFlush)
	{
		super(out, def, size, syncFlush);
		this.syncFlush = syncFlush;
	}
	
	protected void deflate() throws IOException
	{
		int len = def.deflate(buf, 0, buf.length, syncFlush ? Deflater.SYNC_FLUSH : Deflater.NO_FLUSH);
		if (len > 0)
		{
			out.write(buf, 0, len);
		}
	}
}