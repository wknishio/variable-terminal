package vate.com.pngencoder;

import java.util.zip.Deflater;

/**
 * We save time by allocating and reusing some thread local state.
 * <p>
 * Creating a new Deflater instance takes a surprising amount of time.
 * Resetting an existing Deflater instance is almost free though.
 */
class PngEncoderDeflaterThreadLocalDeflater {
    private static final ThreadLocal<PngEncoderDeflaterThreadLocalDeflater> THREAD_LOCAL = new ThreadLocal<PngEncoderDeflaterThreadLocalDeflater>();

    public static Deflater getInstance(int compressionLevel, int strategy) {
        return THREAD_LOCAL.get().getDeflater(compressionLevel, strategy);
    }

    private final Deflater[] deflaters;

    public PngEncoderDeflaterThreadLocalDeflater() {
        this.deflaters = new Deflater[11];
    }

    public Deflater getDeflater(int compressionLevel, int compressionStrategy) {
        Deflater deflater = this.deflaters[compressionLevel + 1];
        if (deflater == null) {
            boolean nowrap = true;
            deflater = new Deflater(compressionLevel, nowrap);
            this.deflaters[compressionLevel + 1] = deflater;
        }
        deflater.setStrategy(compressionStrategy);
        deflater.reset();
        return deflater;
    }
}
