package org.vash.vate.stream.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.iq80.snappy.SnappyFramedInputStream;
import org.iq80.snappy.SnappyFramedOutputStream;
import org.vash.vate.VTSystem;
import org.vash.vate.stream.array.VTByteArrayOutputStream;
import org.vash.vate.stream.filter.VTBlockSplitOutputStream;
import org.vash.vate.stream.filter.VTBufferedOutputStream;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

import io.airlift.compress.zstd.ZstdCompressor;
import io.airlift.compress.zstd.ZstdDecompressor;
import io.airlift.compress.zstd.ZstdHadoopInputStream;
import io.airlift.compress.zstd.ZstdHadoopOutputStream;
import io.airlift.compress.zstd.ZstdInputStream;
import io.airlift.compress.zstd.ZstdOutputStream;
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
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //VTNoFlushDeflaterOutputStream javaDeflaterOutputStream = new VTNoFlushDeflaterOutputStream(out, javaDeflater, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return javaDeflaterOutputStream;
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
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
      ZInputStream jzlibInflater = new ZInputStream(in, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      jzlibInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
      return jzlibInflater;
      //VTNoFlushInflaterInputStream javaInflaterInputStream = new VTNoFlushInflaterInputStream(in, new Inflater(true), VT.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return javaInflaterInputStream;
    }
    catch (Throwable e)
    {
      
    }
    return null;
  }
  
  public static OutputStream createDirectZstdOutputStream(OutputStream out)
  {
    //return new VTHadoopOutputStream(new ZstdHadoopOutputStream(out, false));
    return new ZstdOutputStream(out, false);
    //return new VTAirliftOutputStream(out, new ZstdCompressor());
  }
  
  public static InputStream createDirectZstdInputStream(InputStream in)
  {
    //return new VTHadoopInputStream(new ZstdHadoopInputStream(in));
    return new ZstdInputStream(in);
    //return new VTAirliftInputStream(in, new ZstdDecompressor());
  }
  
  public static OutputStream createBufferedSyncFlushDefaultZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedSyncFlushDefaultDeflaterOutputStream(out);
  }
  
  public static OutputStream createBufferedSyncFlushFilteredZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedSyncFlushFilteredDeflaterOutputStream(out);
  }
  
  public static OutputStream createBufferedSyncFlushHuffmanZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedSyncFlushHuffmanDeflaterOutputStream(out);
  }
  
  public static InputStream createBufferedSyncFlushZlibInputStream(InputStream in)
  {
    return createFlushBufferedSyncFlushInflaterInputStream(in);
  }
  
  public static OutputStream createBufferedNoFlushDefaultZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedNoFlushDefaultDeflaterOutputStream(out);
  }
  
  public static OutputStream createBufferedNoFlushFilteredZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedNoFlushFilteredDeflaterOutputStream(out);
  }
  
  public static OutputStream createBufferedNoFlushHuffmanZlibOutputStream(OutputStream out)
  {
    return createFlushBufferedNoFlushHuffmanDeflaterOutputStream(out);
  }
  
  public static InputStream createBufferedNoFlushZlibInputStream(InputStream in)
  {
    return createFlushBufferedNoFlushInflaterInputStream(in);
  }
  
  public static OutputStream createBufferedZstdOutputStream(OutputStream out)
  {
    //return new VTFlushBufferedOutputStream(new VTHadoopOutputStream(new ZstdHadoopOutputStream(out, false)), new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
    //return new VTBufferedOutputStream(new VTHadoopOutputStream(new ZstdHadoopOutputStream(out, false)), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
    return new BufferedOutputStream(new ZstdOutputStream(out, false), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    //return new VTBufferedOutputStream(new VTAirliftOutputStream(out, new ZstdCompressor()), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES, false);
  }
  
  public static InputStream createBufferedZstdInputStream(InputStream in)
  {
    //return new BufferedInputStream(new VTHadoopInputStream(new ZstdHadoopInputStream(in)), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    return new BufferedInputStream(new ZstdInputStream(in), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    //return new BufferedInputStream(new VTAirliftInputStream(in, new ZstdDecompressor()), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
  }
  
  private static OutputStream createFlushBufferedSyncFlushFilteredDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.FILTERED);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_FILTERED);
        //return new VTFlushBufferedOutputStream(jzlibDeflater, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
        return new BufferedOutputStream(jzlibDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
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
  
  private static OutputStream createFlushBufferedSyncFlushDefaultDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.DEFAULT_STRATEGY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_DEFAULT_STRATEGY);
        //return new VTFlushBufferedOutputStream(jzlibDeflater, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
        return new BufferedOutputStream(jzlibDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
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
  
  private static OutputStream createFlushBufferedSyncFlushHuffmanDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.HUFFMAN_ONLY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTSyncFlushDeflaterOutputStream javaDeflaterOutputStream = new VTSyncFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      try
      {
        ZOutputStream jzlibDeflater = new ZOutputStream(out, JZlib.Z_BEST_SPEED, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
        jzlibDeflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
        jzlibDeflater.getZStream().deflateParams(JZlib.Z_BEST_SPEED, JZlib.Z_HUFFMAN_ONLY);
        //return new VTFlushBufferedOutputStream(jzlibDeflater, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
        return new BufferedOutputStream(jzlibDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
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
  
  private static InputStream createFlushBufferedSyncFlushInflaterInputStream(InputStream in)
  {
    try
    {
      ZInputStream jzlibInflater = new ZInputStream(in, true, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      jzlibInflater.setFlushMode(JZlib.Z_SYNC_FLUSH);
      return new BufferedInputStream(jzlibInflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable e)
    {
      
    }
    return null;
    // return in;
  }
  
  private static OutputStream createFlushBufferedNoFlushFilteredDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.FILTERED);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTNoFlushDeflaterOutputStream javaDeflaterOutputStream = new VTNoFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      
    }
    return null;
  }
  
  private static OutputStream createFlushBufferedNoFlushDefaultDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.DEFAULT_STRATEGY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTNoFlushDeflaterOutputStream javaDeflaterOutputStream = new VTNoFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      
    }
    return null;
  }
  
  private static OutputStream createFlushBufferedNoFlushHuffmanDeflaterOutputStream(OutputStream out)
  {
    try
    {
      Deflater javaDeflater = new Deflater(Deflater.BEST_SPEED, true);
      javaDeflater.setStrategy(Deflater.HUFFMAN_ONLY);
      javaDeflater.setLevel(Deflater.BEST_SPEED);
      VTNoFlushDeflaterOutputStream javaDeflaterOutputStream = new VTNoFlushDeflaterOutputStream(out, javaDeflater, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      //return new VTFlushBufferedOutputStream(javaDeflaterOutputStream, new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(javaDeflaterOutputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      
    }
    return null;
  }
  
  private static InputStream createFlushBufferedNoFlushInflaterInputStream(InputStream in)
  {
    try
    {
      VTNoFlushInflaterInputStream javaInflaterInputStream = new VTNoFlushInflaterInputStream(in, new Inflater(true), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
      return new BufferedInputStream(javaInflaterInputStream, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable e)
    {
      
    }
    return null;
    // return in;
  }
  
  public static OutputStream createDirectLz4OutputStream(OutputStream out)
  {
    return new LZ4BlockOutputStream(out, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES, LZ4Factory.safeInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true);
  }
  
  public static InputStream createDirectLz4InputStream(InputStream in)
  {
    return new LZ4BlockInputStream(in, LZ4Factory.safeInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false);
  }
  
  public static OutputStream createBufferedLz4OutputStream(OutputStream out)
  {
    //return new VTFlushBufferedOutputStream(new LZ4BlockOutputStream(out, VT.VT_COMPRESSION_BUFFER_SIZE_BYTES, LZ4Factory.safeInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
    return new BufferedOutputStream(new LZ4BlockOutputStream(out, VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES, LZ4Factory.safeInstance().fastCompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), true), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
  }
  
  public static InputStream createBufferedLz4InputStream(InputStream in)
  {
    return new BufferedInputStream(new LZ4BlockInputStream(in, LZ4Factory.safeInstance().fastDecompressor(), XXHashFactory.disabledInstance().newStreamingHash32(0x9747b28c).asChecksum(), false), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
  }
  
  public static OutputStream createBufferedSnappyOutputStream(OutputStream out)
  {
    try
    {
      //return new VTFlushBufferedOutputStream(new SnappyFramedOutputStream(out, false), new VTByteArrayOutputStream(VT.VT_COMPRESSION_BUFFER_SIZE_BYTES));
      return new BufferedOutputStream(new SnappyFramedOutputStream(out, false), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public static InputStream createBufferedSnappyInputStream(InputStream in)
  {
    try
    {
      return new BufferedInputStream(new SnappyFramedInputStream(in, false), VTSystem.VT_COMPRESSION_BUFFER_SIZE_BYTES);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public static OutputStream createDirectSnappyOutputStream(OutputStream out)
  {
    try
    {
      return new SnappyFramedOutputStream(out, false);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public static InputStream createDirectSnappyInputStream(InputStream in)
  {
    try
    {
      return new SnappyFramedInputStream(in, false);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
}