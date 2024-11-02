package com.pngencoder;

import java.util.concurrent.Callable;
import java.util.zip.Deflater;

import org.vash.vate.compatibility.VTObjects;

public class PngEncoderDeflaterSegmentTaskCallable implements Callable<PngEncoderDeflaterSegmentResult>
{
  private final PngEncoderDeflaterBuffer originalSegment;
  private final PngEncoderDeflaterBuffer deflatedSegment;
  private final int compressionLevel;
  private final int compressionStrategy;
  private final boolean lastSegment;
  
  public PngEncoderDeflaterSegmentTaskCallable(
      PngEncoderDeflaterBuffer originalSegment,
      PngEncoderDeflaterBuffer deflatedSegment,
      int compressionLevel,
      int compressionStrategy,
      boolean lastSegment) {
  //super(null);
  this.originalSegment = VTObjects.requireNonNull(originalSegment, "originalSegment");
  this.deflatedSegment = VTObjects.requireNonNull(deflatedSegment, "deflatedSegment");
  this.compressionLevel = compressionLevel;
  this.compressionStrategy = compressionStrategy;
  this.lastSegment = lastSegment;
}

  public PngEncoderDeflaterSegmentResult call() throws Exception {
    final long originalSegmentAdler32 = originalSegment.calculateAdler32();
    final int originalSegmentLength = originalSegment.length;
    deflate(originalSegment, deflatedSegment, compressionLevel, compressionStrategy, lastSegment);
    return new PngEncoderDeflaterSegmentResult(originalSegment, deflatedSegment, originalSegmentAdler32, originalSegmentLength);
  }
  
  private static void deflate(PngEncoderDeflaterBuffer originalSegment, PngEncoderDeflaterBuffer deflatedSegment, int compressionLevel, int compressionStrategy, boolean lastSegment) {
    final Deflater deflater = PngEncoderDeflaterThreadLocalDeflater.getInstance(compressionLevel, compressionStrategy);
    deflater.setInput(originalSegment.bytes, 0, originalSegment.length);

    if (lastSegment) {
        deflater.finish();
    }

    deflatedSegment.length = deflater.deflate(deflatedSegment.bytes, 0, deflatedSegment.bytes.length/*, lastSegment ? Deflater.NO_FLUSH : Deflater.SYNC_FLUSH*/);
}


}
