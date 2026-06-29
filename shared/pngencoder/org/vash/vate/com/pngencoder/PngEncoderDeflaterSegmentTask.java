package org.vash.vate.com.pngencoder;

import java.util.concurrent.FutureTask;

class PngEncoderDeflaterSegmentTask extends FutureTask<PngEncoderDeflaterSegmentResult> {

    public PngEncoderDeflaterSegmentTask(
            PngEncoderDeflaterBuffer originalSegment,
            PngEncoderDeflaterBuffer deflatedSegment,
            int compressionLevel,
            int compressionStrategy,
            boolean lastSegment) {
        //super(null);
        super(new PngEncoderDeflaterSegmentTaskCallable(originalSegment, deflatedSegment, compressionLevel, compressionStrategy, lastSegment));
    }
}
