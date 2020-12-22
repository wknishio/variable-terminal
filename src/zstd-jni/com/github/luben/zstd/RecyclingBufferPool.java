package com.github.luben.zstd;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An pool of buffers which uses a simple reference queue to recycle old buffers.
 */
public class RecyclingBufferPool implements BufferPool {
    public static final BufferPool INSTANCE = new RecyclingBufferPool();

    private final Map<Integer, SoftReference<Queue<ByteBuffer>>> pools;
    
    private RecyclingBufferPool() {
        this.pools = new HashMap<Integer, SoftReference<Queue<ByteBuffer>>>();
    }

    private Queue<ByteBuffer> getQueue(int capacity) {
        SoftReference<Queue<ByteBuffer>> dequeReference = pools.get(capacity);
        Queue<ByteBuffer> deque;
        if (dequeReference == null || (deque = dequeReference.get()) == null) {
            deque = new LinkedList<ByteBuffer>();
            dequeReference = new SoftReference<Queue<ByteBuffer>>(deque);
            pools.put(capacity, dequeReference);
        }
        return deque;
    }

    
    public synchronized ByteBuffer get(int capacity) {
        ByteBuffer buffer = getQueue(capacity).poll();
        if (buffer == null) {
            buffer = ByteBuffer.allocate(capacity);
        }
        return buffer;
    }

    
    public synchronized void release(ByteBuffer buffer) {
    	getQueue(buffer.capacity()).offer(buffer);
    }
}