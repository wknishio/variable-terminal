package org.vash.vate.stream.compress;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;

import org.vash.vate.VT;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.array.VTFlushBufferedOutputStream;
import org.vash.vate.stream.filter.VTBlockSplitOutputStream;
import org.vash.vate.stream.filter.VTBufferedOutputStream;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

//import io.airlift.compress.zstd.Constants;
//import io.airlift.compress.lzo.LzoCompressor;
//import io.airlift.compress.lzo.LzoDecompressor;
import io.airlift.compress.zstd.ZstdCompressor;
import io.airlift.compress.zstd.ZstdDecompressor;
import io.airlift.compress.zstd.ZstdHadoopInputStream;
import io.airlift.compress.zstd.ZstdHadoopOutputStream;
//import io.airlift.compress.zstd.ZstdCompressor;
//import io.airlift.compress.zstd.ZstdDecompressor;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.lz4.LZ4FrameOutputStream.BLOCKSIZE;
import net.jpountz.xxhash.XXHashFactory;

@SuppressWarnings(
{ "unused", "deprecation" })
public class VTCompressorSelector
{
  public static OutputStream createDirectZlibOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.FILTERED);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return javaDeflaterOutputStream;
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
        return jzlibDeflater;
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
  
  public static InputStream createDirectZlibInputStream(InputStream in)
  {
    try
    {
      // return new InflaterInputStream(in, VT.VT_COMPRESSED_DATA_BUFFER_SIZE,
      // true);
      ZInputStream jzlibInflater = new ZInputStream(in, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      jzlibInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
      return jzlibInflater;
    }
    catch (Throwable e)
    {
      
    }
    return null;
  }
  
  public static OutputStream createDirectZstdOutputStream(OutputStream out)
  {
    return new VTHadoopOutputStream(new ZstdHadoopOutputStream(out, true));
    //return new VTBufferedOutputStream(new VTAirliftOutputStream(out, new ZstdCompressor()), VT.VT_COMPRESSED_DATA_BUFFER_SIZE, true);
    //return new VTBufferedOutputStream(new ZstdOutputStream(out), VT.VT_COMPRESSED_DATA_BUFFER_SIZE, true);
  }
  
  public static InputStream createDirectZstdInputStream(InputStream in)
  {
    return new VTHadoopInputStream(new ZstdHadoopInputStream(in));
    //return new BufferedInputStream(new VTAirliftInputStream(in, new ZstdDecompressor()), VT.VT_COMPRESSED_DATA_BUFFER_SIZE);
    //return new BufferedInputStream(new ZstdInputStream(in), VT.VT_COMPRESSED_DATA_BUFFER_SIZE);
  }
  
  public static OutputStream createBufferedZlibOutputStreamDefault(OutputStream out)
  {
    return createFlushBufferedSyncFlushDeflaterOutputStreamDefaultStrategy(out);
  }
  
  public static OutputStream createBufferedZlibOutputStreamFiltered(OutputStream out)
  {
    return createFlushBufferedSyncFlushDeflaterOutputStreamFilteredStrategy(out);
  }
  
  public static OutputStream createBufferedZlibOutputStreamHuffmanOnly(OutputStream out)
  {
    return createFlushBufferedSyncFlushDeflaterOutputStreamHuffmanOnlyStrategy(out);
  }
  
  public static InputStream createBufferedZlibInputStream(InputStream in)
  {
    return createFlushBufferedSyncFlushInflaterInputStream(in);
  }
  
  public static OutputStream createBufferedZstdOutputStream(OutputStream out)
  {
    return new VTBufferedOutputStream(new VTHadoopOutputStream(new ZstdHadoopOutputStream(out, false)), VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    //return new VTBufferedOutputStream(new VTAirliftOutputStream(out, new ZstdCompressor()), VT.VT_COMPRESSED_DATA_BUFFER_SIZE, true);
    //return new VTBufferedOutputStream(new ZstdOutputStream(out), VT.VT_COMPRESSED_DATA_BUFFER_SIZE, true);
  }
  
  public static InputStream createBufferedZstdInputStream(InputStream in)
  {
    return new BufferedInputStream(new VTHadoopInputStream(new ZstdHadoopInputStream(in)), VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    //return new BufferedInputStream(new VTAirliftInputStream(in, new ZstdDecompressor()), VT.VT_COMPRESSED_DATA_BUFFER_SIZE);
    //return new BufferedInputStream(new ZstdInputStream(in), VT.VT_COMPRESSED_DATA_BUFFER_SIZE);
  }
  
  public static OutputStream createFlushBufferedSyncFlushDeflaterOutputStreamFilteredStrategy(OutputStream out)
  {
    // out = new VTBufferedOutputStream(out, VT.VT_COMPRESSED_DATA_BUFFER_SIZE,
    // true);
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.FILTERED);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return new VTBufferedOutputStream(javaDeflaterOutputStream, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
        return new VTBufferedOutputStream(jzlibDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
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
  
  public static OutputStream createFlushBufferedSyncFlushDeflaterOutputStreamDefaultStrategy(OutputStream out)
  {
    // out = new VTBufferedOutputStream(out, VT.VT_COMPRESSED_DATA_BUFFER_SIZE,
    // true);
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.DEFAULT_STRATEGY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return new VTBufferedOutputStream(javaDeflaterOutputStream, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_DEFAULT_STRATEGY);
        return new VTBufferedOutputStream(jzlibDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
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
  
  public static OutputStream createFlushBufferedSyncFlushDeflaterOutputStreamHuffmanOnlyStrategy(OutputStream out)
  {
    // out = new VTBufferedOutputStream(out, VT.VT_COMPRESSED_DATA_BUFFER_SIZE,
    // true);
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.HUFFMAN_ONLY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return new VTBufferedOutputStream(javaDeflaterOutputStream, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_HUFFMAN_ONLY);
        return new VTBufferedOutputStream(jzlibDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
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
      // return new InflaterInputStream(in, VT.VT_COMPRESSED_DATA_BUFFER_SIZE,
      // true);
      ZInputStream jzlibInflater = new ZInputStream(in, true, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      jzlibInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
      return new BufferedInputStream(jzlibInflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable e)
    {
      
    }
    return null;
    // return in;
  }
  
  public static OutputStream createDirectLz4OutputStream(OutputStream out)
  {
    // return out;
    return new LZ4BlockOutputStream(out, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, LZ4Factory.safeInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true);
    //return new LZ4FrameOutputStream(out, BLOCKSIZE.SIZE_64KB, false);
  }
  
  public static InputStream createDirectLz4InputStream(InputStream in)
  {
    // return in;
    return new LZ4BlockInputStream(in, LZ4Factory.safeInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false);
    //return new LZ4FrameInputStream(in, false);
  }
  
  public static OutputStream createBufferedLz4OutputStream(OutputStream out)
  {
    // return out;
    // return createFlushBufferedSyncFlushDeflaterOutputStream(out);
    return new VTBufferedOutputStream(new LZ4BlockOutputStream(out, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, LZ4Factory.safeInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    //return new VTBufferedOutputStream(new LZ4FrameOutputStream(out, BLOCKSIZE.SIZE_64KB, false), VT.VT_COMPRESSED_DATA_BUFFER_SIZE, false);
  }
  
  public static InputStream createBufferedLz4InputStream(InputStream in)
  {
    // return in;
    // return createFlushBufferedSyncFlushInflaterInputStream(in);
    return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.safeInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    //return new BufferedInputStream(new LZ4FrameInputStream(in, false), VT.VT_COMPRESSED_DATA_BUFFER_SIZE);
  }  
}
