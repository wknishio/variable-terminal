package com.pngencoder;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.vash.vate.compatibility.VTObjects;

class PngEncoderCountingOutputStream extends FilterOutputStream {
    private int count;

    PngEncoderCountingOutputStream(OutputStream out) {
        super(VTObjects.requireNonNull(out, "out"));
    }

    public int getCount() {
        return count;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        count++;
    }
}
