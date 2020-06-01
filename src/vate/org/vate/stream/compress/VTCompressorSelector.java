package org.vate.stream.compress;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import org.vate.stream.filter.VTBufferedOutputStream;
import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;
import com.jcraft.jzlib.JZlib;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class VTCompressorSelector
{
	public static OutputStream createCompatibleSyncFlushDeflaterOutputStream(OutputStream out)
	{
		//return out;
		try
		{
			java.util.zip.Deflater javaDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
			javaDeflater.setStrategy(Deflater.FILTERED);
			javaDeflater.setLevel(Deflater.BEST_SPEED);
			VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, 1024 * 32);
			return new VTBufferedOutputStream(javaDeflaterOutputStream, 1024 * 64);
		}
		catch (Throwable t)
		{
			DeflaterOutputStream jzlibDeflater;
			try
			{
				jzlibDeflater = new com.jcraft.jzlib.DeflaterOutputStream(out, 1024 * 32, JZlib.Z_BEST_SPEED, true);
				jzlibDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
				jzlibDeflater.setSyncFlush(true);
				return new VTBufferedOutputStream(jzlibDeflater, 1024 * 64);
			}
			catch (Throwable e)
			{
				
			}
		}
		finally
		{
			
		}
		return null;
	}
	
	public static InputStream createCompatibleSyncFlushInflaterInputStream(InputStream in)
	{
		try
		{
			return new InflaterInputStream(in, 1024 * 64, true);
		}
		catch (Throwable e)
		{
			
		}
		return null;
		//return in;
	}
	
	public static OutputStream createCompatibleLZ4OutputStream(OutputStream out)
	{
		//return out;
		return new VTBufferedOutputStream(new LZ4BlockOutputStream(out, 1024 * 32, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), 1024 * 64);
	}
	
	public static InputStream createCompatibleLZ4InputStream(InputStream in)
	{
		//return in;
		return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), 1024 * 64);
	}
}
