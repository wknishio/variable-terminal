package org.vash.vate.com.pngencoder;

import java.util.LinkedList;
import java.util.Queue;

class PngEncoderDeflaterBufferPool {
    private final int bufferMaxLength;
    protected final Queue<PngEncoderDeflaterBuffer> buffers;

    PngEncoderDeflaterBufferPool(int bufferMaxLength) {
        this.bufferMaxLength = bufferMaxLength;
        this.buffers = new LinkedList<PngEncoderDeflaterBuffer>();
    }

    public int getBufferMaxLength() {
        return bufferMaxLength;
    }

    PngEncoderDeflaterBuffer borrow() {
        PngEncoderDeflaterBuffer buffer = buffers.poll();
        if (buffer == null) {
            buffer = new PngEncoderDeflaterBuffer(this, bufferMaxLength);
        }
        return buffer;
    }

    void giveBack(PngEncoderDeflaterBuffer buffer) {
        buffer.length = 0;
        buffers.offer(buffer);
    }

    int size() {
        return buffers.size();
    }
}
