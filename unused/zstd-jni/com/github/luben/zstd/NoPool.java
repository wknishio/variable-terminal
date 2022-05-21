package com.github.luben.zstd;

import java.nio.ByteBuffer;

/**
 * Implementation of `BufferPool` that does not recycle buffers.
 */
public class NoPool implements BufferPool {
    public static final BufferPool INSTANCE = new NoPool();

    private NoPool() {
    }

    public ByteBuffer get(int capacity) {
       return ByteBuffer.allocate(capacity);
    }

    public void release(ByteBuffer buffer) {
    }
}
