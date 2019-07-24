package org.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class VTSyncFlushDeflaterOutputStream extends DeflaterOutputStream
{
	public VTSyncFlushDeflaterOutputStream(OutputStream out, Deflater def, int size)
	{
		super(out, def, size, true);
	}
	
	protected void deflate() throws IOException
	{
		int len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);
		if (len > 0)
		{
			out.write(buf, 0, len);
		}
		//out.write(buf, 0, len);
		//out.flush();
	}
}