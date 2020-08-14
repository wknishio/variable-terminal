package org.vate.stream.compress;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;

import org.iq80.snappy.SnappyFramedInputStream;
import org.iq80.snappy.SnappyFramedOutputStream;
import org.iq80.snappy.SnappyInputStream;
import org.iq80.snappy.SnappyOutputStream;
import org.vate.VT;
import org.vate.stream.filter.VTBufferedOutputStream;
import com.jcraft.jzlib.DeflaterOutputStream;
import com.jcraft.jzlib.InflaterInputStream;
import com.jcraft.jzlib.JZlib;

import io.airlift.compress.zstd.ZstdCompressor;
import io.airlift.compress.zstd.ZstdDecompressor;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

@SuppressWarnings({ "unused", "deprecation" })
public class VTCompressorSelector
{
	public static OutputStream createCompatibleZstdOutputStream(OutputStream out)
	{
		OutputStream stream = new VTBufferedOutputStream(new VTAirliftOutputStream(new VTBufferedOutputStream(out, VT.VT_STANDARD_DATA_BUFFER_SIZE), new ZstdCompressor()), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		//OutputStream stream = new VTBufferedOutputStream(new VTAirliftOutputStream(out, new ZstdCompressor()), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		return stream;
	}
	
	public static InputStream createCompatibleZstdInputStream(InputStream in)
	{
		InputStream stream = new VTAirliftInputStream(new BufferedInputStream(in, VT.VT_STANDARD_DATA_BUFFER_SIZE), new ZstdDecompressor());
		//InputStream stream = new VTAirliftInputStream(in, new ZstdDecompressor());
		return stream;
	}
	
	public static OutputStream createCompatibleSyncFlushDeflaterOutputStream(OutputStream out)
	{
		//return out;
		try
		{
			java.util.zip.Deflater javaDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
			javaDeflater.setStrategy(Deflater.FILTERED);
			javaDeflater.setLevel(Deflater.BEST_SPEED);
			VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VT.VT_STANDARD_DATA_BUFFER_SIZE);
			return new VTBufferedOutputStream(javaDeflaterOutputStream, VT.VT_STANDARD_DATA_BUFFER_SIZE);
		}
		catch (Throwable t)
		{
			DeflaterOutputStream jzlibDeflater;
			try
			{
				jzlibDeflater = new com.jcraft.jzlib.DeflaterOutputStream(out, VT.VT_STANDARD_DATA_BUFFER_SIZE, JZlib.Z_BEST_SPEED, true);
				jzlibDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
				jzlibDeflater.setSyncFlush(true);
				return new VTBufferedOutputStream(jzlibDeflater, VT.VT_STANDARD_DATA_BUFFER_SIZE);
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
			return new InflaterInputStream(in, VT.VT_STANDARD_DATA_BUFFER_SIZE, true);
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
		return new VTBufferedOutputStream(new LZ4BlockOutputStream(out, VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
	}
	
	public static InputStream createCompatibleLZ4InputStream(InputStream in)
	{
		//return in;
		return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
	}
	
	public static OutputStream createCompatibleSnappyOutputStream(OutputStream out)
	{
		try
		{
			return new SnappyOutputStream(out, false);
		}
		catch (Throwable t)
		{
			
		}
		return null;
	}
	
	public static InputStream createCompatibleSnappyInputStream(InputStream in)
	{
		try
		{
			return new SnappyInputStream(in, false);
		}
		catch (Throwable t)
		{
			
		}
		return null;
	}

	
}
