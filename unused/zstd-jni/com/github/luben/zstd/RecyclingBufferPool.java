package com.github.luben.zstd;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An pool of buffers which uses a simple reference queue to recycle buffers.
 *
 * Do not use it as generic buffer pool - it is optimized and supports only
 * buffer sizes used by the Zstd classes.
 */
public class RecyclingBufferPool implements BufferPool {
    public static final BufferPool INSTANCE = new RecyclingBufferPool();

    private static final int buffSize = Math.max(Math.max(
            (int) ZstdOutputStreamNoFinalizer.recommendedCOutSize(),
            (int) ZstdInputStreamNoFinalizer.recommendedDInSize()),
            (int) ZstdInputStreamNoFinalizer.recommendedDOutSize());

    private final Queue<SoftReference<ByteBuffer>> pool;

    private RecyclingBufferPool() {
        this.pool = new LinkedList<SoftReference<ByteBuffer>>();
    }

    
    public synchronized ByteBuffer get(int capacity) {
        if (capacity > buffSize) {
            throw new RuntimeException(
                    "Unsupported buffer size: " + capacity +
                    ". Supported buffer sizes: " + buffSize + " or smaller."
                );
        }
        while(true) {
            SoftReference<ByteBuffer> sbuf = pool.poll();
            if (sbuf == null) {
                return ByteBuffer.allocate(buffSize);
            }
            ByteBuffer buf = sbuf.get();
            if (buf != null) {
                return buf;
            }
        }
    }

    
    public synchronized void release(ByteBuffer buffer) {
        if (buffer.capacity() >= buffSize) {
            buffer.clear();
            //buffer.limit(buffer.capacity());
            //buffer.position(0);
            pool.offer(new SoftReference<ByteBuffer>(buffer));
        }
    }
}
