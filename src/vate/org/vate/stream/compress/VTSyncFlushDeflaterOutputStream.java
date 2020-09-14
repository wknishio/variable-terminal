package org.vate.stream.compress;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public final class VTSyncFlushDeflaterOutputStream extends DeflaterOutputStream
{
	public VTSyncFlushDeflaterOutputStream(OutputStream out, Deflater def, int size)
	{
		super(out, def, size, true);
	}
	
	protected final void deflate() throws IOException
	{
		int len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);
		if (len > 0)
		{
			out.write(buf, 0, len);
		}
		//out.write(buf, 0, len);
		//out.flush();
	}
	
	public final void flush() throws IOException
	{
        if (!def.finished()) {
            int len = 0;
            while ((len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH)) > 0)
            {
                out.write(buf, 0, len);
                if (len < buf.length)
                    break;
            }
        }
        out.flush();
    }
}