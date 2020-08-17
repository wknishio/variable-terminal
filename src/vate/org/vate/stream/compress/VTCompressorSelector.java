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
import org.vate.stream.array.VTByteArrayOutputStream;
import org.vate.stream.array.VTFlushBufferedOutputStream;
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
	public static OutputStream createFlushBufferedZstdOutputStream(OutputStream out)
	{
		OutputStream stream = new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), new VTAirliftOutputStream(new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), out), new ZstdCompressor()));
		//OutputStream stream = new VTBufferedOutputStream(new VTAirliftOutputStream(out, new ZstdCompressor()), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		return stream;
	}
	
	public static InputStream createFlushBufferedZstdInputStream(InputStream in)
	{
		InputStream stream = new VTAirliftInputStream(new BufferedInputStream(in, VT.VT_STANDARD_DATA_BUFFER_SIZE), new ZstdDecompressor());
		//InputStream stream = new VTAirliftInputStream(in, new ZstdDecompressor());
		return stream;
	}
	
	public static OutputStream createFlushBufferedSyncFlushDeflaterOutputStream(OutputStream out)
	{
		//return out;
		try
		{
			java.util.zip.Deflater javaDeflater = new java.util.zip.Deflater(Deflater.BEST_SPEED, true);
			javaDeflater.setStrategy(Deflater.FILTERED);
			javaDeflater.setLevel(Deflater.BEST_SPEED);
			VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), out), javaDeflater, VT.VT_STANDARD_DATA_BUFFER_SIZE);
			return new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), javaDeflaterOutputStream);
		}
		catch (Throwable t)
		{
			DeflaterOutputStream jzlibDeflater;
			try
			{
				jzlibDeflater = new com.jcraft.jzlib.DeflaterOutputStream(new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), out), VT.VT_STANDARD_DATA_BUFFER_SIZE, JZlib.Z_BEST_SPEED, true);
				jzlibDeflater.getDeflater().params(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
				jzlibDeflater.setSyncFlush(true);
				return new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), jzlibDeflater);
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
	
	public static InputStream createFlushBufferedSyncFlushInflaterInputStream(InputStream in)
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
	
	public static OutputStream createDirectLZ4OutputStream(OutputStream out)
	{
		//return out;
		return new LZ4BlockOutputStream(out, VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true);
	}
	
	public static InputStream createDirectLZ4InputStream(InputStream in)
	{
		//return in;
		return new LZ4BlockInputStream(in, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false);
	}
	
	public static OutputStream createBufferedLZ4OutputStream(OutputStream out)
	{
		//return out;
		return new VTBufferedOutputStream(new LZ4BlockOutputStream(out, VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VT.VT_STANDARD_DATA_BUFFER_SIZE);
	}
	
	public static InputStream createBufferedLZ4InputStream(InputStream in)
	{
		//return in;
		return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
	}
	
	public static OutputStream createFlushBufferedLZ4OutputStream(OutputStream out)
	{
		//return out;
		return new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), new LZ4BlockOutputStream(new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), out), VT.VT_STANDARD_DATA_BUFFER_SIZE, LZ4Factory.fastestJavaInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true));
	}
	
	public static InputStream createFlushBufferedLZ4InputStream(InputStream in)
	{
		//return in;
		return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.fastestJavaInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
	}
	
	public static OutputStream createFlushBufferedSnappyOutputStream(OutputStream out)
	{
		//return out;
		try
		{
			return new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), new SnappyOutputStream(new VTFlushBufferedOutputStream(new VTByteArrayOutputStream(VT.VT_STANDARD_DATA_BUFFER_SIZE), out), false));
		}
		catch (Throwable t)
		{
			
		}
		return null;
	}
	
	public static InputStream createFlushBufferedSnappyInputStream(InputStream in)
	{
		//return in;
		try
		{
			return new BufferedInputStream(new SnappyInputStream(in, false), VT.VT_STANDARD_DATA_BUFFER_SIZE);
		}
		catch (Throwable t)
		{
			
		}
		return null;
	}
	
	public static OutputStream createSnappyOutputStream(OutputStream out)
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
	
	public static InputStream createSnappyInputStream(InputStream in)
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
